package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.IconsImpl;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

/**
 * CellFactory for {@link DirectoryListItem#getFile()} value.
 */
class DirListNameColumnCellFactory
      implements Callback<TableColumn<DirectoryListItem, DirectoryListItem>, TableCell<DirectoryListItem, DirectoryListItem>> {

   private final boolean nameOnly;

   /**
    * Constructor
    * @param nameOnly Indicates if the cell text should include the file.getPath() value
    *                 or file.getName()
    */
   DirListNameColumnCellFactory(final boolean nameOnly) {
      this.nameOnly = nameOnly;
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
            } else {
               if (nameOnly && !StringUtils.isEmpty(item.getFile().getName())) {
                  setText(item.getFile().getName());
               } else {
                  setText(item.getFile().toString());
               }

               final ImageView graphic = new ImageView(item.getIcon());
               graphic.setFitHeight(IconsImpl.SMALL_ICON_HEIGHT);
               graphic.setFitWidth(IconsImpl.SMALL_ICON_WIDTH);
               graphic.setPreserveRatio(true);
               setGraphic(graphic);
            }
         }
      };
   }
}
