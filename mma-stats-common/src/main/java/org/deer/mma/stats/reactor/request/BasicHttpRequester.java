package org.deer.mma.stats.reactor.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Qualifier("basic")
public class BasicHttpRequester implements HtmlPageRequester {

  private final HttpClient client;

  @Autowired
  public BasicHttpRequester(@Nonnull final HttpClient client) {
    this.client = client;
  }

  @Override
  public CompletableFuture<Optional<String>> requestLink(String link) {
    final HttpGet httpGet = new HttpGet(link);

    return CompletableFuture.supplyAsync(() -> {
      final HttpResponse response;
      try {
        response = client.execute(httpGet);

        try (final BufferedReader reader = new BufferedReader(
            new InputStreamReader(response.getEntity().getContent()))) {

          final StringBuilder stringContentBuilder = new StringBuilder();
          String line = "";

          while ((line = reader.readLine()) != null) {
            stringContentBuilder.append(line);
          }

          return Optional.of(stringContentBuilder.toString())
              .map(String::trim)
              .map(s -> s.isEmpty() ? null : s);
        }
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    });
  }
}
