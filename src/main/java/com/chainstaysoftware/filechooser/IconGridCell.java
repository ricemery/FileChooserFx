package com.chainstaysoftware.filechooser;

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

   public IconGridCell() {
      this(true);
   }

   public IconGridCell(final boolean preserveImageProperties) {
      this(preserveImageProperties, null);
   }

   public IconGridCell(final boolean preserveImageProperties,
                       final IconGridCellContextMenuFactory contextMenuFactory) {
      getStyleClass().add("image-grid-cell");

      this.preserveImageProperties = preserveImageProperties;
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
         final Image image = item.getIcon();
         final String text = item.getFile().getName();

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
