package com.chainstaysoftware.filechooser.preview;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
      hBox.setMinSize(0, 0);

      imageView.fitWidthProperty().bind(hBox.widthProperty());
      imageView.fitHeightProperty().bind(hBox.heightProperty());
      imageView.setPreserveRatio(true);
   }

   /**
    * Sets the file to display within the Pane.
    */
   @Override
   public void setFile(final File file) {
      try (final InputStream is = new FileInputStream(file)){
         final Image image = new Image(is);
         imageView.setImage(image);
      } catch (IOException e) {
         logger.log(Level.WARNING, "Error opening - " + file, e);
      }
   }

   @Override
   public Pane getPane() {
      return hBox;
   }
}
