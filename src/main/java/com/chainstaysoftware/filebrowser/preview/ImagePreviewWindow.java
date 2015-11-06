package com.chainstaysoftware.filebrowser.preview;


import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * {@link PreviewWindow} implementation for displaying image file types.
 */
public class ImagePreviewWindow implements PreviewWindow {
   /**
    * Show preview of passed in {@link File}. Note that the entire file will
    * be read into memory. So, there is a potential for OutOfMemoryException
    * for large files (and untuned JVMs).
    * @param parent Parent {@link Window}
    * @param file   {@link File} to preview.
    */
   @Override
   public void showPreview(final Window parent,
                           final File file) {
      try {
         final Image image = new Image(new FileInputStream(file));
         final ImageView imageView = new ImageView(image);

         final BorderPane borderPane = new BorderPane();
         borderPane.setCenter(imageView);

         final Stage stage = new Stage();

         final Scene scene = new Scene(borderPane);
         scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
               stage.close();
            }
         });

         stage.setScene(scene);
         stage.initOwner(parent);
         stage.setAlwaysOnTop(true);
         stage.initStyle(StageStyle.UTILITY);
         stage.initModality(Modality.APPLICATION_MODAL);
         stage.setTitle(getTitle(file));
         stage.show();
      } catch (FileNotFoundException e) {
      }
   }

   private String getTitle(final File file) {
      final int maxLength = 75;
      return StringUtils.abbreviateMiddle(file.getPath(), "...", maxLength);
   }
}
