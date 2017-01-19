package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFx Service to update a {@link List} of {@link DirectoryListItem} with
 * the files/directories found at the passed in {@link DirectoryStream}. This code
 * is not run on the JavaFx thread so that the UI does not block while retreiving the
 * list of files from the OS.
 */
class UpdateDirectoryList extends Service<Void> {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.UpdateDirectoryList");

   private final DirectoryStream<Path> dirStream;
   private final DirectoryStream<Path> unfilteredDirStream;
   private final List<DirectoryListItem> itemList;
   private final Icons icons;
   private final CountDownLatch latch = new CountDownLatch(1);

   /**
    * Constructor
    * @param dirStream Stream of all files/directories that matched the used file filter.
    * @param unfilteredDirStream Stream of all files/directories that did NOT match the used file
    *                            filter. This is used to find directories that were excluded
    *                            by the filter.
    * @param itemList List to update with results.
    * @param icons Icon retriever.
    */
   UpdateDirectoryList(final DirectoryStream<Path> dirStream,
                       final DirectoryStream<Path> unfilteredDirStream,
                       final List<DirectoryListItem> itemList,
                       final Icons icons) {
      this.dirStream = dirStream;
      this.unfilteredDirStream = unfilteredDirStream;
      this.itemList = itemList;
      this.icons = icons;
   }

   protected Task<Void> createTask() {
      return new UpdateListTask();
   }

   private class UpdateListTask extends Task<Void> {
      @Override
      protected Void call() throws Exception {
         updateFiltered();
         if (isCancelled()) {
            return null;
         }

         updateUnfiltered();

         latch.await();

         return null;
      }

      private void updateFiltered()  {
         try {
            for (Path path: dirStream) {
               if (isCancelled()) {
                  return;
               }

               final DirectoryListItem dirListItem = getDirListItem(path.toFile());
               Platform.runLater(() -> itemList.add(dirListItem));
            }
         } finally {
            closeStream(dirStream);
         }
      }

      private void updateUnfiltered() {
         try {
            for (Path path: unfilteredDirStream) {
               if (isCancelled()) {
                  return;
               }

               final File file = path.toFile();
               if (file.isDirectory()) {
                  // Hard code icon to directory.
                  final DirectoryListItem dirListItem = getDirListItem(path.toFile());
                  Platform.runLater(() -> itemList.add(dirListItem));
               }
            }

            Platform.runLater(latch::countDown);
         } finally {
            closeStream(unfilteredDirStream);
         }
      }

      private void closeStream(final DirectoryStream<Path> directoryStream) {
         try {
            directoryStream.close();
         } catch (IOException e) {
            logger.log(Level.WARNING, "Error closing directory stream", e);
         }
      }
   }

   private DirectoryListItem getDirListItem(final File file) {
      return new DirectoryListItem(file, icons.getIconForFile(file));
   }
}
