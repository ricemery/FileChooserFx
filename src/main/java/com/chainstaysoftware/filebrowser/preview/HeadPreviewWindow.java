package com.chainstaysoftware.filebrowser.preview;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.stream.Stream;

/**
 * {@link PreviewWindow} implementation for displaying N number of lines from the
 * head of text file types.
 */
public class HeadPreviewWindow implements PreviewWindow {
   private final Charset encoding;
   private final int maxLines;

   public HeadPreviewWindow(final Charset encoding,
                            final int maxLines) {
      this.encoding = encoding;
      this.maxLines = maxLines;
   }

   /**
    * Show preview of passed in {@link File}. If maxLines is large and the file
    * is large then there is a potential for an OutOfMemoryException.
    *
    * @param parent Parent {@link Window}
    * @param file   {@link File} to preview.
    */
   @Override
   public void showPreview(final Window parent,
                           final File file) {
      String text;
      try (Stream<String> stream = Files.lines(file.toPath(), encoding)) {
         text = stream.limit(maxLines).reduce((t, u) -> t + "\r\n" + u).orElse("");
      } catch (IOException e) {
         // Error reading file.
         return;
      }

      final TextArea textArea = new TextArea();
      textArea.setId("fileTextArea");
      textArea.setText(text);
      textArea.setEditable(false);

      final Stage stage = new Stage();

      final Scene scene = new Scene(textArea);
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
   }

   private String getTitle(final File file) {
      final int maxLength = 75;
      return StringUtils.abbreviateMiddle(file.getPath(), "...", maxLength);
   }
}
