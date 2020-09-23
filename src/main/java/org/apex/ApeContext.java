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

import org.apex.annotation.ConfigBean;
import org.apex.beans.KlassInfo;
import org.apex.creator.ConfigBeanCreator;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author WangYi
 * @since 2020/6/22
 */
public class ApeContext extends AbstractFactory {
  private final Logger log = LoggerFactory.getLogger(ApeContext.class);
  private final Map<Class<? extends Annotation>, Set<Class<?>>> pool = new HashMap<>();
  private final Set<Class<?>> configBeans = new HashSet<>();
  private final BeanCreatorResolver beanCreatorResolver = new BeanCreatorResolver();
  private Reflections reflections;
  private Apex apex;

  @Override
  public void init(Apex apex) throws Exception {
    this.apex = apex;
    this.reflections = initReflection(apex);

    Environment environment = apex.environment();
    environment.mainArgs(apex.mainArgs());
    environment.init();

    log.info("Reflections initialization completed");
    log.info("Environment initialization completed");

    this.collectionBean(apex);
    this.registerKlassInfo();

    if(configBeans.isEmpty()){
      return;
    }
    BeanCreator beanCreator = new ConfigBeanCreator();
    for (Class<?> configBean : configBeans) {
      if(!beanCreator.support(configBean)){
        return;
      }
      KlassInfo handle = beanCreator.create(configBean);
      invokeInject(handle,handle.target());
    }
  }

  private void registerKlassInfo() throws Exception {
    for(Map.Entry<Class<? extends Annotation>, Set<Class<?>>> item : pool.entrySet()){
      BeanCreator beanCreator = beanCreatorResolver.lookup(item.getKey());
      if (Objects.isNull(beanCreator)) {
        return;
      }
      for (Class<?> clazz : item.getValue()) {
        if(!beanCreator.support(clazz)){
          continue;
        }
        final KlassInfo klassInfo = beanCreator.create(clazz);
        registerKlassInfo(klassInfo);
      }
    }
  }

  private void collectionBean(Apex apex) {
    final List<Class<? extends Annotation>> annotations = apex.withTypeAnnotations();
    for (Class<? extends Annotation> annotation : annotations) {
      Set<Class<?>> types = reflections.getTypesAnnotatedWith(annotation);
      Set<Class<?>> clsSet = filter(types);
      if (types.isEmpty() || clsSet.isEmpty()) {
        continue;
      }
      if(annotation.isAssignableFrom(ConfigBean.class)){
        configBeans.addAll(clsSet);
      }else{
        this.pool.put(annotation, clsSet);
      }
    }
  }

  private Reflections initReflection(Apex apex) {
    ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
    configurationBuilder.forPackages(apex.packages().toArray(new String[0]));
    configurationBuilder.addScanners(new SubTypesScanner());
    configurationBuilder.addScanners(new FieldAnnotationsScanner());
    configurationBuilder.addScanners(new MethodAnnotationsScanner());
    configurationBuilder.addScanners(new MethodParameterScanner());
    return new Reflections(configurationBuilder);
  }

  public void registerKlassInfo(KlassInfo klassInfo) throws Exception {
    this.klassInfoMap.put(klassInfo.name(),klassInfo);
    for (Map.Entry<String, KlassInfo> entry : klassInfoMap.entrySet()) {
      this.instanceMap.put(entry.getKey(), entry.getValue().target());
    }

    if(klassInfoMap.isEmpty() && instanceMap.isEmpty()){
      return;
    }
    for (Map.Entry<String, Object> entry : instanceMap.entrySet()) {
      KlassInfo def = this.klassInfoMap.get(entry.getKey());
      final Object obj = entry.getValue();
      if (def == null) {
        def = KlassInfo.create(obj);
      }
      invokeInject(def, obj);
    }
  }

  private void invokeInject(KlassInfo def, Object obj) throws Exception {
    for (final Injector injector : injectors) {
      injector.inject(obj, def);
    }
  }

  private Set<Class<?>> filter(Set<Class<?>> typesAnnotatedWith) {
    Set<Class<?>> clsSet = new HashSet<>();
    List<TypeFilter> typeFilters = apex.typeFilters();
    for (Class<?> cls : typesAnnotatedWith) {
      if (!typeFilters.isEmpty()) {
        for (TypeFilter typeFilter : typeFilters) {
          if (!typeFilter.filter(cls)) {
            continue;
          }
          clsSet.add(cls);
        }
      } else {
        clsSet.add(cls);
      }
    }
    return clsSet;
  }

  public static ApeContext instance() {
    return ApexContextHolder.instance;
  }

  private static class ApexContextHolder {
    private static final ApeContext instance = new ApeContext();
  }
}
