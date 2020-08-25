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
package org.apex.injector.type;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author WangYi
 * @since 2020/8/11
 */
public class ListTypeInjector implements TypeInjector {
  @Override
  public Type getType() {
    return List.class;
  }

  @Override
  public Object inject(String name) {
    final Map<String, String> propsMap = this.environment().toStringMap();
    final long count = propsMap.keySet().stream()
            .filter(key -> key.startsWith(name + "[") && key.endsWith("]"))
            .count();

    final int idx = ((int) count);
    final List<Object> fieldList = new ArrayList<>(idx);
    if (propsMap.isEmpty()) {
      return fieldList;
    }
    final Map<String, String> sortMap = new TreeMap<>(String::compareTo);
    for (String key : propsMap.keySet()) {
      if (key.startsWith(name + "[") && key.endsWith("]")) {
        sortMap.put(key, propsMap.get(key));
      }
    }
    fieldList.addAll(sortMap.values());
    return fieldList;
  }
}
