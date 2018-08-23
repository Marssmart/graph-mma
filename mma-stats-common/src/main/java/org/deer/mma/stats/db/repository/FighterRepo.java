package org.deer.mma.stats.db.repository;

import java.util.Set;
import java.util.stream.Stream;
import org.deer.mma.stats.db.node.Fighter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface FighterRepo extends Neo4jRepository<Fighter, Long> {

  Fighter findByFullname(String fullname);

  @Query("MATCH (f:Fighter) RETURN f.fullname")
  Set<String> getAllFighterNames();

  @Query("MATCH (f:Fighter) RETURN f")
  Stream<Fighter> findAllAsStream();

  Long countBySherdogLinkIsNotNull();

  Page<Fighter> findBySherdogLinkIsNotNull(Pageable pageable);
}
