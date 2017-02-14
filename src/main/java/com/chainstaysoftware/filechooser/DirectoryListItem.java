package com.chainstaysoftware.filechooser;

import java.io.File;

/**
 * Wraps a File instance to be rendered in a
 * Grid, List, or TableView.
 */
class DirectoryListItem {
   private final File file;

   // Cache isDir to minimize calls to OS.
   private Boolean isDirectory;

   public DirectoryListItem(final File file) {
      this.file = file;
   }

   public File getFile() {
      return file;
   }

   public boolean isDirectory() {
      if (isDirectory == null) {
         isDirectory = file.isDirectory();
      }

      return isDirectory;
   }
}
