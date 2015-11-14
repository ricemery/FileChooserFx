package com.chainstaysoftware.filechooser.icons;

import javafx.scene.image.Image;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Icons {
   public static final int SMALL_ICON_WIDTH = 25;
   public static final int SMALL_ICON_HEIGHT = 25;

   public static final String BACK_ARROW_24 = "/icons8/24x24/Back-24.png";
   public static final String ICON_VIEW_24 = "/icons8/24x24/Small Icons-24.png";
   public static final String LIST_VIEW_24 = "/icons8/24x24/List-24.png";
   public static final String LIST_WITH_PREVIEW_VIEW_24 = "/icons8/24x24/Columns-24.png";

   public static final String FOLDER_64 = "/icons8/64x64/Folder-64.png";
   public static final String HARDDISK_64 = "/icons8/64x64/HDD-64.png";
   public static final String OPEN_FOLDER_64 = "/icons8/64x64/Open Folder-64.png";
   public static final String USER_HOME_64 = "/icons8/64x64/User Folder-64.png";

   public static final String GENERIC_FILE_64 = "/icons8/64x64/File-64.png";
   public static final String JPG_64 = "/icons8/64x64/JPG-64.png";
   public static final String PDF_64 = "/icons8/64x64/PDF-64.png";
   public static final String PNG_64 = "/icons8/64x64/PNG-64.png";
   public static final String PS_64 = "/icons8/64x64/PS-64.png";
   public static final String TXT_64 = "/icons8/64x64/TXT-64.png";
   public static final String XML_64 = "/icons8/64x64/XML-64.png";

   private static final Map<String, String> fileTypeIcons = new HashMap<>();
   static {
      fileTypeIcons.put("jpg", JPG_64);
      fileTypeIcons.put("pdf", PDF_64);
      fileTypeIcons.put("png", PNG_64);
      fileTypeIcons.put("ps", PS_64);
      fileTypeIcons.put("txt", TXT_64);
      fileTypeIcons.put("xml", XML_64);
   }

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
      if (file.isDirectory()) {
         return getIcon(Icons.FOLDER_64);
      }

      final String extension = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ENGLISH);
      final String iconFile = fileTypeIcons.get(extension);
      if (iconFile == null) {
         return getIcon(Icons.GENERIC_FILE_64);
      }

      return getIcon(iconFile);
   }
}

