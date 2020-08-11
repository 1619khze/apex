package org.apex.injector.type;

import org.apex.Environment;

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
  public Object inject(Environment environment, String name) {
    final Map<String, String> propsMap = environment.toStringMap();
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
