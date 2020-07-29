package org.apex.injector;

import org.apex.Apex;
import org.apex.Environment;
import org.apex.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author WangYi
 * @since 2020/7/29
 */
public class FieldValueInjector implements Injector {
  private static final Logger log = LoggerFactory.getLogger(FieldValueInjector.class);

  @Override
  public void inject(Map<String, Object> instanceMapping) {
    final Environment environment = Apex.of().environment();
    for (Map.Entry<String, Object> entry : instanceMapping.entrySet()) {
      Field[] fields = entry.getValue().getClass().getDeclaredFields();
      if (fields.length == 0) {
        break;
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
            field.set(entry.getValue(), environment.getString(key, null));
          } catch (IllegalAccessException e) {
            log.error("An exception occurred while injecting value field");
          }
        }
      }
    }
  }
}
