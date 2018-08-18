package org.deer.mma.stats.db.node;


import java.util.ArrayList;
import java.util.List;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class Fighter {

  @Id
  @GeneratedValue
  private Long id;

  private String fullname;

  private String fightMatrixLink;

  @Relationship(type = "FOUGHT_IN_FIGHT")
  private List<Fight> fights = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFullname() {
    return fullname;
  }

  public Fighter setFullname(String fullname) {
    this.fullname = fullname;
    return this;
  }

  public String getFightMatrixLink() {
    return fightMatrixLink;
  }

  public Fighter setFightMatrixLink(String fightMatrixLink) {
    this.fightMatrixLink = fightMatrixLink;
    return this;
  }
}
