package org.deer.mma.stats.db.node;

import java.util.Objects;
import java.util.Set;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class Fighter {

  public static final String PROP_SHERDOG_LINK = "SHERDOG_LINK";

  @Id
  @GeneratedValue
  private Long id;

  @Relationship(type = "WON")
  private Set<Fight> wins;

  @Relationship(type = "LOST")
  private Set<Fight> losses;

  @Relationship(type = "DRAW")
  private Set<Fight> draws;

  @Relationship(type = "NC")
  private Set<Fight> nc;

  private String fullname;

  private String fightMatrixLink;

  private String sherdogLink;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Fighter fighter = (Fighter) o;
    return Objects.equals(wins, fighter.wins) &&
        Objects.equals(losses, fighter.losses) &&
        Objects.equals(draws, fighter.draws) &&
        Objects.equals(nc, fighter.nc) &&
        Objects.equals(fullname, fighter.fullname) &&
        Objects.equals(fightMatrixLink, fighter.fightMatrixLink) &&
        Objects.equals(sherdogLink, fighter.sherdogLink);
  }

  @Override
  public int hashCode() {
    return Objects.hash(wins, losses, draws, nc, fullname, fightMatrixLink, sherdogLink);
  }

  @Override
  public String toString() {
    return "Fighter{" +
            "id=" + id +
            ", fullname='" + fullname + '\'' +
            ", fightMatrixLink='" + fightMatrixLink + '\'' +
            ", sherdogLink='" + sherdogLink + '\'' +
            '}';
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFullname() {
    return fullname;
  }

  public Fighter setFullname(String fullname) {
    this.fullname = fullname;
    return this;
  }

  public String getFightMatrixLink() {
    return fightMatrixLink;
  }

  public Fighter setFightMatrixLink(String fightMatrixLink) {
    this.fightMatrixLink = fightMatrixLink;
    return this;
  }

  public String getSherdogLink() {
    return sherdogLink;
  }

  public Fighter setSherdogLink(String sherdogLink) {
    this.sherdogLink = sherdogLink;
    return this;
  }


  public Set<Fight> getWins() {
    return wins;
  }

  public Fighter setWins(Set<Fight> wins) {
    this.wins = wins;
    return this;
  }

  public Set<Fight> getLosses() {
    return losses;
  }

  public Fighter setLosses(Set<Fight> losses) {
    this.losses = losses;
    return this;
  }

  public Set<Fight> getDraws() {
    return draws;
  }

  public Fighter setDraws(Set<Fight> draws) {
    this.draws = draws;
    return this;
  }

  public Set<Fight> getNc() {
    return nc;
  }

  public Fighter setNc(Set<Fight> nc) {
    this.nc = nc;
    return this;
  }
}
