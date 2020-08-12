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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * @author WangYi
 * @since 2019/6/5
 */
public final class Environment {
  private static final Logger log = LoggerFactory.getLogger(Environment.class);

  private final Properties properties = new Properties();

  public Environment() {
    try {
      this.properties.putAll(System.getProperties());
      System.setProperties(this.properties);
    }
    catch (SecurityException e) {
      log.error("System Properties can't be read or write", e);
    }
  }

  public Environment(Properties properties) {
    this();
    this.properties.putAll(toMap(properties));
  }

  public Environment(String location) {
    this.load(location);
  }

  public static Environment of(Properties properties) {
    return new Environment(properties);
  }

  public static Environment of(String location) {
    return new Environment(location);
  }

  public void add(String key, Object value) {
    this.properties.put(key, value);
  }

  public void addAll(Map<String, Object> map) {
    this.properties.putAll(map);
  }

  public void remove(String key, Object value) {
    this.properties.remove(key, value);
  }

  public void removeAll() {
    this.properties.clear();
  }

  public String get(String name) {
    return this.properties.getProperty(name);
  }

  public String get(String name, String defaultValue) {
    return Objects.isNull(this.properties.getProperty(name)) ? defaultValue : String.valueOf(this.properties.get(name));
  }

  public Object getObject(String name, String defaultValue) {
    return Objects.isNull(this.properties.get(name)) ? defaultValue : this.properties.get(name);
  }

  public Object getObject(String name) {
    return this.properties.get(name);
  }

  public boolean getBoolean(String name, boolean defaultValue) {
    final Object o = this.properties.get(name);
    if (o == null) {
      return defaultValue;
    }
    return Boolean.parseBoolean(o.toString());
  }

  public String getString(String name, String defaultValue) {
    final Object o = this.properties.get(name);
    if (o == null) {
      return defaultValue;
    }
    return o.toString();
  }

  public int getInt(String name, int defaultValue) {
    final Object o = this.properties.get(name);
    if (o == null) {
      return defaultValue;
    }
    return Integer.parseInt(o.toString());
  }

  public int size() {
    return this.properties.size();
  }

  public boolean isEmpty() {
    return this.properties.isEmpty();
  }

  public void load(Reader reader) {
    try {
      this.properties.load(reader);
    }
    catch (IOException e) {
      log.error("IOException:", e);
    }
  }

  public void load(InputStream inputStream) {
    try {
      this.properties.load(inputStream);
    }
    catch (IOException e) {
      log.error("IOException:", e);
    }
  }

  public void load(String location) {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(location);
    if (inputStream != null) {
      load(inputStream);
    }
  }

  public Map<String, Object> toMap(Properties properties) {
    Map<String, Object> argsMap = new HashMap<>();
    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
      argsMap.put(String.valueOf(entry.getKey()), entry.getValue());
    }
    return argsMap;
  }

  public Map<String, Object> toMap() {
    return toMap(properties);
  }

  public Map<String, String> toStringMap() {
    Map<String, String> argsMap = new HashMap<>();
    Set<Map.Entry<Object, Object>> propertySet = properties.entrySet();
    for (Map.Entry<Object, Object> o : propertySet) {
      argsMap.put(String.valueOf(o.getKey()), String.valueOf(o.getValue()));
    }
    return argsMap;
  }

  public Properties props() {
    return properties;
  }
}
