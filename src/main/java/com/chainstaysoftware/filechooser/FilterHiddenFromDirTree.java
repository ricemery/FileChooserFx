package com.chainstaysoftware.filechooser;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;

/**
 * JavaFx Service to possibly filter out hidden files from
 * a {@link List} of {@link TreeItem<File>}. This code is
 * not run on the main JavaFx thread to avoid calling the slow
 * File.isHidden method.
 */
class FilterHiddenFromDirTree extends Service<Void> {
   private final List<TreeItem<File>> itemList;
   private final Predicate<File> shouldHideFile;

   private final CountDownLatch latch = new CountDownLatch(1);

   FilterHiddenFromDirTree(final List<TreeItem<File>> itemList,
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
         final List<TreeItem<File>> filterItems = new LinkedList<>();

         for (TreeItem<File> item : itemList) {
            if (isCancelled()) {
               return;
            }

            if (!shouldHideFile.test(item.getValue())) {
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

      private boolean shouldSchedule(final List<TreeItem<File>> items) {
         return items.size() % 100 == 0;
      }

      private void scheduleJavaFx(final List<TreeItem<File>> items) {
         final List<TreeItem<File>> temp = new LinkedList<>();
         temp.addAll(items);

         Platform.runLater(() -> temp.forEach(itemList::remove));
      }
   }
}
