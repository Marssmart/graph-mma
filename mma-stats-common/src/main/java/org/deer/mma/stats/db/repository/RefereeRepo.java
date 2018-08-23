package org.deer.mma.stats.db.repository;

import java.util.stream.Stream;
import org.deer.mma.stats.db.node.Referee;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface RefereeRepo extends Neo4jRepository<Referee, Long> {

  @Query("MATCH (r:Referee) RETURN r")
  Stream<Referee> findAllAsStream();
}
