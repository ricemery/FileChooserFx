package com.chainstaysoftware.filebrowser;

public class FileBrowserCss {
   public String getUrl() {
      return getClass().getResource("filebrowser.css").toExternalForm();
   }
}
