package org.deer.mma.stats.db.repository;

import java.util.Optional;
import org.deer.mma.stats.db.node.Team;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface TeamRepo extends Neo4jRepository<Team, Long> {

  Optional<Team> findByName(String name);
}
