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
package org.apex.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author WangYi
 * @since 2020/7/23
 */
public class FileBaseResource implements Resource {
  private final File file;
  private final Path filePath;
  private final String path;

  public FileBaseResource(File file) {
    this.file = file;
    this.filePath = this.file.toPath();
    this.path = this.file.getPath();
  }

  public FileBaseResource(String path) {
    this.path = path;
    this.file = new File(path);
    this.filePath = this.file.toPath();
  }

  public FileBaseResource(Path path) {
    this.filePath = path;
    this.path = filePath.toString();
    this.file = new File(this.path);
  }

  @Override
  public boolean isExist() {
    return Objects.nonNull(this.file) ? this.file.exists() : Files.exists(filePath);
  }

  @Override
  public boolean isFile() {
    return Objects.nonNull(this.file) ? this.file.isFile() : Files.isRegularFile(filePath);
  }

  @Override
  public boolean isDirectory() {
    return Objects.nonNull(this.file) ? this.file.isDirectory() : Files.isDirectory(filePath);
  }

  @Override
  public File getFile() {
    if (Objects.nonNull(this.file)) {
      return this.file;
    }
    return Objects.nonNull(filePath) ? filePath.toFile() : new File(path);
  }

  @Override
  public InputStream getInputStream() throws FileNotFoundException {
    return new FileInputStream(getFile());
  }

  @Override
  public String getFileName() {
    return getFile().getName();
  }

  @Override
  public long contentLength() {
    return getFile().length();
  }

  @Override
  public long lastModified() {
    return getFile().lastModified();
  }

  @Override
  public URL getLocation() throws IOException {
    return getFile().toURI().toURL();
  }

  @Override
  public URI getURI() {
    return getFile().toURI();
  }

  @Override
  public Path getPath() {
    return getFile().toPath();
  }

  @Override
  public String getPathString() {
    return getFile().getPath();
  }
}
