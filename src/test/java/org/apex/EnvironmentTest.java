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

import org.junit.Test;

import java.util.Map;

/**
 * @author WangYi
 * @since 2020/9/22
 */
public class EnvironmentTest {

  @Test
  public void testApexConfig() {
    String[] arg = new String[]{"testArgs:aa"};
    Environment environment = Environment.create();
    environment.mainArgs(arg);
    try {
      environment.init();
      Map<String, Object> stringObjectMap = environment.toMap();
      for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
        System.out.println(entry.getKey());
        System.out.println(entry.getValue());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
