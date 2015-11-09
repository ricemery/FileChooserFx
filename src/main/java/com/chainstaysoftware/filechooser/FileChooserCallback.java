package com.chainstaysoftware.filechooser;

import java.io.File;
import java.util.Optional;

@FunctionalInterface
public interface FileChooserCallback {
   void fileChoosen(Optional<File> fileOptional);
}
