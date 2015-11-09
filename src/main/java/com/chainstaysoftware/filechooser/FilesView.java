package com.chainstaysoftware.filechooser;

import javafx.scene.Node;

import java.io.File;
import java.util.stream.Stream;

interface FilesView {
   Node getNode();

   void setCallback(FilesViewCallback callback);

   void setFiles(Stream<File> fileStream);
}
