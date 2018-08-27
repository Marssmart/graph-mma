package org.deer.mma.stats.reactor.task;

import java.util.concurrent.Callable;

public interface TaskReactor<INPUT, RESULT> extends AutoCloseable, Runnable {

  String getName();

  void start();

  void submitTask(final INPUT input);

  void onTaskFinished(final DescribedTask<RESULT> task, final RESULT result);

  void onTaskFailed(final DescribedTask<RESULT> task, final Throwable ex);

  interface DescribedTask<TASKRESULT> extends Callable<TASKRESULT> {

    String describe();
  }
}
