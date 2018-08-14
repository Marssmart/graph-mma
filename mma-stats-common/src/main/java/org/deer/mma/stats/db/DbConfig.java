package org.deer.mma.stats.db;

import java.io.File;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.deer.mma.stats.db")
public class DbConfig {

  @Bean
  public GraphDatabaseService graphDbService(@Value("${neo.db.file.path}") String neoDbFilePath) {
    return new GraphDatabaseFactory().newEmbeddedDatabase(new File(neoDbFilePath));
  }
}
