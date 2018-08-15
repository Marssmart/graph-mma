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
import org.deer.mma.stats.app.AppRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FighterLinkScrapingScenarioRunner {

  private static final Logger LOG = LoggerFactory
      .getLogger(FighterLinkScrapingScenarioRunner.class);

  private static final String[] START_LINKS = {
      "\"http://www.fightmatrix.com/fighter-profile/Gegard+Mousasi/13436/\"",
      "\"http://www.fightmatrix.com/fighter-profile/Georges+St.+Pierre/9489/\"",
      "\"http://www.fightmatrix.com/fighter-profile/Anderson+Silva/1342/\"",
      "\"http://www.fightmatrix.com/fighter-profile/Jose+Aldo/23446/\"",
      "\"http://www.fightmatrix.com/fighter-profile/Jon+Jones/8116/\""
  };

  public static void main(String[] args) throws IOException, InterruptedException {
    LOG.info("Starting stats app");
    AppRunner.main(args);

    LOG.info("Starting scraping client");
    final OkHttpClient client = new OkHttpClient();

    LOG.info("Sending scraping request for links {}", Arrays.toString(START_LINKS));
    triggerScraping(client);

    Request checkRequest = createCheckScrapingRequest();
    boolean isRunning = true;

    while (isRunning) {
      LOG.info("Scraping running ...");
      TimeUnit.SECONDS.sleep(10);
      final Response checkResponse = client.newCall(checkRequest).execute();
      if (checkResponse.isSuccessful()) {
        isRunning = Boolean.valueOf(checkResponse.body().string());
      } else {
        LOG.error("Scraping check request failed", checkResponse);
        checkResponse.close();
        System.exit(-1);
      }
    }
    LOG.info("Scraping finished, sending shutdown request");
    System.exit(0);
  }

  private static void triggerScraping(OkHttpClient client) throws IOException {
    final Response triggerResponse = client.newCall(createScrapingRequest(START_LINKS)).execute();

    if (!triggerResponse.isSuccessful()) {
      LOG.error("Scraping request failed : {}", triggerResponse);
      triggerResponse.close();
      System.exit(-1);
    }
    triggerResponse.close();

    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        LOG.error("Scraping timed out, shutting down scenario");
        System.exit(-1);
      }
    }, TimeUnit.MINUTES.toMillis(60));
  }

  private static Request createScrapingRequest(String[] links) {
    MediaType mediaType = MediaType.parse("application/json");
    RequestBody body = RequestBody.create(mediaType, Arrays.toString(links));
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
