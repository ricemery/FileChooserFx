package com.chainstaysoftware.filebrowser.preview;


import javafx.stage.Window;

import java.io.File;

public interface PreviewWindow {
   /**
    * Show preview of passed in {@link File}. Note that it is likely that the
    * implementations will read the entire file will memory. So, there is a
    * potential for OutOfMemoryException for large files (and untuned JVMs).
    * @param parent Parent {@link Window}
    * @param file {@link File} to preview.
    */
   void showPreview(Window parent, File file);
}
