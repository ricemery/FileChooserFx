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
   private final OrderBy orderBy;

   public FilenameComparator(final OrderBy orderBy) {
      this.orderBy = orderBy;
   }

   @Override
   public int compare(final File o1, final File o2) {
      if (OrderBy.ModificationDate.equals(orderBy)) {
         if (o1.lastModified() < o2.lastModified()) return -1;
         if (o1.lastModified() > o2.lastModified()) return 1;
         return 0;
      }

      if (OrderBy.Size.equals(orderBy)) {
         if (o1.length() < o2.length()) return -1;
         if (o1.length() > o2.length()) return 1;
         return 0;
      }

      if (OrderBy.Type.equals(orderBy)) {
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

      return o1.getName().compareTo(o2.getName());
   }
}
