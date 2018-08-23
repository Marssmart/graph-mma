package org.deer.mma.stats.db.node;

import java.util.Objects;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Referee {

  @Id
  @GeneratedValue
  private Long id;

  private String name;

  @Override
  public String toString() {
    return "Referee{" +
        "id=" + id +
        ", name='" + name + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Referee referee = (Referee) o;
    return Objects.equals(name, referee.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  public long getId() {
    return id;
  }

  public Referee setId(long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public Referee setName(String name) {
    this.name = name;
    return this;
  }
}
