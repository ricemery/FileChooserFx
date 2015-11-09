package com.chainstaysoftware.filechooser;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Window;

import java.io.File;

public interface DirectoryChooser {
   void setInitialDirectory(File value);

   File getInitialDirectory();

   ObjectProperty<File> initialDirectoryProperty();

   void setTitle(String value);

   String getTitle();

   StringProperty titleProperty();

   void setHelpCallback(HelpCallback helpCallback);

   void showDialog(Window ownerWindow, FileChooserCallback fileChooserCallback);
}
