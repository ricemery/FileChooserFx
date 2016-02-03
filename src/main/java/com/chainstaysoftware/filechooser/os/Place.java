package com.chainstaysoftware.filechooser.os;

import java.io.File;

public class Place {
   private final PlaceType type;
   private final File path;

   public Place(PlaceType type, File path) {
      this.type = type;
      this.path = path;
   }

   public PlaceType getType() {
      return type;
   }

   public File getPath() {
      return path;
   }

   @Override
   public String toString() {
      return "Place{" +
         "type=" + type +
         ", path=" + path +
         '}';
   }
}
