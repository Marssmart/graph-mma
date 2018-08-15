package org.deer.mma.stats.trigger.apps;

import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FighterLinkScrapingScenarioRunner {

  private static final Logger LOG = LoggerFactory
      .getLogger(FighterLinkScrapingScenarioRunner.class);

  private static Timer timer = new Timer();

  public static void main(String[] args) throws IOException, InterruptedException {
    LOG.info("Starting scraping client");
    if(args.length != 1){
      LOG.info("Illegal arguments detected : {}",Arrays.toString(args));
      System.exit(-1);
    }
    final OkHttpClient client = new OkHttpClient();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      timer.cancel();
      timer.purge();
      client.dispatcher().executorService().shutdownNow();
      client.connectionPool().evictAll();
      if (client.cache() != null) {
        try {
          client.cache().close();
        } catch (IOException e) {
          throw new IllegalStateException(e);
        }
      }
    }));

    LOG.info("Sending scraping request for links {}", args[0]);
    triggerScraping(client,args[0]);

    Request checkRequest = createCheckScrapingRequest();
    boolean isRunning = true;

    while (isRunning) {
      LOG.info("Scraping running ...");
      TimeUnit.SECONDS.sleep(10);
      try (final Response checkResponse = client.newCall(checkRequest).execute()) {
        if (checkResponse.isSuccessful()) {
          isRunning = Boolean.valueOf(checkResponse.body().string());
        } else {
          LOG.error("Scraping check request failed", checkResponse);
          checkResponse.close();
          System.exit(-1);
        }
      }
    }

    LOG.info("Scraping finished");
  }

  private static void triggerScraping(OkHttpClient client, String links) throws IOException {
    final Response triggerResponse = client.newCall(createScrapingRequest(links)).execute();

    if (!triggerResponse.isSuccessful()) {
      LOG.error("Scraping request failed : {}", triggerResponse);
      triggerResponse.close();
      System.exit(-1);
    }
    triggerResponse.close();

    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        LOG.error("Scraping timed out, shutting down scenario");
        System.exit(-1);
      }
    }, TimeUnit.MINUTES.toMillis(60));
  }

  private static Request createScrapingRequest(String links) {
    MediaType mediaType = MediaType.parse("application/json");
    RequestBody body = RequestBody.create(mediaType, links);
    return new Request.Builder()
        .url("http://localhost:8445/api/reactor/trigger-scraping")
        .post(body)
        .addHeader("Content-Type", "application/json")
        .addHeader("Cache-Control", "no-cache")
        .build();
  }

  private static Request createCheckScrapingRequest() {
    return new Request.Builder()
        .url("http://localhost:8445/api/reactor/check-scraping-running")
        .get()
        .addHeader("Content-Type", "application/json")
        .addHeader("Cache-Control", "no-cache")
        .build();
  }
}
