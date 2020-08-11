package org.apex.injector.type;

import org.apex.Environment;

import java.lang.reflect.Type;

/**
 * @author WangYi
 * @since 2020/8/11
 */
public interface TypeInjector {
  Type getType();

  Object inject(Environment environment, String name);
}
