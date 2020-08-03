package org.apex;

/**
 * @author WangYi
 * @since 2020/8/4
 */
public class BeanDefinitionFactory {
  public static BeanDefinition createBeanDefinition(Object instants, Class<?> clazz) {
    final BeanDefinition beanDefinition = new BeanDefinition();
    beanDefinition.setName(clazz.getName());
    beanDefinition.setSimpleName(clazz.getSimpleName());
    beanDefinition.setInstants(instants);
    beanDefinition.setFields(clazz.getDeclaredFields());
    beanDefinition.setMethods(clazz.getDeclaredMethods());
    beanDefinition.setExtendsClass(clazz.getSuperclass());
    beanDefinition.setImplInterfaces(clazz.getInterfaces());
    return beanDefinition;
  }
}
