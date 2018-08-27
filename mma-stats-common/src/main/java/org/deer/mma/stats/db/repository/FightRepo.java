package org.deer.mma.stats.db.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.deer.mma.stats.db.node.Fight;
import org.deer.mma.stats.db.node.projections.CountFightsByFighter;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

public interface FightRepo extends Neo4jRepository<Fight, Long> {

  @Query("MATCH (b:Fight) RETURN b")
  Stream<Fight> findAllAsStream();

  @Query("MATCH (participant:Fighter)-[hadParticipant]->(fight:Fight)"
      + " RETURN id(participant) as fighterId,COUNT(fight) as countOfFights")
  List<CountFightsByFighter> countAllByParticipant();

  @Query("MATCH (a:Fighter)-[r1]->(f:Fight)<-[r2]-(b:Fighter),"
      + " (f)-[r3]->(e:Event)"
      + " WHERE id(a)= {fighterOne}"
      + " AND id(b)= {fighterTwo}"
      + " AND e.date = {date}"
      + " RETURN f")
  Optional<Fight> matchFight(@Param("fighterOne") Long fighterOne,
      @Param("fighterTwo") Long fighterTwo,
      @Param("date") String date);
}
