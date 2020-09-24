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

import org.apache.commons.lang3.ObjectUtils;
import org.apex.Apex;
import org.apex.Environment;
import org.apex.InjectContext;
import org.apex.Injector;
import org.apex.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * @author WangYi
 * @since 2020/7/29
 */
public class FieldValueInjector implements Injector {
  private static final Logger log = LoggerFactory.getLogger(FieldValueInjector.class);
  private final Environment environment = Apex.of().environment();

  @Override
  public void inject(InjectContext injectContext) throws IllegalAccessException {
    Field[] fields = injectContext.getKlassInfo().clazz().getDeclaredFields();
    if (ObjectUtils.isEmpty(fields)) {
      return;
    }
    for (Field field : fields) {
      if (!field.isAnnotationPresent(Value.class)) {
        continue;
      }
      Value value = field.getAnnotation(Value.class);
      String elValue = value.value();
      if (elValue.length() > 0 && elValue.startsWith("${") && elValue.endsWith("}")) {
        final String key = value.value().replace("${", "")
                .replace("}", "");
        field.setAccessible(true);
        try {
          field.set(injectContext.getObject(), environment.getString(key, null));
        } catch (IllegalAccessException e) {
          log.error("An exception occurred while injecting value field");
          throw e;
        }
      }
    }
  }
}
