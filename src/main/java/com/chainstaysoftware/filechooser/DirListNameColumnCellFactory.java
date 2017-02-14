package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.icons.IconsImpl;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

/**
 * CellFactory for {@link DirectoryListItem#getFile()} value.
 */
class DirListNameColumnCellFactory
      implements Callback<TableColumn<DirectoryListItem, DirectoryListItem>, TableCell<DirectoryListItem, DirectoryListItem>> {

   private final boolean nameOnly;
   private final FilesViewCallback callback;
   private final Icons icons;

   /**
    * Constructor
    * @param nameOnly Indicates if the cell text should include the file.getPath() value
    *                 or file.getName()
    * @param callback {@link FilesViewCallback}
    * @param icons Icon retriever for File objects.
    */
   DirListNameColumnCellFactory(final boolean nameOnly,
                                final FilesViewCallback callback,
                                final Icons icons) {
      this.nameOnly = nameOnly;
      this.callback = callback;
      this.icons = icons;
   }

   @Override
   public TableCell<DirectoryListItem, DirectoryListItem> call(TableColumn<DirectoryListItem, DirectoryListItem> param) {
      return new TableCell<DirectoryListItem, DirectoryListItem>() {
         @Override
         protected void updateItem(DirectoryListItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
               setText(null);
               setGraphic(null);
               setOnMouseClicked(null);
            } else {
               if (nameOnly) {
                  final String systemDisplayName = FileSystemView.getFileSystemView().getSystemDisplayName(item.getFile());
                  setText("".equals(systemDisplayName) ? item.getFile().toString() : systemDisplayName);
               } else {
                  setText(item.getFile().toString());
               }

               final Image image = item.isDirectory()
                  ? icons.getIcon(IconsImpl.FOLDER_64)
                  : icons.getIconForFile(item.getFile());
               final ImageView graphic = new ImageView(image);
               graphic.setFitHeight(IconsImpl.SMALL_ICON_HEIGHT);
               graphic.setFitWidth(IconsImpl.SMALL_ICON_WIDTH);
               graphic.setPreserveRatio(true);
               setGraphic(graphic);

               setOnMouseClicked(event -> {
                  final File file = item.getFile();
                  if (event.getClickCount() < 2) {
                     return;
                  }

                  if (file.isDirectory()) {
                     callback.requestChangeDirectory(file);
                  } else {
                     callback.fireDoneButton();
                  }
               });
            }
         }
      };
   }
}
