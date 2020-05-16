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

import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.apex.annotation.Component;
import org.apex.annotation.Configuration;
import org.apex.annotation.PostConstruct;
import org.apex.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class DefaultBeanResolver implements BeanResolver {
  private static final Logger log = LoggerFactory.getLogger(DefaultBeanResolver.class);

  private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(64);
  private final List<String> beanDefinitionNames = new ArrayList<>(64);

  private final List<String> annotationNames = new ArrayList<>();

  public List<String> getAnnotationNames() {
    annotationNames.add(Component.class.getName());
    annotationNames.add(Configuration.class.getName());
    annotationNames.add(Service.class.getName());
    return annotationNames;
  }

  @Override
  public Map<String, BeanDefinition> resolve(ScanResult scanResult) {
    long startMs = System.currentTimeMillis();

    if (null == scanResult || scanResult.getAllClasses().isEmpty()) {
      log.info("No candidates were found in the scan");
      return beanDefinitionMap;
    }

    final List<Class<?>> candidates = new ArrayList<>();
    this.getAnnotationNames().stream()
            .map(getResultSet(scanResult))
            .forEach(getClassInfoList(candidates));

    CompletableFuture<List<Class<?>>> future = CompletableFuture.completedFuture(candidates);
    future.thenApply(candidate -> this.filterCandidates(candidate, startMs))
            .thenAccept(this::registerBeanDefinition);

    scanResult.close();
    future.complete(candidates);

    long completeTime = (System.currentTimeMillis() - startMs);
    if (beanDefinitionMap.isEmpty()) {
      log.info("No suitable candidates were found in this scan");
    } else {
      log.info("The process of encapsulating the scanned class into Bean " +
              "Definition has been completed，Time consuming：{}ms", completeTime);
    }
    return beanDefinitionMap;
  }

  private Function<String, ClassInfoList> getResultSet(ScanResult scanResult) {
    return scanResult::getClassesWithAnnotation;
  }

  private Consumer<ClassInfoList> getClassInfoList(List<Class<?>> candidates) {
    return classInfos -> candidates.addAll(classInfos.loadClasses());
  }

  private void registerBeanDefinition(Map<Object, Class<?>> candidateMap) {
    if (candidateMap.isEmpty()) return;
    for (Map.Entry<Object, Class<?>> entry : candidateMap.entrySet()) {
      BeanDefinition beanDefinition = this.buildBeanDefinition(entry);
      if (log.isDebugEnabled())
        log.debug("The beans that have completed the " +
                "Bean Definition construction are:{}", beanDefinition.getName());
      this.beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
    }
  }

  private List<Method> getInitMethod(Method[] declaredMethods) {
    List<Method> initMethods = new ArrayList<>();
    Arrays.stream(declaredMethods).filter(this::isInitMethod)
            .forEach(initMethods::add);
    return initMethods;
  }

  private boolean isInitMethod(Method method) {
    return !Objects.isNull(method.getAnnotation(PostConstruct.class));
  }

  private long sizeOf(Object instants) {
    try (ByteArrayOutputStream byteArrayOps =
                 new ByteArrayOutputStream();
         ObjectOutputStream objectOps =
                 new ObjectOutputStream(byteArrayOps);) {
      objectOps.writeObject(instants);
      return byteArrayOps.size();
    } catch (IOException e) {
      log.error("An exception occurred while " +
              "calculating the size of the object bytes", e);
      return 0;
    }
  }

  private BeanDefinition buildBeanDefinition(Map.Entry<Object, Class<?>> entry) {
    final Object instants = entry.getKey();
    final Class<?> clazz = entry.getValue();
    BeanDefinition beanDefinition = new BeanDefinition();
    beanDefinition.setName(clazz.getName());
    beanDefinition.setSimpleName(clazz.getSimpleName());
    beanDefinition.setInstants(instants);
    beanDefinition.setClassSize(sizeOf(instants));
    beanDefinition.setFields(clazz.getDeclaredFields());
    beanDefinition.setMethods(clazz.getDeclaredMethods());
    beanDefinition.setInitMethod(getInitMethod(clazz.getDeclaredMethods()));
    beanDefinition.setExtendsClass(clazz.getSuperclass());
    beanDefinition.setImplInterfaces(clazz.getInterfaces());
    return beanDefinition;
  }

  private Map<Object, Class<?>> filterCandidates(List<Class<?>> collection, long startMs) {
    Map<Object, Class<?>> candidates = new HashMap<>();
    Iterator<Class<?>> iterator = collection.iterator();
    try {
      while (iterator.hasNext()) {
        Class<?> next = iterator.next();
        int modifiers = next.getModifiers();
        if (next.isAnnotation() || next.isEnum() || Modifier.isAbstract(modifiers)) {
          iterator.remove();
          continue;
        }
        if (log.isDebugEnabled()) {
          log.debug("Classes that currently meet " +
                  "the registration requirements:{}", next.getSimpleName());
        }
        Object instance = next.getConstructor().newInstance();
        candidates.put(Objects.requireNonNull(instance), next);
        this.beanDefinitionNames.add(next.getName());
      }
    } catch (ReflectiveOperationException e) {
      log.error("An exception occurred while instantiating the object", e);
    }
    long completeTime = (System.currentTimeMillis() - startMs);
    log.info("The bean scan is complete time consuming: {}ms", completeTime);
    log.info("A total of {} eligible and registerable beans were scanned", candidates.size());
    log.info("Candidate selection is completed and encapsulated as Bean Definition after being instantiated");
    return Objects.requireNonNull(candidates);
  }
}
