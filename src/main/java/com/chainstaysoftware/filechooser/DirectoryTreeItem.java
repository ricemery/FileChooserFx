package com.chainstaysoftware.filechooser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

class DirectoryTreeItem extends TreeItem<File> {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.DirectoryTreeItem");

   private final FilesViewCallback callback;
   private final PopulateTreeItemRunnableFactory factory;

   // Cache isLeaf, lastModified and length so that sorting by either field is performant on slow filesystems.
   private Boolean isLeaf;
   private Long lastModified;
   private Long length;

   private boolean directoryListLoaded = false;

   DirectoryTreeItem(final File value,
                     final FilesViewCallback callback,
                     final PopulateTreeItemRunnableFactory factory) {
      super(value, null);

      this.callback = callback;
      this.factory = factory;
   }

   @Override
   public boolean isLeaf() {
      if (isLeaf == null) {
         isLeaf = !getValue().isDirectory();
      }

      return isLeaf;
   }

   @Override
   public ObservableList<TreeItem<File>> getChildren() {
      if (isLeaf()) {
         return FXCollections.observableArrayList();
      }

      if (!directoryListLoaded) {
         loadChildren();
      }

      return super.getChildren();
   }

   private void loadChildren() {
      directoryListLoaded = true;

      try {
         final DirectoryStream<Path> filteredStream = callback.getDirectoryStream(getValue());
         final DirectoryStream<Path> unfilteredStream = callback.unfilteredDirectoryStream(getValue());
         final Runnable runnable = factory.create(filteredStream, unfilteredStream, this);
         runnable.run();

      } catch (IOException e) {
         logger.log(Level.WARNING, "Error loading directory children.", e);
      }
   }

   long length() {
      if (length == null) {
         length = getValue().length();
      }

      return length;
   }

   long lastModified() {
      if (lastModified == null) {
         lastModified = getValue().lastModified();
      }

      return lastModified;
   }
}
