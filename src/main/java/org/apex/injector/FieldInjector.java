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

import org.apex.InjectContext;
import org.apex.Injector;
import org.apex.annotation.Inject;
import org.apex.annotation.Named;
import org.apex.annotation.Qualifier;
import org.apex.QualifierNotUniqueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/7/9
 */
public class FieldInjector implements Injector {
  private static final Logger log = LoggerFactory.getLogger(FieldInjector.class);

  @Override
  public void inject(InjectContext injectContext) throws Exception {
    Field[] fields = injectContext.klassInfo().clazz().getDeclaredFields();
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
        if (!field.getType().isInterface()) {
          field.set(injectContext.object(), injectContext.instances().get(id));
        } else {
          Object injectObj = injectContext.instances().get(id);
          if (Objects.isNull(injectObj)) {
            final List<Object> refs = new ArrayList<>();
            for (Map.Entry<String, Object> entry : injectContext.instances().entrySet()) {
              Object value = entry.getValue();
              if (field.getType().isAssignableFrom(value.getClass())) {
                refs.add(value);
              }
            }
            if (refs.size() > 1) {
              if (!field.isAnnotationPresent(Qualifier.class)) {
                throw new QualifierNotUniqueException("Qualifier are not unique " + field.getName());
              } else {
                final Qualifier qualifier = field.getAnnotation(Qualifier.class);
                injectObj = injectContext.instances().get(qualifier.value());
              }
            } else {
              injectObj = refs.get(0);
            }
          }
          field.set(injectContext.object(), injectObj);
        }
      } catch (IllegalAccessException e) {
        log.error("An exception occurred while injecting field");
        throw e;
      }
    }
  }
}
