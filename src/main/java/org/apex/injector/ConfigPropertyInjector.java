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
