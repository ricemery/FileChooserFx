package com.chainstaysoftware.filechooser;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;

/**
 * Callbacks from the {@link FilesView} implementations back into the
 * {@link FileChooserFx}.
 */
public interface FilesViewCallback {
   /**
    * Request that the directory is changed, and update the
    * view to include the directory contents.
    */
   void requestChangeDirectory(File directory);

   DirectoryStream<Path> getDirectoryStream(File directory) throws IOException;

   DirectoryStream<Path> unfilteredDirectoryStream(File directory) throws IOException;

   /**
    * Update the currently selected file.
    */
   void setCurrentSelection(File file);

   File getCurrentSelection();

   void fireDoneButton();

   void disableAddFavoriteButton(boolean disable);

   void disableRemoveFavoritieButton(boolean disable);

   /**
    * Update all the files in the view.
    */
   void updateFiles();

   ObjectProperty<OrderBy> orderByProperty();

   ObjectProperty<OrderDirection> orderDirectionProperty();

   ObservableList<File> favoriteDirsProperty();

   BooleanProperty showMountPointsProperty();

   BooleanProperty showHiddenFilesProperty();

   BooleanProperty shouldHideFilesProperty();
}
