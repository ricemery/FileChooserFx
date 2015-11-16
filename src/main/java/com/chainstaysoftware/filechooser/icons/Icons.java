package com.chainstaysoftware.filechooser.icons;

import javafx.beans.property.MapProperty;
import javafx.scene.image.Image;

import java.io.File;
import java.util.Map;

/**
 * Icon loading.
 */
public interface Icons {
   /**
    * Load an icon from the resourceName location in the classpath.
    * @param resourceName Must be a filename in the classpath.
    * @return {@link Image}
    * @throws IllegalArgumentException If the resource is not found.
    */
   Image getIcon(String resourceName);

   /**
    * Load an icon for the passed in file.
    * @param file to load an icon for.
    * @return {@link Image}
    * @throws IllegalArgumentException If the resource is not found.
    */
   Image getIconForFile(File file);

   /**
    * Mapping of file extensions to icon images.
    */
   MapProperty<String, Image> fileTypeIconsProperty();

   /**
    * Sets the mapping of file extensions to icon images.
    */
   void setFileTypeIcons(Map<String, Image> map);

   /**
    * Mapping of file extensions to icon images.
    */
   Map<String, Image> getFileTypeIcons();
}
