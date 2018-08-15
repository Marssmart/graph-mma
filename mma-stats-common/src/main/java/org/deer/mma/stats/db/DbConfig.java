package org.deer.mma.stats.db;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableNeo4jRepositories("org.deer.mma.stats.db.repository")
public class DbConfig {

  @Bean
  public SessionFactory sessionFactory(final org.neo4j.ogm.config.Configuration configuration) {
    return new SessionFactory(configuration, "org.deer.mma.stats.db.node");
  }

  @Bean
  public Neo4jTransactionManager transactionManager(final SessionFactory sessionFactory) {
    return new Neo4jTransactionManager(sessionFactory);
  }
}
