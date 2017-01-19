package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.preview.PreviewPane;
import com.chainstaysoftware.filechooser.preview.PreviewPaneQuery;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;

class ListFilesView extends AbstractFilesView {
   private static final int DATE_MODIFIED_COL_PREF_WIDTH = 175;
   private static final int SIZE_COLUMN_PREF_WIDTH = 100;

   private final Map<String, Class<? extends PreviewPane>> previewHandlers;
   private final ResourceBundle resourceBundle = ResourceBundle.getBundle("filechooser");
   private final TreeTableView<File> filesTreeView;
   private final Icons icons;
   private final List<TreeTableColumn<File, ?>> sortOrder = new LinkedList<>();
   private final TreeTableColumn<File, String> nameColumn;
   private final TreeTableColumn<File, ZonedDateTime> dateModifiedColumn;
   private final TreeTableColumn<File, Long> sizeColumn;
   private final FilesViewCallback callback;

   private EventHandler<? super KeyEvent> keyEventHandler;

   public ListFilesView(final Stage parent,
                        final Map<String, Class<? extends PreviewPane>> previewHandlers,
                        final Icons icons,
                        final FilesViewCallback callback) {
      super(parent);

      this.previewHandlers = previewHandlers;
      this.icons = icons;
      this.callback = callback;

      filesTreeView = new TreeTableView<>();

      nameColumn = createNameColumn(filesTreeView);
      dateModifiedColumn = createDateModifiedColumn();
      sizeColumn = createSizeColumn();

      filesTreeView.setShowRoot(false);
      filesTreeView.setPlaceholder(new Label(""));
      filesTreeView.getSelectionModel().selectedItemProperty().addListener(new TreeViewSelectItemListener());
      filesTreeView.getColumns().setAll(nameColumn, dateModifiedColumn, sizeColumn);
      filesTreeView.setRowFactory(new RowFactory());
      filesTreeView.setOnMouseClicked(event -> {
         if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
            if (filesTreeView.getSelectionModel().getSelectedItem() == null) {
               return;
            }

            final File file = filesTreeView.getSelectionModel().getSelectedItem().getValue();
            if (file.isDirectory()) {
               callback.requestChangeDirectory(file);
            } else {
               callback.fireDoneButton();
            }
         }
      });
      filesTreeView.setOnKeyPressed(event -> {if (keyEventHandler != null) {keyEventHandler.handle(event);}});

