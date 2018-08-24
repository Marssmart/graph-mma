package org.deer.mma.stats.db.repository;

import java.util.stream.Stream;
import org.deer.mma.stats.db.node.Fight;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface FightRepo extends Neo4jRepository<Fight, Long> {

  @Query("MATCH (b:Fight) RETURN b")
  Stream<Fight> findAllAsStream();
}
