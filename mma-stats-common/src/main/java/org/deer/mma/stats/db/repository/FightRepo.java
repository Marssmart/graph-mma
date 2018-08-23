package org.deer.mma.stats.db.repository;

import java.util.Optional;
import java.util.stream.Stream;
import org.deer.mma.stats.db.node.Fight;
import org.deer.mma.stats.db.node.enumerated.FightEndType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

public interface FightRepo extends Neo4jRepository<Fight, Long> {

  @Query("MATCH (b:Fight) RETURN b")
  Stream<Fight> findAllAsStream();

  @Query(
      "MATCH (fighter:Fighter)-[:WON|:LOSS|:DRAW|:NC]->(fight:Fight)<-[refereed:REFEREED]-(ref:Referee)"
          + " WHERE (id(fighter) = {fighterId})"
          + " AND fight.numberOfRounds = {stoppageRound}"
          + " AND fight.stoppageTime = {stoppageTime}"
          + " AND fight.fightEndType = {fightEndType}"
          + " RETURN fight,refereed,ref")
  Optional<Fight> matchByFighterAndStopageTimeAndRound(
      @Param("fighterId") Long fighterId,
      @Param("fightEndType") FightEndType fightEndType,
      @Param("stoppageRound") Byte stopageRound,
      @Param("stoppageTime") Long stoppageTime);
}
