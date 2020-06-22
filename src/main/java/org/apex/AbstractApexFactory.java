/*
 * MIT License
 *
 * Copyright (c) 2019 1619kHz
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WangYi
 * @since 2020/6/22
 */
public class AbstractApexFactory implements ApexFactory {
  protected final Map<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>(64);
  protected final Map<String, Object> instanceMapping = new ConcurrentHashMap<>();

  @Override
  public <T> T getBean(Class<T> cls) {
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getBean(String beanName) {
    return ((T) instanceMapping.get(beanName));
  }

  @Override
  public <T> T getBean(Object obj) {
    return null;
  }

  @Override
  public <T> T addBean(Class<T> cls) {
    return null;
  }

  @Override
  public <T> T addBean(String beanName) {
    return null;
  }

  @Override
  public <T> T addBean(Object obj) {
    return null;
  }

  @Override
  public <T> List<Class<T>> getBeanByType(Class<T> cls) {
    return null;
  }

  @Override
  public <T> List<Class<T>> getBeanByType(Object obj) {
    return null;
  }

  @Override
  public <T> void removeAll(Class<T> cls) {

  }

  @Override
  public void removeAll() {

  }

  @Override
  public void removeAll(Object obj) {

  }

  @Override
  public void removeBean(String beanName) {

  }
}
