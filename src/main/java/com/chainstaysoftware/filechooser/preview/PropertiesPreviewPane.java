/**
 * Copyright 2015 © Denmar Technical Services Inc
 * The U.S. Government has unlimited rights per DFAR 252.227-7014, all other
 * rights reserved.
 * <p>
 * WARNING - This software contains Technical Data whose export is restricted by
 * the Arms Export Control Act (Title 22, U.S.C., Sec 2751, et seq.) or the
 * Export Administration Act of 1979, as amended (Title 50, U.S. C. App. 2401
 * et seq.). Violations of these export laws are subject to severe criminal
 * penalties.
 */
package com.chainstaysoftware.filechooser.preview;

import com.chainstaysoftware.filechooser.Icons;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertiesPreviewPane implements PreviewPane {
   private static Logger logger = Logger.getLogger("com.chainstaysoftware.filechooser.PropertiesPreviewPane");

   private static final int CREATED_LABEL_COL = 0;
   private static final int CREATED_LABEL_ROW = 0;
   private static final int CREATED_VAL_COL = 1;
   private static final int CREATED_VAL_ROW = 0;

   private static final int MODIFIED_LABEL_COL = 0;
   private static final int MODIFIED_LABEL_ROW = 1;
   private static final int MODIFIED_VAL_COL = 1;
   private static final int MODIFIED_VAL_ROW = 1;

   private static final int LASTOPENED_LABEL_COL = 0;
   private static final int LASTOPENED_LABEL_ROW = 2;
   private static final int LASTOPENED_VAL_COL = 1;
   private static final int LASTOPENED_VAL_ROW = 2;

   private static final int SIZE_LABEL_COL = 0;
   private static final int SIZE_LABEL_ROW = 3;
   private static final int SIZE_VAL_COL = 1;
   private static final int SIZE_VAL_ROW = 3;

   private final ResourceBundle resourceBundle = ResourceBundle.getBundle("filechooser");
   private final Icons icons = new Icons();

   private final VBox vBox;
   private final Label nameLabel = createValueLabel();
   private final Label createdValLabel = createValueLabel();
   private final Label modifiedValLabel = createValueLabel();
   private final Label lastOpenedLabel = createValueLabel();
   private final Label sizeLabel = createValueLabel();
   private final ImageView imageView = createImageView();

   public PropertiesPreviewPane()
   {
      final GridPane gridPane = createGridPane();

      vBox = new VBox();
      vBox.setId("propertiesPreviewVbox");
      vBox.getStyleClass().add("propertiespreview-vbox");
      vBox.setAlignment(Pos.CENTER);
      vBox.getChildren().addAll(imageView, nameLabel, gridPane);
   }

   private GridPane createGridPane() {
      final GridPane gridPane = new GridPane();
      gridPane.setId("propertiesPreviewGrid");
      gridPane.getStyleClass().add("propertiespreview-grid");

      gridPane.add(createLabel("propertiespreview.create"), CREATED_LABEL_COL, CREATED_LABEL_ROW);
      gridPane.add(createdValLabel, CREATED_VAL_COL, CREATED_VAL_ROW);

      gridPane.add(createLabel("propertiespreview.modified"), MODIFIED_LABEL_COL, MODIFIED_LABEL_ROW);
      gridPane.add(modifiedValLabel, MODIFIED_VAL_COL, MODIFIED_VAL_ROW);

      gridPane.add(createLabel("propertiespreview.lastopened"), LASTOPENED_LABEL_COL, LASTOPENED_LABEL_ROW);
      gridPane.add(lastOpenedLabel, LASTOPENED_VAL_COL, LASTOPENED_VAL_ROW);

      gridPane.add(createLabel("propertiespreview.size"), SIZE_LABEL_COL, SIZE_LABEL_ROW);
      gridPane.add(sizeLabel, SIZE_VAL_COL, SIZE_VAL_ROW);

      gridPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

      return gridPane;
   }

   private ImageView createImageView() {
      final ImageView imageView = new ImageView();
      imageView.setId("propertiesPreviewImageView");
      imageView.setPreserveRatio(true);
      return imageView;
   }

   /**
    * Sets the file to display within the Pane.
    *
    * @param file
    */
   @Override
   public void setFile(final File file) {
      try {
         imageView.setImage(icons.getIconForFile(file));

         nameLabel.setText(file.getName());

         final BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

         createdValLabel.setText(formatTime(attr.creationTime()));
         modifiedValLabel.setText(formatTime(attr.lastModifiedTime()));
         lastOpenedLabel.setText(formatTime(attr.lastAccessTime()));
         sizeLabel.setText(FileUtils.byteCountToDisplaySize(attr.size()));
      } catch (Exception e) {
         logger.log(Level.WARNING, "Could not retrieve file attributes for - " + file, e);

         imageView.setImage(null);
         nameLabel.setText(null);
         createdValLabel.setText(null);
         modifiedValLabel.setText(null);
         lastOpenedLabel.setText(null);
         sizeLabel.setText(null);
      }
   }

   private String formatTime(final FileTime fileTime) {
      final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(fileTime.toInstant(),
            ZoneId.systemDefault());

      return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .format(zonedDateTime);
   }

   @Override
   public Pane getPane() {
      return vBox;
   }

   private Label createLabel(final String resourceTag) {
      final Label label = new Label(resourceBundle.getString(resourceTag));
      label.getStyleClass().add("propertiespreview-label");
      return label;
   }

   private Label createValueLabel() {
      final Label label = new Label();
      label.getStyleClass().add("propertiespreview-valuelabel");
      return label;
   }
}
