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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/6/22
 */
public class ApexContext extends AbstractApexFactory {
  private static final Logger log = LoggerFactory.getLogger(ApexContext.class);

  private ApexContext() {}

  public static ApexContext of() {
    return ApexContextHolder.instance();
  }

  void init(Map<String, BeanDefinition> beanDefinitions) {
    this.beanDefinitions.putAll(beanDefinitions);
    for (Map.Entry<String, BeanDefinition> entry : beanDefinitions.entrySet()) {
      instanceMapping.put(entry.getKey(), entry.getValue().getInstants());
    }
    invokeInject();
  }

  private void invokeInject() {
    for (Map.Entry<String, Object> entry : instanceMapping.entrySet()) {
      Field[] fields = entry.getValue().getClass().getDeclaredFields();
      for (Field field : fields) {
        if (!field.isAnnotationPresent(Inject.class)) {
          continue;
        }
        String id = field.getType().getName();
        Inject inject = field.getAnnotation(Inject.class);
        if (Objects.nonNull(inject)) {
          Named named = field.getAnnotation(Named.class);
          if (Objects.nonNull(named) &&
                  Objects.equals(named.value(), "")) {
            id = named.value();
          }
        }
        field.setAccessible(true);
        try {
          field.set(entry.getValue(), instanceMapping.get(id));
        } catch (IllegalAccessException e) {
          log.error("An exception occurred while injecting field");
        }
      }
    }
  }

  private static class ApexContextHolder {
    private static final ApexContext instance = new ApexContext();
    public static ApexContext instance() {
      return instance;
    }
  }
}
