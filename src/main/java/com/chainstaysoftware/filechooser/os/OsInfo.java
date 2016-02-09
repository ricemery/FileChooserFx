package com.chainstaysoftware.filechooser.os;

import java.util.Locale;

/**
 * Util to determine the OS that the JVM is running within.
 */
public final class OsInfo {
   private static final String osName = System.getProperty("os.name").toLowerCase(Locale.getDefault());

   private OsInfo() {}

   public static boolean isWindows() {
      return osName.contains("windows");
   }

   public static boolean isLinux() {
      return osName.contains("linux");
   }

   public static boolean isMac() {
      return osName.contains("mac os");
   }
}