      initializeSort();
   }

   private void initializeSort() {
      final TreeTableColumn<File, ?> sortColumn = orderByToColumn(callback.orderByProperty().get());
      sortColumn.setSortType(orderDirectionToSortType(callback.orderDirectionProperty().get()));
      sortOrder.add(sortColumn);

      // Set the updated sort values back into the properties of the FileChooser.
      filesTreeView.getSortOrder().addListener((ListChangeListener<TreeTableColumn<File, ?>>) c -> {
         if (c.getList().isEmpty()) {
            return;
         }

         callback.orderByProperty().setValue(columnToOrderBy(c.getList().get(0)));
      });

      final ChangeListener<TreeTableColumn.SortType> sortTypeChangeListener = (observable, oldValue, newValue) ->
            callback.orderDirectionProperty().setValue(sortTypeToOrderDirection(newValue));
      nameColumn.sortTypeProperty().addListener(sortTypeChangeListener);
      dateModifiedColumn.sortTypeProperty().addListener(sortTypeChangeListener);
      sizeColumn.sortTypeProperty().addListener(sortTypeChangeListener);
   }

   private TreeTableColumn<File, String> createNameColumn(final TreeTableView parent) {
      final TreeTableColumn<File, String> column
            = new TreeTableColumn<>(resourceBundle.getString("listfilesview.name"));
      column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getName()));
      column.prefWidthProperty().bind(parent.widthProperty()
            .subtract(DATE_MODIFIED_COL_PREF_WIDTH)
            .subtract(SIZE_COLUMN_PREF_WIDTH));

      column.setCellFactory(param -> new TreeTableCell<File, String>() {
         @Override
         protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            setText("");
            setGraphic(null);

            if (!empty) {
               // This is a hack to work around JDK bug - https://bugs.openjdk.java.net/browse/JDK-8156049 .
               final TreeTableRow row = getTreeTableRow();
               if (row != null && row.getTreeItem() != null) {
                  final TreeItem treeItem = row.getTreeItem();
                  if (getTreeTableRow().getTreeItem() instanceof DirectoryTreeItem) {
                     setGraphic(((DirectoryTreeItem)treeItem).getGraphic2());
                  } else {
                     setGraphic(treeItem.getGraphic());
                  }
               }

               setText(item);
            }
         }
      });

      return column;
   }

   private TreeTableColumn<File, ZonedDateTime> createDateModifiedColumn() {
      final TreeTableColumn<File, ZonedDateTime> column
            = new TreeTableColumn<>(resourceBundle.getString("listfilesview.datemodified"));

      column.setCellValueFactory(param -> {
         final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(param.getValue().getValue().lastModified()),
               ZoneId.systemDefault());
         return new ReadOnlyObjectWrapper<>(zonedDateTime);
      });

      column.setCellFactory(param ->  new TreeTableCell<File, ZonedDateTime>() {
               @Override
               protected void updateItem(ZonedDateTime item, boolean empty) {
                  super.updateItem(item, empty);

                  if (empty) {
                     setText("");
                  } else {
                     setText(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                           .format(item));
                  }
               }
            });

      column.setPrefWidth(DATE_MODIFIED_COL_PREF_WIDTH);
      return column;
   }

   private TreeTableColumn<File, Long> createSizeColumn() {
      final TreeTableColumn<File, Long> column
            = new TreeTableColumn<>(resourceBundle.getString("listfilesview.size"));

      column.setCellValueFactory(param ->
            param.getValue().getValue().isDirectory()
               ? null
               : new ReadOnlyObjectWrapper<>(param.getValue().getValue().length()));

      column.setCellFactory(param -> new TreeTableCell<File, Long>() {
         @Override
         protected void updateItem(Long item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
               setText("");
            } else {
               setText(FileUtils.byteCountToDisplaySize(item));
            }
         }
      });

      column.setPrefWidth(SIZE_COLUMN_PREF_WIDTH);
      return column;
   }

   private class RowFactory implements Callback<TreeTableView<File>, TreeTableRow<File>> {
      @Override
      public TreeTableRow<File> call(TreeTableView<File> param) {
         return new TreeTableRow<File>() {
            @Override
            protected void updateItem(File file, boolean empty) {
               super.updateItem(file, empty);

               setContextMenu(null);

               if (!empty) {
                  if (file.isDirectory()) {
                     return;
                  }

                  final Class<? extends PreviewPane> previewPaneClass = PreviewPaneQuery.query(previewHandlers, file);
                  if (previewPaneClass == null) {
                     return;
                  }

                  final MenuItem imagePreviewItem = new MenuItem("Preview");
                  imagePreviewItem.setOnAction(v ->showPreview(previewPaneClass, file));

                  setContextMenu(new ContextMenu(imagePreviewItem));
               }
            }
         };
      }
   }

   @Override
   public Node getNode() {
      return filesTreeView;
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

      final TreeItem<File> rootItem = new TreeItem<>();
      filesTreeView.setRoot(rootItem);

      new PopulateTreeItemRunnable(directoryStream, remainingDirectoryStream, rootItem).run();
   }

   private class PopulateTreeItemRunnable implements Runnable {
      private final DirectoryStream<Path> directoryStream;
      private final DirectoryStream<Path> unfilteredDirectoryStream;
      private final TreeItem<File> parentItem;

      PopulateTreeItemRunnable(final DirectoryStream<Path> directoryStream,
                               final DirectoryStream<Path> unfilteredDirectoryStream,
                               final TreeItem<File> parentItem) {
         this.directoryStream = directoryStream;
         this.unfilteredDirectoryStream = unfilteredDirectoryStream;
         this.parentItem = parentItem;
      }

      @Override
      public void run() {
         final List<TreeItem<File>> directoryTreeItems = parentItem.getChildren();

         // Update the TreeView from Services so that the UI is not blocked on OS calls.
         final UpdateIconsTree updateIconsTreeService = new UpdateIconsTree(directoryTreeItems, callback, new PopulateFactory(), icons);
         final UpdateDirectoryTree updateDirectoryTreeService = new UpdateDirectoryTree(directoryStream, unfilteredDirectoryStream, directoryTreeItems, icons);
         final Predicate<File> shouldHideFile
            = new ShowHiddenFilesPredicate(callback.showHiddenFilesProperty(), callback.shouldHideFilesProperty());
         final FilterHiddenFromDirTree filterTreeService = new FilterHiddenFromDirTree(directoryTreeItems, shouldHideFile);
         final SelectCurrentService selectCurrentService = new SelectCurrentService();
         filterTreeService.setOnSucceeded(event -> updateIconsTreeService.start());
         updateIconsTreeService.setOnSucceeded(event ->  selectCurrentService.start());
         updateDirectoryTreeService.setOnSucceeded(event -> filterTreeService.start());
         updateDirectoryTreeService.start();
      }
   }

   private class PopulateFactory implements PopulateTreeItemRunnableFactory {
      @Override
      public Runnable create(final DirectoryStream<Path> directoryStream,
                             final DirectoryStream<Path> unfilteredDirectoryStream,
                             final TreeItem<File> parentItem) {
         return new PopulateTreeItemRunnable(directoryStream, unfilteredDirectoryStream, parentItem);
      }
   }

   /**
    * Save the sort order of the filesTreeView
    */
   private void saveSortOrder() {
      if (!filesTreeView.getSortOrder().isEmpty()) {
         sortOrder.clear();
         sortOrder.addAll(filesTreeView.getSortOrder());

         // Reset OrderBy property in case it was mapped to something other than input.
         callback.orderByProperty().setValue(columnToOrderBy(filesTreeView.getSortOrder().get(0)));
      }
   }

   /**
    * If there is a currently selected file, then update the GridView with
    * the selection.
    */
   private class SelectCurrentService extends Service<Void> {

      private final CountDownLatch latch = new CountDownLatch(1);

      protected Task<Void> createTask() {
         return new SelectTask();
      }

      private class SelectTask extends Task<Void> {
         @Override
         protected Void call() throws Exception {
            Platform.runLater(() -> {
               restoreSortOrder();

               final File currentSelectedFile = callback.getCurrentSelection();
               filesTreeView.getRoot().getChildren()
                  .stream()
                  .filter(item -> compareFilePaths(item.getValue(), currentSelectedFile))
                  .findFirst()
                  .ifPresent(item -> filesTreeView.getSelectionModel().select(item));


               filesTreeView.refresh();

               latch.countDown();
            });

            latch.await();

            return null;
         }
      }


      /**
       * Reapply the sort order of the filesTreeView
       */
      private void restoreSortOrder() {
         filesTreeView.getSortOrder().clear();
         filesTreeView.getSortOrder().addAll(sortOrder);
         sortOrder.get(0).setSortable(true); // This performs a sort
      }
   }

   void setOnKeyPressed(final EventHandler<? super KeyEvent> eventHandler) {
      this.keyEventHandler = eventHandler;
   }

   private class TreeViewSelectItemListener implements ChangeListener<TreeItem<File>> {
      @Override
      public void changed(final ObservableValue<? extends TreeItem<File>> observable,
                          final TreeItem<File> oldValue,
                          final TreeItem<File> newValue) {
         if (newValue == null) {
            return;
         }

         callback.setCurrentSelection(newValue.getValue());
      }
   }

   /**
    * Map {@link OrderBy} to {@link TreeTableCell}
    */
   private TreeTableColumn<File, ?> orderByToColumn(final OrderBy orderBy) {
      if (OrderBy.ModificationDate.equals(orderBy)) {
         return dateModifiedColumn;
      }

      if (OrderBy.Size.equals(orderBy)) {
         return sizeColumn;
      }

      return nameColumn;
   }

   /**
    * Map {@link TreeTableCell} to {@link OrderBy}
    */
   private OrderBy columnToOrderBy(final TreeTableColumn<File, ?> column) {
      if (column.equals(dateModifiedColumn)) {
         return OrderBy.ModificationDate;
      }

      if (column.equals(sizeColumn)) {
         return OrderBy.Size;
      }

      return OrderBy.Name;
   }

   /**
    * Map {@link OrderDirection} to {@link javafx.scene.control.TreeTableColumn.SortType}
    */
   private TreeTableColumn.SortType orderDirectionToSortType(final OrderDirection orderDirection) {
      return OrderDirection.Descending.equals(orderDirection)
            ? TreeTableColumn.SortType.DESCENDING
            : TreeTableColumn.SortType.ASCENDING;
   }

   /**
    * Map {@link javafx.scene.control.TreeTableColumn.SortType} to {@link OrderDirection}
    */
   private OrderDirection sortTypeToOrderDirection(final TreeTableColumn.SortType sortType) {
      return TreeTableColumn.SortType.DESCENDING.equals(sortType)
            ? OrderDirection.Descending
            : OrderDirection.Ascending;
   }
}
