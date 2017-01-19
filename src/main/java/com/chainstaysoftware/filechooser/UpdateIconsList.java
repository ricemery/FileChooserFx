package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.icons.IconsImpl;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * JavaFx Service to update the Icons associated with directory items contained
 * within a {@link List} of {@link DirectoryListItem}.
 * This code is not run on the main JavaFx thread so that calls to File.isDirectory
 * are run in the background.
 */
class UpdateIconsList extends Service<Void> {
   private final List<DirectoryListItem> itemList;
   private final Icons icons;
   private final CountDownLatch latch = new CountDownLatch(1);

   UpdateIconsList(final List<DirectoryListItem> itemList,
                   final Icons icons) {
      this.itemList = itemList;
      this.icons = icons;
   }

   protected Task<Void> createTask() {
      return new UpdateListTask();
   }

   private class UpdateListTask extends Task<Void> {
      @Override
      protected Void call() throws Exception {
         updateIcons();

         latch.await();

         return null;
      }

      private void updateIcons() {
         final List<DirectoryListItem> temp = new LinkedList<>();
         temp.addAll(itemList);

         for (DirectoryListItem item: temp) {
            if (isCancelled()) {
               return;
            }

            if (item.getFile().isDirectory()) {
               Platform.runLater(() -> {
                  itemList.remove(item);
                  itemList.add(new DirectoryListItem(item.getFile(), icons.getIcon(IconsImpl.FOLDER_64)));
               });
            }
         }

         Platform.runLater(latch::countDown);
      }
   }
}
