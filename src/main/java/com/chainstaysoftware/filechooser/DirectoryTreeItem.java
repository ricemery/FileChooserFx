package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.icons.IconsImpl;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

class DirectoryTreeItem extends TreeItem<File> {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.DirectoryTreeItem");

   private final FilesViewCallback callback;
   private final Icons icons;
   private final boolean isLeaf;
   private final PopulateTreeItemRunnableFactory factory;

   // Hold onto graphic at this level instead of passing to super class.
   // This is to work around JDK issue - https://bugs.openjdk.java.net/browse/JDK-8156049.
   private Node graphic;

   private boolean directoryListLoaded = false;

   DirectoryTreeItem(final File value,
                     final Node graphic,
                     final Icons icons,
                     final boolean isLeaf) {
      this(value, graphic, null, null, icons, isLeaf);
   }

   DirectoryTreeItem(final File value,
                     final Node graphic,
                     final FilesViewCallback callback,
                     final PopulateTreeItemRunnableFactory factory,
                     final Icons icons,
                     final boolean isLeaf) {
      super(value, null);

      this.callback = callback;
      this.icons = icons;
      this.graphic = graphic;
      this.factory = factory;
      this.isLeaf = isLeaf;

      expandedProperty().addListener(new ExpandedListener());
   }

   @Override
   public boolean isLeaf() {
      return isLeaf;
   }

   @Override
   public ObservableList<TreeItem<File>> getChildren() {
      if (isLeaf) {
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

   Node getGraphic2() {
      return graphic;
   }

   void setGraphic2(final Node graphic) {
      this.graphic = graphic;
   }

   private class ExpandedListener implements ChangeListener<Boolean> {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean expanded) {
         if (getValue().isDirectory()) {
            setGraphic2(getIcon(expanded));
         }
      }

      private ImageView getIcon(final boolean expanded) {
         final ImageView graphic = new ImageView(icons.getIcon(expanded ? IconsImpl.OPEN_FOLDER_64 : IconsImpl.FOLDER_64));
         graphic.setFitWidth(IconsImpl.SMALL_ICON_WIDTH);
         graphic.setFitHeight(IconsImpl.SMALL_ICON_HEIGHT);
         graphic.setPreserveRatio(true);
         return graphic;
      }
   }
}
