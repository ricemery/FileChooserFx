package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.icons.IconsImpl;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DirectoryTreeItem extends TreeItem<File> {
   private final FilesViewCallback callback;
   private final Icons icons;
   private final boolean isLeaf;

   private boolean directoryListLoaded = false;

   public DirectoryTreeItem(final File value, final FilesViewCallback callback,
                            final Icons icons) {
      this(value, null, callback, icons);
   }

   public DirectoryTreeItem(final File value, final Node graphic,
                            final FilesViewCallback callback,
                            final Icons icons) {
      super(value, graphic);

      this.callback = callback;
      this.icons = icons;
      this.isLeaf = !value.isDirectory();

      expandedProperty().addListener(new ExpandedListener());
   }

   @Override
   public boolean isLeaf() {
      return isLeaf;
   }

   @Override
   public ObservableList<TreeItem<File>> getChildren() {
      if (!directoryListLoaded) {
         loadChildren();
      }

      return super.getChildren();
   }

   private void loadChildren() {
      directoryListLoaded = true;

      final Stream<File> fileStream = callback.getFileStream(getValue());
      final List<TreeItem<File>> children = fileStream
            .map(f -> {
               final ImageView graphic = new ImageView(icons.getIconForFile(f));
               graphic.setFitWidth(IconsImpl.SMALL_ICON_WIDTH);
               graphic.setFitHeight(IconsImpl.SMALL_ICON_HEIGHT);
               graphic.setPreserveRatio(true);

               return f.isDirectory()
                  ? new DirectoryTreeItem(f, graphic, callback, icons)
                  : new TreeItem<>(f, graphic);
            })
            .collect(Collectors.toList());
      getChildren().addAll(children);
   }

   private class ExpandedListener implements ChangeListener<Boolean> {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean expanded) {
         if (getValue().isDirectory()) {
            setGraphic(getIcon(expanded));
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
