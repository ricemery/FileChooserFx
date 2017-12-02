package com.chainstaysoftware.filechooser;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Task to watch for file system changes.
 */
class DirectoryWatcherTask extends Task<Void> {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.DirectoryWatcherTask");

   private final File directory;
   private final FilesViewCallback callback;

   public DirectoryWatcherTask(final File directory,
                               final FilesViewCallback callback) {
      this.directory = directory;
      this.callback = callback;
   }

   @Override
   protected Void call() {
      try (FileSystem fileSystem = directory.toPath().getFileSystem();
           WatchService watcher = fileSystem.newWatchService()) {
         directory.toPath().register(watcher,
                 StandardWatchEventKinds.ENTRY_CREATE,
                 StandardWatchEventKinds.ENTRY_DELETE,
                 StandardWatchEventKinds.ENTRY_MODIFY);
         while (!isCancelled()) {
            WatchKey key = watcher.take();

            if (key.pollEvents().isEmpty()) {
               continue;
            }

            logger.log(Level.FINE, "Directory contents changed. Updating view.");
            Platform.runLater(callback::updateFiles);

            boolean valid = key.reset();
            if (!valid) {
               break;
            }
         }
      } catch (InterruptedException e) {
         if (!isCancelled()) {
            logger.log(Level.FINE, "interrupted while waiting for key: " + e.getMessage());
         }
      } catch (IOException e) {
         logger.log(Level.FINE, "watching " + directory + " failed: " + e.getMessage());
      }

      logger.log(Level.FINE, "exiting watcher task");
      return null;
   }
}
