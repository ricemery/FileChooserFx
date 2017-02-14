package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.icons.IconsImpl;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.controlsfx.control.GridCell;

class IconGridCell extends GridCell<DirectoryListItem> {
   private final ImageView imageView;
   private final boolean preserveImageProperties;
   private final IconGridCellContextMenuFactory contextMenuFactory;
   private final Icons icons;

   IconGridCell(final boolean preserveImageProperties,
                final IconGridCellContextMenuFactory contextMenuFactory,
                final Icons icons) {
      getStyleClass().add("image-grid-cell");

      this.preserveImageProperties = preserveImageProperties;
      this.icons = icons;

      imageView = new ImageView();
      imageView.fitHeightProperty().bind(heightProperty().subtract(40));
      imageView.fitWidthProperty().bind(widthProperty());

      setContentDisplay(ContentDisplay.TOP);
      setWrapText(true);
      setAlignment(Pos.TOP_CENTER);

      this.contextMenuFactory = contextMenuFactory;
   }

   @Override
   protected void updateItem(final DirectoryListItem item,
                             final boolean empty) {
      super.updateItem(item, empty);

      setContextMenu(null);

      if (empty) {
         setGraphic(null);
         setText(null);
         setUserData(null);
      } else {
         final String text = item.getFile().getName();

         final Image image = item.isDirectory()
            ? icons.getIcon(IconsImpl.FOLDER_64)
            : icons.getIconForFile(item.getFile());

         if (preserveImageProperties) {
            imageView.setPreserveRatio(true);
            imageView.setSmooth(image.isSmooth());
         }
         imageView.setImage(image);

         setGraphic(imageView);
         setText(text);
         setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
         setUserData(item.getFile());

         setOnContextMenuRequested(event -> {
            if (contextMenuFactory != null) {
               contextMenuFactory.create(item).show(this, event.getScreenX(), event.getScreenY());
            }
         });
      }
   }

   @Override
   public void updateSelected(final boolean selected) {
      setStyle(selected ? "-fx-border-color: -fx-cell-focus-inner-border;" : null);
   }
}
