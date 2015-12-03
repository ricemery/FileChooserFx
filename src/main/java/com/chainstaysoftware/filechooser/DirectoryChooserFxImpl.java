package com.chainstaysoftware.filechooser;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
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

   /**
    * List of directories to show in the Favorites list. As favorites are add and removed
    * by the user, the list is updated.
    */
   @Override
   public ObservableList<File> getFavoriteDirs() {
      return fileChooser.getFavoriteDirs();
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
