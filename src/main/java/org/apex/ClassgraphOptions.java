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

import java.util.Set;

public final class ClassgraphOptions implements Options {
  private final Set<String> scanPackages;
  private final Set<String> skipPackages;
  private final boolean verbose;
  private final boolean enableRealtimeLogging;

  public ClassgraphOptions(Set<String> scanPackages, Set<String> skipPackages, boolean verbose, boolean enableRealtimeLogging) {
    this.scanPackages = scanPackages;
    this.skipPackages = skipPackages;
    this.verbose = verbose;
    this.enableRealtimeLogging = enableRealtimeLogging;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean isVerbose() {
    return verbose;
  }

  @Override
  public boolean isEnableRealtimeLogging() {
    return enableRealtimeLogging;
  }

  @Override
  public Set<String> getScanPackages() {
    return scanPackages;
  }

  @Override
  public Set<String> getSkipPackages() {
    return skipPackages;
  }

  public static class Builder {
    private Set<String> scanPackages;
    private Set<String> skipPackages;
    private boolean verbose;
    private boolean enableRealtimeLogging;

    public Builder scanPackages(Set<String> scanPackages) {
      this.scanPackages = scanPackages;
      return this;
    }

    public Builder skipPackages(Set<String> skipPackages) {
      this.skipPackages = skipPackages;
      return this;
    }

    public Builder verbose(boolean verbose) {
      this.verbose = verbose;
      return this;
    }

    public Builder realtimeLogging(boolean enableRealtimeLogging) {
      this.enableRealtimeLogging = enableRealtimeLogging;
      return this;
    }

    public ClassgraphOptions build() {
      return new ClassgraphOptions(this.scanPackages, this.skipPackages, this.verbose, this.enableRealtimeLogging);
    }
  }
}
