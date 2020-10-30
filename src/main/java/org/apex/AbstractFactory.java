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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WangYi
 * @since 2020/6/22
 */
public abstract class AbstractFactory implements ApexFactory {
  private static final Logger log = LoggerFactory.getLogger(AbstractFactory.class);

  protected final Map<String, KlassInfo> klassInfoMap = new ConcurrentHashMap<>(64);
  protected final Map<String, Object> instanceMap = new ConcurrentHashMap<>();
  protected final ServiceLoader<Injector> injectors = ServiceLoader.load(Injector.class);

  public Map<String, Object> instances() {
    return instanceMap;
  }

  protected <T> T getInjectBean(Class<T> cls) {
    Validate.notNull(cls, "cls must not be null");
    Object obj = instanceMap.get(cls.getName());
    return getInjectBean(cls.isAssignableFrom(obj.getClass()) ? cls.cast(obj) : obj);
  }

  protected <T> T getInjectBean(Object obj) {
    Validate.notNull(obj, "obj must not be null");
    try {
      for (final Injector next : injectors) {
        next.inject(InjectContext.create(
                klassInfoMap.getOrDefault(obj.getClass().getName(),
                        KlassInfo.create(obj)), instanceMap));
      }
      return (T) obj;
    } catch (Exception e) {
      throw new BeanInstantiationException("obj can't be injected");
    }
  }

  @Override
  public <T> T getBean(Class<T> cls) {
    Validate.notNull(cls, "cls must not be null");
    if (instanceMap.containsKey(cls.getName())) {
      return this.getInjectBean(cls);
    } else {
      return null;
    }
  }

  @Override
  public <T> T getBean(String beanName) {
    Validate.notNull(beanName, "beanName must not be null");
    return getBean(instanceMap.get(beanName));
  }

  @Override
  public <T> T getBean(Object obj) {
    Validate.notNull(obj, "obj must not be null");
    return ((T) getBean(obj.getClass()));
  }

  @Override
  public <T> T addBean(Class<T> cls) {
    Validate.notNull(cls, "cls must not be null");
    final T ref = ReflectionHelper.newInstance(cls);
    this.instanceMap.put(cls.getName(), ref);
    return getBean(cls);
  }

  @Override
  public <T> T addBean(String beanName) {
    Validate.notNull(beanName, "beanName must not be null");
    try {
      return addBean((Class<T>) Class.forName(beanName));
    } catch (ClassNotFoundException e) {
      log.error("An exception occurred while creating an instance via reflection", e);
      return null;
    }
  }

  @Override
  public <T> T addBean(Object obj) {
    Validate.notNull(obj, "obj must not be null");
    this.instanceMap.put(obj.getClass().getName(), obj);
    return getBean(obj);
  }

  @Override
  public <T> List<T> getBeanByType(Class<T> cls) {
    Validate.notNull(cls, "cls must not be null");
    List<T> refs = new ArrayList<>();
    if (instanceMap.isEmpty()) {
      return refs;
    }
    for (Map.Entry<String, Object> entry : instanceMap.entrySet()) {
      Object value = entry.getValue();
      if (value.getClass().isAssignableFrom(cls)) {
        refs.add((T) value);
      }
    }
    return refs;
  }

  @Override
  public <T> List<T> getBeanByType(Object obj) {
    Validate.notNull(obj, "obj must not be null");
    Class<T> ref = (Class<T>) obj.getClass();
    return getBeanByType(ref);
  }

  @Override
  public void removeAll() {
    this.instanceMap.clear();
    this.klassInfoMap.clear();
  }

  @Override
  public void removeBean(String beanName) {
    this.instanceMap.remove(beanName);
    this.klassInfoMap.remove(beanName);
  }

  public abstract void init(Apex apex) throws Throwable;
}
