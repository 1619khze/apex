package org.apex;

import org.apache.commons.lang3.Validate;
import org.apex.annotation.PropertyBean;
import org.apex.annotation.Scheduled;
import org.apex.annotation.Singleton;
import org.apex.creator.PropertyBeanCreator;
import org.apex.creator.ScheduledBeanCreator;
import org.apex.creator.SingletonBeanCreator;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author WangYi
 * @since 2020/9/23
 */
public class BeanCreatorResolver {
  private final Map<Class<? extends Annotation>, BeanCreator> klassInfoHandlerMap = new HashMap<>();

  public BeanCreatorResolver() {
    this.init();
  }

  public void init() {
    this.register(Scheduled.class, ScheduledBeanCreator.class);
    this.register(Singleton.class, SingletonBeanCreator.class);
    this.register(PropertyBean.class, PropertyBeanCreator.class);
  }

  public void register(Class<? extends Annotation> annotation, Class<? extends BeanCreator> handler) {
    Validate.notNull(annotation, "Bind annotation can't be null");
    Validate.notNull(handler, "klassInfoHandler can't be null");
    Validate.isAssignableFrom(BeanCreator.class, handler);

    BeanCreator beanCreator = ReflectionHelper.newInstance(handler);
    this.klassInfoHandlerMap.put(annotation, beanCreator);
  }

  public BeanCreator lookup(Class<?> cls) {
    return klassInfoHandlerMap.get(cls);
  }
}
