package com.chainstaysoftware.filechooser.preview;

import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * {@link PreviewPane} implementation for displaying N number of lines from the
 * head of text file types.
 */
public class HeadPreviewPane implements PreviewPane {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.HeadPreviewWindow");

   private final Charset encoding;
   private final int maxLines;
   private final BorderPane borderPane;
   private final TextArea textArea;

   public HeadPreviewPane(final Charset encoding,
                          final int maxLines) {
      this.encoding = encoding;
      this.maxLines = maxLines;
      this.textArea = new TextArea();

      textArea.setId("headPreviewTextArea");
      textArea.setEditable(false);
      textArea.setMinSize(0, 0);
      textArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

      borderPane = new BorderPane();
      borderPane.setId("headPreviewPane");
      borderPane.setCenter(textArea);
   }

   /**
    * Sets the file to display within the Pane.
    *
    * @param file
    */
   @Override
   public void setFile(final File file) {
      try (Stream<String> stream = Files.lines(file.toPath(), encoding)) {
         textArea.setText(stream.limit(maxLines).reduce((t, u) -> t + "\r\n" + u).orElse(""));
      } catch (IOException e) {
         logger.log(Level.WARNING, "Error reading file - " + file, e);
         // Error reading file.
      }
   }

   @Override
   public Pane getPane() {
      return borderPane;
   }
}
