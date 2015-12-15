package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.preview.PreviewPane;
import com.chainstaysoftware.filechooser.preview.PreviewPaneQuery;
import impl.org.controlsfx.skin.GridViewSkin;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.controlsfx.control.GridView;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class IconsFilesView extends AbstractFilesView {
   private static final int NOT_SELECTED = -1;

   // Width and Height of Icon and Filename Cell.
   private static final int CELL_HEIGHT = 90;
   private static final int CELL_WIDTH = 90;

   private final GridView<DirectoryListItem> gridView = new GridView<>();
   private final Map<String, Class<? extends PreviewPane>> previewHandlers;
   private final Icons icons;
   private final IntegerProperty selectedCellIndex = new SimpleIntegerProperty(NOT_SELECTED);

   private FilesViewCallback callback;
   private EventHandler<? super KeyEvent> keyEventHandler;

   public IconsFilesView(final Stage parent,
                         final Map<String, Class<? extends PreviewPane>> previewHandlers,
                         final Icons icons) {
      super(parent);

      this.previewHandlers = previewHandlers;
      this.icons = icons;

      gridView.setCellFactory(gridView1 -> {
         final IconGridCell cell = new IconGridCell(true, new IconGridCellContextMenuFactImpl());
         cell.indexProperty().addListener((observable, oldValue, newValue) -> {
            cell.updateSelected(selectedCellIndex.intValue() == newValue.intValue());
         });
         selectedCellIndex.addListener((observable, oldValue, newValue) ->
               cell.updateSelected(newValue.intValue() == cell.getIndex()));
         return cell;
      });
      gridView.setCellHeight(CELL_HEIGHT);
      gridView.setCellWidth(CELL_WIDTH);
      gridView.setOnMouseClicked(new MouseClickHandler());
      gridView.setOnKeyPressed(new KeyClickHandler());
   }

   @Override
   public Node getNode() {
      return gridView;
   }

   @Override
   public void setCallback(final FilesViewCallback callback) {
      this.callback = callback;
   }

   @Override
   public void setFiles(final Stream<File> fileStream) {
      selectedCellIndex.setValue(NOT_SELECTED);

      gridView.getItems().setAll(getIcons(fileStream));

      selectCurrent();
   }

   /**
    * If there is a currently selected file, then update the GridView with
    * the selection.
    */
   private void selectCurrent() {
      final File currentSelection = callback.getCurrentSelection();
      final List<DirectoryListItem> items = gridView.getItems();
      IntStream.range(0, items.size())
            .filter(i -> compareFilePaths(items.get(i).getFile(), currentSelection))
            .findFirst()
            .ifPresent(selectedCellIndex::setValue);
   }

   public void setOnKeyPressed(final EventHandler<? super KeyEvent> eventHandler) {
      this.keyEventHandler = eventHandler;
   }

   private List<DirectoryListItem> getIcons(final Stream<File> fileStream) {
      return fileStream.map(this::getDirListItem).collect(Collectors.toList());
   }

   private DirectoryListItem getDirListItem(final File file) {
      return new DirectoryListItem(file, icons.getIconForFile(file));
   }

   private class IconGridCellContextMenuFactImpl implements IconGridCellContextMenuFactory {
      @Override
      public ContextMenu create(final DirectoryListItem item) {
         final File file = item.getFile();
         if (file.isDirectory()) {
            return null;
         }

         final Class<? extends PreviewPane> previewPaneClass = PreviewPaneQuery.query(previewHandlers, file);
         if (previewPaneClass == null) {
            return null;
         }

         final MenuItem imagePreviewItem = new MenuItem("Preview");
         imagePreviewItem.setOnAction(v -> showPreview(previewPaneClass, file));

         return new ContextMenu(imagePreviewItem);
      }
   }

   private class MouseClickHandler implements EventHandler<MouseEvent> {
      @Override
      public void handle(MouseEvent event) {
         if (!(event.getTarget() instanceof IconGridCell)) {
            return;
         }

         final IconGridCell target = (IconGridCell)event.getTarget();
         if (isDoubleClick(event)) {
            final File file = (File)target.getUserData();
            if (file.isDirectory()) {
               callback.requestChangeDirectory(file);
            } else {
               callback.fireDoneButton();
            }
         } else {
            selectedCellIndex.setValue(target.getIndex());

            final File file = gridView.getItems().get(selectedCellIndex.get()).getFile();
            callback.setCurrentSelection(file);
         }
      }

      private boolean isDoubleClick(MouseEvent event) {
         return event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2;
      }
   }

   private class KeyClickHandler implements EventHandler<KeyEvent> {
      @Override
      public void handle(KeyEvent event) {
         if (keyEventHandler != null) {
            keyEventHandler.handle(event);

            if (event.isConsumed()) {
               return;
            }
         }

         final KeyCode keyCode = event.getCode();
         if (keyCode == KeyCode.LEFT) {
            selectedCellIndex.setValue(Math.max(0, selectedCellIndex.get() - 1));
            event.consume();
         } else if (keyCode == KeyCode.RIGHT) {
            selectedCellIndex.setValue(Math.min(selectedCellIndex.get() + 1, gridView.getItems().size() - 1));
            event.consume();
         } else if (keyCode == KeyCode.UP) {
            selectedCellIndex.setValue(Math.max(0, selectedCellIndex.get() - ((GridViewSkin)gridView.getSkin()).computeMaxCellsInRow()));
            event.consume();
         }  else if (keyCode == KeyCode.DOWN) {
            selectedCellIndex.setValue(Math.min(gridView.getItems().size() - 1, selectedCellIndex.get() + ((GridViewSkin)gridView.getSkin()).computeMaxCellsInRow()));
            event.consume();
         } else if (keyCode == KeyCode.ENTER) {
            if (selectedCellIndex.get() == NOT_SELECTED) {
               return;
            }

            final DirectoryListItem item = gridView.getItems().get(selectedCellIndex.get());
            final File file = item.getFile();
            if (!file.isDirectory()) {
               return;
            }

            callback.requestChangeDirectory(file);
            event.consume();
         }

         if (selectedCellIndex.get() == NOT_SELECTED) {
            return;
         }

         final File file = gridView.getItems().get(selectedCellIndex.get()).getFile();
         callback.setCurrentSelection(file);
      }
   }
}
