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

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author WangYi
 * @since 2020/8/4
 */
public class ConfigPropertyInjector implements Injector {
  private final Environment environment = Apex.of().environment();

  @Override
  public void inject(Map<String, Object> instanceMapping) throws Exception {
    for (Map.Entry<String, Object> entry : instanceMapping.entrySet()) {
      final Object obj = entry.getValue();
      final Class<?> ref = obj.getClass();

      if (!ref.isAnnotationPresent(ConfigurationProperty.class)) {
        continue;
      }
      final ConfigurationProperty annotation =
              ref.getAnnotation(ConfigurationProperty.class);

      final String prefix = annotation.value();
      Field[] declaredFields = ref.getDeclaredFields();
      if (declaredFields.length == 0) {
        return;
      }
      this.inject(obj, prefix, declaredFields);
    }
  }

  private void inject(Object obj, String prefix, Field[] declaredFields) throws IllegalAccessException {
    for (Field field : declaredFields) {
      String name = prefix + "." + field.getName();
      field.setAccessible(true);
      Object fieldProperty = null;

      if (field.getType().isAssignableFrom(Map.class)) {
        fieldProperty = injectMap(environment, name);
      }

      if (field.getType().isAssignableFrom(List.class)) {
        fieldProperty = injectList(environment, name);
      }

      if (Objects.isNull(fieldProperty)) {
        fieldProperty = environment.getObject(name);
      }
      field.set(obj, fieldProperty);
    }
  }

  private Object injectList(Environment environment, String name) {
    Map<String, String> propsMap = environment.toStringMap();
    final long count = propsMap.keySet().stream()
            .filter(key -> key.startsWith(name + "[") && key.endsWith("]"))
            .count();

    final int idx = ((int) count);
    final List<Object> fieldList = new ArrayList<>(idx);
    if (propsMap.isEmpty()) {
      return fieldList;
    }
    for (String key : propsMap.keySet()) {
      if (key.startsWith(name + "[") && key.endsWith("]")) {
        fieldList.add(propsMap.get(key));
      }
    }
    return fieldList;
  }

  private Object injectMap(Environment environment, String name) {
    final Map<String, Object> fieldMap = new HashMap<>();
    Map<String, String> propsMap = environment.toStringMap();
    if (propsMap.isEmpty()) {
      return fieldMap;
    }
    propsMap.keySet().forEach(key -> {
      if (key.startsWith(name + ".key")) {
        String replaceKey = key
                .replace(name + ".", "");
        fieldMap.put(replaceKey, propsMap.get(key));
      }
    });
    return fieldMap;
  }
}
