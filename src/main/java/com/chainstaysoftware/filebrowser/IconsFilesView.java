package com.chainstaysoftware.filebrowser;

import com.chainstaysoftware.filebrowser.preview.PreviewWindow;
import impl.org.controlsfx.skin.GridViewSkin;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.GridView;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class IconsFilesView implements FilesView {
   private final static int NOT_SELECTED = -1;

   // Width and Height of Icon and Filename Cell.
   private static final int CELL_HEIGHT = 90;
   private static final int CELL_WIDTH = 90;

   private final Stage parent;
   private final GridView<Pair<Image, File>> gridView = new GridView<>();
   private final Map<String, PreviewWindow> previewHandlers;
   private final Icons icons = new Icons();

   private IntegerProperty selectedCell = new SimpleIntegerProperty(NOT_SELECTED);
   private FilesViewCallback callback;
   private EventHandler<? super KeyEvent> keyEventHandler;

   public IconsFilesView(final Stage parent,
                         final Map<String, PreviewWindow> previewHandlers) {
      this.parent = parent;
      this.previewHandlers = previewHandlers;

      gridView.setCellFactory(gridView1 -> {
         final IconGridCell cell = new IconGridCell(true, new IconGridCellContextMenuFactImpl());
         selectedCell.addListener((observable, oldValue, newValue) ->
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
      selectedCell.setValue(NOT_SELECTED);

      gridView.getItems().setAll(getIcons(fileStream));
   }

   public void setOnKeyPressed(final EventHandler<? super KeyEvent> eventHandler) {
      this.keyEventHandler = eventHandler;
   }

   private List<Pair<Image, File>> getIcons(final Stream<File> fileStream) {
      return fileStream.map(this::getIconPair).collect(Collectors.toList());
   }

   private Pair<Image, File> getIconPair(final File file) {
      return new Pair<>(icons.getIconForFile(file), file);
   }

   private class IconGridCellContextMenuFactImpl implements IconGridCellContextMenuFactory {
      @Override
      public ContextMenu create(final Pair<Image, File> pair) {
         final File file = pair.getValue();
         if (file.isDirectory()) {
            return null;
         }

         final String fileExtension = FilenameUtils.getExtension(file.getName());
         final PreviewWindow previewWindow = previewHandlers.get(fileExtension.toLowerCase());
         if (previewWindow == null) {
            return null;
         }

         final MenuItem imagePreviewItem = new MenuItem("Preview");
         imagePreviewItem.setOnAction(v -> previewWindow.showPreview(parent, file));

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
            if (!file.isDirectory()) {
               return;
            }

            System.out.println("Double click on - " + file.toString());

            callback.requestChangeDirectory(file);
         } else {
            selectedCell.setValue(target.getIndex());

            final File file = gridView.getItems().get(selectedCell.get()).getValue();
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
            selectedCell.setValue(Math.max(0, selectedCell.get() - 1));
            event.consume();
         } else if (keyCode == KeyCode.RIGHT) {
            selectedCell.setValue(Math.min(selectedCell.get() + 1, gridView.getItems().size() - 1));
            event.consume();
         } else if (keyCode == KeyCode.UP) {
            selectedCell.setValue(Math.max(0, selectedCell.get() - ((GridViewSkin)gridView.getSkin()).computeMaxCellsInRow()));
            event.consume();
         }  else if (keyCode == KeyCode.DOWN) {
            selectedCell.setValue(Math.min(gridView.getItems().size() - 1, selectedCell.get() + ((GridViewSkin)gridView.getSkin()).computeMaxCellsInRow()));
            event.consume();
         } else if (keyCode == KeyCode.ENTER) {
            if (selectedCell.get() == NOT_SELECTED) {
               return;
            }

            final Pair<Image, File> pair = gridView.getItems().get(selectedCell.get());
            final File file = pair.getValue();
            if (!file.isDirectory()) {
               return;
            }

            callback.requestChangeDirectory(file);
            event.consume();
         }

         if (selectedCell.get() == NOT_SELECTED) {
            return;
         }

         final File file = gridView.getItems().get(selectedCell.get()).getValue();
         callback.setCurrentSelection(file);
      }
   }
}
