package org.deer.mma.stats.db.standalone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StandaloneDbConfig {

  @Bean
  public org.neo4j.ogm.config.Configuration standaloneConfig(
      @Value("${neo.db.username}") String user, @Value("${neo.db.password}") String password) {
    return new org.neo4j.ogm.config.Configuration.Builder()
        .uri("http://localhost:7474")
        .credentials(user, password)
        .build();
  }
}
