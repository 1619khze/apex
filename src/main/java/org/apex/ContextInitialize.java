package org.apex;

import java.util.Map;

/**
 * @author WangYi
 * @since 2020/8/4
 */
@FunctionalInterface
public interface ContextInitialize {
  void init(Map<String, BeanDefinition> beanDefinitionMap) throws Exception;
}
