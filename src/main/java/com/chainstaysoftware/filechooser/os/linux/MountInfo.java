package com.chainstaysoftware.filechooser.os.linux;

/**
 * Adapted from - https://gist.github.com/ikonst/3394662
 */
public class MountInfo {
   /**
    * The mounted device (can be "none" or any arbitrary string for virtual
    * file systems).
    */
   private final String device;
   /**
    * The path where the file system is mounted.
    */
   private final String mountpoint;
   /**
    * The file system.
    */
   private final String fs;
   /**
    * The mount options.
    */
   private final String options;
   /**
    * The dumping frequency for dump(8); see fstab(5).
    */
   private final int fs_freq;
   /**
    * The order in which file system checks are done at reboot time; see
    * fstab(5).
    */
   private final int fs_passno;

   public MountInfo(final String device,
                    final String mountpoint,
                    final String fs,
                    final String options,
                    final int fs_freq,
                    final int fs_passno) {
      this.device = device;
      this.mountpoint = mountpoint;
      this.fs = fs;
      this.options = options;
      this.fs_freq = fs_freq;
      this.fs_passno = fs_passno;
   }

   public String getDevice() {
      return device;
   }

   public String getMountpoint() {
      return mountpoint;
   }

   public String getFs() {
      return fs;
   }

   public String getOptions() {
      return options;
   }

   public int getFs_freq() {
      return fs_freq;
   }

   public int getFs_passno() {
      return fs_passno;
   }
}
