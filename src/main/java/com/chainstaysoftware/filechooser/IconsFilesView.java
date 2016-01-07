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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
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
   private OrderBy currentOrderBy;

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
      gridView.setContextMenu(createContextMenu());

      currentOrderBy = OrderBy.Name;
   }

   private ContextMenu createContextMenu() {
      final MenuItem nameItem = new MenuItem();
      nameItem.setId("nameMenuItem");
      nameItem.setText("Name");
      nameItem.onActionProperty().setValue(event -> sort(OrderBy.Name));

      final MenuItem sizeItem = new MenuItem();
      sizeItem.setId("sizeMenuItem");
      sizeItem.setText("Size");
      sizeItem.onActionProperty().setValue(event -> sort(OrderBy.Size));

      final MenuItem typeItem = new MenuItem();
      typeItem.setId("typeMenuItem");
      typeItem.setText("Type");
      typeItem.onActionProperty().setValue(event -> sort(OrderBy.Type));

      final MenuItem dateItem = new MenuItem();
      dateItem.setId("dateMenuItem");
      dateItem.setText("Modification Date");
      dateItem.onActionProperty().setValue(event -> sort(OrderBy.ModificationDate));

      final Menu sortOrderMenu = new Menu();
      sortOrderMenu.setId("sortOrderMenu");
      sortOrderMenu.setText("Arrange By");
      sortOrderMenu.getItems().addAll(nameItem, sizeItem, typeItem, dateItem);

      final ContextMenu contextMenu = new ContextMenu();
      contextMenu.getItems().addAll(sortOrderMenu);

      return contextMenu;
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
      setFiles(fileStream, currentOrderBy);
   }

   private void setFiles(final Stream<File> fileStream,
                         final OrderBy orderBy) {
      selectedCellIndex.setValue(NOT_SELECTED);

      gridView.getItems().setAll(getIcons(sort(fileStream, orderBy)));

      selectCurrent();
      currentOrderBy = orderBy;
   }

   /**
    * Sort the existing view contents.
    */
   private void sort(final OrderBy orderBy) {
      setFiles(getFileStream(), orderBy);
   }

   /**
    * Sort the passed in Stream<File>
    */
   private Stream<File> sort(final Stream<File> fileStream,
                             final OrderBy orderBy) {
      // TODO: Move into own class and write junit..
      return fileStream.sorted((o1, o2) -> {
         if (OrderBy.ModificationDate.equals(orderBy)) {
            if (o1.lastModified() < o2.lastModified()) return -1;
            if (o1.lastModified() > o2.lastModified()) return 1;
            return 0;
         }

         if (OrderBy.Size.equals(orderBy)) {
            if (o1.length() < o2.length()) return -1;
            if (o1.length() > o2.length()) return 1;
            return 0;
         }

         if (OrderBy.Type.equals(orderBy)) {
            if (o1.isDirectory()) {
               if (o2.isDirectory()) {
                  return o1.compareTo(o2);
               }

               return -1;
            } else if (o2.isDirectory()) {
               return 1;
            }

            final String extension1 = FilenameUtils.getExtension(o1.getName());
            final String extension2 = FilenameUtils.getExtension(o2.getName());
            return extension1.compareTo(extension2);
         }

         return o1.getName().compareTo(o2.getName());
      });
   }

   private Stream<File> getFileStream() {
      return gridView.getItems().stream().map(DirectoryListItem::getFile);
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
         final ContextMenu contextMenu = createContextMenu();

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

         contextMenu.getItems().addAll(new SeparatorMenuItem(), imagePreviewItem);
         return contextMenu;
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
