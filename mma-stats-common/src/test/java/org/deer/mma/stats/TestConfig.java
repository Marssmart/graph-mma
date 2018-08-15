package org.deer.mma.stats;

import org.deer.mma.stats.db.DbConfig;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application-test.properties")
@ComponentScan(basePackages = {
    "org.deer.mma.stats.db",
    "org.deer.mma.stats.reactor"
}, excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE,value = DbConfig.class))
public class TestConfig {

  @Bean(destroyMethod = "shutdown")
  public GraphDatabaseService testDb() {
    return new TestGraphDatabaseFactory().newImpermanentDatabase();
  }
}
