package com.chainstaysoftware.filechooser;


import javafx.scene.image.Image;

import java.io.File;
import java.util.Optional;

/**
 * Wrapper for items being placed in the Places tree.
 */
class PlacesTreeItem {
   private final Optional<String> text;
   private final Optional<File> file;
   private final Image icon;

   public PlacesTreeItem(final Optional<String> text,
                         final Optional<File> file,
                         final Image icon) {
      this.text = text;
      this.file = file;
      this.icon = icon;
   }

   public Optional<String> getText() {
      return text;
   }

   public Optional<File> getFile() {
      return file;
   }

   public Image getIcon() {
      return icon;
   }
}
