# FileChooserFx
JavaFx based FileChooser and DirectoryChooser

The FileChooser and DirectoryChooser implementations in JavaFx call out to OS native implementations.
Native implementations are great, unless customization is needed. This project implements a FileChooser
and DirectoryChooser completely in JavaFx code so that the implementations can be customized as needed.

## Limitations
* The FileChooser does not support multifile select.
* There are file previews implemented for jpg, png and text files. Other file previews can be plugged in.
* Linux and OSX can optionally be shown in the "Places" list. The code to determine the list of mount points is "experimental". Java does not include a good way of determining all the mount points and the types. The names and types may show differently than a native file browser.
* Localization hooks are provided, but only English text is provided.

## Dependencies
* Apache Commmons Lang and IO
* ControlsFx - http://fxexperience.com/controlsfx/
* Icons from Icons8 - https://icons8.com

## Usage
See src/test/java/com/chainstaysoftware/filebrowser/FileChooserDemo.java for sample usage.

## File Preview
The preview code to execute for a file is determined from the file's mimetype.
FileChooserFx uses the Java Files.probeContentType(Path path) method to determine
the mimetype.

The Sun Java OS-X implementation of probeContentType is problematic. The
implementation will always return null to the probeContentType
call if there is no ~/.mime.types file found. And, the ~/.mime.types file is
not installed within OS-X by default.

To work around the problem a ~/mime.types file can be created. One source of the
file content  can be found at
http://svn.apache.org/viewvc/httpd/httpd/branches/2.2.x/docs/conf/mime.types?revision=1576707&view=co

Alternately, a custom FileTypeDetector implementation could be installed.
For an example look at https://odoepner.wordpress.com/2013/07/29/transparently-improve-java-7-mime-type-recognition-with-apache-tika/
