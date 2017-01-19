package com.chainstaysoftware.filechooser;

import javafx.scene.control.TreeItem;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;

public interface PopulateTreeItemRunnableFactory {
   Runnable create(DirectoryStream<Path> directoryStream,
                   DirectoryStream<Path> unfilteredDirectoryStream,
                   TreeItem<File> parentItem);
}
