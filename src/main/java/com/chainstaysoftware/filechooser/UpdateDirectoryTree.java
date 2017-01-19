package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.icons.IconsImpl;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

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
   UpdateDirectoryTree(final DirectoryStream<Path> dirStream,
                       final DirectoryStream<Path> unfilteredDirStream,
                       final List<TreeItem<File>> itemList,
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
         final ImageView graphic = new ImageView(icons.getIconForFile(file));
         graphic.setFitWidth(IconsImpl.SMALL_ICON_WIDTH);
         graphic.setFitHeight(IconsImpl.SMALL_ICON_HEIGHT);
         graphic.setPreserveRatio(true);

         // Assume for now, that this is a leaf node. If the file is a directory, the TreeItem will
         // be replaced later in the UpdateIconsTree service.
         return new DirectoryTreeItem(file, graphic, icons, true);
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
