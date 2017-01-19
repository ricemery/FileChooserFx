package com.chainstaysoftware.filechooser;

import java.io.Serializable;
import java.util.Comparator;

public class DirListItemComparator implements Comparator<DirectoryListItem>, Serializable {
   private static final long serialVersionUID = 6642373165691519041L;

   private final FileMetaDataComparator fileMetaDataComparator;

   public DirListItemComparator(final OrderBy orderBy, final OrderDirection direction) {
      this.fileMetaDataComparator = new FileMetaDataComparator(orderBy, direction);
   }

   @Override
   public int compare(final DirectoryListItem o1, final DirectoryListItem o2) {
      return fileMetaDataComparator.compare(o1.getFile(), o2.getFile());
   }
}
