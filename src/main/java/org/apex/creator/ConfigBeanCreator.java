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
package org.apex.creator;

import org.apache.commons.lang3.ObjectUtils;
import org.apex.InjectContext;
import org.apex.KlassInfo;
import org.apex.annotation.Bean;
import org.apex.annotation.ConfigBean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author WangYi
 * @since 2020/9/22
 */
public class ConfigBeanCreator {

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
