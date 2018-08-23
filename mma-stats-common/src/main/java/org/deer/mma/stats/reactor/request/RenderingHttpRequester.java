package org.deer.mma.stats.reactor.request;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ImmediateRefreshHandler;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Qualifier("rendering")
public class RenderingHttpRequester implements HtmlPageRequester {

  private static final Logger LOG = LoggerFactory.getLogger(RenderingHttpRequester.class);

  @Override
  public CompletableFuture<Optional<String>> requestLink(@Nonnull String link) {
    return CompletableFuture.supplyAsync(() -> {
      try (final WebClient webClient = createWebClient()) {
        final HtmlPage page = webClient.getPage(link);

        webClient.waitForBackgroundJavaScript(7000);

        /*final JavaScriptJobManager javascriptJobManager = page.getEnclosingWindow().getJobManager();
        int waitAttempts = 5;
        while (javascriptJobManager.getJobCount() > 0 && waitAttempts > 0) {
          waitAttempts--;
          LOG.debug("Waiting for javascript loading for {}", link);
          try {
            TimeUnit.SECONDS.sleep(1);
          } catch (InterruptedException e) {
            throw new IllegalStateException(e);
          }
        }*/

        return Optional.ofNullable(page.asXml());
      } catch (IOException e) {
        LOG.error("Error while requesting link {}", link, e);
        throw new IllegalStateException(e);
      }
    });
  }

  private WebClient createWebClient() {
    WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45);
    webClient.getOptions().setCssEnabled(false);
    webClient.getOptions().setRedirectEnabled(true);
    webClient.setRefreshHandler(new ImmediateRefreshHandler());
    webClient.setAjaxController(new NicelyResynchronizingAjaxController());
    webClient.waitForBackgroundJavaScript(5000);
    webClient.waitForBackgroundJavaScriptStartingBefore(5000);
    webClient.setJavaScriptTimeout(5000);
    webClient.getOptions().setThrowExceptionOnScriptError(false);
    webClient.setIncorrectnessListener((s, o) -> {//NOOP
    });
    return webClient;
  }
}
