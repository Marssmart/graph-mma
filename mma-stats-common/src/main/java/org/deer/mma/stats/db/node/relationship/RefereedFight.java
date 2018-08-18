package org.deer.mma.stats.db.node.relationship;

import org.deer.mma.stats.db.node.Fight;
import org.deer.mma.stats.db.node.Referee;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type = RefereedFight.TYPE)
public class RefereedFight {

  public static final String TYPE = "REFEREED";

  @Id
  @GeneratedValue
  private long id;

  @StartNode
  private Referee referee;

  @EndNode
  private Fight fight;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Referee getReferee() {
    return referee;
  }

  public void setReferee(Referee referee) {
    this.referee = referee;
  }

  public Fight getFight() {
    return fight;
  }

  public void setFight(Fight fight) {
    this.fight = fight;
  }
}
