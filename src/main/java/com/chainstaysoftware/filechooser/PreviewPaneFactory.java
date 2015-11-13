package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.preview.PreviewPane;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

class PreviewPaneFactory {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.PreviewPaneFactory");

   private PreviewPaneFactory() {}

   /**
    * Creates a {@link PreviewPane} instance from the passed in class. If there is an error, then an
    * empty Optional is returned.
    * @param previewPaneClass Class to create instance for.
    * @return {@link Optional} that contains the created {@link PreviewPane} instance. Or, empty on error.
    */
   static Optional<PreviewPane> create(final Class<? extends PreviewPane> previewPaneClass) {
      try {
         return Optional.of(previewPaneClass.newInstance());
      } catch (InstantiationException | IllegalAccessException e) {
         logger.log(Level.SEVERE, "Error creating PreviewPane", e);
         return Optional.empty();
      }
   }
}
