package org.deer.mma.stats.db.node;

import static org.neo4j.ogm.annotation.Relationship.INCOMING;

import java.util.HashSet;
import java.util.Set;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Relationship;

public class Team {

  @Id
  @GeneratedValue
  private Long id;

  private String name;

  @Relationship(type = "TRAINS_AT", direction = INCOMING)
  private Set<Fighter> fighters = new HashSet<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public Team setName(String name) {
    this.name = name;
    return this;
  }

  public Set<Fighter> getFighters() {
    return fighters;
  }

  public void setFighters(Set<Fighter> fighters) {
    this.fighters = fighters;
  }

  public Team addMember(final Fighter fighter) {
    this.fighters.add(fighter);
    return this;
  }
}
