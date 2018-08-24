package org.deer.mma.stats.db.node;


import static org.neo4j.ogm.annotation.Relationship.INCOMING;

import java.time.Duration;
import java.util.Objects;
import org.deer.mma.stats.db.node.convertor.DurationConvertor;
import org.deer.mma.stats.db.node.enumerated.FightEndType;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;

@NodeEntity
public class Fight {

  private static final DurationConvertor INTERNAL_CONVERTER = new DurationConvertor();

  @Id
  @GeneratedValue
  private Long id;

  @Relationship(type = "REFEREED", direction = INCOMING)
  private Referee referee;

  private FightEndType fightEndType;
  private Byte numberOfRounds;

  @Convert(DurationConvertor.class)
  private Duration stoppageTime;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Fight fight = (Fight) o;
    return numberOfRounds == fight.numberOfRounds &&
        Objects.equals(referee, fight.referee) &&
        fightEndType == fight.fightEndType &&
        Objects.equals(stoppageTime, fight.stoppageTime);
  }

  @Override
  public String toString() {
    return "Fight{" +
        "id=" + id +
        ", referee=" + referee +
        ", fightEndType=" + fightEndType +
        ", numberOfRounds=" + numberOfRounds +
        ", stoppageTime=" + stoppageTime +
        '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(referee, fightEndType, numberOfRounds, stoppageTime);
  }

  public long getId() {
    return id;
  }

  public Fight setId(long id) {
    this.id = id;
    return this;
  }

  public FightEndType getFightEndType() {
    return fightEndType;
  }

  public Fight setFightEndType(FightEndType fightEndType) {
    this.fightEndType = fightEndType;
    return this;
  }

  public Byte getNumberOfRounds() {
    return numberOfRounds;
  }

  public Fight setNumberOfRounds(Byte numberOfRounds) {
    this.numberOfRounds = numberOfRounds;
    return this;
  }

  public Duration getStoppageTime() {
    return stoppageTime;
  }

  public Long getStoppageTimeConverted() {
    return Fight.convertDuration(getStoppageTime());
  }

  public static Long convertDuration(final Duration duration) {
    return INTERNAL_CONVERTER.toGraphProperty(duration);
  }

  public Fight setStoppageTime(Duration stoppageTime) {
    this.stoppageTime = stoppageTime;
    return this;
  }

  public Referee getReferee() {
    return referee;
  }

  public Fight setReferee(Referee referee) {
    this.referee = referee;
    return this;
  }
}
