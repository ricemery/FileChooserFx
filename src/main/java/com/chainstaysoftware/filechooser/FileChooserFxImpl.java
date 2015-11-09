package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.preview.PreviewWindow;
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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.Pair;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileChooserFxImpl implements FileChooserFx {
   private static final int SCENE_WIDTH = 800;
   private static final int SCENE_HEIGHT = 600;

   private final ResourceBundle resourceBundle = ResourceBundle.getBundle("filechooser");
   private final ObservableList<javafx.stage.FileChooser.ExtensionFilter> extensionFilters =
         FXCollections.observableArrayList();
   private final ObservableMap<String, PreviewWindow> previewHandlers = FXCollections.observableHashMap();
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
   private FilesView currentView;
   private IconsFilesView iconsFilesView;
   private ListFilesView listFilesView;
   private SplitPane splitPane;
   private FileChooserCallback fileChooserCallback;
   private HelpCallback helpCallback;
   private Stage stage;
   private Node placesView;
   private boolean saveMode;
   private boolean hideFiles;

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
   public ObservableMap<String, PreviewWindow> getPreviewHandlers() {
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
      stage.setOnCloseRequest(event -> fileChooserCallback.fileChoosen(Optional.empty()));
      stage.show();

      currentDirectory = getInitialDirectory() == null
            ? new File(".")
            : initialDirectory.getValue();
      updateFiles(currentDirectory);
   }

   private SplitPane createSplitPane() {
      iconsFilesView = createIconsFilesView();
      listFilesView = createListFilesView();
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

      final TextField nameField = new TextField();
      nameField.setId("nameField");
      nameField.setText(getInitialFileName());
      nameField.setPrefWidth(300);

      currentSelection.addListener((observable, oldValue, newValue) -> {
         if (newValue != null && newValue.isFile()) {
            nameField.setText(newValue.getName());
         }
      });

      final ToolBar filenameBar = new ToolBar();
      filenameBar.setId("filenameBar");
      filenameBar.getStyleClass().add("filenamebar");
      filenameBar.getItems().addAll(label, nameField);

      return filenameBar;
   }

   private ToolBar createToolbar() {
      backButton = createBackButton();

      final ToolBar toolBar = new ToolBar();
      toolBar.setId("Toolbar");
      toolBar.getStyleClass().add("toolbar");
      toolBar.getItems().setAll(backButton,
            new Separator(),
            createViewIconsButton(),
            createViewListButton());

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

   private Button createViewListButton() {
      final Button viewListButton = new Button();
      viewListButton.setId("viewListButton");
      viewListButton.getStyleClass().add("toolbarbutton");
      viewListButton.setGraphic(new ImageView(icons.getIcon(Icons.LIST_VIEW_24)));
      viewListButton.setTooltip(new Tooltip(resourceBundle.getString("listview.tooltip")));
      viewListButton.setFocusTraversable(false);
      viewListButton.setOnAction(event -> setCurrentView(listFilesView));

      return viewListButton;
   }

   private Button createViewIconsButton() {
      final Button viewIconsButton = new Button();
      viewIconsButton.setId("viewIconsButton");
      viewIconsButton.getStyleClass().add("toolbarbutton");
      viewIconsButton.setGraphic(new ImageView(icons.getIcon(Icons.ICON_VIEW_24)));
      viewIconsButton.setTooltip(new Tooltip(resourceBundle.getString("iconview.tooltip")));
      viewIconsButton.setFocusTraversable(false);
      viewIconsButton.setOnAction(event -> setCurrentView(iconsFilesView));

      return viewIconsButton;
   }

   private Node createPlacesView() {
      // TODO: Do a better job determining/showing the available mount points.
      final LinkedList<Pair<Image, File>> places = new LinkedList<>();

      final Image driveIcon = icons.getIcon(Icons.HARDDISK_64);
      final List<File> roots = Arrays.asList(File.listRoots());
      roots.forEach(r -> places.add(new Pair<>(driveIcon, r)));

      final String homeDirStr = System.getProperty("user.home");
      if (homeDirStr != null) {
         places.add(new Pair<>(icons.getIcon(Icons.USER_HOME_64), new File(homeDirStr)));
      }

      final TableView<Pair<Image, File>> view = new TableView<>();
      view.setId("placesView");

      final TableColumn<Pair<Image, File>, Pair<Image, File>> placesColumn
            = new TableColumn<>(resourceBundle.getString("placeslist.text"));
      placesColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
      placesColumn.setCellFactory(new PlacesColumnCellFactory());
      placesColumn.prefWidthProperty().bind(view.widthProperty());

      view.getColumns().addAll(placesColumn);
      view.getItems().addAll(places);
      view.setOnMouseClicked(event -> changeDirectory(view.getSelectionModel().getSelectedItem().getValue()));
      view.setOnKeyPressed(new KeyEventHandler());

      return view;
   }

   private static class PlacesColumnCellFactory
         implements Callback<TableColumn<Pair<Image, File>, Pair<Image, File>>, TableCell<Pair<Image, File>, Pair<Image, File>>> {
      @Override
      public TableCell<Pair<Image, File>, Pair<Image, File>> call(TableColumn<Pair<Image, File>, Pair<Image, File>> param) {
         return new TableCell<Pair<Image, File>, Pair<Image, File>>() {
            @Override
            protected void updateItem(Pair<Image, File> item, boolean empty) {
               super.updateItem(item, empty);

               if (empty || item == null) {
                  setText(null);
                  setGraphic(null);
               } else {
                  setText(item.getValue().toString());

                  final ImageView graphic = new ImageView(item.getKey());
                  graphic.setFitHeight(Icons.SMALL_ICON_HEIGHT);
                  graphic.setFitWidth(Icons.SMALL_ICON_WIDTH);
                  graphic.setPreserveRatio(true);
                  setGraphic(graphic);
               }
            }
         };
      }
   }

   private ButtonBar createButtonBar() {
      final List<Button> buttons = new LinkedList<>();

      buttons.add(createDoneButton());
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
      doneButton.setDisable(true);
      doneButton.setOnAction(event -> {
         if (currentSelection == null) {
            return;
         }

         fileChooserCallback.fileChoosen(Optional.of(currentSelection.getValue()));
         stage.close();
      });
      currentSelection.addListener((observable, oldValue, newValue) ->
            doneButton.setDisable(newValue == null || (!hideFiles && newValue.isDirectory()))
      );
      return doneButton;
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

   private Pane createExtensionsPane() {
      final BorderPane borderPane = new BorderPane();
      borderPane.setId("extensionsBorderPane");
      borderPane.getStyleClass().add("extensionspane");

      if (!hideFiles) {
         // TODO: This is goofy
         final ComboBox<String> extensions = new ComboBox<>();
         extensions.getItems().addAll(extensionFilters.isEmpty()
               ? "*.*"
               : extensionFilters.stream()
               .map(javafx.stage.FileChooser.ExtensionFilter::getDescription)
               .collect(Collectors.joining(", ")));
         extensions.setValue(extensions.getItems().get(0));
         borderPane.setRight(extensions);
      }

      if (saveMode || hideFiles) {
         final Button newFolderButton = new Button(resourceBundle.getString("newfolderbutton.text"));
         newFolderButton.setId("newFolderButton");
         newFolderButton.setOnAction(new NewFolderAction());
         borderPane.setLeft(newFolderButton);
      }

      return borderPane;
   }

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
      currentView.setFiles(getFiles(directory));
   }

   private void changeDirectory(final File file) {
      if (currentDirectory != null) {
         directoryStack.push(currentDirectory);
         backButton.setDisable(false);
      }
      currentDirectory = file;
      currentSelection.setValue(null);

      updateFiles(currentDirectory);
   }

   private Stream<File> getFiles(final File directory) {
      if (!directory.isDirectory()) {
         return Stream.empty();
      }

      final FileFilter filter = getFileFilter();
      final File[] files = filter == null ? directory.listFiles() : directory.listFiles(filter);
      return files == null
            ? Stream.empty()
            : Arrays.stream(files).filter(new GetFilesPredicate());
   }

   private WildcardFileFilter getFileFilter() {
      final List<String> extensionFilter
            = getSelectedExtensionFilter() == null
               ? extensionFilters.isEmpty() ? null : extensionFilters.get(0).getExtensions()
               : selectedExtensionFilter.get().getExtensions();
      return extensionFilter == null ? null : new DirOrWildcardFilter(extensionFilter);
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
   }

   private class KeyEventHandler implements EventHandler<KeyEvent> {
      @Override
      public void handle(KeyEvent event) {
         if (event.getCode() == KeyCode.ESCAPE) {
            stage.close();
            fileChooserCallback.fileChoosen(Optional.empty());
            event.consume();
         }
      }
   }
}
