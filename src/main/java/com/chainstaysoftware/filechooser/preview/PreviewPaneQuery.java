/**
 * Copyright 2015 © Denmar Technical Services Inc
 * The U.S. Government has unlimited rights per DFAR 252.227-7014, all other
 * rights reserved.
 * <p>
 * WARNING - This software contains Technical Data whose export is restricted by
 * the Arms Export Control Act (Title 22, U.S.C., Sec 2751, et seq.) or the
 * Export Administration Act of 1979, as amended (Title 50, U.S. C. App. 2401
 * et seq.). Violations of these export laws are subject to severe criminal
 * penalties.
 */
package com.chainstaysoftware.filechooser.preview;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Determine the {@link PreviewPane} to utilize for a specific file.
 */
public class PreviewPaneQuery {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.preview.PreviewPaneQuery");

   private PreviewPaneQuery() {}

   /**
    * Determine the {@link PreviewPane} to utilize.
    * @param previewHandlers Map of configured {@link PreviewPane} keyed on MimeType.
    * @param file File to return a {@link PreviewPane} for.
    * @return {@link PreviewPane} class to use for the file. Or, null if none found.
    */
   public static Class<? extends PreviewPane> query(final Map<String, Class<? extends PreviewPane>> previewHandlers,
                                                    final File file) {
      try {
         final String mimeType = Files.probeContentType(file.toPath());
         return previewHandlers.get(mimeType);
      } catch (IOException e) {
         logger.log(Level.WARNING, "could not determine mimetype for - " + file, e);
         return null;
      }
   }
}
