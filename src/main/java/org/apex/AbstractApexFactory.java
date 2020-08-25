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

import org.apex.injector.Injector;
import org.apex.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

/**
 * @author WangYi
 * @since 2020/6/22
 */
@SuppressWarnings("unchecked")
public abstract class AbstractApexFactory implements ApexFactory {
  private static final Logger log = LoggerFactory.getLogger(AbstractApexFactory.class);

  protected final Map<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>(64);
  protected final Map<String, Object> instanceMapping = new ConcurrentHashMap<>();

  public Map<String, Object> getInstances() {
    return instanceMapping;
  }

  private <T> T getInjectBean(Class<T> cls) {
    requireNonNull(cls, "cls must not be null");
    Object o = instanceMapping.get(cls.getName());
    return getInjectBean(cls.isAssignableFrom(o.getClass()) ? cls.cast(o) : (T) o);
  }

  private <T> T getInjectBean(Object obj) {
    requireNonNull(obj, "obj must not be null");
    final ServiceLoader<Injector> injectors = ServiceLoader.load(Injector.class);
    try {
      for (final Injector next : injectors) {
        next.inject(obj);
      }
      return (T) obj;
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public <T> T getBean(Class<T> cls) {
    requireNonNull(cls, "cls must not be null");
    if (instanceMapping.containsKey(cls.getName())) {
      return this.getInjectBean(cls);
    }
    return null;
  }

  @Override
  public <T> T getBean(String beanName) {
    requireNonNull(beanName, "beanName must not be null");
    try {
      return ((T) getBean(Class.forName(beanName)));
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  @Override
  public <T> T getBean(Object obj) {
    requireNonNull(obj, "obj must not be null");
    return ((T) getBean(obj.getClass()));
  }

  @Override
  public <T> T addBean(Class<T> cls) {
    requireNonNull(cls, "cls must not be null");
    final T ref = ReflectionUtils.newInstance(cls);
    this.instanceMapping.put(cls.getName(), ref);
    return getBean(cls);
  }

  @Override
  public <T> T addBean(String beanName) {
    requireNonNull(beanName, "beanName must not be null");
    try {
      return addBean((Class<T>) Class.forName(beanName));
    } catch (ClassNotFoundException e) {
      log.error("An exception occurred while creating an instance via reflection", e);
    }
    return null;
  }

  @Override
  public <T> T addBean(Object obj) {
    requireNonNull(obj, "obj must not be null");
    return addBean((Class<T>) obj.getClass());
  }

  @Override
  public <T> List<T> getBeanByType(Class<T> cls) {
    requireNonNull(cls, "cls must not be null");
    List<T> refs = new ArrayList<>();
    if (instanceMapping.isEmpty()) {
      return refs;
    }
    for (Map.Entry<String, Object> entry : instanceMapping.entrySet()) {
      Object value = entry.getValue();
      if (value.getClass().isAssignableFrom(cls)) {
        refs.add((T) value);
      }
    }
    return refs;
  }

  @Override
  public <T> T newInstance(Class<T> cls) {
    return null;
  }

  @Override
  public <T> List<T> getBeanByType(Object obj) {
    requireNonNull(obj, "obj must not be null");
    Class<T> ref = (Class<T>) obj.getClass();
    return getBeanByType(ref);
  }

  @Override
  public void removeAll() {
    this.instanceMapping.clear();
    this.beanDefinitions.clear();
  }

  @Override
  public void removeBean(String beanName) {
    this.instanceMapping.remove(beanName);
    this.beanDefinitions.remove(beanName);
  }
}
