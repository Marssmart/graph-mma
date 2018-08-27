package org.deer.mma.stats.reactor.task;

import static com.google.common.base.Preconditions.checkState;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.deer.mma.stats.reactor.request.HtmlPageRequester;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("sherdog-page-request-agent")
public class SherdogPageRequestReactor implements TaskReactor<String, String> {

  private static final Logger LOG = LoggerFactory.getLogger(SherdogPageRequestReactor.class);
  private static final String SHERDOG_PAGE_REQUEST_AGENT = "sherdog-page-request-agent";
  /**
   * This part is resource heavy as hell, so max 5 requests at the time
   */
  private final Semaphore accessSemaphore = new Semaphore(5);
  private final ExecutorService executorService = Executors.newWorkStealingPool(5);

  private final BlockingQueue<DescribedTask<String>> queue = new LinkedBlockingDeque<>();

  private boolean isRunning;
  private CountDownLatch shutdownLock;

  @Autowired
  @Qualifier("sherdog-page-parse-agent")
  private TaskReactor<String, Document> nextStage;

  @Autowired
  @Qualifier("rendering")
  private HtmlPageRequester htmlRequester;

  @Override
  public String getName() {
    return SHERDOG_PAGE_REQUEST_AGENT;
  }

  @Override
  public void submitTask(String link) {
    queue.add(new DescribedTask<String>() {
      @Override
      public String describe() {
        return "Request for link " + link;
      }

      @Override
      public String call() {
        LOG.info("Requesting link {}", link);
        return htmlRequester.requestLink(link)
            .join()
            .orElseThrow(() -> new IllegalStateException("No content for " + link));
      }
    });
  }

  @Override
  public void onTaskFinished(DescribedTask<String> task, String htmlPage) {
    accessSemaphore.release();
    LOG.info("Task {} successfully finished", task.describe());
    nextStage.submitTask(htmlPage);
  }

  @Override
  public void onTaskFailed(DescribedTask<String> task, Throwable ex) {
    accessSemaphore.release();
    LOG.error("Error detected while running task {}", task.describe(), ex);
  }

  @Override
  public void run() {
    isRunning = true;
    while (isRunning) {
      try {
        accessSemaphore.acquire();
        DescribedTask<String> task = queue.poll(500L, TimeUnit.MILLISECONDS);

        if (task == null) {
          continue;
        }

        CompletableFuture.supplyAsync(() -> {
          try {
            return task.call();
          } catch (Exception e) {
            throw new IllegalStateException(e);
          }
        }, executorService)
            .whenComplete((htmlPage, throwable) -> {
              if (throwable != null) {
                this.onTaskFailed(task, throwable);
              } else {
                this.onTaskFinished(task, htmlPage);
              }
            });
      } catch (InterruptedException e) {
        LOG.error("Error in task execution", e);
        throw new IllegalStateException(e);
      }
    }
    LOG.info("Shutting down ...");
    shutdownLock.countDown();
  }

  @Override
  public void start() {
    checkState(!isRunning, "Agent already running");
    LOG.info("Starting {}", getName());
    shutdownLock = new CountDownLatch(1);
    new Thread(this, getName()).start();
    LOG.info("{} successfully started", getName());
  }

  @Override
  public void close() throws InterruptedException {
    checkState(isRunning, "Agent not running");
    LOG.info("Stopping {}", getName());
    isRunning = false;
    accessSemaphore.release(5);
    shutdownLock.await();
    executorService.shutdownNow();
    LOG.info("{} successfully stopped", getName());
  }
}
