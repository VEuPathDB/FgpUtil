package org.gusdb.fgputil.runtime;

import java.util.Optional;

public class JvmUtil {
  public static final boolean IS_64BIT;

  /**
   * Estimate header size for an object instance not considering certain
   * optimizations the JVM may make based on context.
   *
   * Generally an object header will be at minimum 12 bytes on 64 bit JVMs or
   * 8 bytes for 32 bit.
   */
  public static final byte OBJECT_HEADER_SIZE;

  /**
   * Padding factor for objects.
   * <p>
   * The JVM may pad the memory used by an object by this factor to.  This
   * should be 8 for 64 bit JVMs and 4 for 32 bit.
   */
  public static final byte OBJECT_SIZE_PADDING_FACTOR;

  public static final byte REFERENCE_SIZE;

  static {
    // Ref https://github.com/OpenHFT/Chronicle-Core/blob/4fc0a5aa9c014db494e66c819745a069ba720e31/src/main/java/net/openhft/chronicle/core/Jvm.java#L156
    IS_64BIT = systemProp("com.ibm.vm.bitmode")
      .or(() -> systemProp("sun.arch.data.model"))
      .map("64"::equals)
      .or(() -> systemProp("java.vm.version")
        .map(s -> s.contains("_64")))
      .orElse(false);

    OBJECT_HEADER_SIZE = (byte) (IS_64BIT ? 12 : 8);
    OBJECT_SIZE_PADDING_FACTOR = (byte) (IS_64BIT ? 8 : 4);
    REFERENCE_SIZE = (byte) (IS_64BIT ? Long.BYTES : Integer.BYTES);
  }

  public static Optional<String> systemProp(String key) {
    return Optional.ofNullable(System.getProperty(key));
  }
}
