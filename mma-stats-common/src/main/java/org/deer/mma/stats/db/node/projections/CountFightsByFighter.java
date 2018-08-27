package org.deer.mma.stats.db.node.projections;

import org.springframework.data.neo4j.annotation.QueryResult;

@QueryResult
public class CountFightsByFighter {

  private long fighterId;
  private long countOfFights;

  public long getFighterId() {
    return fighterId;
  }

  public long getCountOfFights() {
    return countOfFights;
  }
}
