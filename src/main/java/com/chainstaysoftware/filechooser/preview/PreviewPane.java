package com.chainstaysoftware.filechooser.preview;


import javafx.scene.layout.Pane;

import java.io.File;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Show preview of passed in {@link File}. Note that it is possible that the
 * implementations will read the entire file will memory. So, there is a
 * potential for OutOfMemoryException for large files (and untuned JVMs).
 */
public interface PreviewPane {
   /**
    * Sets the file to display within the Pane.
    * @param file
    */
   void setFile(File file);

   Pane getPane();
}
