package org.deer.mma.stats.cfg;

import org.deer.mma.stats.db.DbConfig;
import org.neo4j.ogm.testutil.TestServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = DbConfig.class)
public class TestDbConfig {

  @Bean(destroyMethod = "shutdown")
  public TestServer createServer() {
    return new TestServer(false, false, 5000, 5678);
  }

  @Bean
  public org.neo4j.ogm.config.Configuration tstDbConfig(final TestServer server) {
    return new org.neo4j.ogm.config.Configuration.Builder()
        .uri("http://localhost:5678")
        .build();
  }
}
