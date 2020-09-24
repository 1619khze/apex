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

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WangYi
 * @since 2020/9/24
 */
public class Discoverer {
  public static Map<Object, Class<?>> discover(Apex apex) {
    Map<Object, Class<?>> result = new ConcurrentHashMap<>(64);

    final Set<Class<? extends Annotation>> annotations = apex.typeAnnotations();
    ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(
            apex.packages().toArray(new String[0])).scan();
    if (scanResult.getAllClasses().isEmpty()) {
      return result;
    }
    ClassInfoList allClasses = scanResult.getAllClasses();
    allClasses.filter(classInfo -> ReflectionHelper.isNormal(classInfo.loadClass()));

    for (ClassInfo allClass : allClasses) {
      if (allClass.implementsInterface(TypeFilter.class.getName())) {
        Object typeFilter = ReflectionHelper.newInstance(allClass.loadClass());
        apex.typeFilters().add((TypeFilter) typeFilter);
      }
    }
    for (Class<? extends Annotation> annotation : annotations) {
      final ClassInfoList classesWithAnnotation =
              scanResult.getClassesWithAnnotation(annotation.getName());
      for (ClassInfo classInfo : classesWithAnnotation) {
        Class<?> aClass = classInfo.loadClass();
        if (apex.typeFilters().isEmpty()) {
          result.put(ReflectionHelper.newInstance(aClass), aClass);
        } else {
          for (TypeFilter typeFilter : apex.typeFilters()) {
            if (!typeFilter.filter(classInfo.loadClass())) {
              continue;
            }
            result.put(ReflectionHelper.newInstance(aClass), aClass);
          }
        }
      }
    }
    scanResult.close();
    return result;
  }
}
