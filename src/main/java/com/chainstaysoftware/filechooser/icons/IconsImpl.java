package com.chainstaysoftware.filechooser.icons;

import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Icon loading.
 */
public class IconsImpl implements Icons {
   public static final int SMALL_ICON_WIDTH = 25;
   public static final int SMALL_ICON_HEIGHT = 25;

   public static final String BACK_ARROW_24 = "/icons8/24x24/Back-24.png";
   public static final String ICON_VIEW_24 = "/icons8/24x24/Small Icons-24.png";
   public static final String LIST_VIEW_24 = "/icons8/24x24/List-24.png";
   public static final String LIST_WITH_PREVIEW_VIEW_24 = "/icons8/24x24/Columns-24.png";

   public static final String COMPUTER_64 = "/icons8/64x64/Computer-64.png";
   public static final String STAR_64 = "/icons8/64x64/Star Filled-64.png";
   public static final String FOLDER_64 = "/icons8/64x64/Folder-64.png";
   public static final String HARDDISK_64 = "/icons8/64x64/HDD-64.png";
   public static final String CD_64 = "/icons8/64x64/CD-64.png";
   public static final String DVD_64 = "/icons8/64x64/CD-64.png";
   public static final String FLOPPY_64 = "/icons8/64x64/Save-64.png";
   public static final String NETWORK_64 = "/icons8/64x64/Network-64.png";
   public static final String USB_64 = "/icons8/64x64/USB 2-64.png";
   public static final String OPEN_FOLDER_64 = "/icons8/64x64/Open Folder-64.png";
   public static final String USER_HOME_64 = "/icons8/64x64/User Folder-64.png";

   public static final String GENERIC_FILE_64 = "/icons8/64x64/File-64.png";
   public static final String AAC_64 = "/icons8/64x64/AAC-64.png";
   public static final String AVI_64 = "/icons8/64x64/AVI-64.png";
   public static final String CONSOLE_64 = "/icons8/64x64/Console-64.png";
   public static final String CSS_64 = "/icons8/64x64/CSS-64.png";
   public static final String EXE_64 = "/icons8/64x64/EXE-64.png";
   public static final String GIF_64 = "/icons8/64x64/GIF-64.png";
   public static final String HTML_64 = "/icons8/64x64/HTML-64.png";
   public static final String JPG_64 = "/icons8/64x64/JPG-64.png";
   public static final String MOV_64 = "/icons8/64x64/MOV-64.png";
   public static final String MP3_64 = "/icons8/64x64/MP3-64.png";
   public static final String PDF_64 = "/icons8/64x64/PDF-64.png";
   public static final String PNG_64 = "/icons8/64x64/PNG-64.png";
   public static final String POWERPOINT_64 = "/icons8/64x64/PowerPoint-64.png";
   public static final String PS_64 = "/icons8/64x64/PS-64.png";
   public static final String TXT_64 = "/icons8/64x64/TXT-64.png";
   public static final String WMA_64 = "/icons8/64x64/WMA-64.png";
   public static final String WORD_64 = "/icons8/64x64/Word-64.png";
   public static final String XLS_64 = "/icons8/64x64/XLS-64.png";
   public static final String XML_64 = "/icons8/64x64/XML-64.png";
   public static final String ZIP_64 = "/icons8/64x64/ZIP-64.png";

   private final MapProperty<String, Image> fileTypeIconsProperty = new SimpleMapProperty<>();

   public IconsImpl() {
      final Map<String, Image> fileTypeIcons = new HashMap<>();
      fileTypeIcons.put("aac", getIcon(AAC_64));
      fileTypeIcons.put("avi", getIcon(AVI_64));
      fileTypeIcons.put("cmd", getIcon(CONSOLE_64));
      fileTypeIcons.put("css", getIcon(CSS_64));
      fileTypeIcons.put("doc", getIcon(WORD_64));
      fileTypeIcons.put("docx", getIcon(WORD_64));
      fileTypeIcons.put("exe", getIcon(EXE_64));
      fileTypeIcons.put("gif", getIcon(GIF_64));
      fileTypeIcons.put("html", getIcon(HTML_64));
      fileTypeIcons.put("jpg", getIcon(JPG_64));
      fileTypeIcons.put("mov", getIcon(MOV_64));
      fileTypeIcons.put("mp3", getIcon(MP3_64));
      fileTypeIcons.put("pdf", getIcon(PDF_64));
      fileTypeIcons.put("png", getIcon(PNG_64));
      fileTypeIcons.put("ppt", getIcon(POWERPOINT_64));
      fileTypeIcons.put("pptm", getIcon(POWERPOINT_64));
      fileTypeIcons.put("pptx", getIcon(POWERPOINT_64));
      fileTypeIcons.put("ps", getIcon(PS_64));
      fileTypeIcons.put("sh", getIcon(CONSOLE_64));
      fileTypeIcons.put("txt", getIcon(TXT_64));
      fileTypeIcons.put("wma", getIcon(WMA_64));
      fileTypeIcons.put("xls", getIcon(XLS_64));
      fileTypeIcons.put("xlsx", getIcon(XLS_64));
      fileTypeIcons.put("xlt", getIcon(XLS_64));
      fileTypeIcons.put("xml", getIcon(XML_64));
      fileTypeIcons.put("zip", getIcon(ZIP_64));

      fileTypeIconsProperty.setValue(FXCollections.observableMap(fileTypeIcons));
   }

   /**
    * Load an icon from the CURSOR_PATH location in the class path.
    * @param resourceName Must be a filename of an icon located in the
    *                     CURSOR_PATH location of the classpath.
    * @return {@link Image}
    * @throws IllegalArgumentException If the resource is not found.
    */
   @Override
   public Image getIcon(final String resourceName) {
      if (resourceName == null) {
         throw new IllegalArgumentException("resourceName must not be null");
      }

      final InputStream inputStream = IconsImpl.class.getResourceAsStream(resourceName);
      if (inputStream == null) {
         throw new IllegalArgumentException(resourceName + " is not a valid resource");
      }

      return new Image(inputStream);
   }

   /**
    * Load an icon for the passed in file.
    * @param file to load an icon for.
    * @return {@link Image}
    * @throws IllegalArgumentException If the resource is not found.
    */
   @Override
   public Image getIconForFile(final File file) {
      if (file.isDirectory()) {
         return getIcon(IconsImpl.FOLDER_64);
      }

      final String extension = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ENGLISH);
      final Image image = fileTypeIconsProperty.getValue().get(extension);
      if (image == null) {
         return getIcon(IconsImpl.GENERIC_FILE_64);
      }

      return image;
   }

   /**
    * Mapping of file extensions to icon images.
    */
   @Override
   public MapProperty<String, Image> fileTypeIconsProperty() {
      return fileTypeIconsProperty;
   }

   /**
    * Sets the mapping of file extensions to icon images.
    */
   @Override
   public void setFileTypeIcons(final Map<String, Image> map) {
      fileTypeIconsProperty().setValue(FXCollections.observableMap(map));
   }

   /**
    * Mapping of file extensions to icon images.
    */
   @Override
   public final Map<String, Image> getFileTypeIcons() {
      return fileTypeIconsProperty().get();
   }
}

