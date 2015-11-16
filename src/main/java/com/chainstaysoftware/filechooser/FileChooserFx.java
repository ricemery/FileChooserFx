package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.preview.PreviewPane;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.stage.Window;

import java.io.File;

public interface FileChooserFx {
   ObservableList<javafx.stage.FileChooser.ExtensionFilter> getExtensionFilters();

   ObjectProperty<javafx.stage.FileChooser.ExtensionFilter> selectedExtensionFilterProperty();

   void setSelectedExtensionFilter(javafx.stage.FileChooser.ExtensionFilter filter);

   javafx.stage.FileChooser.ExtensionFilter getSelectedExtensionFilter();

   ObservableMap<String, Class<? extends PreviewPane>> getPreviewHandlers();

   void setInitialDirectory(File value);

   File getInitialDirectory();

   ObjectProperty<File> initialDirectoryProperty();

   void setInitialFileName(String value);

   String getInitialFileName();

   ObjectProperty<String> initialFileNameProperty();

   void setTitle(String value);

   String getTitle();

   StringProperty titleProperty();

   void setViewType(ViewType viewType);

   ViewType getViewType();

   ObjectProperty<ViewType> viewTypeProperty();

   void setShowHiddenFiles(boolean value);

   boolean showHiddenFiles();

   BooleanProperty showHiddenFilesProperty();

   void setHelpCallback(HelpCallback helpCallback);

   void showOpenDialog(Window ownerWindow, FileChooserCallback fileChooserCallback);

   void showSaveDialog(Window ownerWindow, FileChooserCallback fileChooserCallback);
}
