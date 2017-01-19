package com.chainstaysoftware.filechooser;

import javafx.scene.Node;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;

interface FilesView {
   Node getNode();

   /**
    * sets the files on the view.
    * @param directoryStream Filtered stream of files to show in view.
    * @param remainingDirectoryStream This stream contains the files not to
    *                                 show in the view. But, may include
    *                                 directories that should be shown in the
    *                                 view but were filtered out in the directoryStream.
    */
   void setFiles(DirectoryStream<Path> directoryStream,
                 DirectoryStream<Path> remainingDirectoryStream);
}
