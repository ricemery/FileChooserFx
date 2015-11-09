package com.chainstaysoftware.filechooser;

import javafx.scene.image.Image;

import java.io.File;
import java.io.InputStream;

public class Icons {
   public static final int SMALL_ICON_WIDTH = 25;
   public static final int SMALL_ICON_HEIGHT = 25;

   public static final String BACK_ARROW_24 = "/icons8/24x24/Back-24.png";
   public static final String ICON_VIEW_24 = "/icons8/24x24/Small Icons-24.png";
   public static final String LIST_VIEW_24 = "/icons8/24x24/List-24.png";

   public static final String FOLDER_64 = "/icons8/64x64/Folder-64.png";
   public static final String GENERIC_FILE_64 = "/icons8/64x64/File-64.png";
   public static final String HARDDISK_64 = "/icons8/64x64/HDD-64.png";
   public static final String OPEN_FOLDER_64 = "/icons8/64x64/Open Folder-64.png";
   public static final String USER_HOME_64 = "/icons8/64x64/User Folder-64.png";

   /**
    * Load an icon from the CURSOR_PATH location in the class path.
    * @param resourceName Must be a filename of an icon located in the
    *                     CURSOR_PATH location of the classpath.
    * @return {@link Image}
    * @throws IllegalArgumentException If the resource is not found.
    */
   public Image getIcon(final String resourceName) {
      if (resourceName == null) {
         throw new IllegalArgumentException("resourceName must not be null");
      }

      final InputStream inputStream = getClass().getResourceAsStream(resourceName);
      if (inputStream == null) {
         throw new IllegalArgumentException(resourceName + " is not a valid resource");
      }

      return new Image(inputStream);
   }

   public Image getIconForFile(final File file) {
      // TODO: Optimize
      return file.isDirectory()
            ? getIcon(Icons.FOLDER_64)
            : getIcon(Icons.GENERIC_FILE_64);
   }
}

