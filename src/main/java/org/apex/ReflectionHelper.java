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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * @author WangYi
 * @since 2020/7/24
 */
public class ReflectionHelper {
  private ReflectionHelper() {}

  /**
   * Create a object with default constructor
   *
   * @param targetClass Target class
   * @return a target class object
   */
  public static <T> T newInstance(Class<T> targetClass) {
    try {
      final Constructor<T> constructor = targetClass.getConstructor();
      return constructor.newInstance();
    } catch (InstantiationException e) {
      throw new BeanInstantiationException(targetClass + "is  an abstract class", e);
    } catch (IllegalAccessException e) {
      throw new BeanInstantiationException("Illegal Access '" + targetClass + "' default constructor", e);
    } catch (InvocationTargetException e) {
      throw new BeanInstantiationException("Exception occurred when invoking '"
              + targetClass + "''s default constructor", e);
    } catch (NoSuchMethodException e) {
      throw new BeanInstantiationException(targetClass + " didn't have a default constructor", e);
    }
  }

  public static <T> boolean isInterface(Class<T> clazz) {
    return clazz.isInterface();
  }

  public static <T> boolean isAbstract(Class<T> clazz) {
    int modifiers = clazz.getModifiers();
    return Modifier.isAbstract(modifiers);
  }

  public static <T> boolean isEnum(Class<T> clazz) {
    return clazz.isEnum();
  }

  public static <T> boolean isNormal(Class<T> clazz) {
    return !isAbstract(clazz) && !isEnum(clazz);
  }
}
