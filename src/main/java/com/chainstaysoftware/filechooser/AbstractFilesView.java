package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.preview.PreviewPane;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract class AbstractFilesView implements FilesView {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.AbstractFilesView");

   private final Stage parent;

   AbstractFilesView(final Stage parent) {
      this.parent = parent;
   }

   /**
    * Create {@link Stage} to display {@link PreviewPane} and show the {@link Stage}
    * @param previewPaneClass {@link Node} to display the file within.
    * @param file {@link File} to preview.
    */
   void showPreview(final Class<? extends PreviewPane> previewPaneClass,
                    final File file) {
      final Optional<PreviewPane> previewPaneOpt = PreviewPaneFactory.create(previewPaneClass);
      if (!previewPaneOpt.isPresent()) {
         logger.log(Level.SEVERE, "No PreviewPane created.");
         return;
      }

      final Stage stage = new Stage();

      final PreviewPane previewPane = previewPaneOpt.get();
      final Pane pane = previewPane.getPane();
      pane.prefWidthProperty().bind(stage.widthProperty());
      pane.prefHeightProperty().bind(stage.heightProperty());
      pane.maxWidthProperty().bind(stage.widthProperty());
      pane.maxHeightProperty().bind(stage.heightProperty());
      pane.minWidthProperty().bind(stage.widthProperty());
      pane.minHeightProperty().bind(stage.heightProperty());

      final Scene scene = new Scene(new Pane(pane));
      scene.setOnKeyPressed(event -> {
         if (event.getCode() == KeyCode.ESCAPE) {
            stage.close();
         }
      });

      previewPane.setFile(file);

      stage.setScene(scene);
      stage.initOwner(parent);
      stage.setAlwaysOnTop(true);
      stage.initStyle(StageStyle.UTILITY);
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.setTitle(getTitle(file));
      stage.setWidth(1024);
      stage.setHeight(768);
      stage.show();
   }

   /**
    * Build a window title for the passed in file.
    * @param file
    */
   private String getTitle(final File file) {
      final int maxLength = 75;
      return StringUtils.abbreviateMiddle(file.getPath(), "...", maxLength);
   }
}
