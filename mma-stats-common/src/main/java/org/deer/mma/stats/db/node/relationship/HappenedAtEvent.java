package org.deer.mma.stats.db.node.relationship;

import org.deer.mma.stats.db.node.Event;
import org.deer.mma.stats.db.node.Fight;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;


@RelationshipEntity(type = "HAPPENED_AT_EVENT")
public class HappenedAtEvent {

  @Id
  @GeneratedValue
  private long id;

  @StartNode
  private Fight fight;

  @EndNode
  private Event event;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Fight getFight() {
    return fight;
  }

  public void setFight(Fight fight) {
    this.fight = fight;
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(Event event) {
    this.event = event;
  }
}
