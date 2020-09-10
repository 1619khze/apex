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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public final class BeanDefinition implements Serializable {
  private String name;
  private String simpleName;
  private Class<?>[] implInterfaces;
  private Class<?> extendsClass;
  private Field[] fields;
  private Method[] methods;
  private List<Method> initMethod;
  private Object instants;
  private Class<?> ref;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSimpleName() {
    return simpleName;
  }

  public void setSimpleName(String simpleName) {
    this.simpleName = simpleName;
  }

  public Class<?>[] getImplInterfaces() {
    return implInterfaces;
  }

  public void setImplInterfaces(Class<?>[] implInterfaces) {
    this.implInterfaces = implInterfaces;
  }

  public Class<?> getExtendsClass() {
    return extendsClass;
  }

  public void setExtendsClass(Class<?> extendsClass) {
    this.extendsClass = extendsClass;
  }

  public Field[] getFields() {
    return fields;
  }

  public void setFields(Field[] fields) {
    this.fields = fields;
  }

  public Method[] getMethods() {
    return methods;
  }

  public void setMethods(Method[] methods) {
    this.methods = methods;
  }

  public List<Method> getInitMethod() {
    return initMethod;
  }

  public void setInitMethod(List<Method> initMethod) {
    this.initMethod = initMethod;
  }

  public Object getInstants() {
    return instants;
  }

  public void setInstants(Object instants) {
    this.instants = instants;
  }

  public Class<?> getRef() {
    return ref;
  }

  public void setRef(Class<?> ref) {
    this.ref = ref;
  }

  @Override
  public String toString() {
    return "BeanDefinition{" +
            "name='" + name + '\'' +
            ", simpleName='" + simpleName + '\'' +
            ", implInterfaces=" + Arrays.toString(implInterfaces) +
            ", extendsClass=" + extendsClass +
            ", fields=" + Arrays.toString(fields) +
            ", methods=" + Arrays.toString(methods) +
            ", initMethod=" + initMethod +
            ", instants=" + instants +
            ", ref=" + ref +
            '}';
  }
}
