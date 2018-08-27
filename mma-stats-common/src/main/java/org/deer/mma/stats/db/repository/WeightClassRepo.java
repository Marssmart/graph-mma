package org.deer.mma.stats.db.repository;

import java.util.Optional;
import org.deer.mma.stats.db.node.WeightClass;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface WeightClassRepo extends Neo4jRepository<WeightClass, Long> {

  Optional<WeightClass> findByName(String name);
}
