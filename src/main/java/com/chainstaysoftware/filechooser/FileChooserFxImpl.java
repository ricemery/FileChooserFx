package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.icons.IconsImpl;
import com.chainstaysoftware.filechooser.preview.PreviewPane;
import impl.org.controlsfx.skin.BreadCrumbBarSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
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
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
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
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.BreadCrumbBar;
import org.controlsfx.control.spreadsheet.StringConverterWithFormat;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FileChooserFxImpl implements FileChooserFx {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.FileChooserFxImpl");

   private static final int SCENE_WIDTH = 800;
   private static final int SCENE_HEIGHT = 600;
   private static final double PLACES_DIVIDER_POSITION = 0.25;
   private static final double PREVIEW_DIVIDER_POSITION = 0.25;

   private final DoubleProperty heightProperty = new SimpleDoubleProperty(SCENE_HEIGHT);
   private final DoubleProperty widthProperty = new SimpleDoubleProperty(SCENE_WIDTH);
   private final ResourceBundle resourceBundle = ResourceBundle.getBundle("filechooser");
   private final ObservableList<FileChooser.ExtensionFilter> extensionFilters =
         FXCollections.observableArrayList();
   private final ObservableMap<String, Class<? extends PreviewPane>> previewHandlers = FXCollections.observableHashMap();
   private final ObservableList<File> favoriteDirs = FXCollections.observableArrayList();
   private final Deque<File> directoryStack = new LinkedList<>();
   private final ObjectProperty<File> currentSelection = new SimpleObjectProperty<>();
   private final DirectoryWatchingService dirWatchingService = new DirectoryWatchingService(new FilesViewCallbackImpl());
   private final BooleanProperty hideFiles = new SimpleBooleanProperty(this, "shouldHideFiles", false);

   private double placesDivider = PLACES_DIVIDER_POSITION;
   private double previewDivider = PREVIEW_DIVIDER_POSITION;
   private StringProperty title;
   private ObjectProperty<File> initialDirectory;
   private ObjectProperty<String> initialFileName;
   private ObjectProperty<FileChooser.ExtensionFilter> selectedExtensionFilter;
   private ObjectProperty<ViewType> viewTypeProperty;
   private BooleanProperty showHiddenFiles;
   private ObjectProperty<OrderBy> orderByProperty;
   private ObjectProperty<OrderDirection> orderDirectionProperty;
   private BooleanProperty showMountPointsProperty;
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
   private PlacesView placesView;
   private boolean saveMode;
   private ToggleButton viewIconsButton;
   private ToggleButton viewListButton;
   private ToggleButton viewListWithPreviewButton;
   private TextField fileNameField;
   private ComboBox<FileChooser.ExtensionFilter> extensionsComboBox;
   private Button addFavoriteButton;
   private Button removeFavoriteButton;
   private Button doneButton;
   private Icons icons = new IconsImpl();

   /**
    * Property representing the height of the FileChooser.
    */
   @Override
   public ReadOnlyDoubleProperty heightProperty() {
      return heightProperty;
   }

   /**
    * Get the height of the FileChooser.
    */
   @Override
   public double getHeight() {
      return heightProperty.doubleValue();
   }

   /**
    * Set the height of the FileChooser.
    */
   @Override
   public void setHeight(final double height) {
      heightProperty.setValue(height);
   }

   /**
    * Property representing the width of the FileChooser.
    */
   @Override
   public ReadOnlyDoubleProperty widthProperty() {
      return widthProperty;
   }

   /**
    * Get the width of the FileChooser.
    */
   @Override
   public double getWidth() {
      return widthProperty.doubleValue();
   }

   /**
    * Set the width of the FileChooser.
    */
   @Override
   public void setWidth(final double width) {
      widthProperty.setValue(width);
   }

   /**
    * Sets the position of the dividers.
    *
    * @param placesDivider  the position of the divider that separates the places view from
    *                       the file/directories, between 0.0 and 1.0 (inclusive).
    * @param previewDivider the position of the divider that separates the preview pane from
    *                       the list of files/directories, between 0.0 and 1.0 (inclusion).
    *                       The value is relative to the placesDivider. 0.0 indicates right ontop
    *                       of the places divider. And, 1.0 indicates thr far right of the window.
    */
   @Override
   public void setDividerPositions(final double placesDivider, final double previewDivider) {
      this.placesDivider = placesDivider;
      this.previewDivider = previewDivider;
   }

   /**
    * Returns the position of the dividers. The returned array contain 2 elements.
    * The first element is the position of the places divider. The second element
    * is the position of the divider between the file list and the preview pane.
    */
   @Override
   public double[] getDividerPositions() {
      if (splitPane != null) {
         final double placesDivider = splitPane.getDividerPositions()[0];
         final double previewDivider = listFilesWithPreviewView.getDividerPosition();
         return new double[] {placesDivider, previewDivider};
      }

      return new double[] {placesDivider, previewDivider};
   }

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

   @Override
   public void setShouldHideFiles(boolean value) {
      hideFiles.setValue(value);
   }

   @Override
   public boolean shouldHideFiles() {
      return hideFiles.get();
   }

   @Override
   public BooleanProperty shouldHideFilesProperty() {
      return hideFiles;
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

   /**
    * Disable/enable the display of mount points on Linux/OSX.
    */
   @Override
   public void setShowMountPoints(final boolean value) {
      showMountPointsProperty().setValue(value);
   }

   /**
    * Showing/not showing Linux/OSX mount points.
    */
   @Override
   public boolean showMountPoints() {
      return showMountPointsProperty().getValue();
   }

   /**
    * Disable/enable the display of mount points on Linux/OSX.
    */
   @Override
   public BooleanProperty showMountPointsProperty() {
      if (showMountPointsProperty == null) {
         showMountPointsProperty = new SimpleBooleanProperty(this, "showMountPoints", false);
      }

      return showMountPointsProperty;
   }

   @Override
   public ObservableList<File> favoriteDirsProperty() {
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

   @Override
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
      this.hideFiles.setValue(hideFiles);
      showDialog(ownerWindow, fileChooserCallback);
   }

   @Override
   public void showSaveDialog(final Window ownerWindow,
                              final FileChooserCallback fileChooserCallback) {
      saveMode = true;
      hideFiles.set(false);
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

      final Scene scene = new Scene(borderPane, widthProperty.doubleValue(), heightProperty.doubleValue());
      scene.getStylesheets().add(new FileBrowserCss().getUrl());
      scene.setOnKeyPressed(new KeyEventHandler());
      scene.heightProperty().addListener((observable, oldValue, newValue) -> heightProperty.setValue(newValue));
      scene.widthProperty().addListener((observable, oldValue, newValue) -> widthProperty.setValue(newValue));

      stage.setTitle(getTitle());
      stage.setScene(scene);
      stage.initOwner(ownerWindow);
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.setOnShown(event -> updateWatchDirectory());
      stage.setOnHidden(event -> dirWatchingService.cancel());
      stage.setOnCloseRequest(event -> fileChooserCallback.fileChosen(Optional.empty()));
      stage.show();

      placesView.updatePlaces();
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
      pane.setDividerPosition(0, placesDivider);

      placesView.toPane().prefHeightProperty().bind(pane.heightProperty());

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
               previewDivider, new FilesViewCallbackImpl());
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
      fileNameField.setPrefWidth(400);

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
      if (!hideFiles.get()) {
         viewListWithPreviewButton = createViewListWithPreviewButton();
      }
      viewIconsButton = createViewIconsButton();

      final ToolBar toolBar = new ToolBar();
      toolBar.setId("Toolbar");
      toolBar.getStyleClass().add("toolbar");
      final ObservableList<Node> items = toolBar.getItems();
      items.add(viewListButton);
      if(!hideFiles.get()) {
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
         button.focusedProperty().addListener((observable, oldValue, newValue) -> currentView.getNode().requestFocus());
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

   private PlacesView createPlacesView() {
      final PlacesView view = new PlacesView(new FilesViewCallbackImpl(), icons);
      view.toPane().setOnKeyPressed(new KeyEventHandler());
      return view;
   }

   private ButtonBar createButtonBar() {
      final List<Button> buttons = new LinkedList<>();

      doneButton = createDoneButton();
      buttons.add(doneButton);
      buttons.add(createCancelButton());

      final Optional<Button> helpButton = createHelpButton();
      helpButton.ifPresent(buttons::add);

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
         fileNameField.textProperty().addListener((observable, oldValue, newValue)
            -> button.setDisable(newValue == null || "".equals(newValue.trim())));
      }

      currentSelection.addListener((observable, oldValue, newValue) -> {
            if (saveMode)
               button.setDisable(StringUtils.isEmpty(fileNameField.getText()));
            else
               button.setDisable(newValue == null || (!hideFiles.get() && newValue.isDirectory()));
         }
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

      if (!hideFiles.get()) {
         final List<FileChooser.ExtensionFilter> allFiles = new ArrayList<>(1);
         allFiles.add(new FileChooser.ExtensionFilter(resourceBundle.getString("filterdropdown.allfiles"), "*.*"));

         extensionsComboBox = new ComboBox<>();
         extensionsComboBox.getItems().addAll(extensionFilters.isEmpty()
            ? allFiles
            : extensionFilters);
         extensionsComboBox.setCellFactory(new ExtensionsCellFactory());
         extensionsComboBox.setButtonCell(new ExtensionsCell());
         extensionsComboBox.setOnAction(v -> {
            setSelectedExtensionFilter(extensionsComboBox.getSelectionModel().getSelectedItem());
            updateFiles(currentDirectory, false);
         });
         extensionsComboBox.setEditable(true);
         extensionsComboBox.setConverter(new StringConverterWithFormat<FileChooser.ExtensionFilter>() {
            @Override
            public String toString(final FileChooser.ExtensionFilter extensionFilter) {
               return extensionFilter.getDescription();
            }

            @Override
            public FileChooser.ExtensionFilter fromString(final String string) {
               // Since only the description is serialized into the string, lookup
               // the ExtensionFilter by description from the current list of ExtensionFilter.
               return extensionFilters.stream()
                     .filter(filter -> string.equals(filter.getDescription()))
                     .findFirst()
                     .orElse(new FileChooser.ExtensionFilter(string, string));
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

      if (saveMode || hideFiles.get()) {
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
            File canonicalFile = currentSelection.get().getCanonicalFile();
            favoriteDirs.add(canonicalFile);
            if (addFavorite != null) {
               addFavorite.invoke(canonicalFile);
            }
         } catch (IOException e) {
            logger.log(Level.SEVERE, "Error canonicalizing file", e);
         }
      });
      button.setDisable(true);
      button.setTooltip(new Tooltip(resourceBundle.getString("addfavoritebutton.tooltip.txt")));
      return button;
   }

   private Button createRemoveFavoriteButton() {
      final Button button = new Button(resourceBundle.getString("removefavoritebutton.txt"));
      button.setId("removeFavoriteButton");
      button.setOnAction(event -> {
         final Optional<File> selectedFile = placesView.getSelectedItem();
         selectedFile.ifPresent(file -> {
            favoriteDirs.remove(file);
            if (removeFavorite != null) {
               removeFavorite.invoke(file);
            }
         });
      });
      button.setDisable(true);
      button.setTooltip(new Tooltip(resourceBundle.getString("removefavoritebutton.tooltip.txt")));
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
            || ViewType.ListWithPreview.equals(view) && hideFiles.get()) {

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

      splitPane.getItems().setAll(placesView.toPane(), currentView.getNode());

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
      if (updateBreadCrumbBar) {
         updateDirBreadCrumbBar(currentDirectory);
      }

      try {
         currentView.setFiles(getFilteredDirStream(directory), getNegatedFilteredDirStream(directory));
      } catch (IOException e) {
         logger.log(Level.WARNING, "Error settings files on view", e);
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
   private DirectoryStream<Path> getFilteredDirStream(final File directory) throws IOException {
      final FileFilter filter = getFileFilter();
      return Files.newDirectoryStream(directory.toPath(), entry -> filter.accept(entry.toFile()));
   }

   /**
    * Retrieve a Stream of File from the contents of the passed in directory.
    * Depending on the mode of operation, some of the directory contents
    * may be filtered out. This method returns all the files/directories that would be
    * filtered out by getFilteredDirStream.
    */
   private DirectoryStream<Path> getNegatedFilteredDirStream(final File directory) throws IOException {
      final FileFilter filter = getFileFilter();
      return Files.newDirectoryStream(directory.toPath(), entry -> !filter.accept(entry.toFile()));
   }

   /**
    * Build a FileFilter to filter based on the selected value in
    * the extensionsComboBox.
    */
   private WildcardFileFilter getFileFilter() {
      final List<String> extensionFilter = extensionsComboBox == null || extensionsComboBox.getValue() == null
         ? Collections.emptyList()
         : extensionsComboBox.getValue().getExtensions();
      return new WildcardFileFilter(extensionFilter, IOCase.SYSTEM);
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

      @Override
      public DirectoryStream<Path> getDirectoryStream(final File directory) throws IOException {
         return getFilteredDirStream(directory);
      }

      @Override
      public DirectoryStream<Path> unfilteredDirectoryStream(File directory) throws IOException {
         return getNegatedFilteredDirStream(directory);
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

      @Override
      public void disableAddFavoriteButton(final boolean disable) {
         if (addFavoriteButton != null) {
            addFavoriteButton.setDisable(disable);
         }
      }

      @Override
      public void disableRemoveFavoritieButton(final boolean disable) {
         if (removeFavoriteButton != null) {
            removeFavoriteButton.setDisable(disable);
         }
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

      @Override
      public ObservableList<File> favoriteDirsProperty() {
         return FileChooserFxImpl.this.favoriteDirsProperty();
      }

      @Override
      public BooleanProperty showMountPointsProperty() {
         return FileChooserFxImpl.this.showMountPointsProperty();
      }

      @Override
      public BooleanProperty showHiddenFilesProperty() {
         return FileChooserFxImpl.this.showHiddenFilesProperty();
      }

      @Override
      public BooleanProperty shouldHideFilesProperty() {
         return FileChooserFxImpl.this.shouldHideFilesProperty();
      }
   }

   /**
    * Setup WatchService to watch for file system changes.
    */
   private void updateWatchDirectory() {
      dirWatchingService.setDirectory(currentDirectory);

      if (currentDirectory == null) {
         dirWatchingService.cancel();
      }
      else {
         dirWatchingService.restart();
      }
   }

   private static final class DirectoryWatchingService extends Service<Void> {
      private final ObjectProperty<File> directory;
      private final FilesViewCallback callback;

      private DirectoryWatchingService(FilesViewCallback callback) {
         this.callback = callback;

         directory = new SimpleObjectProperty<>();

         setExecutor(Executors.newSingleThreadExecutor(new DaemonThreadFactory()));
      }

      public File getDirectory() {
         return directory.get();
      }

      public void setDirectory(File directory) {
         this.directory.set(directory);
      }

      @Override
      protected Task<Void> createTask() {
         File currentDirectory = getDirectory();
         logger.log(Level.FINE, "Creating watch task for " + currentDirectory);

         return new DirectoryWatcherTask(currentDirectory, callback);
      }
   }

   private static final class DaemonThreadFactory implements ThreadFactory {
      @Override
      public Thread newThread(Runnable runnable) {
         Thread thread = new Thread(runnable);
         thread.setDaemon(true);
         return thread;
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
