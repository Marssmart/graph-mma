package org.deer.mma.stats.db.nodes;

import org.neo4j.graphdb.Label;

public enum NodeLabel {

  FIGHTER;

  public Label getLabel() {
    return Label.label(this.name());
  }
}
