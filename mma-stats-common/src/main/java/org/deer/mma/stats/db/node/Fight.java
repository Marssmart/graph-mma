package org.deer.mma.stats.db.node;


import static org.neo4j.ogm.annotation.Relationship.INCOMING;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Duration;
import org.deer.mma.stats.db.node.enumerated.FightEndType;
import org.deer.mma.stats.db.node.relationship.FoughtInFight;
import org.deer.mma.stats.db.node.relationship.HappenedAtEvent;
import org.deer.mma.stats.db.node.relationship.RefereedFight;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class Fight {

  @Id
  @GeneratedValue
  private long id;

  @JsonIgnoreProperties("event")
  @Relationship(type = "HAPPENED_AT_EVENT")
  private HappenedAtEvent event;

  @JsonIgnoreProperties("fight")
  @Relationship(type = FoughtInFight.TYPE, direction = INCOMING)
  private FoughtInFight blueFighter;

  @JsonIgnoreProperties("fight")
  @Relationship(type = FoughtInFight.TYPE, direction = INCOMING)
  private FoughtInFight redFighter;

  @JsonIgnoreProperties("referee")
  @Relationship(type = RefereedFight.TYPE, direction = INCOMING)
  private RefereedFight referee;

  private FightEndType fightEndType;

  private byte numberOfRounds;

  private Duration stoppageTime;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public FoughtInFight getBlueFighter() {
    return blueFighter;
  }

  public void setBlueFighter(FoughtInFight blueFighter) {
    this.blueFighter = blueFighter;
  }

  public FoughtInFight getRedFighter() {
    return redFighter;
  }

  public void setRedFighter(FoughtInFight redFighter) {
    this.redFighter = redFighter;
  }

  public FightEndType getFightEndType() {
    return fightEndType;
  }

  public void setFightEndType(FightEndType fightEndType) {
    this.fightEndType = fightEndType;
  }

  public byte getNumberOfRounds() {
    return numberOfRounds;
  }

  public void setNumberOfRounds(byte numberOfRounds) {
    this.numberOfRounds = numberOfRounds;
  }

  public Duration getStoppageTime() {
    return stoppageTime;
  }

  public void setStoppageTime(Duration stoppageTime) {
    this.stoppageTime = stoppageTime;
  }

  public RefereedFight getReferee() {
    return referee;
  }

  public void setReferee(RefereedFight referee) {
    this.referee = referee;
  }
}
