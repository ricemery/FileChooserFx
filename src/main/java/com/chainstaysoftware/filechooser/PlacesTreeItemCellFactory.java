package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.IconsImpl;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CellFactory for rendering {@link PlacesTreeItem} instances within a
 * {@link TreeView}
 */
public class PlacesTreeItemCellFactory implements Callback<TreeView<PlacesTreeItem>, TreeCell<PlacesTreeItem>> {
   private final ObservableList<File> favoriteDirs;

   public PlacesTreeItemCellFactory(final ObservableList<File> favoriteDirs) {
      this.favoriteDirs = favoriteDirs;
   }

   @Override
   public TreeCell<PlacesTreeItem> call(TreeView<PlacesTreeItem> param) {
      return new PlacesTreeItemCell();
   }

   private class PlacesTreeItemCell extends TreeCell<PlacesTreeItem> {
      public PlacesTreeItemCell() {
         setOnDragDetected(new DragDetectedHandler());
         setOnDragOver(new DragOverHandler());
         setOnDragEntered(new DragEnteredHandler());
         setOnDragExited(new DragExitedHandler());
         setOnDragDropped(new DragDroppedHandler());
      }

      @Override
      protected void updateItem(PlacesTreeItem item, boolean empty) {
         super.updateItem(item, empty);

         if (empty || item == null) {
            setText(null);
            setGraphic(null);
         } else if (!item.getFile().isPresent()) {
            setText(item.getText().orElse(""));
            setGraphic(toGraphic(item.getIcon()));
         } else {
            final File file = item.getFile().orElseThrow(IllegalStateException::new);
            final String systemDisplayName = FileSystemView.getFileSystemView().getSystemDisplayName(file);
            setText("".equals(systemDisplayName) ? file.toString() : systemDisplayName);
            setGraphic(toGraphic(item.getIcon()));
         }
      }

      /**
       * Handler to setup the {@link Dragboard} if the {@link PlacesTreeItemCell}
       * supports drag and drop. Note that drag and drop is only supported to
       * reorder favorites.
       */
      private class DragDetectedHandler implements EventHandler<MouseEvent> {
         @Override
         public void handle(MouseEvent mouseEvent) {
            if (!getTreeItem().isLeaf() || !getItem().isFavorite()) {
               return;
            }

            final SnapshotParameters snapshotParameters = new SnapshotParameters();
            snapshotParameters.setFill(Color.TRANSPARENT);
            final Dragboard dragboard = getTreeView().startDragAndDrop(TransferMode.MOVE);
            dragboard.setDragView(snapshot(snapshotParameters, null));
            final ClipboardContent content = new ClipboardContent();
            content.putFiles(Collections.singletonList(getItem().getFile().get()));
            dragboard.setContent(content);
            mouseEvent.consume();
         }
      }

      /**
       * Handler to indicate that the {@link PlacesTreeItemCell} can accept
       * move operations (if supported).
       */
      private class DragOverHandler implements EventHandler<DragEvent> {
         @Override
         public void handle(DragEvent event) {
            // Only supporting reordering of favorites.
            if (event.getDragboard().hasFiles() && getItem() != null && getItem().isFavorite()) {
               event.acceptTransferModes(TransferMode.MOVE);
            }
         }
      }

      /**
       * Handler to apply styling to {@link PlacesTreeItemCell} when drag
       * has moved onto a cell. Only applies styling if the cell can accept
       * move operations.
       */
      private class DragEnteredHandler implements EventHandler<DragEvent> {
         @Override
         public void handle(DragEvent event) {
            if (getItem() == null || !getItem().isFavorite()) {
               return;
            }

            final File draggedFile = event.getDragboard().getFiles().get(0);
            if (getItem().getFile().equals(Optional.of(draggedFile))) {
               // NOOP.
               return;
            }

            getStyleClass().add("places-droptarget");
         }
      }

      /**
       * Handler to remove styling when drag has moved off of cell.
       */
      private class DragExitedHandler implements EventHandler<DragEvent> {
         @Override
         public void handle(DragEvent event) {
            getStyleClass().remove("places-droptarget");
         }
      }

      /**
       * Handler to apply move (drop) operation.
       */
      private class DragDroppedHandler implements EventHandler<DragEvent> {
         @Override
         public void handle(DragEvent event) {
            final TreeItem<PlacesTreeItem> dropTarget = getTreeItem();
            final File draggedFile = event.getDragboard().getFiles().get(0);

            if (dropTarget.getValue().getFile().equals(Optional.of(draggedFile))) {
               // NOOP.
               return;
            }

            if (!dropTarget.isLeaf()) {
               moveToHead(dropTarget, draggedFile);
            } else {
               moveAfter(dropTarget, draggedFile);
            }
         }

         /**
          * Move the draggedDir to the head of the list. Update the View and
          * the favoriteDirs property.
          */
         private void moveToHead(final TreeItem<PlacesTreeItem> dropTarget,
                                 final File draggedDir) {
            final List<TreeItem<PlacesTreeItem>> currentItems = dropTarget.getChildren();
            if (currentItems.isEmpty()) {
               // NOOP
               return;
            }

            final List<TreeItem<PlacesTreeItem>> updated = new LinkedList<>();
            updated.add(null);
            for (TreeItem<PlacesTreeItem> item : currentItems) {
               if (item.getValue().getFile().equals(Optional.of(draggedDir))) {
                  updated.set(0, item);
               } else {
                  updated.add(item);
               }
           }

            dropTarget.getChildren().setAll(updated);
            updateFavoriteDirsProperty(updated);
         }

         /**
          * Move the draggedDir to the position After the dropTarget. Update the View and
          * the favoriteDirs property.
          */
         private void moveAfter(final TreeItem<PlacesTreeItem> dropTarget,
                                final File draggedFile) {
            final List<TreeItem<PlacesTreeItem>> currentItems = dropTarget.getParent().getChildren();
            final List<TreeItem<PlacesTreeItem>> updated = new LinkedList<>();

            final TreeItem<PlacesTreeItem> draggedItem = currentItems.stream()
                  .filter(ti -> ti.getValue().getFile().equals(Optional.of(draggedFile)))
                  .findFirst()
                  .orElseThrow(IllegalStateException::new);

            for (TreeItem<PlacesTreeItem> item : currentItems) {
               if (item.equals(draggedItem)) {
                  continue;
               }

               updated.add(item);

               if (item.equals(dropTarget)) {
                  // insert draggedItem after this item.
                  updated.add(draggedItem);
               }
            }

            dropTarget.getParent().getChildren().setAll(updated);
            updateFavoriteDirsProperty(updated);
         }

         /**
          * Update the favoriteDirs property, so that the user can optionally
          * persist the new ordering.
          */
         private void updateFavoriteDirsProperty(final List<TreeItem<PlacesTreeItem>> updated) {
            final List<File> updatedFavDirs = updated.stream()
                  .map(ti -> ti.getValue().getFile().get())
                  .collect(Collectors.toList());
            favoriteDirs.setAll(updatedFavDirs);
         }
      }

      /**
       * Create an {@link ImageView} from an {@link Image}
       */
      private ImageView toGraphic(final Image image) {
         if (image == null) {
            return null;
         }

         final ImageView graphic = new ImageView(image);
         graphic.setFitWidth(IconsImpl.SMALL_ICON_WIDTH);
         graphic.setFitHeight(IconsImpl.SMALL_ICON_HEIGHT);
         graphic.setPreserveRatio(true);
         return graphic;
      }
   }
}
