package com.chainstaysoftware.filechooser;

import java.io.File;

/**
 * Called when user Adds or Removes a favorite directory location. The calling code is responsible
 * for persisting the favorites. FileChooserFx does NOT handle persistence of favorites.
 */
@FunctionalInterface
public interface FavoritesCallback {
   void invoke(File directory);
}
