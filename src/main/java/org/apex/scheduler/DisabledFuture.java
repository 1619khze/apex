package org.apex.scheduler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.requireNonNull;

/**
 * @author WangYi
 * @since 2020/6/23
 */
enum DisabledFuture implements Future<Void> {
  INSTANCE;

  @Override public boolean isDone() {
    return true;
  }
  @Override public boolean isCancelled() {
    return false;
  }
  @Override public boolean cancel(boolean mayInterruptIfRunning) {
    return false;
  }
  @Override public Void get() throws InterruptedException, ExecutionException {
    return null;
  }
  @Override public Void get(long timeout, TimeUnit unit)
          throws InterruptedException, ExecutionException, TimeoutException {
    requireNonNull(unit);
    return null;
  }
}
