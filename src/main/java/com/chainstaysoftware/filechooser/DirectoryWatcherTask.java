package com.chainstaysoftware.filechooser;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;

import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Task to watch for file system changes in the currentDirectory.
 */
class DirectoryWatcherTask extends Task {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.DirectoryWatcherTask");

   private final Stage owner;
   private final WatchService watcher;
   private final FilesViewCallback callback;

   public DirectoryWatcherTask(final Stage owner,
                               final WatchService watcher,
                               final FilesViewCallback callback) {
      this.owner = owner;
      this.watcher = watcher;
      this.callback = callback;
   }

   @Override
   protected Object call() throws Exception {
      while (true) {
         WatchKey key;
         try {
            final long timeout = 5;
            key = watcher.poll(timeout, TimeUnit.SECONDS);
         } catch (InterruptedException ex) {
            return null;
         }

         if (shouldShutdownTask()) {
            break;
         }

         if (key == null) {
            continue;
         }

         for (WatchEvent<?> ignored : key.pollEvents()) {
            logger.log(Level.FINE, "Directory contents changed. Updating view.");
            Platform.runLater(callback::updateFiles);
         }

         boolean valid = key.reset();
         if (!valid) {
            break;
         }
      }

      logger.log(Level.FINE, "exiting watcher task");
      return null;
   }

   private Boolean shouldShutdownTask() {
      return (Boolean)owner.getUserData();
   }
}


