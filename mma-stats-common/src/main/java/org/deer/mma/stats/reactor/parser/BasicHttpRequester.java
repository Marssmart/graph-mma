package org.deer.mma.stats.reactor.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.springframework.lang.NonNull;

public class BasicHttpRequester implements HtmlPageRequester {

  private final String USER_AGENT = "Mozzila/5.0";

  private final HttpClient client;

  public BasicHttpRequester(@NonNull final HttpClient client) {
    this.client = client;
  }

  @Override
  public CompletableFuture<Optional<String>> requestLink(String link) {
    final HttpGet httpGet = new HttpGet(link);
    httpGet.addHeader("User-Agent", USER_AGENT);

    return CompletableFuture.supplyAsync(() -> {
      final HttpResponse response;
      try {
        response = client.execute(httpGet);

        final BufferedReader reader = new BufferedReader(
            new InputStreamReader(response.getEntity().getContent()));

        final StringBuilder stringContentBuilder = new StringBuilder();
        String line = "";

        while ((line = reader.readLine()) != null) {
          stringContentBuilder.append(line);
        }

        return Optional.of(stringContentBuilder.toString())
            .map(String::trim)
            .map(s -> s.isEmpty() ? null : s);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    });
  }
}
