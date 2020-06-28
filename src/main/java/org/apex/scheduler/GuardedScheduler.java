package org.apex.scheduler;

import java.io.Serializable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * @author WangYi
 * @since 2020/6/23
 */
final class GuardedScheduler implements Scheduler, Serializable {
  static final Logger logger = Logger.getLogger(GuardedScheduler.class.getName());
  static final long serialVersionUID = 1;

  final Scheduler delegate;

  GuardedScheduler(Scheduler delegate) {
    this.delegate = requireNonNull(delegate);
  }

  @Override
  public Future<?> schedule(Executor executor, Runnable command, long delay, TimeUnit unit) {
    try {
      Future<?> future = delegate.schedule(executor, command, delay, unit);
      return (future == null) ? DisabledFuture.INSTANCE : future;
    } catch (Throwable t) {
      logger.log(Level.WARNING, "Exception thrown by scheduler; discarded task", t);
      return DisabledFuture.INSTANCE;
    }
  }
}
