package com.chainstaysoftware.filechooser.os.linux;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adapted from - https://gist.github.com/ikonst/3394662
 */
public class LinuxFileSystem {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.os.linux.LinuxFileSystem");

   /**
    * Retrieves the {@link MountInfo} for each entry in /proc/mounts.
    * An empty list is returned if /proc/mounts cannot be loaded.
    */
   public List<MountInfo> getMounts() {
      try (final InputStreamReader reader = new InputStreamReader(new FileInputStream("/proc/mounts"),
            Charset.defaultCharset())) {

         final List<MountInfo> mounts = new ArrayList<>();

         try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            do {
               final String line = bufferedReader.readLine();
               if (line == null) {
                  break;
               }

               getMountInfo(line).ifPresent(mounts::add);
            } while (true);

            return mounts;
         }
      } catch (IOException e) {
         logger.warning("Unable to open /proc/mounts to get mountpoint info");
         return Collections.emptyList();
      }
   }

   private Optional<MountInfo> getMountInfo(final String line) {
      String[] parts = line.split(" ");
      if (parts.length < 6) {
         return Optional.empty();
      }

      try {
         final String device = parts[0];
         final String mountpoint = parts[1].replace("\\040", " ");
         final String fs = parts[2];
         final String options = parts[3];
         final int fs_freq = Integer.parseInt(parts[4]);
         final int fs_passno = Integer.parseInt(parts[5]);

         return Optional.of(new MountInfo(device, mountpoint, fs, options, fs_freq, fs_passno));
      } catch (NumberFormatException e) {
         logger.log(Level.WARNING, "Error parsing fs_freq or fs_passno", e);
         return Optional.empty();
      }
   }
}
