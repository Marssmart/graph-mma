package org.deer.mma.stats.db;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.deer.mma.stats.db.nodes.Fighter;
import org.deer.mma.stats.db.nodes.NodeLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class EmbeddedDbService implements NeoTransactional {

  private static final Logger LOG = LoggerFactory.getLogger(EmbeddedDbService.class);

  @Autowired
  private GraphDatabaseService graphDatabaseService;

  public Optional<Node> createFighterNodeTransactional(@Nonnull final String fullname) {
    return doInTxAndReturnOptional(
        () -> Fighter.addMandatoryAttributes(graphDatabaseService.createNode(), fullname));
  }

  public Node createFighterNode(@Nonnull final String fullname) {
    return Fighter.addMandatoryAttributes(graphDatabaseService.createNode(), fullname);
  }

  public Optional<Node> findFighterNode(@Nonnull final String fullname) {
    return doInTxAndReturnOptional(() -> graphDatabaseService
        .findNode(NodeLabel.FIGHTER.getLabel(), Fighter.FULLNAME, fullname));
  }

  public List<Node> findAllFighterNodes() {
    return doInTxAndReturnOptional(
        () -> graphDatabaseService.findNodes(NodeLabel.FIGHTER.getLabel()).stream().collect(
            Collectors.toList()))
        .orElse(Collections.emptyList());
  }

  @Override
  public GraphDatabaseService getDbService() {
    return graphDatabaseService;
  }
}
