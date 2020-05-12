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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public final class BeanDefinition implements Serializable {
  private String name;
  private String simpleName;
  private Long classSize;
  private Class<?>[] implInterfaces;
  private Class<?>[] extendsClass;
  private Field[] fields;
  private Method[] methods;
  private Method[] initMethod;

  public BeanDefinition() {
  }

  public BeanDefinition(
          String name, String simpleName,
          Long classSize, Class<?>[] implInterfaces,
          Class<?>[] extendsClass,
          Field[] fields, Method[] methods,
          Method[] initMethod) {
    this.name = name;
    this.simpleName = simpleName;
    this.classSize = classSize;
    this.implInterfaces = implInterfaces;
    this.extendsClass = extendsClass;
    this.fields = fields;
    this.methods = methods;
    this.initMethod = initMethod;
  }

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

  public Long getClassSize() {
    return classSize;
  }

  public void setClassSize(Long classSize) {
    this.classSize = classSize;
  }

  public Class<?>[] getImplInterfaces() {
    return implInterfaces;
  }

  public void setImplInterfaces(Class<?>[] implInterfaces) {
    this.implInterfaces = implInterfaces;
  }

  public Class<?>[] getExtendsClass() {
    return extendsClass;
  }

  public void setExtendsClass(Class<?>[] extendsClass) {
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

  public Method[] getInitMethod() {
    return initMethod;
  }

  public void setInitMethod(Method[] initMethod) {
    this.initMethod = initMethod;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BeanDefinition that = (BeanDefinition) o;
    return classSize.equals(that.classSize) &&
            Objects.equals(name, that.name) &&
            Objects.equals(simpleName, that.simpleName) &&
            Arrays.equals(implInterfaces, that.implInterfaces) &&
            Arrays.equals(extendsClass, that.extendsClass) &&
            Arrays.equals(fields, that.fields) &&
            Arrays.equals(methods, that.methods) &&
            Arrays.equals(initMethod, that.initMethod);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(name, simpleName, classSize);
    result = 31 * result + Arrays.hashCode(implInterfaces);
    result = 31 * result + Arrays.hashCode(extendsClass);
    result = 31 * result + Arrays.hashCode(fields);
    result = 31 * result + Arrays.hashCode(methods);
    result = 31 * result + Arrays.hashCode(initMethod);
    return result;
  }
}
