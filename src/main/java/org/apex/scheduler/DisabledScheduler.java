package org.apex.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * @author WangYi
 * @since 2020/6/23
 */
enum DisabledScheduler implements Scheduler {
  INSTANCE;

  @Override
  public Future<Void> schedule(Executor executor, Runnable command, long delay, TimeUnit unit) {
    requireNonNull(executor);
    requireNonNull(command);
    requireNonNull(unit);
    return DisabledFuture.INSTANCE;
  }
}
