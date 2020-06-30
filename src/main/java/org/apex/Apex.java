/*
 * MIT License
 *
 * Copyright (c) 2019 1619kHz
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

import io.github.classgraph.ScanResult;
import org.apex.scheduler.Scheduler;
import org.apex.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static java.util.Objects.requireNonNull;
import static org.apex.Const.*;

public final class Apex {
  private static final Logger log = LoggerFactory.getLogger(Apex.class);

  /** List of paths to be scanned and ignored. */
  private final Set<String> skipPaths = new LinkedHashSet<>();
  private final Set<String> packages = new LinkedHashSet<>();

  /** Variables about whether the scan status and environment configuration are enabled. */
  private final boolean verbose = false;
  private final boolean realtimeLogging = false;
  private boolean envConfig = false;
  private boolean masterConfig = false;

  /** Current environment and unified environment objects. */
  private String envName = ENV_NAME;
  private Environment environment = new Environment();
  private BeanResolver beanResolver = new DefaultBeanResolver();
  private Options options = this.buildOptions();
  private Scanner scanner = new ClassgraphScanner(options);

  /** Scan related objects and configuration information. */
  private String scanPath;
  private Class<?> bootCls;
  private String[] mainArgs;

  /** Cacheable thread pool for running scanning services. */
  private Scheduler scheduler;
  private Executor executor;

  private final ApexContext apexContext = ApexContext.of();

  public ApexContext apexContext() {
    return apexContext;
  }

  private Apex() {
  }

  /** Ensures that the argument expression is true. */
  static void requireArgument(boolean expression, String template, Object... args) {
    if (!expression) {
      throw new IllegalArgumentException(String.format(template, args));
    }
  }

  /** Ensures that the argument expression is true. */
  static void requireArgument(boolean expression) {
    if (!expression) {
      throw new IllegalArgumentException();
    }
  }

  /** Ensures that the state expression is true. */
  static void requireState(boolean expression) {
    if (!expression) {
      throw new IllegalStateException();
    }
  }

  /** Ensures that the state expression is true. */
  static void requireState(boolean expression, String template, Object... args) {
    if (!expression) {
      throw new IllegalStateException(String.format(template, args));
    }
  }

  public static Apex of() {
    return ApexHolder.instance;
  }

  public boolean verbose() {
    return this.verbose;
  }

  public boolean realtimeLogging() {
    return this.realtimeLogging;
  }

  public Environment environment() {
    return environment;
  }

  public boolean masterConfig() {
    return masterConfig;
  }

  public String envName() {
    return envName;
  }

  public Class<?> bootCls() {
    return bootCls;
  }

  public String[] mainArgs() {
    return mainArgs;
  }

  public Apex packages(Collection<String> packages) {
    if (!packages.isEmpty()) {
      this.packages.addAll(packages);
    }
    return this;
  }

  public Apex skipPath(String skipPath) {
    requireArgument(skipPath.contains("."), "Need correct format");
    this.skipPaths.add(Objects.requireNonNull(skipPath));
    return this;
  }

  public Apex skipPath(Collection<String> skipPaths) {
    if (!skipPaths.isEmpty()) {
      this.skipPaths.addAll(skipPaths);
    }
    return this;
  }

  public Apex scanner(Scanner scanner) {
    requireArgument(this.scanner == null, "beanResolver was already set to %s", this.scanner);
    this.scanner = Objects.requireNonNull(scanner);
    return this;
  }

  public Apex packages(String scanPath) {
    requireArgument(Files.exists(Paths.get(scanPath)), "Need a valid and existing scan path");
    this.scanPath = Objects.requireNonNull(scanPath);
    this.packages.add(Objects.requireNonNull(scanPath));
    return this;
  }

  public Apex environment(Environment environment) {
    requireArgument(this.environment == null, "beanResolver was already set to %s", this.environment);
    this.environment = requireNonNull(environment);
    return this;
  }

  public Apex resolver(BeanResolver beanResolver) {
    requireArgument(this.beanResolver == null, "beanResolver was already set to %s", this.beanResolver);
    this.beanResolver = requireNonNull(beanResolver);
    return this;
  }

  public Apex options(Options options) {
    requireArgument(this.options == null, "options was already set to %s", this.options);
    this.options = requireNonNull(options);
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
    requireState(this.scheduler == null, "scheduler was already set to %s", this.scheduler);
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
    requireState(this.executor == null, "executor was already set to %s", this.executor);
    this.executor = requireNonNull(executor);
    return this;
  }

  public Scheduler getScheduler() {
    if ((scheduler == null) || (scheduler == Scheduler.disabledScheduler())) {
      return Scheduler.disabledScheduler();
    } else if (scheduler == Scheduler.systemScheduler()) {
      return scheduler;
    }
    return Scheduler.guardedScheduler(scheduler);
  }

  public Executor getExecutor() {
    return (executor == null) ? ForkJoinPool.commonPool() : executor;
  }

  public void start(Class<?> bootCls, String[] mainArgs) {
    requireNonNull(bootCls, "Apex needs to specify the startup class when starting.");
    try {
      this.bootCls = bootCls;
      this.scanPath = bootCls.getPackage().getName();
      this.loadConfig(mainArgs);
    } catch (IllegalAccessException e) {
      log.error("An exception occurred while loading the configuration", e);
    }
    try {
      Thread thread =new Thread(() -> {
        final ScanResult scanResult = scanner.discover(scanPath);
        final Map<String, BeanDefinition> beanDefinitionMap = beanResolver.resolve(scanResult);
        apexContext.init(beanDefinitionMap);
      }, SERVER_THREAD_NAME);
      thread.start();
    } catch (Exception e) {
      log.error("Bean resolve be exception:", e);
    }
  }

  /**
   * Scan options for building Classgraph
   *
   * @return ClassgraphOptions
   */
  private ClassgraphOptions buildOptions() {
    return ClassgraphOptions.builder()
            .verbose(verbose)
            .scanPackages(packages)
            .skipPackages(skipPaths)
            .realtimeLogging(realtimeLogging)
            .build();
  }

  private static class ApexHolder {
    private static final Apex instance = new Apex();
  }

  /**
   * Load configuration from multiple places between startup services.
   * Support items are: Properties are configured by default, and the
   * properties loaded by default are application.properties If there
   * is no properties configuration, the yaml format is used, and the
   * default yaml loaded is application.yml Support loading
   * configuration from args array of main function Support loading
   * configuration from System.Property
   *
   * @param args main method args
   * @throws IllegalAccessException IllegalAccessException
   */
  private void loadConfig(String[] args) throws IllegalAccessException {
    String bootConf = environment().get(PATH_SERVER_BOOT_CONFIG, PATH_CONFIG_PROPERTIES);
    Environment bootConfEnv = Environment.of(bootConf);

    Map<String, String> argsMap = this.loadMainArgs(args);
    Map<String, String> constField = PropertyUtils.confFieldMap();

    this.loadPropsOrYaml(bootConfEnv, constField);

    /** Support loading configuration from args array of main function. */
    if (!requireNonNull(bootConfEnv).isEmpty()) {
      Map<String, String> bootEnvMap = bootConfEnv.toStringMap();
      Set<Map.Entry<String, String>> entrySet = bootEnvMap.entrySet();

      entrySet.forEach(entry -> this.environment
              .add(entry.getKey(), entry.getValue()));

      this.masterConfig = true;
    }

    if (Objects.nonNull(argsMap.get(PATH_SERVER_PROFILE))) {
      String envNameArg = argsMap.get(PATH_SERVER_PROFILE);
      this.envConfig(envNameArg);
      this.envName = envNameArg;
      argsMap.remove(PATH_SERVER_PROFILE);
      this.envConfig = true;
    }

    if (!envConfig) {
      String profileName = this.environment.get(PATH_SERVER_PROFILE);
      if (Objects.nonNull(profileName) && !"".equals(profileName)) {
        envConfig(profileName);
        this.envName = profileName;
      }
    }
  }

  /**
   * load properties and yaml
   *
   * @param bootConfEnv Environment used when the server starts
   * @param constField  Constant attribute map
   */
  private void loadPropsOrYaml(Environment bootConfEnv, Map<String, String> constField) {
    /** Properties are configured by default, and the properties loaded
     * by default are application.properties */
    constField.keySet().forEach(key ->
            Optional.ofNullable(System.getProperty(constField.get(key)))
                    .ifPresent(property -> bootConfEnv.add(key, property)));

    /** If there is no properties configuration, the yaml format is
     * used, and the default yaml loaded is application.yml */
    if (bootConfEnv.isEmpty()) {
      Optional.ofNullable(PropertyUtils.yaml(PATH_CONFIG_YAML))
              .ifPresent(yamlConfigTreeMap ->
                      bootConfEnv.load(new StringReader(
                              PropertyUtils.toProperties(yamlConfigTreeMap))));
    }
  }

  /**
   * Load main function parameters, and override if main configuration exists
   *
   * @param args String parameter array of main method
   * @return Write the parameters to the map and return
   */
  private Map<String, String> loadMainArgs(String[] args) {
    Map<String, String> argsMap = PropertyUtils.parseArgs(args);
    if (argsMap.size() > 0) {
      log.info("Entered command line:{}", argsMap.toString());
    }

    for (Map.Entry<String, String> next : argsMap.entrySet()) {
      this.environment.add(next.getKey(), next.getValue());
    }
    return argsMap;
  }


  /**
   * Load the environment configuration, if it exists in the main
   * configuration, it will be overwritten in the environment
   * configuration
   *
   * @param envName Environment name
   */
  private void envConfig(String envName) {
    String envFileName = "application" + "-" + envName + ".properties";
    Environment customerEnv = Environment.of(envFileName);
    if (customerEnv.isEmpty()) {
      String envYmlFileName = "application" + "-" + envName + ".yml";
      customerEnv = Environment.of(envYmlFileName);
    }
    if (!customerEnv.isEmpty()) {
      customerEnv.props().forEach((key, value) ->
              this.environment.add(key.toString(), value));
    }
    this.environment.add(PATH_SERVER_PROFILE, envName);
  }
}
