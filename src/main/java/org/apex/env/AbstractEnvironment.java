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
package org.apex.env;

import org.apex.Resource;
import org.apex.io.FileBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;

import static org.apex.Const.BLANK;
import static org.apex.Const.PATH_CONFIG_PROPERTIES;
import static org.apex.Const.PATH_CONFIG_YAML;
import static org.apex.Const.PATH_SERVER_BOOT_CONFIG;
import static org.apex.Const.PATH_SERVER_PROFILE;

/**
 * @author WangYi
 * @since 2020/9/22
 */
public abstract class AbstractEnvironment {
  protected final Properties properties = new Properties();
  private final Logger log = LoggerFactory.getLogger(AbstractEnvironment.class);
  private final String configFilePrefix = "application";
  private final String propsFileSuffix = ".properties";
  private final String ymlFileSuffix = ".yml";

  private String[] args;

  /**
   * Initial configuration loading
   *
   * @param args main method args
   * @throws IOException io exception
   */
  public void init() throws Exception {
    final String loadBootPath = properties
            .getProperty(PATH_SERVER_BOOT_CONFIG,
                    PATH_CONFIG_PROPERTIES);

    this.loadMainArgs(args);
    this.loadSystemProperty();
    this.loadConfigFiles(loadBootPath);
    this.extendAttributes();
  }

  /**
   * Supports nested property functions through ${} in properties
   * and yaml configuration files
   */
  private void extendAttributes() {
    final Map<String, Object> nestedMap = new HashMap<>();
    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
      Object value = entry.getValue();
      String val = value.toString();
      if (!val.contains("${") && !val.contains("}")) {
        continue;
      }
      for (Object key : properties.keySet()) {
        String keyStr = String.valueOf(key);
        String replaceKey = "${" + keyStr
                .split("\\.")[keyStr
                .split("\\.").length - 1] + "}";
        if (!val.contains(replaceKey)) {
          continue;
        }
        val = val.replace(replaceKey, String.valueOf(properties
                .getOrDefault(key, "")));
        nestedMap.put(String.valueOf(entry.getKey()), val);
      }
    }
    this.properties.putAll(nestedMap);
  }

  /**
   * load system properties
   *
   * @throws IllegalAccessException An IllegalAccessException is thrown when an application tries
   *                                to reflectively create an instance (other than an array),
   *                                set or get a field, or invoke a method, but the currently
   *                                executing method does not have access to the definition of
   *                                the specified class, field, method or constructor.
   */
  private void loadSystemProperty() throws IllegalAccessException {
    final Map<String, String> constField = PropertyHelper.confFieldMap();
    for (String key : constField.keySet()) {
      Optional.ofNullable(System.getProperty(constField.get(key)))
              .ifPresent(property -> properties.put(key, property));
    }
    try {
      this.properties.putAll(System.getProperties());
      System.setProperties(this.properties);
    } catch (SecurityException e) {
      log.error("System Properties can't be read or write", e);
    }
  }

  /**
   * oad application.properties
   *
   * @param loadPath load config file path
   * @throws Exception load file exception
   */
  private void loadConfigFiles(String loadPath) throws Exception {
    String suffixName = propsFileSuffix;
    if (!loadPath.startsWith("/")) {
      loadPath = "/" + loadPath;
    }
    if (!loadPath.startsWith("/" + configFilePrefix)) {
      return;
    }

    URL url = Resource.getClassLoader().getResource(loadPath);
    if (Objects.isNull(url)) {
      url = this.getClass().getResource(loadPath
              .replace(propsFileSuffix, ymlFileSuffix));
    }
    loadPath = url.getPath();
    if (loadPath.endsWith(propsFileSuffix)) {
      loadPropsFile(loadPath);
    }

    if (loadPath.endsWith(ymlFileSuffix)) {
      this.loadYmlFile(PATH_CONFIG_YAML);
      suffixName = ymlFileSuffix;
    }
    this.loadProfile(suffixName);
  }

  /**
   * Load the corresponding profile configuration file according to the server.profile configuration
   *
   * @param suffixName config file suffix name
   * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as
   *                            a URI reference.
   * @throws IOException        Signals that an I/O exception of some sort has occurred. This
   *                            class is the general class of exceptions produced by failed or
   *                            interrupted I/O operations.
   */
  private void loadProfile(String suffixName) throws URISyntaxException, IOException {
    final String profile = this.properties.getProperty(PATH_SERVER_PROFILE);
    if (Objects.isNull(profile) || BLANK.equals(profile)) {
      return;
    }
    String profilePath = "/" + configFilePrefix + "-" + profile + suffixName;
    URL profileUrl = Resource.getClassLoader().getResource(profilePath);
    if (Objects.isNull(profileUrl)) {
      return;
    }
    String replace = profilePath.replace(propsFileSuffix, ymlFileSuffix);
    profileUrl = Resource.getClassLoader().getResource(replace);
    if (Objects.isNull(profileUrl)) {
      return;
    }
    if (profilePath.endsWith(propsFileSuffix)) {
      loadPropsFile(profilePath);
    }
    if (profilePath.endsWith(ymlFileSuffix)) {
      this.loadYmlFile(profilePath);
    }
    this.properties.put(PATH_SERVER_PROFILE, suffixName);
  }

  /**
   * load properties file
   *
   * @param propsPath properties file path
   * @throws URISyntaxException Checked exception thrown to indicate that a string could not be parsed as
   *                            a URI reference.
   * @throws IOException        Signals that an I/O exception of some sort has occurred. This
   *                            class is the general class of exceptions produced by failed or
   *                            interrupted I/O operations.
   */
  private void loadPropsFile(String propsPath) throws URISyntaxException, IOException {
    URL url = Resource.getClassLoader().getResource(propsPath);
    if (Objects.isNull(url)) {
      return;
    }
    FileBaseResource resource = new FileBaseResource(Paths.get(url.toURI()));
    this.properties.load(resource.getInputStream());
  }

  /**
   * load yml file
   *
   * @param ymlPath yml file path
   * @throws IOException io exception
   */
  private void loadYmlFile(String ymlPath) throws IOException {
    final TreeMap<String, Map<String, Object>> treeMap =
            PropertyHelper.yaml(ymlPath);
    if (Objects.isNull(treeMap)) {
      return;
    }
    final StringReader reader = new StringReader(
            PropertyHelper.toProperties(treeMap));
    this.properties.load(reader);
  }

  /**
   * Load main function parameters, and override if main configuration exists
   *
   * @param args String parameter array of main method
   */
  protected void loadMainArgs(String[] args) {
    Map<String, String> argsMap = PropertyHelper.parseArgs(args);
    if (argsMap.size() > 0) {
      log.info("Entered command line:{}", argsMap);
    }
    for (Map.Entry<String, String> next : argsMap.entrySet()) {
      this.properties.put(next.getKey(), next.getValue());
    }
  }

  public void setArgs(String[] args) {
    this.args = args;
  }
}
