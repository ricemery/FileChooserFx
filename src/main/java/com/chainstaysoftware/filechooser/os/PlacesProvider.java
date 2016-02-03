package com.chainstaysoftware.filechooser.os;

import java.util.List;

public interface PlacesProvider {
   /**
    * Return the default list of {@link Place} for the supporting OS instance.
    */
   List<Place> getDefaultPlaces();
}
