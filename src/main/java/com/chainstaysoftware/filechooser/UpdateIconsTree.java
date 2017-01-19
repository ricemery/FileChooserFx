package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.icons.IconsImpl;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * JavaFx Service to update the {@link TreeItem<File>} associated with directory items contained
 * within a {@link List} of {@link TreeItem<File>}. When a {@link TreeItem<File>} is
 * found that is associated with a directory, then the item is swapped out with
 * a {@link DirectoryTreeItem} instance.
 * This code is not run on the main JavaFx thread so that calls to File.isDirectory
 * are run in the background.
 */
class UpdateIconsTree extends Service<Void> {
   private final List<TreeItem<File>> itemList;
   private final FilesViewCallback callback;
   private final PopulateTreeItemRunnableFactory factory;
   private final Icons icons;

   private final CountDownLatch latch = new CountDownLatch(1);


   UpdateIconsTree(final List<TreeItem<File>> itemList,
                   final FilesViewCallback callback,
                   final PopulateTreeItemRunnableFactory factory,
                   final Icons icons) {
      this.itemList = itemList;
      this.callback = callback;
      this.factory = factory;
      this.icons = icons;
   }

   protected Task<Void> createTask() {
      return new UpdateTask();
   }

   private class UpdateTask extends Task<Void> {
      @Override
      protected Void call() throws Exception {
         updateIcons();

         latch.await();

         return null;
      }

      private void updateIcons() {
         final List<TreeItem<File>> temp = new LinkedList<>();
         temp.addAll(itemList);

         for (TreeItem<File> item: temp) {
            if (isCancelled()) {
               return;
            }

            final File file = item.getValue();
            if (file.isDirectory()) {
               Platform.runLater(() -> {
                  itemList.remove(item);

                  final ImageView graphic = new ImageView(icons.getIcon(IconsImpl.FOLDER_64));
                  graphic.setFitWidth(IconsImpl.SMALL_ICON_WIDTH);
                  graphic.setFitHeight(IconsImpl.SMALL_ICON_HEIGHT);
                  graphic.setPreserveRatio(true);

                  itemList.add(new DirectoryTreeItem(file, graphic, callback, factory, icons, false));
               });
            }
         }

         Platform.runLater(latch::countDown);
      }
   }
}
