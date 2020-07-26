package org.apex.loader;

import org.apex.AbstractApexFactory;
import org.apex.BeanDefinition;
import org.apex.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
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
  private final MethodHandles.Lookup lookup = MethodHandles.lookup();

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
      if (clazz.isAnnotationPresent(Configuration.class)) {
        Method[] declaredMethods = clazz.getDeclaredMethods();
        if (declaredMethods.length == 0) {
          continue;
        }
        for (Method method : declaredMethods) {
          Object object = invokeMethod(instants, method);
          if (Objects.isNull(object)) {
            continue;
          }
          this.registerBeanDefinition(instants, object.getClass());
        }
      } else {
        this.registerBeanDefinition(instants, clazz);
      }
    }
  }

  private void registerBeanDefinition(Object instants, Class<?> clazz) {
    final BeanDefinition beanDefinition = this.buildBeanDefinition(instants, clazz);
    if (log.isDebugEnabled())
      log.debug("The beans that have completed the " +
              "Bean Definition construction are:{}", beanDefinition.getName());
    this.registerBeanDefinition(beanDefinition);
  }

  private Object invokeMethod(Object instants, Method method) {
    try {
      if (method.getReturnType() == void.class) {
        throw new IllegalArgumentException("The return value of the method marked with " +
                "Bean annotation in the configuration cannot be " +
                "void:{" + instants.getClass().getName() + "}" + "#" + method.getName());
      }
      Object[] EMPTY = new Object[]{};
      return this.lookup.unreflect(method).bindTo(instants).invokeWithArguments(EMPTY);
    } catch (Throwable throwable) {
      log.error("An exception occurred when MethodHandler invoke a method ", throwable);
      return null;
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
      if (declaredAnnotations.length == 0) {
        continue;
      }
      for (Annotation annotation : declaredAnnotations) {
        if (this.annotatedElements.contains(annotation.annotationType())) {
          collection.add(cls);
        }
      }
    }
    Map<Object, Class<?>> objectClassMap = this.filterCandidates(collection);
    registerBeanDefinition(objectClassMap);

    return beanDefinitionMap;
  }
}
