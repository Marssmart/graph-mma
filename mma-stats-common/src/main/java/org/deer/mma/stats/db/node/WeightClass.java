package org.deer.mma.stats.db.node;

import static org.neo4j.ogm.annotation.Relationship.INCOMING;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class WeightClass {

  @Id
  @GeneratedValue
  private Long id;

  private String name;

  @Relationship(type = "FIGHTS_AT", direction = INCOMING)
  private Set<Fighter> fighters = new HashSet<>();

  public WeightClass addMember(final Fighter fighter) {
    this.fighters.add(fighter);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WeightClass that = (WeightClass) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public String toString() {
    return "WeightClass{" +
        "id=" + id +
        ", name='" + name + '\'' +
        '}';
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public WeightClass setName(String name) {
    this.name = name;
    return this;
  }

  public Set<Fighter> getFighters() {
    return fighters;
  }

  public void setFighters(Set<Fighter> fighters) {
    this.fighters = fighters;
  }
}
