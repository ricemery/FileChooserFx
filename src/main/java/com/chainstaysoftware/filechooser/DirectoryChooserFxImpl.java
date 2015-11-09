package com.chainstaysoftware.filechooser;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Window;

import java.io.File;

public class DirectoryChooserFxImpl implements DirectoryChooserFx {
   private final FileChooserFxImpl fileChooser = new FileChooserFxImpl();

   @Override
   public void setInitialDirectory(final File value) {
      fileChooser.setInitialDirectory(value);
   }

   @Override
   public File getInitialDirectory() {
      return fileChooser.getInitialDirectory();
   }

   @Override
   public ObjectProperty<File> initialDirectoryProperty() {
      return fileChooser.initialDirectoryProperty();
   }

   @Override
   public void setTitle(final String value) {
      fileChooser.setTitle(value);
   }

   @Override
   public String getTitle() {
      return fileChooser.getTitle();
   }

   @Override
   public StringProperty titleProperty() {
      return fileChooser.titleProperty();
   }

   @Override
   public void setHelpCallback(final HelpCallback helpCallback) {
      fileChooser.setHelpCallback(helpCallback);
   }

   @Override
   public void showDialog(final Window ownerWindow,
                          final FileChooserCallback fileChooserCallback) {
      fileChooser.showOpenDialog(ownerWindow, fileChooserCallback, true);
   }
}
