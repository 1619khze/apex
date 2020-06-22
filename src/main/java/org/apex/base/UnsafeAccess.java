package org.apex.base;

import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Static access to {@link Unsafe} and convenient utility methods for performing low-level, unsafe
 * operations.
 * <p>
 * <b>Warning:</b> This class is scheduled for removal in version 3.0.0.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@SuppressWarnings("restriction")
public final class UnsafeAccess {
  static final String ANDROID = "THE_ONE";
  static final String OPEN_JDK = "theUnsafe";

  /** The Unsafe instance. */
  public static final Unsafe UNSAFE;

  static {
    try {
      UNSAFE = load(OPEN_JDK, ANDROID);
    } catch (Exception e) {
      throw new Error("Failed to load sun.misc.Unsafe", e);
    }
  }

  /**
   * Returns the location of a given static field.
   *
   * @param clazz the class containing the field
   * @param fieldName the name of the field
   * @return the address offset of the field
   */
  public static long objectFieldOffset(Class<?> clazz, String fieldName) {
    try {
      return UNSAFE.objectFieldOffset(clazz.getDeclaredField(fieldName));
    } catch (NoSuchFieldException | SecurityException e) {
      throw new Error(e);
    }
  }

  static Unsafe load(String openJdk, String android) throws NoSuchMethodException,
          InstantiationException, IllegalAccessException, InvocationTargetException {
    Field field;
    try {
      // try OpenJDK field name
      field = Unsafe.class.getDeclaredField(openJdk);
    } catch (NoSuchFieldException e) {
      try {
        // try Android field name
        field = Unsafe.class.getDeclaredField(android);
      } catch (NoSuchFieldException e2) {
        // try to create a new instance
        Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
        unsafeConstructor.setAccessible(true);
        return unsafeConstructor.newInstance();
      }
    }
    field.setAccessible(true);
    return (Unsafe) field.get(null);
  }

  private UnsafeAccess() {}
}
