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
package org.apex.injector;

import org.apex.BeanDefinition;
import org.apex.annotation.Inject;
import org.apex.annotation.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/7/9
 */
public class FieldInjector implements Injector {
  private static final Logger log = LoggerFactory.getLogger(FieldInjector.class);

  @Override
  public void inject(Object obj, BeanDefinition def) throws Exception {
    Field[] fields = def.getFields();
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
        field.set(obj, this.getInstances().get(id));
      } catch (IllegalAccessException e) {
        log.error("An exception occurred while injecting field");
        throw e;
      }
    }
  }
}
