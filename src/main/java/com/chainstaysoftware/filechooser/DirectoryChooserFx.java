package com.chainstaysoftware.filechooser;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.stage.Window;

import java.io.File;

public interface DirectoryChooserFx {
   /**
    * Property representing the height of the FileChooser.
    */
   ReadOnlyDoubleProperty heightProperty();

   /**
    * Get the height of the FileChooser.
    */
   double getHeight();

   /**
    * Set the height of the FileChooser.
    */
   void setHeight(double height);

   /**
    * Property representing the width of the FileChooser.
    */
   ReadOnlyDoubleProperty widthProperty();

   /**
    * Get the width of the FileChooser.
    */
   double getWidth();

   /**
    * Set the width of the FileChooser.
    */
   void setWidth(double width);

   /**
    * Sets the position of the dividers. Should be called before "show" is invoked.
    *
    * @param placesDivider the position of the divider that separates the places view from
    *                      the directories, between 0.0 and 1.0 (inclusive).
    */
   void setDividerPosition(double placesDivider);

   /**
    * Returns the position of the divider.
    */
   double getDividerPosition();

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
    * Overrides the default button order for Open, Cancel, Help Buttons on
    * the bottom of dialog. See {@link javafx.scene.control.ButtonBar} button order property. Must
    * be called before 'show' function is called.
    */
   void setOpenCancelHelpBtnOrder(String buttonOrder);

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
