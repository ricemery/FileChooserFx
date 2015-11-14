package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.controlsfx.control.BreadCrumbBar;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
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

   private final ResourceBundle resourceBundle = ResourceBundle.getBundle("filechooser");
   private final ObservableList<javafx.stage.FileChooser.ExtensionFilter> extensionFilters =
         FXCollections.observableArrayList();
   private final ObservableMap<String, Class<? extends PreviewPane>> previewHandlers = FXCollections.observableHashMap();
   private final Icons icons = new Icons();
   private final Deque<File> directoryStack = new LinkedList<>();
   private final ObjectProperty<File> currentSelection = new SimpleObjectProperty<>();

   private StringProperty title;
   private ObjectProperty<File> initialDirectory;
   private ObjectProperty<String> initialFileName;
   private ObjectProperty<javafx.stage.FileChooser.ExtensionFilter> selectedExtensionFilter;
   private BooleanProperty showHiddenFiles;
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
   private Stage stage;
   private Node placesView;
   private boolean saveMode;
   private boolean hideFiles;
   private ToggleButton viewIconsButton;
   private ToggleButton viewListButton;
   private ToggleButton viewListWithPreviewButton;
   private TextField fileNameField;
   private ComboBox<FileChooser.ExtensionFilter> extensionsComboBox;
   private Button doneButton;

   @Override
   public ObservableList<javafx.stage.FileChooser.ExtensionFilter> getExtensionFilters() {
      return extensionFilters;
   }

   @Override
   public ObjectProperty<javafx.stage.FileChooser.ExtensionFilter> selectedExtensionFilterProperty() {
      if (selectedExtensionFilter == null) {
         selectedExtensionFilter =
               new SimpleObjectProperty<>(this,
                     "selectedExtensionFilter");
      }
      return selectedExtensionFilter;
   }

   @Override
   public void setSelectedExtensionFilter(javafx.stage.FileChooser.ExtensionFilter filter) {
      selectedExtensionFilterProperty().setValue(filter);
   }

   @Override
   public javafx.stage.FileChooser.ExtensionFilter getSelectedExtensionFilter() {
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
   public StringProperty titleProperty() {
      if (title == null) {
         title = new SimpleStringProperty(this, "title");
      }

      return title;
   }

   @Override
   public void setHelpCallback(final HelpCallback helpCallback) {
      this.helpCallback = helpCallback;
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
      stage.setOnCloseRequest(event -> fileChooserCallback.fileChosen(Optional.empty()));
      stage.show();

      currentDirectory = getInitialDirectory() == null
            ? new File(".")
            : initialDirectory.getValue();
      updateFiles(currentDirectory);
   }

   private SplitPane createSplitPane() {
      iconsFilesView = createIconsFilesView();
      listFilesView = createListFilesView();
      listFilesWithPreviewView = createListFilesWithPreviewView();
      currentView = listFilesView;

      placesView = createPlacesView();

      final SplitPane pane = new SplitPane();
      pane.setId("splitPane");
      pane.getItems().addAll(placesView, currentView.getNode());
      pane.setDividerPosition(0, .15);
      return pane;
   }

   private IconsFilesView createIconsFilesView() {
      final IconsFilesView view = new IconsFilesView(stage, previewHandlers);
      view.setCallback(new FilesViewCallbackImpl());
      view.setOnKeyPressed(new KeyEventHandler());
      return view;
   }

   private ListFilesView createListFilesView() {
      final ListFilesView view = new ListFilesView(stage, previewHandlers);
      view.setCallback(new FilesViewCallbackImpl());
      view.setOnKeyPressed(new KeyEventHandler());
      return view;
   }

   private ListFilesWithPreviewView createListFilesWithPreviewView() {
      final ListFilesWithPreviewView view = new ListFilesWithPreviewView(stage, previewHandlers);
      view.setCallback(new FilesViewCallbackImpl());
      view.setOnKeyPressed(new KeyEventHandler());
      return view;
   }

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
      backButton.setGraphic(new ImageView(icons.getIcon(Icons.BACK_ARROW_24)));
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

   private VBox createBreadCrumbBar() {
      breadCrumbBar = new BreadCrumbBar<>();
      breadCrumbBar.setId("dirsBreadCrumbBar");
      // setting focusTraversable to false is not preventing the breadCrumbBar
      // from getting focus. For now, I do not want any items in the toolbar
      // to get focus. This may need to change for accessibility/useability.
      breadCrumbBar.setCrumbFactory(param -> {
         final Button button = new BreadCrumbBarSkin.BreadCrumbButton(param.getValue() != null ? param.getValue().getName() : "");
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
      viewButton.setGraphic(new ImageView(icons.getIcon(Icons.LIST_VIEW_24)));
      viewButton.setTooltip(new Tooltip(resourceBundle.getString("listview.tooltip")));
      viewButton.setFocusTraversable(false);
      viewButton.setSelected(true);
      viewButton.selectedProperty().addListener((observable, oldValue, selected) -> {
         if (!selected) {
            return;
         }

         setCurrentView(listFilesView);
         viewIconsButton.setSelected(false);
         viewListWithPreviewButton.setSelected(false);
      });

      return viewButton;
   }

   private ToggleButton createViewListWithPreviewButton() {
      final ToggleButton viewButton = new ToggleButton();
      viewButton.setId("viewListWithPreviewButton");
      viewButton.getStyleClass().add("toolbartogglebutton");
      viewButton.setGraphic(new ImageView(icons.getIcon(Icons.LIST_WITH_PREVIEW_VIEW_24)));
      viewButton.setTooltip(new Tooltip(resourceBundle.getString("listwithpreviewview.tooltip")));
      viewButton.setFocusTraversable(false);
      viewButton.setSelected(false);
      viewButton.selectedProperty().addListener((observable, oldValue, selected) -> {
         if (!selected) {
            return;
         }

         setCurrentView(listFilesWithPreviewView);
         viewListButton.setSelected(false);
         viewIconsButton.setSelected(false);
      });

      return viewButton;
   }

   private ToggleButton createViewIconsButton() {
      final ToggleButton viewButton = new ToggleButton();
      viewButton.setId("viewIconsButton");
      viewButton.getStyleClass().add("toolbartogglebutton");
      viewButton.setGraphic(new ImageView(icons.getIcon(Icons.ICON_VIEW_24)));
      viewButton.setTooltip(new Tooltip(resourceBundle.getString("iconview.tooltip")));
      viewButton.setFocusTraversable(false);
      viewButton.selectedProperty().addListener((observable, oldValue, selected) -> {
         if (!selected) {
            return;
         }

         setCurrentView(iconsFilesView);
         viewListButton.setSelected(false);
         viewListWithPreviewButton.setSelected(false);
      });

      return viewButton;
   }

   private Node createPlacesView() {
      // TODO: Do a better job determining/showing the available mount points.
      final LinkedList<DirectoryListItem> places = new LinkedList<>();

      final Image driveIcon = icons.getIcon(Icons.HARDDISK_64);
      final List<File> roots = Arrays.asList(File.listRoots());
      roots.forEach(r -> places.add(new DirectoryListItem(r, driveIcon)));

      final String homeDirStr = System.getProperty("user.home");
      if (homeDirStr != null) {
         places.add(new DirectoryListItem(new File(homeDirStr), icons.getIcon(Icons.USER_HOME_64)));
      }

      final TableView<DirectoryListItem> view = new TableView<>();
      view.setId("placesView");

      final TableColumn<DirectoryListItem, DirectoryListItem> placesColumn
            = new TableColumn<>(resourceBundle.getString("placeslist.text"));
      placesColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
      placesColumn.setCellFactory(new DirListNameColumnCellFactory(false));
      placesColumn.prefWidthProperty().bind(view.widthProperty());

      view.getColumns().addAll(placesColumn);
      view.getItems().addAll(places);
      view.setOnMouseClicked(event -> changeDirectory(view.getSelectionModel().getSelectedItem().getFile()));
      view.setOnKeyPressed(new KeyEventHandler());

      return view;
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
      final Button doneButton = new Button(text);
      ButtonBar.setButtonData(doneButton, ButtonBar.ButtonData.OK_DONE);
      doneButton.setId("doneButton");
      doneButton.setDisable(!saveMode || getInitialFileName() == null);
      doneButton.setOnAction(saveMode ? new SaveDoneEventHandler() : new BrowseDoneEventHandler());

      if (saveMode) {
         fileNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            doneButton.setDisable(newValue == null || "".equals(newValue.trim()));
         });
      }

      currentSelection.addListener((observable, oldValue, newValue) ->
            doneButton.setDisable(newValue == null || (!hideFiles && newValue.isDirectory()))
      );
      return doneButton;
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
      cancelButton.setOnAction(event -> stage.close());
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
         extensionsComboBox.setButtonCell(new ExtensionsButtonCell());
         extensionsComboBox.setOnAction(v -> {
            setSelectedExtensionFilter(extensionsComboBox.getValue());
            updateFiles(currentDirectory, false);
         });

         updateSelectedExtensionFilter(extensionsComboBox);

         borderPane.setRight(extensionsComboBox);
      }

      if (saveMode || hideFiles) {
         final Button newFolderButton = new Button(resourceBundle.getString("newfolderbutton.text"));
         newFolderButton.setId("newFolderButton");
         newFolderButton.setOnAction(new NewFolderAction());
         borderPane.setLeft(newFolderButton);
      }

      return borderPane;
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
         return new ListCell<FileChooser.ExtensionFilter>(){
            @Override
            protected void updateItem(FileChooser.ExtensionFilter item, boolean empty) {
               super.updateItem(item, empty);

               if (item == null) {
                  setText(null);
               } else {
                  setText(item.getDescription());
               }
            }
         };
      }
   }

   /**
    * ButtonCell for file extensions ComboBox - this sets the text for the selected
    * item.
    */
   private static class ExtensionsButtonCell extends ListCell<FileChooser.ExtensionFilter> {
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
            final File newDir = new File(FilenameUtils.concat(".", name));
            newDir.mkdir();

            // Refresh
            updateFiles(currentDirectory);
         });
      }
   }

   private void setCurrentView(final FilesView filesView) {
      currentView = filesView;

      splitPane.getItems().setAll(placesView, currentView.getNode());

      updateFiles(currentDirectory);
   }

   private void updateFiles(final File directory) {
      updateFiles(directory, true);
   }

   private void updateFiles(final File directory,
                            final boolean updateBreadCrumbBar) {
      currentView.setFiles(getFiles(directory));

      if (updateBreadCrumbBar) {
         updateDirBreadCrumbBar(currentDirectory);
      }
   }

   private void changeDirectory(final File directory) {
      changeDirectory(directory, true);
   }

   private void changeDirectory(final File directory,
                                final boolean updateBreadCrumbBar) {
      if (currentDirectory != null) {
         directoryStack.push(currentDirectory);
         backButton.setDisable(false);
      }
      currentDirectory = directory;
      currentSelection.setValue(null);

      updateFiles(currentDirectory, updateBreadCrumbBar);
   }

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

   private WildcardFileFilter getFileFilter() {
      final List<String> extensionFilter = extensionsComboBox == null || extensionsComboBox.getValue() == null
            ? Collections.emptyList()
            : extensionsComboBox.getValue().getExtensions();
      return new DirOrWildcardFilter(extensionFilter);
   }

   private class GetFilesPredicate implements Predicate<File> {
      @Override
      public boolean test(final File file) {
         final boolean filterHidden = !showHiddenFiles();
         return !(filterHidden && file.isHidden()) && (!hideFiles || file.isDirectory());
      }
   }

   private class FilesViewCallbackImpl implements FilesViewCallback {
      @Override
      public void requestChangeDirectory(final File directory) {
         changeDirectory(directory);
      }

      @Override
      public Stream<File> getFileStream(final File directory) {
         return getFiles(directory);
      }

      @Override
      public void setCurrentSelection(final File file) {
         currentSelection.setValue(file);
      }

      @Override
      public void fireDoneButton() {
         doneButton.fire();
      }
   }

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
