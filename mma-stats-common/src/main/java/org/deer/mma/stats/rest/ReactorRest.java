package org.deer.mma.stats.rest;

import static java.util.stream.Collectors.toMap;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.deer.mma.stats.db.EmbeddedDbService;
import org.deer.mma.stats.db.nodes.Fighter;
import org.deer.mma.stats.reactor.LinkResolverReactor;
import org.deer.mma.stats.reactor.LinkResolverReactor.DiscoverySession;
import org.neo4j.graphdb.Node;
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
public class ReactorRest {

  private static final Logger LOG = LoggerFactory.getLogger(ReactorRest.class);
  private final AtomicBoolean scrapingLock = new AtomicBoolean(false);
  @Autowired
  @Qualifier("fight-matrix-reactor")
  private LinkResolverReactor fightMatrixReactor;
  @Autowired
  private EmbeddedDbService dbService;

  @RequestMapping(value = "/trigger-scraping", method = POST, consumes = "application/json")
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
        .map(link -> fightMatrixReactor.extractNewFighters(link))
        .toArray(CompletableFuture[]::new);

    //preparation for multiple
    CompletableFuture.allOf(discoveryFutures)
        .thenAccept(aVoid ->
            dbService.doInTx(() -> {
              final Map<String, Node> existingFighterIndex = dbService.findAllFighterNodes()
                  .stream()
                  .collect(
                      toMap(node -> (String) node.getProperty(Fighter.FULLNAME), node -> node));
              LOG.info("{} existing fighters indexed", existingFighterIndex.size());

              Arrays.stream(discoveryFutures)
                  .map(CompletableFuture::join)
                  .forEach(discoverySession -> {
                    LOG.info("Processing discovery session {}", discoverySession);
                    discoverySession.getDiscoveredLinksPerNameIndex().forEach(
                        (fullName, link) -> {
                          Node fighterNode = existingFighterIndex.get(fullName);
                          if (fighterNode == null) {
                            fighterNode = dbService.createFighterNode(fullName);
                          }
                          discoverySession.getLinkApplier().accept(fighterNode, link);
                        });
                    LOG.info("Processing of discovery session {} finished", discoverySession);
                  });
            }))
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

  @RequestMapping("/check-scraping-running")
  public boolean checkScrapingRunning() {
    return scrapingLock.get();
  }
}
