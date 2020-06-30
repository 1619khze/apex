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

import org.apex.utils.PropertyUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public interface Const {
  String PATH_SERVER_PROFILE = "server.profile";
  String PATH_SERVER_BOOT_CONFIG = "server.boot.conf";
  String PATH_ENV_WATCHER = "server.env.watcher";
  String PATH_SCANNER_VERBOSE = "server.scanner.verbose";
  String PATH_SCANNER_LOGGING = "server.scanner.logging";

  String ENV_NAME = "default";
  String SERVER_THREAD_NAME = "（'-'*) run ✧";
  String SINGLE_EXECUTOR_NAME = "@Apex-SingleExecutor";

  // app setting
  String PATH_APP_BANNER_TEXT = "app.banner.text";
  String PATH_APP_BANNER_FONT = "app.banner.font";
  String PATH_APP_THREAD_NAME = "app.thread.name";

  // full property file name
  String PATH_CONFIG_PROPERTIES = "application.properties";
  String PATH_CONFIG_YAML = "application.yml";

  // setting prefix
  String PATH_PREFIX_ROOT = "PATH_PREFIX";

  // watch env
  Path SERVER_WATCHER_PATH = Paths.get(Objects.requireNonNull(PropertyUtils.getCurrentClassPath()));
}
