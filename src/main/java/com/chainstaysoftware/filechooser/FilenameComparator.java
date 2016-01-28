package com.chainstaysoftware.filechooser;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator for {@link File} that allows specification of the {@link OrderBy}
 * to sort on.
 */
public class FilenameComparator implements Comparator<File>, Serializable {
   private static final long serialVersionUID = 8867211248432156391L;

   private final OrderBy orderBy;
   private final OrderDirection direction;

   public FilenameComparator(final OrderBy orderBy,
                             final OrderDirection direction) {
      this.orderBy = orderBy;
      this.direction = direction;
   }

   @Override
   public int compare(final File o1, final File o2) {
      if (OrderBy.ModificationDate.equals(orderBy)) {
         return getAnswer(compareByDate(o1, o2));
      }

      if (OrderBy.Size.equals(orderBy)) {
         return getAnswer(compareBySize(o1, o2));
      }

      if (OrderBy.Type.equals(orderBy)) {
         return getAnswer(compareByType(o1, o2));
      }

      return getAnswer(o1.getName().compareTo(o2.getName()));
   }

   private int compareByType(final File o1, final File o2) {
      if (o1.isDirectory()) {
         if (o2.isDirectory()) {
            return o1.compareTo(o2);
         }

         return -1;
      } else if (o2.isDirectory()) {
         return 1;
      }

      final String extension1 = FilenameUtils.getExtension(o1.getName());
      final String extension2 = FilenameUtils.getExtension(o2.getName());
      return extension1.compareTo(extension2);
   }

   private int compareBySize(final File o1, final File o2) {
      if (o1.length() < o2.length()) {
         return -1;
      }

      if (o1.length() > o2.length()) {
         return 1;
      }
      return 0;
   }

   private int compareByDate(final File o1, final File o2) {
      if (o1.lastModified() < o2.lastModified()) {
         return -1;
      }

      if (o1.lastModified() > o2.lastModified()) {
         return 1;
      }

      return 0;
   }

   private int getAnswer(final int ascending) {
      if (OrderDirection.Ascending.equals(direction)) {
         return ascending;
      }

      return -ascending;
   }
}
