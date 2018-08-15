package org.deer.mma.stats.db;

import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.List;
import org.deer.mma.stats.db.nodes.Fighter;
import org.deer.mma.stats.db.nodes.NodeLabel;
import org.junit.Test;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

public class EmbeddedDbServiceTest extends NeoDbTest {

  @Test
  public void createFighterNode() {
    Node denisRodman = dbService.createFighterNodeTransactional("Sage Northcut").get();
    dbService.doInTx(() -> {
      Node denisRodmanFromDb = dbService.getDbService()
          .findNode(NodeLabel.FIGHTER.getLabel(), Fighter.FULLNAME, "Sage Northcut");
      verifyFighterNode(denisRodman, "Sage Northcut");
      verifyFighterNode(denisRodmanFromDb, "Sage Northcut");
    });
  }

  @Test
  public void findFighterNode() {
    dbService.doInTx(() -> {
      Node connorMcGregor = Fighter
          .addMandatoryAttributes(dbService.getDbService().createNode(), "Connor McGregor");

      Node foundConnorMcgregor = dbService.findFighterNode("Connor McGregor").get();
      assertEquals(connorMcGregor, foundConnorMcgregor);
      verifyFighterNode(connorMcGregor, "Connor McGregor");
    });
  }

  @Test
  public void findAllFighters() {
    dbService.doInTx(() -> {
      Node connorMcGregor = Fighter
          .addMandatoryAttributes(dbService.getDbService().createNode(), "Connor McGregor");
      Node chadMendes = Fighter
          .addMandatoryAttributes(dbService.getDbService().createNode(), "Chad Mendes");
      Node maxHolloway = Fighter
          .addMandatoryAttributes(dbService.getDbService().createNode(), "Max Holloway");

      List<Node> allFighters = dbService.findAllFighterNodes();
      assertThat(allFighters, hasItems(connorMcGregor, chadMendes, maxHolloway));
    });
  }

  private void verifyFighterNode(Node fighter, String fullName) {
    Iterator<Label> labelsIterator = fighter.getLabels().iterator();
    assertEquals(NodeLabel.FIGHTER.getLabel(), labelsIterator.next());
    assertFalse(labelsIterator.hasNext());
    assertEquals(fighter.getProperty(Fighter.FULLNAME), fullName);
  }
}