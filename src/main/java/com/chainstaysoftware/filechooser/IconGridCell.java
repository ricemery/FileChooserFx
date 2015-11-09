package com.chainstaysoftware.filechooser;

import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;
import org.controlsfx.control.GridCell;

import java.io.File;

class IconGridCell extends GridCell<Pair<Image, File>> {
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
   protected void updateItem(final Pair<Image, File> pair,
                             final boolean empty) {
      super.updateItem(pair, empty);

      setContextMenu(null);

      if (empty) {
         setGraphic(null);
      } else {
         final Image image = pair.getKey();
         final String text = pair.getValue().getName();

         if (preserveImageProperties) {
            imageView.setPreserveRatio(true);
            imageView.setSmooth(image.isSmooth());
         }
         imageView.setImage(image);

         setGraphic(imageView);
         setText(text);
         setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
         setUserData(pair.getValue());

         if (contextMenuFactory != null) {
            setContextMenu(contextMenuFactory.create(pair));
         }
      }
   }

   @Override
   public void updateSelected(final boolean selected) {
      setStyle(selected ? "-fx-border-color: -fx-cell-focus-inner-border;" : null);
   }
}
