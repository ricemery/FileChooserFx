package com.chainstaysoftware.filechooser;

import javafx.beans.property.BooleanProperty;

import java.io.File;
import java.util.function.Predicate;

class ShowHiddenFilesPredicate implements Predicate<File> {
   private final BooleanProperty showHiddenFiles;
   private final BooleanProperty shouldHideFiles;

   ShowHiddenFilesPredicate(final BooleanProperty showHiddenFiles,
                            final BooleanProperty shouldHideFiles) {
      this.showHiddenFiles = showHiddenFiles;
      this.shouldHideFiles = shouldHideFiles;
   }

   @Override
   public boolean test(final File file) {
      final boolean filterHidden = !showHiddenFiles.get();
      return !(filterHidden && file.isHidden()) && (!shouldHideFiles.get() || file.isDirectory());
   }
}

