package com.chainstaysoftware.filechooser.os;

/**
 * Util to determine the OS that the JVM is running within.
 */
public final class OsInfo {
   private static final String osName = System.getProperty("os.name");

   private OsInfo() {}

   public static boolean isWindows() {
      return osName.toLowerCase().contains("windows");
   }

   public static boolean isLinux() {
      return osName.toLowerCase().contains("linux");
   }

   public static boolean isMac() {
      return osName.toLowerCase().contains("mac os");
   }
}
