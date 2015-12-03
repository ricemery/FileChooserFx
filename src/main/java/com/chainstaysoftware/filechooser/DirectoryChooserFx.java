package com.chainstaysoftware.filechooser;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.stage.Window;

import java.io.File;

public interface DirectoryChooserFx {
   void setInitialDirectory(File value);

   File getInitialDirectory();

   ObjectProperty<File> initialDirectoryProperty();

   void setTitle(String value);

   String getTitle();

   StringProperty titleProperty();

   void setViewType(ViewType viewType);

   ViewType getViewType();

   ObjectProperty<ViewType> viewTypeProperty();

   /**
    * List of directories to show in the Favorites list. As favorites are add and removed
    * by the user the list is updated.
    */
   ObservableList<File> getFavoriteDirs();

   /**
    * Sets callbacks for when user wants to add and/or remove director favorites.
    * This method MUST be called with non-null {@link FavoritesCallback} instances
    * for the add/remove favorites buttons to be included in the displayed DirectoryChooserFx
    * instance.
    */
   void setFavoriteDirsCallbacks(FavoritesCallback addFavorite, FavoritesCallback removeFavorite);

   /**
    * Sets the callback for the help button. This method MUST be called with a
    * non-null {@link HelpCallback} for the Help button to be included in
    * the displayed DirectoryChooserFx instance.
    */
   void setHelpCallback(HelpCallback helpCallback);

   void showDialog(Window ownerWindow, FileChooserCallback fileChooserCallback);
}
