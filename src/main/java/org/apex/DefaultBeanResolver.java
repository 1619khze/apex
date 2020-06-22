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

import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultBeanResolver implements BeanResolver {
  private static final Logger log = LoggerFactory.getLogger(DefaultBeanResolver.class);

  private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(64);

  @Override
  public Map<String, BeanDefinition> resolve(ScanResult scanResult) {
    final long startMs = System.currentTimeMillis();
    final List<Class<?>> candidates = new ArrayList<>();

    if (Objects.isNull(scanResult) || scanResult.getAllClasses().isEmpty()) {
      log.info("No candidates were found in the scan");
      return beanDefinitionMap;
    }

    final ClassInfoList allClasses = scanResult.getAllClasses();

    if (allClasses.isEmpty()) {
      return beanDefinitionMap;
    }

    for (ClassInfo classInfo : allClasses) {
      if (classInfo.hasAnnotation(Singleton.class.getName()) &&
              (!classInfo.isAbstract() && !classInfo.isEnum() &&
                      !classInfo.isAnnotation())) {

        Class<?> candidate = classInfo.loadClass();
        candidates.add(candidate);
      }
    }

    Map<Object, Class<?>> objectClassMap = this.filterCandidates(candidates, startMs);
    registerBeanDefinition(objectClassMap);

    if (beanDefinitionMap.isEmpty()) {
      log.info("No suitable candidates were found in this scan");
    } else {
      long completeTime = (System.currentTimeMillis() - startMs);
      log.info("The process of encapsulating the scanned class into Bean " +
              "Definition has been completed，Time consuming：{}ms", completeTime);
    }
    scanResult.close();
    return beanDefinitionMap;
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

  private BeanDefinition buildBeanDefinition(Map.Entry<Object, Class<?>> entry) {
    final Object instants = entry.getKey();
    final Class<?> clazz = entry.getValue();
    BeanDefinition beanDefinition = new BeanDefinition();
    beanDefinition.setName(clazz.getName());
    beanDefinition.setSimpleName(clazz.getSimpleName());
    beanDefinition.setInstants(instants);
    beanDefinition.setFields(clazz.getDeclaredFields());
    beanDefinition.setMethods(clazz.getDeclaredMethods());
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
