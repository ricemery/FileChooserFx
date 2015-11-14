package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.preview.PreviewPane;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.File;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * View files as list along with preview of file.
 */
class ListFilesWithPreviewView extends AbstractFilesView {
   private final ResourceBundle resourceBundle = ResourceBundle.getBundle("filechooser");
   private final TableView<DirectoryListItem> tableView = new TableView<>();
   private final SplitPane splitPane;
   private final HBox previewHbox;
   private final Icons icons = new Icons();
   private final PropertiesPreviewPane propertiesPreviewPane;

   private FilesViewCallback callback;
   private EventHandler<? super KeyEvent> keyEventHandler;


   public ListFilesWithPreviewView(final Stage parent,
                                   final Map<String, PreviewPane> previewHandlers) {
      super(parent);

      propertiesPreviewPane = new PropertiesPreviewPane(previewHandlers);

      previewHbox = new HBox();
      previewHbox.setId("previewHbox");
      previewHbox.setAlignment(Pos.CENTER);
      previewHbox.setMinSize(0, 0);

      final TableColumn<DirectoryListItem, DirectoryListItem> nameColumn
            = new TableColumn<>(resourceBundle.getString("listfilesview.name"));
      nameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
      nameColumn.setCellFactory(new DirListNameColumnCellFactory(true));
      nameColumn.prefWidthProperty().bind(tableView.widthProperty());

      tableView.getColumns().addAll(nameColumn);
      tableView.setOnMouseClicked(event -> {
         final File file = tableView.getSelectionModel().getSelectedItem().getFile();
         if (event.getClickCount() < 2) {
            return;
         }

         if (file.isDirectory()) {
            callback.requestChangeDirectory(file);
         } else {
            callback.fireDoneButton();
         }
      });
      tableView.setOnKeyPressed(new KeyPressedHandler());
      tableView.getSelectionModel().selectedItemProperty().addListener(new SelectedItemChanged());

      splitPane = new SplitPane();
      splitPane.setId("previewSplitPane");
      splitPane.getItems().addAll(tableView, previewHbox);
      splitPane.setDividerPositions(0.30);
   }

   @Override
   public Node getNode() {
      return splitPane;
   }

   @Override
   public void setCallback(final FilesViewCallback callback) {
      this.callback = callback;
   }

   @Override
   public void setFiles(final Stream<File> fileStream) {
      tableView.getItems().setAll(fileStream
            .map(f -> new DirectoryListItem(f, icons.getIconForFile(f)))
            .collect(Collectors.toList()));
   }

   public void setOnKeyPressed(final EventHandler<? super KeyEvent> eventHandler) {
      this.keyEventHandler = eventHandler;
   }

   private class KeyPressedHandler implements EventHandler<KeyEvent> {
      @Override
      public void handle(KeyEvent event) {
         if (keyEventHandler != null) {
            keyEventHandler.handle(event);
         }

         if (event.isConsumed()) {
            return;
         }

         if (KeyCode.ENTER.equals(event.getCode())) {
            final File file = tableView.getSelectionModel().getSelectedItem().getFile();
            callback.requestChangeDirectory(file);
         }
      }
   }

   private class SelectedItemChanged implements ChangeListener<DirectoryListItem> {
      @Override
      public void changed(ObservableValue<? extends DirectoryListItem> observable,
                          DirectoryListItem oldValue,
                          DirectoryListItem newValue) {
         previewHbox.getChildren().clear();

         callback.setCurrentSelection(newValue == null ? null : newValue.getFile());

         if (newValue == null) {
            return;
         }

         preview(newValue.getFile());
      }

      private void preview(final File file) {
         propertiesPreviewPane.setFile(file);
         previewHbox.getChildren().setAll(propertiesPreviewPane.getPane());
         HBox.setHgrow(propertiesPreviewPane.getPane(), Priority.ALWAYS);
      }
   }
}