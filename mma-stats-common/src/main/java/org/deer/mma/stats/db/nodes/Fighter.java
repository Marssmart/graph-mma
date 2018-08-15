package org.deer.mma.stats.db.nodes;


import org.neo4j.graphdb.Node;

public class Fighter {

  public static final String FULLNAME = "fullname";
  public static final String FIGHT_MATRIX_LINK = "fight-matrix-link";

  public static Node addMandatoryAttributes(final Node plainNode, String fullname) {
    plainNode.addLabel(NodeLabel.FIGHTER.getLabel());
    plainNode.setProperty("fullname", fullname);
    return plainNode;
  }
}
