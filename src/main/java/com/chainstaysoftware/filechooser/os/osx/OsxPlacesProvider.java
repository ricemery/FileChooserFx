package com.chainstaysoftware.filechooser.os.osx;

import com.chainstaysoftware.filechooser.os.Place;
import com.chainstaysoftware.filechooser.os.PlaceType;
import com.chainstaysoftware.filechooser.os.PlacesProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link PlacesProvider} for OS X. NOTE - This may not be the best way to
 * get this info from OS X. Java does not make it easy..
 */
public class OsxPlacesProvider implements PlacesProvider {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.os.osx.OsxPlacesProvider");

   private static final List<String> networkFsTypes = Arrays.asList("afpfs", "cifs", "ncpfs", "nfs", "smb", "smbfs");
   private static final List<String> cdFsTypes = Arrays.asList("cd9660");

   /**
    * Return the default list of {@link Place} for the supporting OS instance.
    */
   @Override
   public List<Place> getDefaultPlaces() {
      final List<Place> places = new LinkedList<>();

      final File volumes = new File("/Volumes");
      final String[] children = volumes.list();

      Arrays.stream(children)
            .forEach(s -> {
               try {
                  File path = new File(volumes, s);
                  if (Files.isSymbolicLink(path.toPath())) {
                     path = Files.readSymbolicLink(path.toPath()).toFile();
                  }

                  places.add(new Place(toPlaceType(path), path));
               } catch (IOException e) {
                  logger.log(Level.WARNING, "Error finding OS X mounts", e);
               }
            });

      return places;
   }

   private PlaceType toPlaceType(final File path)
         throws IOException {
      final String fsType = Files.getFileStore(path.toPath()).type();
      if (cdFsTypes.contains(fsType)) {
         return PlaceType.Cd;
      }

      if (networkFsTypes.contains(fsType)) {
         return PlaceType.Network;
      }

      // Code is assuming that "msdos" is a Usb drive.. This probably
      // isn't 100% correct. Haven't been able to find a better solution.
      if ("msdos".equals(fsType)) {
         return PlaceType.Usb;
      }

      return PlaceType.HardDisk;
   }
}
