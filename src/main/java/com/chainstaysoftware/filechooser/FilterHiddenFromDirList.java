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
         final List<DirectoryListItem> filterItems = new LinkedList<>();

         for (DirectoryListItem item : itemList) {
            if (isCancelled()) {
               return;
            }

            if (!shouldHideFile.test(item.getFile())) {
               filterItems.add(item);

               if (shouldSchedule(filterItems)) {
                  scheduleJavaFx(filterItems);
                  filterItems.clear();
               }
            }
         }

         scheduleJavaFx(filterItems);
         Platform.runLater(latch::countDown);
      }

      private boolean shouldSchedule(final List<DirectoryListItem> items) {
         return items.size() % 100 == 0;
      }

      private void scheduleJavaFx(final List<DirectoryListItem> items) {
         final List<DirectoryListItem> temp = new LinkedList<>();
         temp.addAll(items);

         Platform.runLater(() -> temp.forEach(itemList::remove));
      }
   }
}
