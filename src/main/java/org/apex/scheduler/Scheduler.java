package org.apex.scheduler;

import java.util.concurrent.*;

/**
 * @author WangYi
 * @since 2020/6/23
 */
@FunctionalInterface
public interface Scheduler {
  /**
   * Returns a future that will submit the task to the given executor after the given delay.
   *
   * @param executor the executor to run the task
   * @param command  the runnable task to schedule
   * @param delay    how long to delay, in units of {@code unit}
   * @param unit     a {@code TimeUnit} determining how to interpret the {@code delay} parameter
   * @return a scheduled future representing pending completion of the task
   */
  Future<?> schedule(Executor executor, Runnable command, long delay, TimeUnit unit);

  /**
   * Returns a scheduler that always returns a successfully completed future.
   *
   * @return a scheduler that always returns a successfully completed future
   */
  static Scheduler disabledScheduler() {
    return DisabledScheduler.INSTANCE;
  }

  /**
   * Returns a scheduler that uses the system-wide scheduling thread if available, or else returns
   * {@link #disabledScheduler()} if not present. This scheduler is provided in Java 9 or above
   * by using {@link CompletableFuture} {@code delayedExecutor}.
   *
   * @return a scheduler that uses the system-wide scheduling thread if available, or else a
   * disabled scheduler
   */
  static Scheduler systemScheduler() {
    return SystemScheduler.isPresent() ? SystemScheduler.INSTANCE : disabledScheduler();
  }

  /**
   * Returns a scheduler that delegates to the a {@link ScheduledExecutorService}.
   *
   * @param scheduledExecutorService the executor to schedule on
   * @return a scheduler that delegates to the a {@link ScheduledExecutorService}
   */
  static Scheduler forScheduledExecutorService(
          ScheduledExecutorService scheduledExecutorService) {
    return new ExecutorServiceScheduler(scheduledExecutorService);
  }

  /**
   * Returns a scheduler that suppresses and logs any exception thrown by the delegate
   * {@code scheduler}.
   *
   * @param scheduler the scheduler to delegate to
   * @return an scheduler that suppresses and logs any exception thrown by the delegate
   */
  static Scheduler guardedScheduler(Scheduler scheduler) {
    return (scheduler instanceof GuardedScheduler) ? scheduler : new GuardedScheduler(scheduler);
  }
}
