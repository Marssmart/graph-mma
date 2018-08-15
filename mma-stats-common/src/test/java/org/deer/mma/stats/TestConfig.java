package org.deer.mma.stats;

import org.deer.mma.stats.db.DbConfig;
import org.deer.mma.stats.db.standalone.StandaloneDbConfig;
import org.neo4j.ogm.testutil.TestServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:application-test.properties")
@ComponentScan(
    basePackages = {
        "org.deer.mma.stats.reactor",
        "org.deer.mma.stats.db.repository",
        "org.deer.mma.stats.db.node"
    },
    excludeFilters = {
        @Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            value = {
                StandaloneDbConfig.class
            })
    })
@Import(value = {DbConfig.class})
public class TestConfig {

  @Bean(destroyMethod = "shutdown")
  public TestServer createServer() {
    return new TestServer(false, false, 5000, 5678);
  }

  @Bean
  public org.neo4j.ogm.config.Configuration testDbConfig(final TestServer server) {
    return new org.neo4j.ogm.config.Configuration.Builder()
        .uri("http://localhost:5678")
        .build();
  }

  @Bean
  public static PropertySourcesPlaceholderConfigurer propInjector() {
    return new PropertySourcesPlaceholderConfigurer();
  }
}
