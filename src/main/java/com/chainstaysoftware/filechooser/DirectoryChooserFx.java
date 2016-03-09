package com.chainstaysoftware.filechooser;

import javafx.beans.property.BooleanProperty;
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
    * Set the sort field.
    */
   void setOrderBy(OrderBy orderBy);

   /**
    * Retrieve the current sort field. Defaults to {@link OrderBy#Name}.
    */
   OrderBy getOrderBy();

   /**
    * Retrieve the current sort field. Defaults to {@link OrderBy#Name}.
    */
   ObjectProperty<OrderBy> orderByProperty();

   /**
    * Set the sort direction.
    */
   void setOrderDirection(OrderDirection orderDirection);

   /**
    * Retrieve the sort direction. Defaults to {@link OrderDirection#Ascending}.
    */
   OrderDirection getOrderDirection();

   /**
    * Retrieve the sort direction. Defaults to {@link OrderDirection#Ascending}.
    */
   ObjectProperty<OrderDirection> orderDirectionProperty();

   /**
    * Disable/enable the display of mount points on Linux/OSX.
    */
   void setShowMountPoints(boolean value);

   /**
    * Showing/not showing Linux/OSX mount points.
    */
   boolean showMountPoints();

   /**
    * Disable/enable the display of mount points on Linux/OSX.
    */
   BooleanProperty showMountPointsProperty();
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
