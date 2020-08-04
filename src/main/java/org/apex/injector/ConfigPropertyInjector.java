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
import java.util.Map;

/**
 * @author WangYi
 * @since 2020/8/4
 */
public class ConfigPropertyInjector implements Injector {
  @Override
  public void inject(Map<String, Object> instanceMapping) throws Exception {
    final Environment environment = Apex.of().environment();
    for (Map.Entry<String, Object> entry : instanceMapping.entrySet()) {
      final Object obj = entry.getValue();
      final Class<?> ref = obj.getClass();

      if (!ref.isAnnotationPresent(ConfigurationProperty.class)) {
        continue;
      }
      ConfigurationProperty annotation =
              ref.getAnnotation(ConfigurationProperty.class);

      final String prefix = annotation.value();
      Field[] declaredFields = ref.getDeclaredFields();
      if (declaredFields.length == 0) {
        return;
      }
      for (Field field : declaredFields) {
        String name = prefix + "." + field.getName();
        Object fieldProperty = environment.getObject(name);
        field.setAccessible(true);
        field.set(obj, fieldProperty);
      }
    }
  }
}
