package com.chainstaysoftware.filechooser;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.stage.Window;

import java.io.File;

public class DirectoryChooserFxImpl implements DirectoryChooserFx {
   private final FileChooserFxImpl fileChooser = new FileChooserFxImpl();

   /**
    * Property representing the height of the FileChooser.
    */
   @Override
   public ReadOnlyDoubleProperty heightProperty() {
      return fileChooser.heightProperty();
   }

   /**
    * Get the height of the FileChooser.
    */
   @Override
   public double getHeight() {
      return fileChooser.getHeight();
   }

   /**
    * Set the height of the FileChooser.
    *
    * @param height
    */
   @Override
   public void setHeight(final double height) {
      fileChooser.setHeight(height);
   }

   /**
    * Property representing the width of the FileChooser.
    */
   @Override
   public ReadOnlyDoubleProperty widthProperty() {
      return fileChooser.widthProperty();
   }

   /**
    * Get the width of the FileChooser.
    */
   @Override
   public double getWidth() {
      return fileChooser.getWidth();
   }

   /**
    * Set the width of the FileChooser.
    */
   @Override
   public void setWidth(final double width) {
      fileChooser.setWidth(width);
   }

   /**
    * Sets the position of the dividers. Should be called before "show" is invoked.
    *
    * @param placesDivider the position of the divider that separates the places view from
    *                      the directories, between 0.0 and 1.0 (inclusive).
    */
   @Override
   public void setDividerPosition(final double placesDivider) {
      fileChooser.setDividerPositions(placesDivider, 0);
   }

   /**
    * Returns the position of the divider.
    */
   @Override
   public double getDividerPosition() {
      return fileChooser.getDividerPositions()[0];
   }

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

   /**
    * List of directories to show in the Favorites list. As favorites are add and removed
    * by the user, the list is updated.
    */
   @Override
   public ObservableList<File> getFavoriteDirs() {
      return fileChooser.favoriteDirsProperty();
   }

   /**
    * Sets callbacks for when user wants to add and/or remove director favorites.
    * This method MUST be called with non-null {@link FavoritesCallback} instances
    * for the add/remove favorites buttons to be included in the displayed DirectoryChooserFx
    * instance.
    */
   @Override
   public void setFavoriteDirsCallbacks(final FavoritesCallback addFavorite,
                                        final FavoritesCallback removeFavorite) {
      fileChooser.setFavoriteDirsCallbacks(addFavorite, removeFavorite);
   }

   /**
    * Sets the callback for the help button. This method MUST be called with a
    * non-null {@link HelpCallback} for the Help button to be included in
    * the displayed DirectoryChooserFx instance.
    */
   @Override
   public void setViewType(final ViewType viewType) {
      fileChooser.setViewType(viewType);
   }

   @Override
   public ViewType getViewType() {
      return fileChooser.getViewType();
   }

   @Override
   public ObjectProperty<ViewType> viewTypeProperty() {
      return fileChooser.viewTypeProperty();
   }

   /**
    * Set the sort field.
    */
   @Override
   public void setOrderBy(final OrderBy orderBy) {
      fileChooser.setOrderBy(orderBy);
   }

   /**
    * Retrieve the current sort field. Defaults to {@link OrderBy#Name}.
    */
   @Override
   public OrderBy getOrderBy() {
      return fileChooser.getOrderBy();
   }

   /**
    * Retrieve the current sort field. Defaults to {@link OrderBy#Name}.
    */
   @Override
   public ObjectProperty<OrderBy> orderByProperty() {
      return fileChooser.orderByProperty();
   }

   /**
    * Set the sort direction.
    */
   @Override
   public void setOrderDirection(final OrderDirection orderDirection) {
      fileChooser.setOrderDirection(orderDirection);
   }

   /**
    * Retrieve the sort direction. Defaults to {@link OrderDirection#Ascending}.
    */
   @Override
   public OrderDirection getOrderDirection() {
      return fileChooser.getOrderDirection();
   }

   /**
    * Retrieve the sort direction. Defaults to {@link OrderDirection#Ascending}.
    */
   @Override
   public ObjectProperty<OrderDirection> orderDirectionProperty() {
      return fileChooser.orderDirectionProperty();
   }

   /**
    * Disable/enable the display of mount points on Linux/OSX.
    */
   @Override
   public void setShowMountPoints(boolean value) {
      fileChooser.setShowMountPoints(value);
   }

   /**
    * Showing/not showing Linux/OSX mount points.
    */
   @Override
   public boolean showMountPoints() {
      return fileChooser.showMountPoints();
   }

   /**
    * Disable/enable the display of mount points on Linux/OSX.
    */
   @Override
   public BooleanProperty showMountPointsProperty() {
      return fileChooser.showMountPointsProperty();
   }

   /**
    * Overrides the default button order for Open, Cancel, Help Buttons on
    * the bottom of dialog. See {@link javafx.scene.control.ButtonBar} button order property. Must
    * be called before 'show' function is called.
    */
   @Override
   public void setOpenCancelHelpBtnOrder(final String buttonOrder) {
      fileChooser.setOpenCancelHelpBtnOrder(buttonOrder);
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
