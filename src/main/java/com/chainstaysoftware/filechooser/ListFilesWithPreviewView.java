package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.preview.PreviewPane;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
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
   private final Icons icons;
   private final PropertiesPreviewPane propertiesPreviewPane;
   private final List<TableColumn<DirectoryListItem, ?>> sortOrder;

   private FilesViewCallback callback;
   private EventHandler<? super KeyEvent> keyEventHandler;

   public ListFilesWithPreviewView(final Stage parent,
                                   final Map<String, Class<? extends PreviewPane>> previewHandlers,
                                   final Icons icons) {
      super(parent);

      propertiesPreviewPane = new PropertiesPreviewPane(previewHandlers, icons);
      this.icons = icons;

      previewHbox = new HBox();
      previewHbox.setId("previewHbox");
      previewHbox.setAlignment(Pos.CENTER);
      previewHbox.setMinSize(0, 0);

      final TableColumn<DirectoryListItem, DirectoryListItem> nameColumn
            = new TableColumn<>(resourceBundle.getString("listfilesview.name"));
      nameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
      nameColumn.setCellFactory(new DirListNameColumnCellFactory(true));
      nameColumn.prefWidthProperty().bind(tableView.widthProperty());
      nameColumn.setComparator((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getFile().getName(), o2.getFile().getName()));

      tableView.getColumns().addAll(nameColumn);
      tableView.setOnMouseClicked(event -> {
         if (tableView.getSelectionModel().getSelectedItem() == null) {
            return;
         }

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
      tableView.setPlaceholder(new Label(""));
      sortOrder = new LinkedList<>();
      sortOrder.add(nameColumn);

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
      saveSortOrder();

      tableView.getItems().setAll(fileStream
            .map(f -> new DirectoryListItem(f, icons.getIconForFile(f)))
            .collect(Collectors.toList()));

      restoreSortOrder();

      selectCurrent();
   }

   /**
    * Save the sort order of the filesTreeView
    */
   private void saveSortOrder() {
      if (!tableView.getSortOrder().isEmpty()) {
         sortOrder.clear();
         sortOrder.addAll(tableView.getSortOrder());
      }
   }

   /**
    * Reapply the sort order of the filesTreeView
    */
   private void restoreSortOrder() {
      if (sortOrder != null) {
         tableView.getSortOrder().clear();
         tableView.getSortOrder().addAll(sortOrder);
         sortOrder.get(0).setSortable(true); // This performs a sort
      }
   }

   /**
    * If there is a currently selected file, then update the TableView with
    * the selection.
    */
   private void selectCurrent() {
      final File currentSelectedFile = callback.getCurrentSelection();
      tableView.getItems()
            .stream()
            .filter(item -> compareFilePaths(item.getFile(), currentSelectedFile))
            .findFirst()
            .ifPresent(item -> tableView.getSelectionModel().select(item));
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

         if (newValue == null) {
            return;
         }

         callback.setCurrentSelection(newValue.getFile());

         preview(newValue.getFile());
      }

      private void preview(final File file) {
         propertiesPreviewPane.setFile(file);
         previewHbox.getChildren().setAll(propertiesPreviewPane.getPane());
         HBox.setHgrow(propertiesPreviewPane.getPane(), Priority.ALWAYS);
      }
   }
}