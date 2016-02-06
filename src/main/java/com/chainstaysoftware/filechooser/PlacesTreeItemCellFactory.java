package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.IconsImpl;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import javax.swing.filechooser.FileSystemView;

/**
 * CellFactory for rendering {@link PlacesTreeItem} instances within a
 * {@link TreeView}
 */
public class PlacesTreeItemCellFactory implements Callback<TreeView<PlacesTreeItem>, TreeCell<PlacesTreeItem>> {
   @Override
   public TreeCell<PlacesTreeItem> call(TreeView<PlacesTreeItem> param) {
      return new TreeCell<PlacesTreeItem>() {
         @Override
         protected void updateItem(PlacesTreeItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
               setText(null);
               setGraphic(null);
            } else if (!item.getFile().isPresent()) {
               setText(item.getText().orElse(""));
               setGraphic(toGraphic(item.getIcon()));
            } else {
               final String systemDisplayName = FileSystemView.getFileSystemView().getSystemDisplayName(item.getFile().get());
               setText("".equals(systemDisplayName) ? item.getFile().get().toString() : systemDisplayName);
               setGraphic(toGraphic(item.getIcon()));
            }
         }
      };
   }

   private ImageView toGraphic(final Image image) {
      if (image == null) {
         return null;
      }

      final ImageView graphic = new ImageView(image);
      graphic.setFitWidth(IconsImpl.SMALL_ICON_WIDTH);
      graphic.setFitHeight(IconsImpl.SMALL_ICON_HEIGHT);
      graphic.setPreserveRatio(true);
      return graphic;
   }
}
