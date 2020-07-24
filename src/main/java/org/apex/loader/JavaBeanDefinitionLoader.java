package org.apex.loader;

import io.github.classgraph.ScanResult;
import org.apex.AbstractApexFactory;
import org.apex.BeanDefinition;
import org.apex.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author WangYi
 * @since 2020/7/24
 */
public class JavaBeanDefinitionLoader implements BeanDefinitionLoader {
  private static final Logger log = LoggerFactory.getLogger(AbstractApexFactory.class);

  protected final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(64);
  protected final List<Class<? extends Annotation>> annotatedElements = new ArrayList<>();
  protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  public JavaBeanDefinitionLoader() {
    this.annotatedElements.add(Singleton.class);
    this.annotatedElements.add(Configuration.class);
  }

  protected void registerBeanDefinition(Map<Object, Class<?>> candidateMap) {
    if (Objects.isNull(candidateMap) || candidateMap.isEmpty()) {
      log.info("No candidates were found in the scan");
    }
    if (candidateMap.isEmpty()) {
      return;
    }
    for (Map.Entry<Object, Class<?>> entry : candidateMap.entrySet()) {
      final Object instants = entry.getKey();
      final Class<?> clazz = entry.getValue();
      final BeanDefinition beanDefinition = this.buildBeanDefinition(instants, clazz);
      if (log.isDebugEnabled())
        log.debug("The beans that have completed the " +
                "Bean Definition construction are:{}", beanDefinition.getName());
      this.registerBeanDefinition(beanDefinition);
    }
  }

  private void registerBeanDefinition(BeanDefinition beanDefinition) {
    try {
      this.lock.writeLock().lock();
      this.beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  protected BeanDefinition buildBeanDefinition(Object instants, Class<?> clazz) {
    final BeanDefinition beanDefinition = new BeanDefinition();
    beanDefinition.setName(clazz.getName());
    beanDefinition.setSimpleName(clazz.getSimpleName());
    beanDefinition.setInstants(instants);
    beanDefinition.setFields(clazz.getDeclaredFields());
    beanDefinition.setMethods(clazz.getDeclaredMethods());
    beanDefinition.setExtendsClass(clazz.getSuperclass());
    beanDefinition.setImplInterfaces(clazz.getInterfaces());
    return beanDefinition;
  }

  protected void release(ScanResult scanResult) {
    scanResult.close();
  }

  protected Map<Object, Class<?>> filterCandidates(List<Class<?>> collection) {
    final Map<Object, Class<?>> candidates = new HashMap<>();

    collection.stream().filter(next -> {
      int modifiers = next.getModifiers();
      return !next.isAnnotation() && !next.isEnum() && !Modifier.isAbstract(modifiers);
    }).forEach(next -> {
      if (log.isDebugEnabled()) {
        log.debug("Classes that currently meet " +
                "the registration requirements:{}", next.getSimpleName());
      }
      Object instance = getObject(next);
      candidates.put(Objects.requireNonNull(instance), next);
    });

    log.info("A total of {} eligible and registerable beans were scanned", candidates.size());
    log.info("Candidate selection is completed and encapsulated as Bean Definition after being instantiated");
    return Objects.requireNonNull(candidates);
  }

  private Object getObject(Class<?> next) {
    try {
      return next.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      log.error("Class New Object Exception", e);
      return null;
    }
  }

  @Override
  public Map<String, BeanDefinition> load(List<Class<?>> classList) {
    final List<Class<?>> collection = new ArrayList<>();

    for (Class<?> cls : classList) {
      Annotation[] declaredAnnotations = cls.getDeclaredAnnotations();
      this.annotatedElements.containsAll(Arrays.asList(declaredAnnotations));
      collection.add(cls);
    }
    Map<Object, Class<?>> objectClassMap = this.filterCandidates(collection);
    registerBeanDefinition(objectClassMap);

    return beanDefinitionMap;
  }
}
