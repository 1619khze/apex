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
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public abstract class AbstractApexFactory implements ApexFactory {
  private static final Logger log = Logger.getLogger(AbstractApexFactory.class.getName());

  private final Map<String, BeanDefinition> beanPool;
  private final ReentrantReadWriteLock readWriteLock;

  public AbstractApexFactory() {
    this.beanPool = new ConcurrentHashMap<>(64);
    this.readWriteLock = new ReentrantReadWriteLock();
  }

  private ReentrantReadWriteLock.ReadLock readLock() {
    return this.readWriteLock.readLock();
  }

  private ReentrantReadWriteLock.WriteLock writeLock() {
    return this.readWriteLock.writeLock();
  }

  @Override
  public <T> List<Class<T>> getBeanByType(Class<T> cls) {
    return null;
  }
}
