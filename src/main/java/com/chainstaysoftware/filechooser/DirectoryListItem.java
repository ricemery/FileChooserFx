package com.chainstaysoftware.filechooser;

import javafx.scene.image.Image;

import java.io.File;

/**
 * Wraps a File and associated Image instance to be rendered in a
 * Grid, List, or TableView.
 */
class DirectoryListItem {
   private final File file;
   private final Image icon;

   public DirectoryListItem(final File file, final Image icon) {
      this.file = file;
      this.icon = icon;
   }

   public File getFile() {
      return file;
   }

   public Image getIcon() {
      return icon;
   }
}
