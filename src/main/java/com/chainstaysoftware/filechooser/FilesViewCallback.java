package com.chainstaysoftware.filechooser;

import java.io.File;
import java.util.stream.Stream;

public interface FilesViewCallback {
   void requestChangeDirectory(File directory);

   Stream<File> getFileStream(File directory);

   void setCurrentSelection(File file);
}
