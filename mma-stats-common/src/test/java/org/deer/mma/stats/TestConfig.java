package org.deer.mma.stats;

import java.io.File;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.FileSystemUtils;

@Configuration
@PropertySource("classpath:application-test.properties")
@ComponentScan(basePackages = {
    "org.deer.mma.stats.db",
    "org.deer.mma.stats.reactor"
})
public class TestConfig {

  @Bean
  @Qualifier("test-db-remover")
  public AutoCloseable testRunCleanup(@Value("${neo.db.file.path}") String neoDbFilePath) {
    Runtime.getRuntime().addShutdownHook(
        new Thread(() -> FileSystemUtils.deleteRecursively(new File(neoDbFilePath))));
    return () -> {
    };
  }
}
