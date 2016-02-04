package com.chainstaysoftware.filechooser.os.linux;

import com.chainstaysoftware.filechooser.os.Place;
import com.chainstaysoftware.filechooser.os.PlaceType;
import com.chainstaysoftware.filechooser.os.PlacesProvider;
import com.chainstaysoftware.filechooser.os.def.DefaultPlacesProvider;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * {@link PlacesProvider} for Linux. NOTE - This may not be the best way to
 * get this info from Linux. Java does not make it easy..
 */
public class LinuxPlacesProvider implements PlacesProvider {
   // List of Linux file systems that we want to show. Any others will be filtered out.
   private static final List<String> supportedFsTypes = Arrays.asList("btrfs", "cifs", "exfat", "ext", "ext2", "ext3", "ext4",
      "f2fs", "hfs", "hpfs", "iso9660", "jfs", "minix", "msdos", "ncpfs", "nfs", "ntfs", "prl_fs", "reiser4", "reiserFS",
      "smb", "smbfs", "umsdos", "vfat", "xfs", "zfs");
   private static final List<String> networkFsTypes = Arrays.asList("cifs", "ncpfs", "nfs", "smb", "smbfs");
   private static final List<String> cdFsTypes = Arrays.asList("iso9660");

   private final LinuxFileSystem linuxFileSystem = new LinuxFileSystem();
   /**
    * Return the default list of {@link Place} for the supporting OS instance.
    * Only returns Roots and assumes all are {@link PlaceType#HardDisk}
    */
   @Override
   public List<Place> getDefaultPlaces() {
      // File.listRoots() only returns "/" on Linux. So, if possible, get the
      // mounted drives from /proc/mounts
      final List<MountInfo> mountInfos = linuxFileSystem.getMounts();
      if (mountInfos.isEmpty()) {
         return new DefaultPlacesProvider().getDefaultPlaces();
      }

      return mountInfos.stream()
         .filter(new ShouldInclude())
         .map(mountInfo -> new Place(toPlaceType(mountInfo), new File(mountInfo.getMountpoint())))
         .collect(Collectors.toList());
   }

   /**
    * Attempt to map a {@link MountInfo} to a {@link PlaceType}
    */
   private PlaceType toPlaceType(final MountInfo mountInfo) {
      if (networkFsTypes.contains(mountInfo.getFs())) {
         return PlaceType.Network;
      }

      if (cdFsTypes.contains(mountInfo.getFs())) {
         return PlaceType.Cd;
      }

      // Is there a better way to determine if a drive is connected over USB?
      // The Swing FileSystemView seems to be unreliable.
      if (mountInfo.getDevice().startsWith("/dev") && mountInfo.getMountpoint().startsWith("/media")) {
         // Assuming if in media dir, and not a CD or DVD then it must be Usb.
         return PlaceType.Usb;
      }

      return PlaceType.HardDisk;
   }

   /**
    * Predicate to determine if a {@link MountInfo} should be included within the Places view.
    * This code tries to filter out the "special" mounts such as "proc", "rootfs", etc.
    */
   private class ShouldInclude implements Predicate<MountInfo> {
      @Override
      public boolean test(MountInfo mountInfo) {
         return supportedFsTypes.contains(mountInfo.getFs().toLowerCase())
            && !"/boot".equals(mountInfo.getMountpoint())
            && !"/home".equals(mountInfo.getMountpoint());
      }
   }
}
