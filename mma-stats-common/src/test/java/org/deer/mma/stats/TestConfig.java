package org.deer.mma.stats;

import org.deer.mma.stats.reactor.FightMatrixReactor;
import org.deer.mma.stats.reactor.ReactorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:application-test.properties")
@ComponentScan(basePackages = {
    "org.deer.mma.stats.db.repository",
    "org.deer.mma.stats.reactor.request"
})
@Import(value = {ReactorConfig.class})
public class TestConfig {

  @Bean
  public static PropertySourcesPlaceholderConfigurer propInjector() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  public FightMatrixReactor fightMatrixReactor() {
    return new FightMatrixReactor();
  }
}
