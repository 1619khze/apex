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

import java.io.Serializable;

/**
 * @author WangYi
 * @since 2020/9/22
 */
public class KlassInfo implements Serializable {
  private final String name;
  private final Class<?> clazz;
  private final Object target;

  private KlassInfo(String name, Class<?> clazz, Object target) {
    this.name = name;
    this.clazz = clazz;
    this.target = target;
  }

  private KlassInfo(String name, Object target) {
    this(name,target.getClass(),target);
  }

  private KlassInfo(Class<?> clazz, Object target) {
    this.name = clazz.getName();
    this.clazz = clazz;
    this.target = target;
  }

  public KlassInfo(Object target) {
    this(target.getClass(), target);
  }

  public KlassInfo(Class<?> clazz) {
    this(clazz, ReflectionHelper.newInstance(clazz));
  }

  public static KlassInfo create(Object target) {
    return new KlassInfo(target);
  }

  public static KlassInfo create(Class<?> clazz) {
    return new KlassInfo(clazz);
  }

  public static KlassInfo create(String name, Class<?> clazz) {
    return new KlassInfo(name, clazz);
  }

  public Class<?> clazz() {
    return clazz;
  }

  public Object target() {
    return target;
  }

  public String name() {
    return name;
  }
}
