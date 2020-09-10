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
package org.apex.annotation;

/**
 * Provides instances of {@code T}. Typically implemented by an injector. For
 * any type {@code T} that can be injected, you can also inject
 * {@code Provider<T>}. Compared to injecting {@code T} directly, injecting
 * {@code Provider<T>} enables:
 *
 * <ul>
 *   <li>retrieving multiple instances.</li>
 *   <li>lazy or optional retrieval of an instance.</li>
 *   <li>breaking circular dependencies.</li>
 *   <li>abstracting scope so you can look up an instance in a smaller scope
 *      from an instance in a containing scope.</li>
 * </ul>
 *
 * <p>For example:
 *
 * <pre>
 *   class Car {
 *     &#064;Inject Car(Provider&lt;Seat> seatProvider) {
 *       Seat driver = seatProvider.get();
 *       Seat passenger = seatProvider.get();
 *       ...
 *     }
 *   }</pre>
 */
public interface Provider<T> {

  /**
   * Provides a fully-constructed and injected instance of {@code T}.
   *
   * @throws RuntimeException if the injector encounters an error while
   *                          providing an instance. For example, if an injectable member on
   *                          {@code T} throws an exception, the injector may wrap the exception
   *                          and throw it to the caller of {@code get()}. Callers should not try
   *                          to handle such exceptions as the behavior may vary across injector
   *                          implementations and even different configurations of the same injector.
   */
  T get();
}
