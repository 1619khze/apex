/*
 * MIT License
 *
 * Copyright (c) 2020 1619kHz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.apex;

import org.apache.commons.lang3.Validate;
import org.apex.scheduler.Scheduler;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static java.util.Objects.requireNonNull;

/**
 * @author WangYi
 * @since 2020/9/8
 */
public class Apex {
  private final Set<Class<? extends Annotation>> typeAnnotations = new LinkedHashSet<>();
  private final Set<TypeFilter> typeFilters = new LinkedHashSet<>();
  private final Set<String> scanPackages = new LinkedHashSet<>();
  private final Environment environment = Environment.create();
  private final Set<Class<?>> implInterfaces = new LinkedHashSet<>();

  private Scheduler scheduler;
  private Executor executor;
  private String[] args;

  public static Apex of() {
    return ApexHolder.instance;
  }

  /**
   * Get a unified environment object
   *
   * @return environment object
   */
  public Environment environment() {
    return environment;
  }

  /**
   * add impl interface
   *
   * @param type impl interface
   * @return this
   */
  public Apex implInterface(Class<?> type) {
    Validate.notNull(type, "class type can't be null");
    this.implInterfaces.add(type);
    return this;
  }

  /**
   * add impl interface
   *
   * @param type impl interface
   * @return this
   */
  public Apex implInterfaces(List<Class<?>> types) {
    Validate.notNull(types, "class type list can't be null and empty");
    this.implInterfaces.addAll(types);
    return this;
  }

  /**
   * get impl interface list
   *
   * @return impl interface list
   */
  public Set<Class<?>> implInterfaces() {
    return this.implInterfaces;
  }

  /**
   * add type filter
   *
   * @param typeFilters type filter
   * @return this
   */
  public Apex typeFilter(TypeFilter typeFilter) {
    Validate.notNull(typeFilter, "typeFilter can't be null");
    this.typeFilters.add(typeFilter);
    return this;
  }

  /**
   * add type filter list
   *
   * @param typeFilters type filter list
   * @return this
   */
  public Apex typeFilters(List<TypeFilter> typeFilters) {
    Validate.isTrue(typeFilters.isEmpty(), "typeFilter list can't be empty");
    this.typeFilters.addAll(typeFilters);
    return this;
  }

  /**
   * Get type filter
   *
   * @return type filter list
   */
  public Set<TypeFilter> typeFilters() {
    return typeFilters;
  }

  /**
   * Get with type annotations
   *
   * @return Annotation list
   */
  public Set<Class<? extends Annotation>> typeAnnotations() {
    return typeAnnotations;
  }

  /**
   * Configure custom annotations that need to be scanned
   *
   * @param annotatedElements annotations list
   * @return this
   */
  public Apex typeAnnotation(List<Class<? extends Annotation>> annotatedElements) {
    Validate.notNull(annotatedElements, "annotatedElements can't be null");
    this.typeAnnotations.addAll(annotatedElements);
    return this;
  }

  /**
   * Configure custom annotations that need to be scanned
   *
   * @param annotatedElements annotations array
   * @return this
   */
  @SafeVarargs
  public final Apex typeAnnotation(Class<? extends Annotation>... annotations) {
    Validate.notNull(annotations, "annotations can't be null");
    this.typeAnnotation(new ArrayList<>(Arrays.asList(annotations)));
    return this;
  }

  /**
   * Specifies the scheduler to use when scheduling routine maintenance based on an expiration
   * event. This augments the periodic maintenance that occurs during normal cache operations to
   * allow for the prompt removal of expired entries regardless of whether any cache activity is
   * occurring at that time. By default, {@link Scheduler#disabledScheduler()} is used.
   * <p>
   * The scheduling between expiration events is paced to exploit batching and to minimize
   * executions in short succession. This minimum difference between the scheduled executions is
   * implementation-specific, currently at ~1 second (2^30 ns). In addition, the provided scheduler
   * may not offer real-time guarantees (including {@link ScheduledThreadPoolExecutor}). The
   * scheduling is best-effort and does not make any hard guarantees of when an expired entry will
   * be removed.
   * <p>
   * <b>Note for Java 9 and later:</b> consider using {@link Scheduler#systemScheduler()} to
   * leverage the dedicated, system-wide scheduling thread.
   *
   * @param scheduler the scheduler that submits a task to the {@link #executor(Executor)} after a
   *                  given delay
   * @return this {@code Caffeine} instance (for chaining)
   * @throws NullPointerException if the specified scheduler is null
   */
  public Apex scheduler(Scheduler scheduler) {
    Validate.notNull(this.scheduler, "scheduler was already set to %s", this.scheduler);
    this.scheduler = requireNonNull(scheduler);
    return this;
  }

  /**
   * Specifies the executor to use when running asynchronous tasks. The executor is delegated to
   * when sending removal notifications, when asynchronous computations are performed by
   * {@link AsyncCache} or {@link LoadingCache#refresh} or {@link #refreshAfterWrite}, or when
   * performing periodic maintenance. By default, {@link ForkJoinPool#commonPool()} is used.
   * <p>
   * The primary intent of this method is to facilitate testing of caches which have been configured
   * with {@link #removalListener} or utilize asynchronous computations. A test may instead prefer
   * to configure the cache to execute tasks directly on the same thread.
   * <p>
   * Beware that configuring a cache with an executor that throws {@link RejectedExecutionException}
   * may experience non-deterministic behavior.
   *
   * @param executor the executor to use for asynchronous execution
   * @return this {@code Caffeine} instance (for chaining)
   * @throws NullPointerException if the specified executor is null
   */
  public Apex executor(Executor executor) {
    Validate.notNull(this.executor, "executor was already set to %s", this.executor);
    this.executor = requireNonNull(executor);
    return this;
  }

  /**
   * Get Executor, support through custom settings, if it is empty, use ForkJoinPool.commonPool
   *
   * @return An object that executes submitted {@link Runnable} tasks
   */
  public Executor executor() {
    return (executor == null) ? ForkJoinPool.commonPool() : executor;
  }

  /**
   * Get Scheduler
   *
   * @return Scheduler
   */
  public Scheduler scheduler() {
    if ((scheduler == null) || (scheduler == Scheduler.disabledScheduler())) {
      return Scheduler.disabledScheduler();
    } else if (scheduler == Scheduler.systemScheduler()) {
      return scheduler;
    }
    return Scheduler.guardedScheduler(scheduler);
  }

  /**
   * Get scan package
   *
   * @return scan packages
   */
  public Set<String> packages() {
    return scanPackages;
  }

  /**
   * Set main method args
   */
  public void mainArgs(String[] args) {
    this.args = args;
  }

  /**
   * Get main method args
   *
   * @return main method args
   */
  public String[] mainArgs() {
    return args;
  }

  /**
   * Get Singleton Apex Object
   */
  private static class ApexHolder {
    private static final Apex instance = new Apex();
  }
}
