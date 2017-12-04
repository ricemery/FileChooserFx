package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.icons.IconsImpl;
import com.chainstaysoftware.filechooser.os.Place;
import com.chainstaysoftware.filechooser.os.PlaceType;
import com.chainstaysoftware.filechooser.os.Places;
import javafx.beans.Observable;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;


/**
 * Places - JavaFx node creation and logic.
 */
class PlacesView {
   private static final Map<PlaceType, String> placeToIcon = new HashMap<>();
   static {
      placeToIcon.put(PlaceType.Cd, IconsImpl.CD_64);
      placeToIcon.put(PlaceType.Dvd, IconsImpl.DVD_64);
      placeToIcon.put(PlaceType.FloppyDisk, IconsImpl.FLOPPY_64);
      placeToIcon.put(PlaceType.HardDisk, IconsImpl.HARDDISK_64);
      placeToIcon.put(PlaceType.Network, IconsImpl.NETWORK_64);
      placeToIcon.put(PlaceType.Usb, IconsImpl.USB_64);
   }

   private final ResourceBundle resourceBundle = ResourceBundle.getBundle("filechooser");

   private final FilesViewCallback callback;
   private final Icons icons;

   private final TitledPane placesPane;
   private final TreeView<PlacesTreeItem> placesTreeView;
   private final TreeItem<PlacesTreeItem> defaultPlacesNode;
   private final TreeItem<PlacesTreeItem> favoritesPlacesNode;

   PlacesView(final FilesViewCallback callback,
              final Icons icons) {
      this.callback = callback;
      this.icons = icons;

      defaultPlacesNode = createDefaultPlacesItem();
      favoritesPlacesNode = createFavoritePlacesItem();
      placesTreeView = createPlacesView();
      placesPane = createPlacesPane(placesTreeView);

      this.callback.favoriteDirsProperty().addListener((Observable o) -> updatePlaces());
   }

   TitledPane toPane() {
      return placesPane;
   }

   /**
    * Return the selected directory from the Places View (if any).
    */
   Optional<File> getSelectedItem() {
      final TreeItem<PlacesTreeItem> item = placesTreeView.getSelectionModel().getSelectedItem();
      if (item == null || item.getValue() == null || !item.getValue().getFile().isPresent()) {
         return Optional.empty();
      }

      return Optional.of(item.getValue().getFile().get());
   }

   /**
    * Create the {@link TitledPane} to hold the Places Tree. This pane is used
    * to put a title bar on the Places..
    */
   private TitledPane createPlacesPane(final TreeView treeView) {
      final TitledPane titledPane = new TitledPane(resourceBundle.getString("placeslist.text"), treeView);
      titledPane.setId("placesPane");
      titledPane.setCollapsible(false);
      return titledPane;
   }

   private TreeItem<PlacesTreeItem> createDefaultPlacesItem() {
      final TreeItem<PlacesTreeItem> item = new TreeItem<>();
      item.setValue(new PlacesTreeItem(Optional.of(resourceBundle.getString("computer.text")),
            Optional.empty(), icons.getIcon(IconsImpl.COMPUTER_64), false));
      item.setExpanded(true);

      return item;
   }

   private TreeItem<PlacesTreeItem> createFavoritePlacesItem() {
      final TreeItem<PlacesTreeItem> item = new TreeItem<>();
      item.setValue(new PlacesTreeItem(Optional.of(resourceBundle.getString("favorites.text")),
            Optional.empty(), icons.getIcon(IconsImpl.STAR_64), true));
      item.setExpanded(true);
      return item;
   }

   /**
    * Create the Places {@link TreeView}.
    */
   private TreeView<PlacesTreeItem> createPlacesView() {
      final TreeView<PlacesTreeItem> view = new TreeView<>();
      view.setId("placesView");
      view.setShowRoot(false);
      view.setCellFactory(new PlacesTreeItemCellFactory(callback.favoriteDirsProperty()));

      final TreeItem<PlacesTreeItem> root = new TreeItem<>();
      view.setRoot(root);

      view.setOnMouseClicked(event -> {
         final TreeItem<PlacesTreeItem> selectedItem = view.getSelectionModel().getSelectedItem();
         if (selectedItem == null || !selectedItem.getValue().getFile().isPresent()) {
            callback.disableRemoveFavoritieButton(true);
            return;
         }

         final File currentDir = selectedItem.getValue().getFile().get();
         callback.requestChangeDirectory(currentDir);

         callback.disableRemoveFavoritieButton(!callback.favoriteDirsProperty().contains(currentDir));
      });

      return view;
   }

   /**
    * Update the Places View with drives, home dir and favorites.
    */
   void updatePlaces() {
      final TreeItem<PlacesTreeItem> rootNode = placesTreeView.getRoot();
      rootNode.getChildren().clear();
      rootNode.getChildren().add(defaultPlacesNode);

      defaultPlacesNode.getChildren().clear();
      favoritesPlacesNode.getChildren().clear();

      final List<Place> defaultPlaces = new Places().getDefaultPlaces(callback.showMountPointsProperty().get());
      defaultPlaces.forEach(place ->
            defaultPlacesNode.getChildren().add(new TreeItem<>(new PlacesTreeItem(Optional.empty(),
                  Optional.of(place.getPath()), toIcon(place), false), null)));

      final String homeDirStr = System.getProperty("user.home");
      if (homeDirStr != null) {
         defaultPlacesNode.getChildren().add(new TreeItem<>(new PlacesTreeItem(Optional.empty(),
               Optional.of(new File(homeDirStr)), icons.getIcon(IconsImpl.USER_HOME_64), false), null));
      }

      if (!callback.favoriteDirsProperty().isEmpty()) {
         rootNode.getChildren().add(favoritesPlacesNode);

         callback.favoriteDirsProperty().forEach(file ->
               favoritesPlacesNode.getChildren().add(new TreeItem<>(new PlacesTreeItem(Optional.empty(),
                     Optional.of(file), icons.getIcon(IconsImpl.FOLDER_64), true), null)));
      }

      callback.disableAddFavoriteButton(true);
      callback.disableRemoveFavoritieButton(true);
   }

   /**
    * Get the icon for the passed in place.
    */
   private Image toIcon(final Place place) {
      String filename = placeToIcon.get(place.getType());
      if (filename == null) {
         filename = IconsImpl.HARDDISK_64;
      }

      return icons.getIcon(filename);
   }
}
