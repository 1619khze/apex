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

import java.util.Map;

/**
 * @author WangYi
 * @since 2020/9/24
 */
public class InjectContext {
  private final Object object;
  private final KlassInfo klassInfo;
  private final Map<String, Object> instanceMap;

  public InjectContext(KlassInfo klassInfo, Map<String, Object> instanceMap) {
    this.klassInfo = klassInfo;
    this.object = klassInfo.target();
    this.instanceMap = instanceMap;
  }

  public static InjectContext create(KlassInfo klassInfo, Map<String, Object> instanceMap) {
    return new InjectContext(klassInfo, instanceMap);
  }

  public Object getObject() {
    return object;
  }

  public KlassInfo getKlassInfo() {
    return klassInfo;
  }

  public Map<String, Object> getInstanceMap() {
    return instanceMap;
  }
}
