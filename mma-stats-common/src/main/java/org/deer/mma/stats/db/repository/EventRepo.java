package org.deer.mma.stats.db.repository;

import java.util.stream.Stream;
import org.deer.mma.stats.db.node.Event;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface EventRepo extends Neo4jRepository<Event, Long> {

  @Query("MATCH (e:Event) RETURN e")
  Stream<Event> findAllAsStream();
}
