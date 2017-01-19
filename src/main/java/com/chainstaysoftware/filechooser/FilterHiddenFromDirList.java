package com.chainstaysoftware.filechooser;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;

/**
 * JavaFx Service to possibly filter out hidden files from
 * a {@link List} of {@link DirectoryListItem}. This code is
 * not run on the main JavaFx thread to avoid calling the slow
 * File.isHidden method.
 */
class FilterHiddenFromDirList extends Service<Void> {
   private final List<DirectoryListItem> itemList;
   private final Predicate<File> shouldHideFile;
   private final CountDownLatch latch = new CountDownLatch(1);

   FilterHiddenFromDirList(final List<DirectoryListItem> itemList,
                           final Predicate<File> shouldHideFile) {
      this.itemList = itemList;
      this.shouldHideFile = shouldHideFile;
   }

   protected Task<Void> createTask() {
      return new UpdateListTask();
   }

   private class UpdateListTask extends Task<Void> {
      @Override
      protected Void call() throws Exception {
         filterHidden();

         latch.await();

         return null;
      }

      // Move filter and icon update to another thread.
      private void filterHidden() {
         final List<DirectoryListItem> temp = new LinkedList<>();
         temp.addAll(itemList);

         for (DirectoryListItem item : temp) {
            if (isCancelled()) {
               return;
            }

            if (!shouldHideFile.test(item.getFile())) {
               Platform.runLater(() -> itemList.remove(item));
            }
         }

         Platform.runLater(latch::countDown);
      }
   }
}
