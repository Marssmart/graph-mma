package org.deer.mma.stats.db.node.relationship;

import org.deer.mma.stats.db.node.Fight;
import org.deer.mma.stats.db.node.Fighter;
import org.deer.mma.stats.db.node.relationship.enumerated.FightEnd;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type = FoughtInFight.TYPE)
public class FoughtInFight {

  public static final String TYPE = "FOUGHT_IN_FIGHT";

  @Id
  @GeneratedValue
  private long id;

  @StartNode
  private Fighter fighter;

  @EndNode
  private Fight fight;

  private FightEnd fightEnd;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Fighter getFighter() {
    return fighter;
  }

  public void setFighter(Fighter fighter) {
    this.fighter = fighter;
  }

  public Fight getFight() {
    return fight;
  }

  public void setFight(Fight fight) {
    this.fight = fight;
  }

  public FightEnd getFightEnd() {
    return fightEnd;
  }

  public void setFightEnd(FightEnd fightEnd) {
    this.fightEnd = fightEnd;
  }

}
