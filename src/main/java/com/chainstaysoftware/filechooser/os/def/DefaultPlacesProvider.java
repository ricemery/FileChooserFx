package com.chainstaysoftware.filechooser.os.def;

import com.chainstaysoftware.filechooser.os.Place;
import com.chainstaysoftware.filechooser.os.PlaceType;
import com.chainstaysoftware.filechooser.os.PlacesProvider;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultPlacesProvider implements PlacesProvider {
   /**
    * Return the default list of {@link Place} for the supporting OS instance.
    * Only returns Roots and assumes all are {@link PlaceType#HardDisk}
    */
   @Override
   public List<Place> getDefaultPlaces() {
      final List<File> roots = Arrays.asList(File.listRoots());
      return roots.stream()
         .map(r -> new Place(PlaceType.HardDisk, r))
         .collect(Collectors.toList());
   }
}
