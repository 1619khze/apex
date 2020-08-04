package org.apex.annotation;

import java.lang.annotation.*;

/**
 * @author WangYi
 * @since 2020/8/4
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigurationProperty {
  String value();
}
