package org.apex.scheduler;

import java.io.Serializable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * @author WangYi
 * @since 2020/6/23
 */
final class ExecutorServiceScheduler implements Scheduler, Serializable {
  static final Logger logger = Logger.getLogger(ExecutorServiceScheduler.class.getName());
  static final long serialVersionUID = 1;

  final ScheduledExecutorService scheduledExecutorService;

  ExecutorServiceScheduler(ScheduledExecutorService scheduledExecutorService) {
    this.scheduledExecutorService = requireNonNull(scheduledExecutorService);
  }

  @Override
  public Future<?> schedule(Executor executor, Runnable command, long delay, TimeUnit unit) {
    requireNonNull(executor);
    requireNonNull(command);
    requireNonNull(unit);

    if (scheduledExecutorService.isShutdown()) {
      return DisabledFuture.INSTANCE;
    }
    return scheduledExecutorService.schedule(() -> {
      try {
        executor.execute(command);
      } catch (Throwable t) {
        logger.log(Level.WARNING, "Exception thrown when submitting scheduled task", t);
        throw t;
      }
    }, delay, unit);
  }
}
