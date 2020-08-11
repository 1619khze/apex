package org.apex.injector.type;

import org.apex.Environment;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author WangYi
 * @since 2020/8/11
 */
public class MapTypeInjector implements TypeInjector {
  @Override
  public Type getType() {
    return Map.class;
  }

  @Override
  public Object inject(Environment environment, String name) {
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
