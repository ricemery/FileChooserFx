package com.chainstaysoftware.filechooser.os;

import com.chainstaysoftware.filechooser.os.def.DefaultPlacesProvider;
import com.chainstaysoftware.filechooser.os.linux.LinuxPlacesProvider;
import com.chainstaysoftware.filechooser.os.osx.OsxPlacesProvider;

import java.util.List;

/**
 * Get the default list of {@link Place} to show in the Places list.
 */
public class Places {
   private final PlacesProvider defaultPlacesProvider = new DefaultPlacesProvider();
   private final PlacesProvider linuxPlacesProvider = new LinuxPlacesProvider();
   private final PlacesProvider osxPlacesProvider = new OsxPlacesProvider();

   /**
    * Get the default list of {@link Place} to show in the Places list.
    */
   public List<Place> getDefaultPlaces(final boolean showMountPoints) {
      if (!showMountPoints) {
         return defaultPlacesProvider.getDefaultPlaces();
      }

      if (OsInfo.isLinux()) {
         return linuxPlacesProvider.getDefaultPlaces();
      }

      if (OsInfo.isMac()) {
         return osxPlacesProvider.getDefaultPlaces();
      }

      return defaultPlacesProvider.getDefaultPlaces();
   }
}
