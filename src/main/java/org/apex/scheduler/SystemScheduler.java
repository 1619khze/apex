package org.apex.scheduler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * @author WangYi
 * @since 2020/6/23
 */
public enum  SystemScheduler implements Scheduler{
  INSTANCE;

  static final Method delayedExecutor = getDelayedExecutorMethod();

  @Override
  @SuppressWarnings("NullAway")
  public Future<?> schedule(Executor executor, Runnable command, long delay, TimeUnit unit) {
    requireNonNull(executor);
    requireNonNull(command);
    requireNonNull(unit);

    try {
      Executor scheduler = (Executor) delayedExecutor.invoke(
              CompletableFuture.class, delay, unit, executor);
      return CompletableFuture.runAsync(command, scheduler);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  static Method getDelayedExecutorMethod() {
    try {
      return CompletableFuture.class.getMethod(
              "delayedExecutor", long.class, TimeUnit.class, Executor.class);
    } catch (NoSuchMethodException | SecurityException e) {
      return null;
    }
  }

  static boolean isPresent() {
    return (delayedExecutor != null);
  }
}
