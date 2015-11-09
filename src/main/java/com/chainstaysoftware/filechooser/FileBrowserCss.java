package com.chainstaysoftware.filechooser;

public class FileBrowserCss {
   public String getUrl() {
      return getClass().getResource("filechooser.css").toExternalForm();
   }
}
