package com.chainstaysoftware.filechooser.preview;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link PreviewPane} implementation for displaying image file types.
 */
public class ImagePreviewPane implements PreviewPane {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.ImagePreviewPane");

   private final HBox hBox;
   private final ImageView imageView;

   public ImagePreviewPane() {
      imageView = new ImageView();
      imageView.setId("imagePreviewImageView");

      hBox = new HBox();
      hBox.setId("imagePreviewHbox");
      hBox.getChildren().add(imageView);
      hBox.setAlignment(Pos.CENTER);

      imageView.fitWidthProperty().bind(hBox.widthProperty());
      imageView.fitHeightProperty().bind(hBox.heightProperty());
      imageView.setPreserveRatio(true);
   }

   /**
    * Sets the file to display within the Pane.
    *
    * @param file
    */
   @Override
   public void setFile(final File file) {
      try {
         final Image image = new Image(new FileInputStream(file));
         imageView.setImage(image);
      } catch (FileNotFoundException e) {
         logger.log(Level.WARNING, "File not found - " + file);
      }
   }

   @Override
   public Pane getPane() {
      return hBox;
   }
}
