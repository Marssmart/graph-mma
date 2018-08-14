package org.deer.mma.stats.reactor;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactorConfig {

  @Bean
  public HttpClient createHttpClient() {
    return HttpClientBuilder.create()
        .setMaxConnTotal(10)
        .setUserAgent("Mozzila/5.0")
        .build();
  }
}
