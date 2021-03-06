package org.deer.mma.stats.rest;

import static java.util.stream.Collectors.toMap;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.deer.mma.stats.db.node.Fighter;
import org.deer.mma.stats.db.repository.FighterRepo;
import org.deer.mma.stats.reactor.LinkResolverReactor;
import org.deer.mma.stats.reactor.LinkResolverReactor.DiscoverySession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reactor")
public class FightMatrixReactorRest {

  private static final Logger LOG = LoggerFactory.getLogger(FightMatrixReactorRest.class);
  private final AtomicBoolean scrapingLock = new AtomicBoolean(false);
  private final AtomicBoolean discoveryLock = new AtomicBoolean(false);

  @Autowired
  @Qualifier("fight-matrix-reactor")
  private LinkResolverReactor fightMatrixReactor;

  @Autowired
  private FighterRepo fighterRepo;

  @RequestMapping(value = "/trigger-fight-matrix-link-scraping", method = POST, consumes = "application/json")
  public ResponseEntity<String> triggerFightMatrixScraping(@RequestBody List<String> links) {
    if (scrapingLock.get()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Scraping already running !");
    }

    LOG.info("Triggering scraping");
    scrapingLock.set(true);

    final CompletableFuture<DiscoverySession>[] discoveryFutures = links.stream()
        .filter(link -> {
          if (link == null || link.trim().isEmpty()) {
            LOG.warn("Invalid link detected");
            return false;
          }
          return true;
        })
        .peek(link -> LOG.info("Trigerring scraping for link {}", link))
        .map(link -> fightMatrixReactor.extractFighterLinks(link))
        .toArray(CompletableFuture[]::new);

    //preparation for multiple
    CompletableFuture.allOf(discoveryFutures)
        .thenAccept(aVoid -> {
          /*
           * 1) Create index of already existing fighters, to identify if new fighter should be created
           * */
          final Map<String, Fighter> existingFighterIndex = fighterRepo.findAllAsStream()
              .collect(toMap(Fighter::getFullname, fighter -> fighter));
          LOG.info("{} existing fighters indexed", existingFighterIndex.size());

          /*
           * 2) Iterate and retrieve results of discovery sessions
           * */
          Arrays.stream(discoveryFutures)
              .map(CompletableFuture::join)
              .forEach(discoverySession -> {
                /*
                 * 3) If fighter exists, only apply discovered link, otherwise also create new
                 * */
                LOG.info("Processing discovery session {}", discoverySession);
                final List<Fighter> newAndUpdatedBySession = discoverySession
                    .getDiscoveredLinksPerNameIndex()
                    .entrySet().stream().map(
                        (entry) -> {
                          final String fullName = entry.getKey();
                          final String link = entry.getValue();

                          Fighter fighter = existingFighterIndex.get(fullName);
                          if (fighter == null) {
                            fighter = new Fighter().setFullname(fullName);
                          }
                          return discoverySession.applyLink(fighter, link);
                        }).collect(Collectors.toList());
                LOG.info("Processing of discovery session {} finished, saving changes",
                    discoverySession);
                /*
                 * 4) Save all at once
                 * */
                Iterable<Fighter> allSaved = fighterRepo.saveAll(newAndUpdatedBySession);
                LOG.info("Changes for session {} saved, updating existing fighter index",
                    discoverySession);

                /*
                 * 5) Update index by newly create or updated fighters
                 * */
                allSaved.forEach(savedOrUpdated -> existingFighterIndex
                    .merge(savedOrUpdated.getFullname(), savedOrUpdated,
                        (oldFighter, newFighter) -> newFighter));
                LOG.info("Existing fighters index updated");
              });
        })
        .whenComplete((aVoid, throwable) -> {
          if (throwable == null) {
            LOG.info("Processing of all discovery sessions finished");
          } else {
            LOG.error("Error while retrieving discovery sessions", throwable);
          }
          scrapingLock.set(false);//unlock in all cases
        });

    LOG.info("Scraping triggered");
    return ResponseEntity.ok("Scraping triggered");
  }

  @RequestMapping(value = "/trigger-sherdog-link-discovery")
  public ResponseEntity<String> triggerSherdogLinkDiscovery() {
    if (discoveryLock.get()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Discovery already running !");
    }

    discoveryLock.set(true);

    final CompletableFuture[] discoveriesInProgress = fighterRepo.findAllAsStream()
        .map(fighter -> {
          final String fightMatrixLink = fighter.getFightMatrixLink();

          return fightMatrixReactor.discoverAdditionalAttributes(fightMatrixLink)
              .whenComplete((attributes, throwable) -> {
                if (throwable != null) {
                  LOG.error("Discovery for link {} failed", fightMatrixLink, throwable);
                } else {
                  final Fighter decoratedFighter = fightMatrixReactor
                      .decorateFighterByAdditionalAttributes(fighter, attributes);

                  fighterRepo.save(decoratedFighter);
                  LOG.info("Fighter {} decorated by Fight Matrix discovery attributes",
                      fighter.getFullname());
                }
              });
        }).toArray(CompletableFuture[]::new);

    CompletableFuture.allOf(discoveriesInProgress).whenComplete((aVoid, throwable) -> {
      if (throwable != null) {
        LOG.error("Discoveries finished with error[total={}]", discoveriesInProgress.length,
            throwable);
      } else {
        LOG.error("Discoveries finished [total={}]", discoveriesInProgress.length);
      }

      discoveryLock.set(false);
    });

    return ResponseEntity.ok("Discovery triggered");
  }

  @RequestMapping("/check-scraping-running")
  public boolean checkScrapingRunning() {
    return scrapingLock.get();
  }

  @RequestMapping("/check-discovery-running")
  public boolean checkDiscoveryRunning() {
    return discoveryLock.get();
  }
}
