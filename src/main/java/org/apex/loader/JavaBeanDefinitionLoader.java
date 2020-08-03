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
package org.apex.loader;

import org.apex.AbstractApexFactory;
import org.apex.BeanDefinition;
import org.apex.BeanDefinitionFactory;
import org.apex.annotation.Configuration;
import org.apex.annotation.ConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.ref.SoftReference;
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
  private final Object[] EMPTY = new Object[]{};
  private final SoftReference<Object[]> invokeSoftRef = new SoftReference<>(EMPTY);

  public JavaBeanDefinitionLoader() {
    this.annotatedElements.add(Singleton.class);
    this.annotatedElements.add(Configuration.class);
    this.annotatedElements.add(ConfigurationProperty.class);
  }

  private Object invoke(Object instants, Method method) throws Throwable {
    return this.lookup.unreflect(method).bindTo(instants).invokeWithArguments(invokeSoftRef.get());
  }

  protected void registerBeanDefinition(Map<Object, Class<?>> candidateMap) throws Throwable {
    if (Objects.isNull(candidateMap) || candidateMap.isEmpty()) {
      log.info("No candidates were found in the scan");
    }
    if (candidateMap.isEmpty()) {
      return;
    }
    for (Map.Entry<Object, Class<?>> entry : candidateMap.entrySet()) {
      final Object instants = entry.getKey();
      final Class<?> clazz = entry.getValue();
      registerConfigurationBean(instants, clazz);
    }
  }

  private void registerConfigurationBean(Object instants, Class<?> clazz) throws Throwable {
    if (clazz.isAnnotationPresent(Configuration.class)) {
      Method[] declaredMethods = clazz.getDeclaredMethods();
      if (declaredMethods.length == 0) {
        return;
      }
      registerConfigurationBean(instants, declaredMethods);
    } else {
      this.registerBeanDefinition(instants, clazz);
    }
  }

  private void registerConfigurationBean(Object instants, Method[] declaredMethods) throws Throwable {
    for (Method method : declaredMethods) {
      if (method.getReturnType() == void.class) {
        throw new IllegalArgumentException("The return value of the method marked with " +
                "Bean annotation in the configuration cannot be " +
                "void:{" + instants.getClass().getName() + "}" + "#" + method.getName());
      }
      Object object = null;
      if (method.getParameterCount() == 0) {
        object = invoke(instants, method);
      }
      if (Objects.isNull(object)) {
        continue;
      }
      this.registerBeanDefinition(object, object.getClass());
    }
  }

  private void registerBeanDefinition(Object instants, Class<?> clazz) {
    final BeanDefinition beanDefinition = BeanDefinitionFactory.createBeanDefinition(instants, clazz);
    if (log.isDebugEnabled())
      log.debug("The beans that have completed the " +
              "Bean Definition construction are:{}", beanDefinition.getName());
    this.registerBeanDefinition(beanDefinition);
  }

  private void registerBeanDefinition(BeanDefinition beanDefinition) {
    try {
      this.lock.writeLock().lock();
      this.beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
    } finally {
      this.lock.writeLock().unlock();
    }
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
  public Map<String, BeanDefinition> load(List<Class<?>> classList) throws Throwable {
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
