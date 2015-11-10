package com.chainstaysoftware.filechooser;

import java.io.File;
import java.util.Optional;

@FunctionalInterface
public interface FileChooserCallback {
   void fileChosen(Optional<File> fileOptional);
}
