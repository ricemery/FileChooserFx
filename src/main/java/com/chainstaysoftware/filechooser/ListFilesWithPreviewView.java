package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.preview.PreviewPane;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
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
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * View files as list along with preview of file.
 */
class ListFilesWithPreviewView extends AbstractFilesView {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.ListFilesWithPreviewView");

   private final TableView<DirectoryListItem> tableView = new TableView<>();
   private final SplitPane splitPane;
   private final HBox previewHbox;
   private final PropertiesPreviewPane propertiesPreviewPane;
   private final List<TableColumn<DirectoryListItem, ?>> sortOrder;
   private final FilesViewCallback callback;

   private EventHandler<? super KeyEvent> keyEventHandler;
   private final TableColumn<DirectoryListItem, DirectoryListItem> nameColumn;

   ListFilesWithPreviewView(final Stage parent,
                            final Map<String, Class<? extends PreviewPane>> previewHandlers,
                            final Icons icons,
                            final double dividerPosition,
                            final FilesViewCallback callback) {
      super(parent);

      propertiesPreviewPane = new PropertiesPreviewPane(previewHandlers, icons);
      this.callback = callback;

      previewHbox = new HBox();
      previewHbox.setId("previewHbox");
      previewHbox.setAlignment(Pos.CENTER);
      previewHbox.setMinSize(0, 0);

      final ResourceBundle resourceBundle = ResourceBundle.getBundle("filechooser");
      nameColumn = new TableColumn<>(resourceBundle.getString("listfilesview.name"));
      nameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
      nameColumn.setCellFactory(new DirListNameColumnCellFactory(true, callback, icons));
      nameColumn.prefWidthProperty().bind(tableView.widthProperty());
      nameColumn.setComparator((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getFile().getName(), o2.getFile().getName()));
      nameColumn.setSortType(orderDirectionToSortType(callback.orderDirectionProperty().get()));
      nameColumn.sortTypeProperty().addListener((observable, oldValue, newValue) ->
         callback.orderDirectionProperty().set(sortTypeToOrderDirection(newValue)));

      tableView.getColumns().addAll(nameColumn);
      tableView.setOnKeyPressed(new KeyPressedHandler());
      tableView.getSelectionModel().selectedItemProperty().addListener(new SelectedItemChanged());
      tableView.setPlaceholder(new Label(""));
      sortOrder = new LinkedList<>();
      sortOrder.add(nameColumn);

      splitPane = new SplitPane();
      splitPane.setId("previewSplitPane");
      splitPane.getItems().addAll(tableView, previewHbox);
      splitPane.setDividerPositions(dividerPosition);
   }

   @Override
   public Node getNode() {
      return splitPane;
   }

   /**
    * sets the files on the view.
    * @param directoryStream Filtered stream of files to show in view.
    * @param remainingDirectoryStream This stream contains the files not to
    *                                 show in the view. But, may include
    *                                 directories that should be shown in the
    *                                 view but were filtered out in the directoryStream.
    */
   @Override
   public void setFiles(final DirectoryStream<Path> directoryStream,
                        final DirectoryStream<Path> remainingDirectoryStream) {
      saveSortOrder();

      final ObservableList<DirectoryListItem> directoryListItems = FXCollections.observableArrayList();
      final SortedList<DirectoryListItem> items = directoryListItems.sorted();
      items.comparatorProperty().bind(tableView.comparatorProperty());
      tableView.setItems(items);

      // Update the TableView from Services so that the UI is not blocked on OS calls.
      final UpdateDirectoryList updateDirectoryListService = new UpdateDirectoryList(directoryStream, remainingDirectoryStream, directoryListItems);

      final Predicate<File> shouldHideFile
         = new ShowHiddenFilesPredicate(callback.showHiddenFilesProperty(), callback.shouldHideFilesProperty());
      final FilterHiddenFromDirList filterListService = new FilterHiddenFromDirList(directoryListItems, shouldHideFile);

      final SelectCurrentService selectCurrentService = new SelectCurrentService();

      filterListService.setOnSucceeded(event -> selectCurrentService.start());

      updateDirectoryListService.setOnSucceeded(event -> {
         filterListService.start();
         tableView.setCursor(null);
      });
      updateDirectoryListService.setOnRunning(event -> tableView.setCursor(Cursor.WAIT));
      setServiceFailureHandlers(updateDirectoryListService);
      updateDirectoryListService.start();
   }

   /**
    * Sets up event handlers to reset wait icon when service fails or is cancelled.
    */
   private void setServiceFailureHandlers(final Service<Void> service) {
      service.setOnCancelled(event -> {
         logger.warning("Service cancelled - " + service.getClass().getCanonicalName());
         tableView.setCursor(null);

      });
      service.setOnFailed(event -> {
         logger.warning("Service failed - " + service.getClass().getCanonicalName());
         tableView.setCursor(null);
      });
   }

   /**
    * Save the sort order of the filesTreeView
    */
   private void saveSortOrder() {
      if (!tableView.getSortOrder().isEmpty()) {
         sortOrder.clear();
         sortOrder.addAll(tableView.getSortOrder());

         // Only name sort is supported.
         callback.orderByProperty().setValue(OrderBy.Name);
      }
   }

   /**
    * If there is a currently selected file, then update the TableView with
    * the selection.
    */
   private class SelectCurrentService extends Service<Void> {

      protected Task<Void> createTask() {
         return new SelectTask();
      }

      private class SelectTask extends Task<Void> {
         private final CountDownLatch latch = new CountDownLatch(1);

         @Override
         protected Void call() throws Exception {
            Platform.runLater(() -> {
               restoreSortOrder();

               final File currentSelectedFile = callback.getCurrentSelection();
               tableView.getItems()
                  .stream()
                  .filter(item -> compareFilePaths(item.getFile(), currentSelectedFile))
                  .findFirst()
                  .ifPresent(item -> tableView.getSelectionModel().select(item));

               latch.countDown();
            });

            latch.await();

            return null;
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
      }
   }

   /**
    * Map {@link OrderDirection} to {@link javafx.scene.control.TableColumn.SortType}
    */
   private TableColumn.SortType orderDirectionToSortType(final OrderDirection orderDirection) {
      return OrderDirection.Descending.equals(orderDirection)
            ? TableColumn.SortType.DESCENDING
            : TableColumn.SortType.ASCENDING;
   }

   /**
    * Map {@link javafx.scene.control.TableColumn.SortType} to {@link OrderDirection}
    */
   private OrderDirection sortTypeToOrderDirection(final TableColumn.SortType sortType) {
      return TableColumn.SortType.DESCENDING.equals(sortType)
            ? OrderDirection.Descending
            : OrderDirection.Ascending;
   }

   void setOnKeyPressed(final EventHandler<? super KeyEvent> eventHandler) {
      this.keyEventHandler = eventHandler;
   }

   double getDividerPosition() {
      return splitPane.getDividerPositions()[0];
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

         File newFile = newValue == null ? null : newValue.getFile();

         ListFilesWithPreviewView.this.getNode().getScene().setCursor(Cursor.WAIT);
         Platform.runLater(() -> {
            callback.setCurrentSelection(newFile);
            ListFilesWithPreviewView.this.getNode().getScene().setCursor(null);

            if (newFile != null) {
               preview(newFile);
            }
         });
       }

      private void preview(final File file) {
         previewHbox.getChildren().setAll(propertiesPreviewPane.getPane());
         propertiesPreviewPane.setFile(file);
         HBox.setHgrow(propertiesPreviewPane.getPane(), Priority.ALWAYS);
      }
   }
}
