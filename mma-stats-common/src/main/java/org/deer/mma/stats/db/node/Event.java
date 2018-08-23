package org.deer.mma.stats.db.node;

import static org.neo4j.ogm.annotation.Relationship.INCOMING;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class Event {

  @Id
  @GeneratedValue
  private Long id;

  private String name;

  private String sherdogLink;

  private LocalDate date;

  @Relationship(type = "SCHEDULED_ON", direction = INCOMING)
  private Set<Fight> fights;

  public long getId() {
    return id;
  }

  public Event setId(long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public Event setName(String name) {
    this.name = name;
    return this;
  }

  public String getSherdogLink() {
    return sherdogLink;
  }

  public Event setSherdogLink(String sherdogLink) {
    this.sherdogLink = sherdogLink;
    return this;

  }

  public LocalDate getDate() {
    return date;
  }

  public Event setDate(LocalDate date) {
    this.date = date;
    return this;

  }

  public Set<Fight> getFights() {
    return fights;
  }

  public Event setFights(Set<Fight> fights) {
    this.fights = fights;
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
    Event event = (Event) o;
    return Objects.equals(name, event.name) &&
        Objects.equals(sherdogLink, event.sherdogLink) &&
        Objects.equals(date, event.date) &&
        Objects.equals(fights, event.fights);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, sherdogLink, date, fights);
  }
}
