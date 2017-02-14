package com.chainstaysoftware.filechooser;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFx Service to update a {@link List} of {@link TreeItem} with
 * the files/directories found at the passed in {@link DirectoryStream}. This code
 * is not run on the JavaFx thread so that the UI does not block while retreiving the
 * list of files from the OS.
 */
class UpdateDirectoryTree extends Service<Void> {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.UpdateDirectoryTree");

   private final DirectoryStream<Path> dirStream;
   private final DirectoryStream<Path> unfilteredDirStream;
   private final List<TreeItem<File>> itemList;
   private final FilesViewCallback callback;
   private final PopulateTreeItemRunnableFactory populateFactory;

   private final CountDownLatch latch = new CountDownLatch(1);

   /**
    * Constructor
    * @param dirStream Stream of all files/directories that matched the used file filter.
    * @param unfilteredDirStream Stream of all files/directories that did NOT match the used file
    *                            filter. This is used to find directories that were excluded
    *                            by the filter.
    * @param itemList List to update with results.
    * @param callback {@link FilesViewCallback} impl.
    * @param populateFactory factory for creating {@link com.chainstaysoftware.filechooser.ListFilesView.PopulateTreeItemRunnable}
    *                        instances.
    */
   UpdateDirectoryTree(final DirectoryStream<Path> dirStream,
                       final DirectoryStream<Path> unfilteredDirStream,
                       final List<TreeItem<File>> itemList,
                       final FilesViewCallback callback,
                       final PopulateTreeItemRunnableFactory populateFactory) {
      this.dirStream = dirStream;
      this.unfilteredDirStream = unfilteredDirStream;
      this.itemList = itemList;
      this.callback = callback;
      this.populateFactory = populateFactory;
   }

   protected Task<Void> createTask() {
      return new UpdateListTask();
   }

   private class UpdateListTask extends Task<Void> {
      @Override
      protected Void call() throws Exception {
         update(dirStream, false);
         if (isCancelled()) {
            return null;
         }

         update(unfilteredDirStream, true);

         Platform.runLater(latch::countDown);

         latch.await();

         return null;
      }

      private void update(final DirectoryStream<Path> directoryStream,
                          final boolean dirOnly)  {
         final List<TreeItem<File>> itemsToAdd = new LinkedList<>();

         try {
            for (Path path: directoryStream) {
               if (isCancelled()) {
                  return;
               }

               if (!dirOnly || path.toFile().isDirectory()) {
                  final TreeItem<File> treeItem = getTreeItem(path.toFile());
                  itemsToAdd.add(treeItem);

                  if (shouldSchedule(itemsToAdd)) {
                     scheduleJavaFx(itemsToAdd);
                     itemsToAdd.clear();
                  }
               }
            }

            scheduleJavaFx(itemsToAdd);
         } finally {
            closeStream(dirStream);
         }
      }

      private boolean shouldSchedule(final List<TreeItem<File>> items) {
         return items.size() % 100 == 0;
      }

      private void scheduleJavaFx(final List<TreeItem<File>> items) {
         final List<TreeItem<File>> temp = new LinkedList<>();
         temp.addAll(items);

         Platform.runLater(() -> itemList.addAll(temp));
      }

      private TreeItem<File> getTreeItem(final File file) {
         return new DirectoryTreeItem(file, callback, populateFactory);
      }

      private void closeStream(final DirectoryStream<Path> directoryStream) {
         try {
            directoryStream.close();
         } catch (IOException e) {
            logger.log(Level.WARNING, "Error closing directory stream", e);
         }
      }
   }
}
