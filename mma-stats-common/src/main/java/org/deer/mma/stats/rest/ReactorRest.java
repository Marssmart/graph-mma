package org.deer.mma.stats.rest;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.deer.mma.stats.db.EmbeddedDbService;
import org.deer.mma.stats.db.nodes.Fighter;
import org.deer.mma.stats.reactor.FightMatrixReactor;
import org.deer.mma.stats.reactor.LinkResolverReactor;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api/reactor")
public class ReactorRest {

  private static final Logger LOG = LoggerFactory.getLogger(ReactorRest.class);

  @Autowired
  @Qualifier("fight-matrix-reactor")
  private LinkResolverReactor fightMatrixReactor;

  @Autowired
  private EmbeddedDbService dbService;

  @RequestMapping("/trigger-fight-matrix")
  public String triggerFightMatrixScraping() {
    LOG.info("Triggering FightMatrix scraping");

    fightMatrixReactor
        .extractNewFighters("http://www.fightmatrix.com/fighter-profile/Yoel+Romero/61978/")
        .thenAccept(discoverySession -> {
          final List<String> discoveredLinks = discoverySession.getDiscoveredLinks();
          LOG.info("{} links discovered", discoveredLinks.size());

          final AtomicInteger successCounter = new AtomicInteger();

          dbService.doInTx(() -> {
            final Map<String, Node> existingFighterIndex = dbService.findAllFighterNodes()
                .stream()
                .collect(toMap(node -> (String) node.getProperty(Fighter.FULLNAME), node -> node));

            LOG.info("{} existing fighters indexed", existingFighterIndex.size());
            discoveredLinks.forEach(link -> {
              final Optional<String> fullName = FightMatrixReactor.parseFullName(link);
              if (fullName.isPresent()) {
                Objects.requireNonNullElseGet(existingFighterIndex.get(fullName.get()),
                    () -> dbService.createFighterNode(fullName.get()))
                    .setProperty(Fighter.FIGHT_MATRIX_LINK, link);
                successCounter.incrementAndGet();
              }
            });
          });
          LOG.info("{} links successfully set", successCounter.get());
        });

    LOG.info("FightMatrix scraping triggered");
    return "FightMatrix scraping triggered";
  }
}
