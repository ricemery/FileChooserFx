# FileChooserFx
JavaFx based FileChooser and DirectoryChooser

The FileChooser and DirectoryChooser implementations in JavaFx call out to OS native implementations. 
Native implementations are great, unless customization is needed. This project implements a FileChooser
and DirectoryChooser completely in JavaFx code so that the implementations can be customized as needed.

## Limitations
* The FileChooser does not support multifile select.
* There are file previews implemented for jpg, png and text files. Other file previews can be plugged in.
* Refresh of file system changes is not yet implemented.
* Unix mountpoints do not show in "Places" list. This work is planned. 
* Localization hooks are provided, but only English text is provided.

## Dependencies
* Apache Commmons Lang and IO
* ControlsFx - http://fxexperience.com/controlsfx/
* Icons from Icons8 - https://icons8.com

## Usage
See src/test/java/com/chainstaysoftware/filebrowser/FileChooserDemo.java for sample usage. 
