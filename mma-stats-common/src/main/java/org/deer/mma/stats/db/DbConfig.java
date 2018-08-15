package org.deer.mma.stats.db;

import java.io.File;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.logging.slf4j.Slf4jLogProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.deer.mma.stats.db")
public class DbConfig {

  @Bean(destroyMethod = "shutdown")
  public GraphDatabaseService graphDbService(@Value("${neo.db.file.path}") String neoDbFilePath) {
    return new GraphDatabaseFactory()
        .setUserLogProvider(new Slf4jLogProvider())
        .newEmbeddedDatabaseBuilder(new File(neoDbFilePath))
        .newGraphDatabase();
  }
}
