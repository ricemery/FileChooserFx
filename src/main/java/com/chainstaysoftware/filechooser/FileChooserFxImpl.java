package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.icons.IconsImpl;
import com.chainstaysoftware.filechooser.os.Place;
import com.chainstaysoftware.filechooser.os.PlaceType;
import com.chainstaysoftware.filechooser.os.Places;
import com.chainstaysoftware.filechooser.preview.PreviewPane;
import impl.org.controlsfx.skin.BreadCrumbBarSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.controlsfx.control.BreadCrumbBar;
import org.controlsfx.control.spreadsheet.StringConverterWithFormat;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public final class FileChooserFxImpl implements FileChooserFx {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.FileChooserFxImpl");

   private static final int SCENE_WIDTH = 800;
   private static final int SCENE_HEIGHT = 600;
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
   private final ObservableList<FileChooser.ExtensionFilter> extensionFilters =
         FXCollections.observableArrayList();
   private final ObservableMap<String, Class<? extends PreviewPane>> previewHandlers = FXCollections.observableHashMap();
   private final ObservableList<File> favoriteDirs = FXCollections.observableArrayList();
   private final Deque<File> directoryStack = new LinkedList<>();
   private final ObjectProperty<File> currentSelection = new SimpleObjectProperty<>();

   private StringProperty title;
   private ObjectProperty<File> initialDirectory;
   private ObjectProperty<String> initialFileName;
   private ObjectProperty<FileChooser.ExtensionFilter> selectedExtensionFilter;
   private ObjectProperty<ViewType> viewTypeProperty;
   private BooleanProperty showHiddenFiles;
   private ObjectProperty<OrderBy> orderByProperty;
   private ObjectProperty<OrderDirection> orderDirectionProperty;
   private File currentDirectory;
   private Button backButton;
   private BreadCrumbBar<File> breadCrumbBar;
   private FilesView currentView;
   private IconsFilesView iconsFilesView;
   private ListFilesView listFilesView;
   private ListFilesWithPreviewView listFilesWithPreviewView;
   private SplitPane splitPane;
   private FileChooserCallback fileChooserCallback;
   private HelpCallback helpCallback;
   private FavoritesCallback addFavorite;
   private FavoritesCallback removeFavorite;
   private Stage stage;
   private TableView<DirectoryListItem> placesView;
   private boolean saveMode;
   private boolean hideFiles;
   private ToggleButton viewIconsButton;
   private ToggleButton viewListButton;
   private ToggleButton viewListWithPreviewButton;
   private TextField fileNameField;
   private ComboBox<FileChooser.ExtensionFilter> extensionsComboBox;
   private Button addFavoriteButton;
   private Button removeFavoriteButton;
   private Button doneButton;
   private Icons icons = new IconsImpl();
   private WatchService watcher;
   private WatchKey watchKey;


   @Override
   public ObservableList<FileChooser.ExtensionFilter> getExtensionFilters() {
      return extensionFilters;
   }

   @Override
   public ObjectProperty<FileChooser.ExtensionFilter> selectedExtensionFilterProperty() {
      if (selectedExtensionFilter == null) {
         selectedExtensionFilter =
               new SimpleObjectProperty<>(this,
                     "selectedExtensionFilter");
      }
      return selectedExtensionFilter;
   }

   @Override
   public void setSelectedExtensionFilter(FileChooser.ExtensionFilter filter) {
      selectedExtensionFilterProperty().setValue(filter);
   }

   @Override
   public FileChooser.ExtensionFilter getSelectedExtensionFilter() {
      return (selectedExtensionFilter != null)
            ? selectedExtensionFilter.get()
            : null;
   }

   @Override
   public ObservableMap<String, Class<? extends PreviewPane>> getPreviewHandlers() {
      return previewHandlers;
   }

   @Override
   public void setInitialDirectory(final File value) {
      initialDirectoryProperty().set(value);
   }

   @Override
   public File getInitialDirectory() {
      return (initialDirectory != null) ? initialDirectory.get() : null;
   }

   @Override
   public ObjectProperty<File> initialDirectoryProperty() {
      if (initialDirectory == null) {
         initialDirectory = new SimpleObjectProperty<>(this, "initialDirectory");
      }

      return initialDirectory;
   }

   @Override
   public void setInitialFileName(final String value) {
      initialFileNameProperty().set(value);
   }

   @Override
   public String getInitialFileName() {
      return (initialFileName != null) ? initialFileName.get() : null;
   }

   @Override
   public ObjectProperty<String> initialFileNameProperty() {
      if (initialFileName == null) {
         initialFileName =
               new SimpleObjectProperty<>(this, "initialFileName");
      }

      return initialFileName;
   }

   @Override
   public void setTitle(String value) {
      titleProperty().setValue(value);
   }

   @Override
   public String getTitle() {
      return title != null ? title.get() : null;
   }

   @Override
   public void setShowHiddenFiles(final boolean value) {
      showHiddenFilesProperty().setValue(value);
   }

   @Override
   public boolean showHiddenFiles() {
      return showHiddenFilesProperty().getValue();
   }

   @Override
   public BooleanProperty showHiddenFilesProperty() {
      if (showHiddenFiles == null) {
         showHiddenFiles = new SimpleBooleanProperty(this, "showHiddenFiles", false);
      }

      return showHiddenFiles;
   }

   /**
    * Set the sort field.
    */
   @Override
   public void setOrderBy(final OrderBy orderBy) {
      orderByProperty().setValue(orderBy);
   }

   /**
    * Retrieve the current sort field. Defaults to {@link OrderBy#Name}.
    */
   @Override
   public OrderBy getOrderBy() {
      return orderByProperty().getValue();
   }

   /**
    * Retrieve the current sort field. Defaults to {@link OrderBy#Name}.
    */
   @Override
   public ObjectProperty<OrderBy> orderByProperty() {
      if (orderByProperty == null) {
         orderByProperty = new SimpleObjectProperty<>(this, "orderBy", OrderBy.Name);
      }

      return orderByProperty;
   }

   /**
    * Set the sort direction.
    */
   @Override
   public void setOrderDirection(final OrderDirection orderDirection) {
      orderDirectionProperty().setValue(orderDirection);
   }

   /**
    * Retrieve the sort direction. Defaults to {@link OrderDirection#Ascending}.
    */
   @Override
   public OrderDirection getOrderDirection() {
      return orderDirectionProperty().get();
   }

   /**
    * Retrieve the sort direction. Defaults to {@link OrderDirection#Ascending}.
    */
   @Override
   public ObjectProperty<OrderDirection> orderDirectionProperty() {
      if (orderDirectionProperty == null) {
         orderDirectionProperty = new SimpleObjectProperty<>(this, "orderDirection", OrderDirection.Ascending);
      }

      return orderDirectionProperty;
   }

   @Override
   public StringProperty titleProperty() {
      if (title == null) {
         title = new SimpleStringProperty(this, "title");
      }

      return title;
   }

   @Override
   public ObservableList<File> getFavoriteDirs() {
      return favoriteDirs;
   }

   /**
    * Sets callbacks for when user wants to add and/or remove director favorites.
    * This method MUST be called with non-null {@link FavoritesCallback} instances
    * for the add/remove favorites buttons to be included in the displayed FileChooserFx
    * instance.
    */
   @Override
   public void setFavoriteDirsCallbacks(final FavoritesCallback addFavorite,
                                        final FavoritesCallback removeFavorite) {
      this.addFavorite = addFavorite;
      this.removeFavorite = removeFavorite;
   }

   public void setViewType(final ViewType viewType) {
      viewTypeProperty().setValue(viewType);
   }

   @Override
   public ViewType getViewType() {
      return viewTypeProperty.getValue();
   }

   @Override
   public ObjectProperty<ViewType> viewTypeProperty() {
      if (viewTypeProperty == null) {
         viewTypeProperty = new SimpleObjectProperty<>();
         viewTypeProperty.setValue(ViewType.List);
      }

      return viewTypeProperty;
   }

   /**
    * Sets the callback for the help button. This method MUST be called with a
    * non-null {@link HelpCallback} for the Help button to be included in
    * the displayed FileChooserFx instance.
    */
   @Override
   public void setHelpCallback(final HelpCallback helpCallback) {
      this.helpCallback = helpCallback;
   }

   /**
    * Set the implementation to use for Icon handling. This does not need
    * to be called unless there is a desire to override the default Icon set.
    */
   @Override
   public void setIcons(final Icons icons) {
      this.icons = icons;
   }

   @Override
   public void showOpenDialog(final Window ownerWindow,
                              final FileChooserCallback fileChooserCallback) {
      saveMode = false;
      showOpenDialog(ownerWindow, fileChooserCallback, false);
   }

   void showOpenDialog(final Window ownerWindow,
                       final FileChooserCallback fileChooserCallback,
                       final boolean hideFiles) {
      saveMode = false;
      this.hideFiles = hideFiles;
      showDialog(ownerWindow, fileChooserCallback);
   }

   @Override
   public void showSaveDialog(final Window ownerWindow,
                              final FileChooserCallback fileChooserCallback) {
      saveMode = true;
      hideFiles = false;
      showDialog(ownerWindow, fileChooserCallback);
   }

   private void showDialog(final Window ownerWindow,
                           final FileChooserCallback fileChooserCallback) {
      this.fileChooserCallback = fileChooserCallback;

      stage = new Stage();

      final VBox topVbox = createTopVBox();
      splitPane = createSplitPane();
      final VBox bottomVbox = createBottomVBox();

      final BorderPane borderPane = new BorderPane();
      borderPane.setTop(topVbox);
      borderPane.setCenter(splitPane);
      borderPane.setBottom(bottomVbox);

      final Scene scene = new Scene(borderPane, SCENE_WIDTH, SCENE_HEIGHT);
      scene.getStylesheets().add(new FileBrowserCss().getUrl());
      scene.setOnKeyPressed(new KeyEventHandler());

      stage.setTitle(getTitle());
      stage.setScene(scene);
      stage.initOwner(ownerWindow);
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.setUserData(Boolean.FALSE);
      stage.setOnCloseRequest(event -> {
         stage.setUserData(Boolean.TRUE);
         fileChooserCallback.fileChosen(Optional.empty());
      });
      stage.show();

      updatePlaces();
      currentDirectory = getInitialDirectory() == null
            ? new File(".")
            : initialDirectory.getValue();

      setCurrentView(viewTypeProperty().getValue());
      updateWatchDirectory();
   }

   /**
    * Creates a SplitPane where the left child is the placesView
    * and the right child is an implementation of {@link FilesView}
    */
   private SplitPane createSplitPane() {
      iconsFilesView = createIconsFilesView();
      listFilesView = createListFilesView();
      listFilesWithPreviewView = createListFilesWithPreviewView();

      placesView = createPlacesView();

      final SplitPane pane = new SplitPane();
      pane.setId("splitPane");
      pane.setDividerPosition(0, .15);
      return pane;
   }

   private IconsFilesView createIconsFilesView() {
      final IconsFilesView view = new IconsFilesView(stage, previewHandlers, icons,
            new FilesViewCallbackImpl());
      view.setOnKeyPressed(new KeyEventHandler());
      return view;
   }

   private ListFilesView createListFilesView() {
      final ListFilesView view = new ListFilesView(stage, previewHandlers,
            icons, new FilesViewCallbackImpl());
      view.setOnKeyPressed(new KeyEventHandler());
      return view;
   }

   private ListFilesWithPreviewView createListFilesWithPreviewView() {
      final ListFilesWithPreviewView view
            = new ListFilesWithPreviewView(stage, previewHandlers, icons,
               new FilesViewCallbackImpl());
      view.setOnKeyPressed(new KeyEventHandler());
      return view;
   }

   /**
    * Create a VBox to hold the top toolbar and optionally the save filename bar.
    */
   private VBox createTopVBox() {
      final ToolBar toolBar = createToolbar();
      final VBox vBox = new VBox();
      vBox.setId("topVbox");
      if (saveMode) {
         vBox.getChildren().add(createFileNameBar());
      }
      vBox.getChildren().add(toolBar);

      return vBox;
   }

   /**
    * Create a VBox to hold the bottom buttons and file extensions dropdown.
    */
   private VBox createBottomVBox() {
      final ButtonBar buttonBar = createButtonBar();
      final Pane extensionsPane = createExtensionsPane();
      final VBox vBox = new VBox();
      vBox.getChildren().addAll(extensionsPane, buttonBar);
      return vBox;
   }

   private Node createFileNameBar() {
      final Label label = new Label(resourceBundle.getString("namelabel.text"));
      label.setId("nameLabel");

      fileNameField = new TextField();
      fileNameField.setId("nameField");
      fileNameField.setText(getInitialFileName());
      fileNameField.setPrefWidth(300);

      currentSelection.addListener((observable, oldValue, newValue) -> {
         if (newValue != null && newValue.isFile()) {
            fileNameField.setText(newValue.getName());
         }
      });

      final ToolBar filenameBar = new ToolBar();
      filenameBar.setId("filenameBar");
      filenameBar.getStyleClass().add("filenamebar");
      filenameBar.getItems().addAll(label, fileNameField);

      return filenameBar;
   }

   private ToolBar createToolbar() {
      backButton = createBackButton();
      final VBox breadCrumbHBox = createBreadCrumbBar();
      viewListButton = createViewListButton();
      if (!hideFiles) {
         viewListWithPreviewButton = createViewListWithPreviewButton();
      }
      viewIconsButton = createViewIconsButton();

      final ToolBar toolBar = new ToolBar();
      toolBar.setId("Toolbar");
      toolBar.getStyleClass().add("toolbar");
      final ObservableList<Node> items = toolBar.getItems();
      items.add(viewListButton);
      if(!hideFiles) {
         items.add(viewListWithPreviewButton);
      }
      items.add(viewIconsButton);
      items.add(new Separator());
      items.add(backButton);
      items.add(breadCrumbHBox);

      return toolBar;
   }

   private Button createBackButton() {
      backButton = new Button();
      backButton.setId("backButton");
      backButton.getStyleClass().add("toolbarbutton");
      backButton.setGraphic(new ImageView(icons.getIcon(IconsImpl.BACK_ARROW_24)));
      backButton.setTooltip(new Tooltip(resourceBundle.getString("backbutton.tooltip")));
      backButton.setDisable(true);
      backButton.setFocusTraversable(false);
      backButton.setOnAction(event -> {
         if (directoryStack.isEmpty()) {
            return;
         }

         currentDirectory = directoryStack.pop();
         backButton.setDisable(directoryStack.isEmpty());

         updateFiles(currentDirectory);
      });

      return backButton;
   }

   /**
    * Create a VBox that hold the BredCrumbBar. The BreadCrumbBar will hold
    * the currently displayed directory path and can be used for navigation
    * to parent directories.
    */
   private VBox createBreadCrumbBar() {
      breadCrumbBar = new BreadCrumbBar<>();
      breadCrumbBar.setId("dirsBreadCrumbBar");
      // setting focusTraversable to false is not preventing the breadCrumbBar
      // from getting focus. For now, I do not want any items in the toolbar
      // to get focus. This may need to change for accessibility/usability.
      breadCrumbBar.setCrumbFactory(param -> {
         final Button button = new BreadCrumbBarSkin.BreadCrumbButton(getBreadCrumbButtonText(param));
         button.setFocusTraversable(false);
         button.focusedProperty().addListener((observable, oldValue, newValue) -> {
            currentView.getNode().requestFocus();
         });
         return button;
      });
      breadCrumbBar.setOnCrumbAction(event -> {
         final File selected = event.getSelectedCrumb().getValue();
         if (pathsEqual(selected, currentDirectory)) {
            // Ignore clicks on the current dir.
            return;
         }

         changeDirectory(selected, false);
      });

      final VBox vBox = new VBox();
      vBox.setAlignment(Pos.CENTER_LEFT);
      vBox.setFocusTraversable(false);
      vBox.getChildren().add(breadCrumbBar);

      return vBox;
   }

   private String getBreadCrumbButtonText(final TreeItem<File> param) {
      final String text = param.getValue() != null
            ? "".equals(param.getValue().getName())
               ? FileSystemView.getFileSystemView().getSystemDisplayName(param.getValue())
               : param.getValue().getName()
            : "";
      return "/".equals(text) ? "" : text;
   }

   /**
    * Determines if 2 File instances point to the same file/directory
    */
   private boolean pathsEqual(final File p1, final File p2) {
      try {
         return p1.getCanonicalPath().compareTo(p2.getCanonicalPath()) == 0;
      } catch (IOException e) {
         logger.log(Level.SEVERE, "Error comparing paths", e);
         return false;
      }
   }

   private ToggleButton createViewListButton() {
      final ToggleButton viewButton = new ToggleButton();
      viewButton.setId("viewListButton");
      viewButton.getStyleClass().add("toolbartogglebutton");
      viewButton.setGraphic(new ImageView(icons.getIcon(IconsImpl.LIST_VIEW_24)));
      viewButton.setTooltip(new Tooltip(resourceBundle.getString("listview.tooltip")));
      viewButton.setFocusTraversable(false);
      viewButton.setSelected(true);
      viewButton.selectedProperty().addListener((observable, oldValue, selected) -> {
         if (!selected) {
            return;
         }

         setCurrentView(ViewType.List);
      });

      return viewButton;
   }

   private ToggleButton createViewListWithPreviewButton() {
      final ToggleButton viewButton = new ToggleButton();
      viewButton.setId("viewListWithPreviewButton");
      viewButton.getStyleClass().add("toolbartogglebutton");
      viewButton.setGraphic(new ImageView(icons.getIcon(IconsImpl.LIST_WITH_PREVIEW_VIEW_24)));
      viewButton.setTooltip(new Tooltip(resourceBundle.getString("listwithpreviewview.tooltip")));
      viewButton.setFocusTraversable(false);
      viewButton.setSelected(false);
      viewButton.selectedProperty().addListener((observable, oldValue, selected) -> {
         if (!selected) {
            return;
         }

         setCurrentView(ViewType.ListWithPreview);
      });

      return viewButton;
   }

   private ToggleButton createViewIconsButton() {
      final ToggleButton viewButton = new ToggleButton();
      viewButton.setId("viewIconsButton");
      viewButton.getStyleClass().add("toolbartogglebutton");
      viewButton.setGraphic(new ImageView(icons.getIcon(IconsImpl.ICON_VIEW_24)));
      viewButton.setTooltip(new Tooltip(resourceBundle.getString("iconview.tooltip")));
      viewButton.setFocusTraversable(false);
      viewButton.selectedProperty().addListener((observable, oldValue, selected) -> {
         if (!selected) {
            return;
         }

         setCurrentView(ViewType.Icon);
      });

      return viewButton;
   }

   private TableView<DirectoryListItem> createPlacesView() {
      final TableView<DirectoryListItem> view = new TableView<>();
      view.setId("placesView");

      final TableColumn<DirectoryListItem, DirectoryListItem> placesColumn
            = new TableColumn<>(resourceBundle.getString("placeslist.text"));
      placesColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
      placesColumn.setCellFactory(new DirListNameColumnCellFactory(true));
      placesColumn.prefWidthProperty().bind(view.widthProperty());
      placesColumn.setSortable(false);

      view.getColumns().addAll(placesColumn);
      view.setOnMouseClicked(event -> {
         if (view.getSelectionModel().getSelectedItem() == null) {
            return;
         }

         final File currentDir = view.getSelectionModel().getSelectedItem().getFile();
         changeDirectory(currentDir);

         if (removeFavoriteButton != null) {
            removeFavoriteButton.setDisable(!favoriteDirs.contains(currentDir));
         }
      });
      view.setOnKeyPressed(new KeyEventHandler());

      return view;
   }

   /**
    * Update the Places View with drives, home dir and favorites.
    */
   private void updatePlaces() {
      final LinkedList<DirectoryListItem> places = new LinkedList<>();

      final List<Place> defaultPlaces = new Places().getDefaultPlaces();
      defaultPlaces.forEach(place -> places.add(new DirectoryListItem(place.getPath(), toIcon(place))));

      final String homeDirStr = System.getProperty("user.home");
      if (homeDirStr != null) {
         places.add(new DirectoryListItem(new File(homeDirStr), icons.getIcon(IconsImpl.USER_HOME_64)));
      }

      favoriteDirs.forEach(file -> places.add(new DirectoryListItem(file, icons.getIcon(IconsImpl.FOLDER_64))));

      if (addFavoriteButton != null) {
         addFavoriteButton.setDisable(true);
         removeFavoriteButton.setDisable(true);
      }

      placesView.getItems().setAll(places);
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

   private ButtonBar createButtonBar() {
      final List<Button> buttons = new LinkedList<>();

      doneButton = createDoneButton();
      buttons.add(doneButton);
      buttons.add(createCancelButton());

      final Optional<Button> helpButton = createHelpButton();
      if (helpButton.isPresent()) {
         buttons.add(helpButton.get());
      }

      final ButtonBar buttonBar = new ButtonBar();
      buttonBar.setId("buttonBar");
      buttonBar.getStyleClass().add("buttonbar");
      buttonBar.getButtons().addAll(buttons);

      return buttonBar;
   }

   private Button createDoneButton() {
      final String text = getDoneButtonText();
      final Button button = new Button(text);
      ButtonBar.setButtonData(button, ButtonBar.ButtonData.OK_DONE);
      button.setId("doneButton");
      button.setDisable(!saveMode || getInitialFileName() == null);
      button.setOnAction(saveMode ? new SaveDoneEventHandler() : new BrowseDoneEventHandler());

      if (saveMode) {
         fileNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            button.setDisable(newValue == null || "".equals(newValue.trim()));
         });
      }

      currentSelection.addListener((observable, oldValue, newValue) ->
            button.setDisable(newValue == null || (!hideFiles && newValue.isDirectory()))
      );
      return button;
   }

   /**
    * EventHandler to invoke when Done Button is pressed and in browse mode.
    */
   private class BrowseDoneEventHandler implements EventHandler<ActionEvent> {
      @Override
      public void handle(ActionEvent event) {
         if (currentSelection.getValue() == null) {
            return;
         }

         fileChooserCallback.fileChosen(Optional.of(currentSelection.getValue()));
         stage.close();
      }
   }

   /**
    * EventHandler to invoke when Done Button is pressed and in file save mode.
    */
   private class SaveDoneEventHandler implements EventHandler<ActionEvent> {
      @Override
      public void handle(ActionEvent event) {
         final String filename = fileNameField.getText();
         if (filename == null || "".equals(filename.trim())) {
            return;
         }

         fileChooserCallback.fileChosen(Optional.of(new File(currentDirectory, filename)));
         stage.close();
      }
   }

   private String getDoneButtonText() {
      return saveMode ?
            resourceBundle.getString("savebutton.text") :
            resourceBundle.getString("openbutton.text");
   }

   private Button createCancelButton() {
      final Button cancelButton = new Button(resourceBundle.getString("cancelbutton.text"));
      cancelButton.setId("cancelButton");
      ButtonBar.setButtonData(cancelButton, ButtonBar.ButtonData.CANCEL_CLOSE);
      cancelButton.setOnAction(event -> {
         fileChooserCallback.fileChosen(Optional.empty());
         stage.close();
      });
      return cancelButton;
   }

   private Optional<Button> createHelpButton() {
      if (helpCallback == null) {
         return Optional.empty();
      }

      final Button helpButton = new Button(resourceBundle.getString("helpbutton.text"));
      helpButton.setId("helpButton");
      ButtonBar.setButtonData(helpButton, ButtonBar.ButtonData.HELP);
      helpButton.setOnAction(event -> helpCallback.invoke());
      return Optional.of(helpButton);
   }

   /**
    * Create the Pane that contains the Extensions ComboBox and optionally
    * the New Folder button.
    */
   private Pane createExtensionsPane() {
      final BorderPane borderPane = new BorderPane();
      borderPane.setId("extensionsBorderPane");
      borderPane.getStyleClass().add("extensionspane");

      if (!hideFiles) {
         final List<FileChooser.ExtensionFilter> allFiles = new ArrayList<>(1);
         allFiles.add(new FileChooser.ExtensionFilter(resourceBundle.getString("filterdropdown.allfiles"), "*.*"));

         extensionsComboBox = new ComboBox<>();
         extensionsComboBox.getItems().addAll(extensionFilters.isEmpty()
            ? allFiles
            : extensionFilters);
         extensionsComboBox.setCellFactory(new ExtensionsCellFactory());
         extensionsComboBox.setButtonCell(new ExtensionsCell());
         extensionsComboBox.setOnAction(v -> {
            setSelectedExtensionFilter(extensionsComboBox.getValue());
            updateFiles(currentDirectory, false);
         });
         extensionsComboBox.setEditable(true);
         extensionsComboBox.setConverter(new StringConverterWithFormat<FileChooser.ExtensionFilter>() {
            @Override
            public String toString(FileChooser.ExtensionFilter extensionFilter) {
               return extensionFilter.getDescription();
            }

            @Override
            public FileChooser.ExtensionFilter fromString(String string) {
               return new FileChooser.ExtensionFilter(string, string);
            }
         });

         updateSelectedExtensionFilter(extensionsComboBox);

         borderPane.setRight(extensionsComboBox);
      }

      borderPane.setLeft(createExtensionsLeftButtonsHbox());

      return borderPane;
   }

   /**
    * Create a HBox that optionally includes Add/Remove Favorite buttons.
    */
   private HBox createExtensionsLeftButtonsHbox() {
      final HBox buttonsHbox = new HBox();
      buttonsHbox.setId("extensionsLeftButtonsHBox");
      buttonsHbox.getStyleClass().add("extensionspane-buttonshbox");

      if (shouldAddFavoritesButtons()) {
         addFavoriteButton = createAddFavoriteButton();
         removeFavoriteButton = createRemoveFavoriteButton();
         buttonsHbox.getChildren().addAll(addFavoriteButton, removeFavoriteButton);
      }

      if (saveMode || hideFiles) {
         // Include the New FolderButton
         buttonsHbox.getChildren().add(createNewFolderButton());
      }

      return buttonsHbox;
   }

   private boolean shouldAddFavoritesButtons() {
      return addFavorite != null && removeFavorite != null;
   }

   private Button createAddFavoriteButton() {
      final Button button = new Button(resourceBundle.getString("addfavoritebutton.txt"));
      button.setId("addFavoriteButton");
      button.setOnAction(event -> {
         try {
            favoriteDirs.add(currentSelection.get().getCanonicalFile());
         } catch (IOException e) {
            logger.log(Level.SEVERE, "Error canonicalizing file", e);
         }
         updatePlaces();
      });
      button.setDisable(true);
      return button;
   }

   private Button createRemoveFavoriteButton() {
      final Button button = new Button(resourceBundle.getString("removefavoritebutton.txt"));
      button.setId("removeFavoriteButton");
      button.setOnAction(new NewFolderAction());
      button.setOnAction(event -> {
         final DirectoryListItem item = placesView.getSelectionModel().getSelectedItem();
         if (item == null) {
            return;
         }

         favoriteDirs.remove(item.getFile());
         updatePlaces();
      });
      button.setDisable(true);
      return button;
   }

   private Button createNewFolderButton() {
      final Button newFolderButton = new Button(resourceBundle.getString("newfolderbutton.text"));
      newFolderButton.setId("newFolderButton");
      newFolderButton.setOnAction(new NewFolderAction());
      return newFolderButton;
   }

   /**
    * Sets the selected item within the Extensions ComboBox
    */
   private void updateSelectedExtensionFilter(final ComboBox<FileChooser.ExtensionFilter> extensions) {
      if (selectedExtensionFilter == null || selectedExtensionFilter.getValue() == null) {
         setSelectedExtensionFilter(extensions.getItems().get(0));
      }

      final List<String> selectedExtensions = selectedExtensionFilter.getValue().getExtensions();
      final Optional<FileChooser.ExtensionFilter> extensionFilter
            = extensionFilters.stream()
               .filter(filter -> filter.getExtensions().containsAll(selectedExtensions))
               .findFirst();
      if (extensionFilter.isPresent()) {
         setSelectedExtensionFilter(extensionFilter.get());
         extensions.setValue(extensionFilter.get());
      } else {
         setSelectedExtensionFilter(extensions.getItems().get(0));
         extensions.setValue(extensions.getItems().get(0));
      }
   }

   /**
    * Cell Factory for file extensions ComboBox.
    */
   private static class ExtensionsCellFactory implements Callback<ListView<FileChooser.ExtensionFilter>, ListCell<FileChooser.ExtensionFilter>> {
      @Override
      public ListCell<FileChooser.ExtensionFilter> call(ListView<FileChooser.ExtensionFilter> param) {
         return new ExtensionsCell();
      }
   }

   /**
    * ButtonCell for file extensions ComboBox - this sets the text for the selected
    * item.
    */
   private static class ExtensionsCell extends ListCell<FileChooser.ExtensionFilter> {
      @Override
      protected void updateItem(FileChooser.ExtensionFilter item, boolean empty) {
         super.updateItem(item, empty);

         if (item == null) {
            setText(null);
         } else {
            setText(item.getDescription());
         }
      }
   }

   /**
    * Action for displaying dialog for creating a new folder.
    */
   private class NewFolderAction implements EventHandler<ActionEvent> {
      @Override
      public void handle(ActionEvent event) {
         final TextInputDialog dialog = new TextInputDialog();
         dialog.setTitle(resourceBundle.getString("createfolder.title"));
         dialog.setHeaderText(null);
         dialog.setContentText(resourceBundle.getString("createfolder.text"));
         Optional<String> result = dialog.showAndWait();

         result.ifPresent(name -> {
            final File newDir = new File(currentDirectory, name);
            newDir.mkdir();

            // Refresh
            updateFiles(currentDirectory);
         });
      }
   }

   /**
    * Update the SplitPane to include the passed in {@link FilesView}
    */
   private void setCurrentView(final ViewType view) {
      if (ViewType.List.equals(view)
            || ViewType.ListWithPreview.equals(view) && hideFiles) {

         setListView();
         return;
      }

      if (ViewType.ListWithPreview.equals(view)) {
         setListWithPreview();
         return;
      }

      setIconsView();
   }

   private void setListView() {
      viewTypeProperty.set(ViewType.List);

      setCurrentView(listFilesView);
      viewListButton.setSelected(true);
      viewIconsButton.setSelected(false);
      if (viewListWithPreviewButton != null) {
         viewListWithPreviewButton.setSelected(false);
      }
   }

   private void setListWithPreview() {
      viewTypeProperty.set(ViewType.ListWithPreview);

      setCurrentView(listFilesWithPreviewView);
      viewListButton.setSelected(false);
      viewListWithPreviewButton.setSelected(true);
      viewIconsButton.setSelected(false);
   }

   private void setIconsView() {
      viewTypeProperty.set(ViewType.Icon);

      setCurrentView(iconsFilesView);
      viewListButton.setSelected(false);
      if (viewListWithPreviewButton != null) {
         viewListWithPreviewButton.setSelected(false);
      }
      viewIconsButton.setSelected(true);
   }

   private void setCurrentView(final FilesView filesView) {
      currentView = filesView;

      splitPane.getItems().setAll(placesView, currentView.getNode());

      updateFiles(currentDirectory);
   }

   /**
    * Update the view to contain the files located within the current directory.
    * The BreadCrumbBar is not updated.
    */
   private void updateFiles() {
      updateFiles(currentDirectory, false);
   }

   /**
    * Update the view to contain the files located within the passed in
    * directory. The BreadCrumbBar will be updated to reflect the passed in dir.
    */
   private void updateFiles(final File directory) {
      updateFiles(directory, true);
   }

   /**
    * Update the view to contain the files located within the passed in
    * directory. Optionally update the BreadCrumbBar.
    */
   private void updateFiles(final File directory,
                            final boolean updateBreadCrumbBar) {
      currentView.setFiles(getFiles(directory));

      if (updateBreadCrumbBar) {
         updateDirBreadCrumbBar(currentDirectory);
      }
   }

   /**
    * Update the view to reflect the files in the passed in directory.
    */
   private void changeDirectory(final File directory) {
      changeDirectory(directory, true);
   }

   /**
    * Update the view to reflect the files in the passed in directory.
    */
   private void changeDirectory(final File directory,
                                final boolean updateBreadCrumbBar) {
      if (currentDirectory != null) {
         directoryStack.push(currentDirectory);
         backButton.setDisable(false);
      }
      currentDirectory = directory;
      currentSelection.setValue(null);

      updateFiles(currentDirectory, updateBreadCrumbBar);

      updateWatchDirectory();
   }

   /**
    * Update the BreadCrumbBar based on the passed in directory.
    */
   private void updateDirBreadCrumbBar(final File directory) {
      try {
         File currentDir = directory.getCanonicalFile();
         TreeItem<File> lastItem = null;
         TreeItem<File> selectedItem = null;
         while (currentDir != null) {
            final TreeItem<File> item = new TreeItem<>();
            item.setValue(currentDir);
            item.getChildren().add(lastItem);

            lastItem = item;

            if (selectedItem == null) {
               selectedItem = lastItem;
            }

            currentDir = currentDir.getParentFile();
         }

         breadCrumbBar.setSelectedCrumb(selectedItem);
      } catch (IOException e) {
         logger.log(Level.SEVERE, "Error building BreadCrumbBar", e);
      }
   }

   /**
    * Retrieve a Stream of File from the contents of the passed in directory.
    * Depending on the mode of operation, some of the directory contents
    * may be filtered out.
    */
   private Stream<File> getFiles(final File directory) {
      if (!directory.isDirectory()) {
         return Stream.empty();
      }

      final FileFilter filter = getFileFilter();
      final File[] files = directory.listFiles(filter);
      return files == null
            ? Stream.empty()
            : Arrays.stream(files).filter(new GetFilesPredicate());
   }

   /**
    * Build a FileFilter to filter based on the selected value in
    * the extensionsComboBox.
    */
   private WildcardFileFilter getFileFilter() {
      final List<String> extensionFilter = extensionsComboBox == null || extensionsComboBox.getValue() == null
            ? Collections.emptyList()
            : extensionsComboBox.getValue().getExtensions();
      return new DirOrWildcardFilter(extensionFilter, IOCase.SYSTEM);
   }

   /**
    * Predicate to determine if a File should be included in the file list.
    */
   private class GetFilesPredicate implements Predicate<File> {
      @Override
      public boolean test(final File file) {
         final boolean filterHidden = !showHiddenFiles();
         return !(filterHidden && file.isHidden()) && (!hideFiles || file.isDirectory());
      }
   }

   /**
    * Callbacks from the {@link FilesView} implementations back into the
    * {@link FileChooserFx}.
    */
   private class FilesViewCallbackImpl implements FilesViewCallback {
      /**
       * Request that the directory is changed, and update the
       * view to include the directory contents.
       */
      @Override
      public void requestChangeDirectory(final File directory) {
         changeDirectory(directory);
      }

      /**
       * Get a Stream of File for the the passed in directory.
       */
      @Override
      public Stream<File> getFileStream(final File directory) {
         return getFiles(directory);
      }

      /**
       * Update the currently selected file.
       */
      @Override
      public void setCurrentSelection(final File file) {
         currentSelection.setValue(file);

         if (addFavoriteButton != null) {
            addFavoriteButton.setDisable(!shouldEnableAddFavBtn(file));
         }
      }

      /**
       * Determine if the Add Favorite button should be enabled based
       * on the currently selected File.
       */
      private boolean shouldEnableAddFavBtn(final File file) {
         if (addFavoriteButton == null || file == null || !file.isDirectory()) {
            return false;
         }

         try {
            final File canonicalized = file.getCanonicalFile();
            return !favoriteDirs.contains(canonicalized);
         } catch (IOException e) {
            logger.log(Level.WARNING, "error canonicalizing file", e);
            return false;
         }
      }

      @Override
      public File getCurrentSelection() {
         return currentSelection.get();
      }

      @Override
      public void fireDoneButton() {
         doneButton.fire();
      }

      /**
       * Update all the files in the view.
       */
      @Override
      public void updateFiles() {
         FileChooserFxImpl.this.updateFiles();
      }

      @Override
      public ObjectProperty<OrderBy> orderByProperty() {
         return FileChooserFxImpl.this.orderByProperty();
      }

      @Override
      public ObjectProperty<OrderDirection> orderDirectionProperty() {
         return FileChooserFxImpl.this.orderDirectionProperty();
      }
   }

   /**
    * Setup WatchService to watch for file system changes.
    */
   private void updateWatchDirectory() {
      try {
         if (watcher == null) {
            watcher = FileSystems.getDefault().newWatchService();

            final Thread watcherThread = new Thread(new DirectoryWatcherTask(stage,
                  watcher, new FilesViewCallbackImpl()));
            watcherThread.setDaemon(true);
            watcherThread.start();
         }

         if (watchKey != null) {
            watchKey.cancel();
         }

         watchKey = currentDirectory.toPath().register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
               StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
      } catch (IOException e) {
         logger.log(Level.WARNING, "Error setting up WatchService", e);
      }
   }

   /**
    * EventHandler to watch keyboard for ESC key presses.
    */
   private class KeyEventHandler implements EventHandler<KeyEvent> {
      @Override
      public void handle(KeyEvent event) {
         if (event.getCode() == KeyCode.ESCAPE) {
            stage.close();
            fileChooserCallback.fileChosen(Optional.empty());
            event.consume();
         }
      }
   }
}
