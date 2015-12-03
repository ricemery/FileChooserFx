package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.icons.IconsImpl;
import com.chainstaysoftware.filechooser.preview.PreviewPane;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ListFilesView extends AbstractFilesView {
   private final Map<String, Class<? extends PreviewPane>> previewHandlers;
   private final ResourceBundle resourceBundle = ResourceBundle.getBundle("filechooser");
   private final TreeTableView<File> filesTreeView;
   private final Icons icons;

   private FilesViewCallback callback;
   private EventHandler<? super KeyEvent> keyEventHandler;

   public ListFilesView(final Stage parent,
                        final Map<String, Class<? extends PreviewPane>> previewHandlers,
                        final Icons icons) {
      super(parent);

      this.previewHandlers = previewHandlers;
      this.icons = icons;

      final TreeTableColumn<File, String> nameColumn = createNameColumn(parent);
      final TreeTableColumn<File, ZonedDateTime> dateModifiedColumn = createDateModifiedColumn();
      final TreeTableColumn<File, Long> sizeColumn = createSizeColumn();

      filesTreeView = new TreeTableView<>();
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
   }

   private TreeTableColumn<File, String> createNameColumn(Stage parent) {
      final TreeTableColumn<File, String> nameColumn
            = new TreeTableColumn<>(resourceBundle.getString("listfilesview.name"));
      nameColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getName()));
      nameColumn.prefWidthProperty().bind(parent.widthProperty().divide(2));
      return nameColumn;
   }

   private TreeTableColumn<File, ZonedDateTime> createDateModifiedColumn() {
      final TreeTableColumn<File, ZonedDateTime> dateModifiedColumn
            = new TreeTableColumn<>(resourceBundle.getString("listfilesview.datemodified"));

      dateModifiedColumn.setCellValueFactory(param -> {
         final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(param.getValue().getValue().lastModified()),
               ZoneId.systemDefault());
         return new ReadOnlyObjectWrapper<>(zonedDateTime);
      });

      dateModifiedColumn.setCellFactory(param ->  new TreeTableCell<File, ZonedDateTime>() {
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

      dateModifiedColumn.setPrefWidth(175);
      return dateModifiedColumn;
   }

   private TreeTableColumn<File, Long> createSizeColumn() {
      final TreeTableColumn<File, Long> sizeColumn
            = new TreeTableColumn<>(resourceBundle.getString("listfilesview.size"));

      sizeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue().length()));

      sizeColumn.setCellFactory(param -> new TreeTableCell<File, Long>() {
         @Override
         protected void updateItem(Long item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
               setText("");
            } else {
               setText(FileUtils.byteCountToDisplaySize(item));
            }
         }
      });

      sizeColumn.setPrefWidth(100);
      return sizeColumn;
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

                  final String fileExtension = FilenameUtils.getExtension(file.getName());
                  final Class<? extends PreviewPane> previewPaneClass = previewHandlers.get(fileExtension.toLowerCase(Locale.ENGLISH));
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

   @Override
   public void setCallback(final FilesViewCallback callback) {
      this.callback = callback;
   }

   @Override
   public void setFiles(final Stream<File> fileStream) {
      final TreeItem<File> rootItem = new TreeItem<>();
      rootItem.getChildren().addAll(fileStream
            .map(f -> {
               final ImageView graphic = new ImageView(icons.getIconForFile(f));
               graphic.setFitWidth(IconsImpl.SMALL_ICON_WIDTH);
               graphic.setFitHeight(IconsImpl.SMALL_ICON_HEIGHT);
               graphic.setPreserveRatio(true);
               return new DirectoryTreeItem(f, graphic, callback, icons);
            })
            .collect(Collectors.toList()));

      filesTreeView.setRoot(rootItem);
      selectCurrent();
   }

   /**
    * If there is a currently selected file, then update the TreeView with
    * the selection.
    */
   private void selectCurrent() {
      final File currentSelectedFile = callback.getCurrentSelection();
      filesTreeView.getRoot().getChildren()
            .stream()
            .filter(item -> compareFilePaths(item.getValue(), currentSelectedFile))
            .findFirst()
            .ifPresent(item -> filesTreeView.getSelectionModel().select(item));
   }

   public void setOnKeyPressed(final EventHandler<? super KeyEvent> eventHandler) {
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
}
