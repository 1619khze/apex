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
package org.apex.injector;

import org.apex.Apex;
import org.apex.Environment;
import org.apex.annotation.ConfigurationProperty;
import org.apex.injector.type.TypeInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * @author WangYi
 * @since 2020/8/4
 */
public class ConfigBeanInjector implements Injector {
  private final Logger log = LoggerFactory.getLogger(ConfigBeanInjector.class);
  private final Environment environment = Apex.of().environment();

  @Override
  public void inject(Object obj) {
    final Class<?> ref = obj.getClass();
    final Field[] declaredFields = ref.getDeclaredFields();
    if (!ref.isAnnotationPresent(ConfigurationProperty.class)
            || declaredFields.length == 0) {
      return;
    }
    final ConfigurationProperty annotation = ref.getAnnotation(
            ConfigurationProperty.class);

    final String prefix = annotation.value();
    this.inject(obj, prefix, declaredFields);
  }

  private void inject(Object obj, String prefix, Field[] declaredFields) {
    for (Field field : declaredFields) {
      field.setAccessible(true);
      Object fieldProperty = null;

      String name = prefix + "." + field.getName();
      final ServiceLoader<TypeInjector> typeInjectors
              = ServiceLoader.load(TypeInjector.class);

      for (TypeInjector typeInjector : typeInjectors) {
        if (field.getType().equals(typeInjector.getType())) {
          fieldProperty = typeInjector.inject(name);
        }
      }
      if (Objects.isNull(fieldProperty)) {
        fieldProperty = environment.getObject(name);
      }
      fieldInject(obj, field, fieldProperty);
    }
  }

  private void fieldInject(Object obj, Field field, Object fieldProperty) {
    try {
      field.set(obj, fieldProperty);
    } catch (IllegalAccessException e) {
      log.error("Injection exception, current field: {} " +
              "injection {} failed", field.getName(), fieldProperty);
    }
  }
}
