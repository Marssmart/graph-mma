package org.deer.mma.stats.db.node;

import static org.neo4j.ogm.annotation.Relationship.INCOMING;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class Event {

  @Id
  @GeneratedValue
  private long id;

  private String name;

  private String sherdogLink;

  private LocalDate date;

  @Relationship(type = "HAPPENED_AT_EVENT", direction = INCOMING)
  private List<Fight> fights = new ArrayList<>();

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSherdogLink() {
    return sherdogLink;
  }

  public void setSherdogLink(String sherdogLink) {
    this.sherdogLink = sherdogLink;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }
}
