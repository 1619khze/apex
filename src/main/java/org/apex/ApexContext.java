/*
 * MIT License
 *
 * Copyright (c) 2020 1619kHz
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
package org.apex;

import org.apache.commons.lang3.ObjectUtils;
import org.apex.annotation.Bean;
import org.apex.annotation.ConfigBean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/6/22
 */
public class ApexContext extends AbstractFactory {
  private final ConfigBeanCreator configBeanCreator = new ConfigBeanCreator();

  public ApexContext() {}

  public static ApexContext instance() {
    return ApexContextHolder.instance;
  }

  @Override
  public void init(Apex apex) throws Exception {
    Environment environment = apex.environment();
    environment.mainArgs(apex.mainArgs());
    environment.init();

    Map<Object, Class<?>> discover = Discoverer.discover(apex);
    for (Map.Entry<Object, Class<?>> entry : discover.entrySet()) {
      Object key = entry.getKey();
      Class<?> value = entry.getValue();
      this.klassInfoMap.put(value.getName(), KlassInfo.create(key));
      this.instanceMap.put(value.getName(), key);
    }
    for (Map.Entry<String, KlassInfo> entry : klassInfoMap.entrySet()) {
      this.instanceMap.put(entry.getKey(), entry.getValue().target());
    }
    inject();
    for (Map.Entry<String, Object> entry : instanceMap.entrySet()) {
      if (entry.getValue().getClass().isAnnotationPresent(ConfigBean.class)) {
        registerConfigBean(entry.getValue(), entry.getValue().getClass());
      }
    }
    inject();
  }

  private void inject() throws Exception {
    for (Map.Entry<String, Object> entry : instanceMap.entrySet()) {
      KlassInfo def = this.klassInfoMap.get(entry.getKey());
      final Object obj = entry.getValue();
      if (def == null) {
        def = KlassInfo.create(obj);
      }
      InjectContext injectContext = InjectContext.create(def, instanceMap);
      for (final Injector next : injectors) {
        next.inject(injectContext);
      }
    }
  }

  private void registerConfigBean(Object key, Class<?> value) throws Exception {
    if (!configBeanCreator.support(value)) {
      return;
    }
    if (value.getMethods().length != 0) {
      Method[] methods = value.getDeclaredMethods();
      InjectContext injectContext = InjectContext.create(KlassInfo.create(key), instanceMap);
      for (Method method : methods) {
        if (method.getReturnType() == void.class) {
          throw new IllegalArgumentException("The return value of the method marked with " +
                  "Bean annotation in the configuration cannot be " +
                  "void:{" + value.getName() + "}" + "#" + method.getName());
        }
        KlassInfo klassInfo = configBeanCreator.create(injectContext, method);
        if (Objects.isNull(klassInfo)) {
          continue;
        }
        klassInfoMap.put(klassInfo.name(), klassInfo);
        instanceMap.put(klassInfo.name(), klassInfo.target());
      }
    }
  }

  private static class ConfigBeanCreator {
    public boolean support(Class<?> cls) {
      return cls.isAnnotationPresent(ConfigBean.class) && ObjectUtils.isNotEmpty(cls.getDeclaredMethods());
    }

    public KlassInfo create(InjectContext injectContext, Method method) throws Exception {
      if (!method.isAnnotationPresent(Bean.class)) {
        return null;
      }
      List<Object> invokeParam = new ArrayList<>();
      Class<?>[] parameterTypes = method.getParameterTypes();
      for (Class<?> parameterType : parameterTypes) {
        Object o = injectContext.instances().get(parameterType.getName());
        invokeParam.add(o);
      }
      Object invoke;
      if (method.getParameterCount() == 0) {
        invoke = method.invoke(injectContext.object());
      } else {
        invoke = method.invoke(injectContext.object(), invokeParam);
      }
      return KlassInfo.create(invoke);
    }
  }

  private static class ApexContextHolder {
    private static final ApexContext instance = new ApexContext();
  }
}
