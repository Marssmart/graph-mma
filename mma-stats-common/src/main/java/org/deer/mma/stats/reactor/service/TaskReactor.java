package org.deer.mma.stats.reactor.service;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TaskReactor extends Thread {

  private final BlockingDeque<Task> queue = new LinkedBlockingDeque<>();
  private final AtomicBoolean shutdownScheduled;

  public TaskReactor() {
    shutdownScheduled = new AtomicBoolean(false);
  }

  public void submitSyncTask(Consumer<Void> work) {
    queue.add(new Task(false, work));
  }

  public void submitAsyncTask(Consumer<Void> work) {
    queue.add(new Task(false, work));
  }

  public void scheduleShutdown() {
    shutdownScheduled.set(true);
  }

  public class Task implements Runnable {

    private final boolean async;
    private final Consumer<Void> work;

    public Task(boolean async, Consumer<Void> work) {
      this.async = async;
      this.work = work;
    }

    public boolean isAsync() {
      return async;
    }

    @Override
    public void run() {
      work.accept(null);
    }
  }
}
