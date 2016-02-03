package com.chainstaysoftware.filechooser.os;

import com.chainstaysoftware.filechooser.os.def.DefaultPlacesProvider;
import com.chainstaysoftware.filechooser.os.linux.LinuxPlacesProvider;

import java.util.List;

/**
 * Get the default list of {@link Place} to show in the Places list.
 */
public class Places implements PlacesProvider {
   private final PlacesProvider defaultPlacesProvider = new DefaultPlacesProvider();
   private final PlacesProvider linuxPlacesProvider = new LinuxPlacesProvider();

   /**
    * Get the default list of {@link Place} to show in the Places list.
    */
   @Override
   public List<Place> getDefaultPlaces() {
      if (OsInfo.isLinux()) {
         return linuxPlacesProvider.getDefaultPlaces();
      }

      return defaultPlacesProvider.getDefaultPlaces();
   }
}
