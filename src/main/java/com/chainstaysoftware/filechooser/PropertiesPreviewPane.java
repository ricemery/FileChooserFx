package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.icons.Icons;
import com.chainstaysoftware.filechooser.preview.PreviewPane;
import com.chainstaysoftware.filechooser.preview.PreviewPaneQuery;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pane used to preview a file contents and show the properties of a file (name, size, etc).
 */
public class PropertiesPreviewPane {
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
   private final Icons icons;
   private final Map<String, Class<? extends PreviewPane>> previewHandlers;

   private final VBox vBox;
   private final Label nameLabel = createNameValueLabel();
   private final Label createdValLabel = createValueLabel();
   private final Label modifiedValLabel = createValueLabel();
   private final Label lastOpenedLabel = createValueLabel();
   private final Label sizeLabel = createValueLabel();
   private final HBox previewPaneContainerPane = createPreviewContainerPane();
   private final ImageView imageView = createImageView();

   public PropertiesPreviewPane(final Map<String, Class<? extends PreviewPane>> previewHandlers,
                                final Icons icons)
   {
      this.previewHandlers = previewHandlers;
      this.icons = icons;

      final GridPane gridPane = createGridPane();

      vBox = new VBox();
      vBox.setId("propertiesPreviewVbox");
      vBox.getStyleClass().add("propertiespreview-vbox");
      vBox.setAlignment(Pos.CENTER);
      vBox.getChildren().addAll(previewPaneContainerPane, nameLabel, gridPane);
      VBox.setVgrow(previewPaneContainerPane, Priority.ALWAYS);
   }

   /**
    * Create control to contain the preview for a file.
    */
   private HBox createPreviewContainerPane() {
      final HBox hBox = new HBox();
      hBox.setMinSize(0,0);
      hBox.setAlignment(Pos.CENTER);
      return hBox;
   }

   /**
    * Create the Grid to contain the properties for the previewed file.
    */
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

   /**
    * Create ImageView to contain the Icon for the previewed file if no other
    * preview is available.
    */
   private ImageView createImageView() {
      final ImageView view = new ImageView();
      view.setId("propertiesPreviewImageView");
      view.setPreserveRatio(true);
      return view;
   }

   /**
    * Sets the file to display within the Pane.
    *
    * @param file
    */
   public void setFile(final File file) {
      try {
         setContainerNode(file);

         nameLabel.setText(file.getName());

         final BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);

         createdValLabel.setText(formatTime(attr.creationTime()));
         modifiedValLabel.setText(formatTime(attr.lastModifiedTime()));
         lastOpenedLabel.setText(formatTime(attr.lastAccessTime()));
         sizeLabel.setText(FileUtils.byteCountToDisplaySize(attr.size()));
      } catch (IOException e) {
         logger.log(Level.WARNING, "Could not retrieve file attributes for - " + file, e);

         imageView.setImage(null);
         nameLabel.setText(null);
         createdValLabel.setText(null);
         modifiedValLabel.setText(null);
         lastOpenedLabel.setText(null);
         sizeLabel.setText(null);
      }
   }

   /**
    * Update the preview node with the preview of the passed in file.
    */
   private void setContainerNode(final File file) {
      final Class<? extends PreviewPane> previewPaneClass = PreviewPaneQuery.query(previewHandlers, file);
      if (previewPaneClass == null) {
         previewPaneContainerPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
         imageView.setImage(icons.getIconForFile(file));
         previewPaneContainerPane.getChildren().setAll(imageView);
      } else {
         final Optional<PreviewPane> previewPaneOpt = PreviewPaneFactory.create(previewPaneClass);
         if (!previewPaneOpt.isPresent()) {
            logger.log(Level.SEVERE, "No PreviewPane created.");
            return;
         }

         final PreviewPane previewPane = previewPaneOpt.get();
         previewPaneContainerPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_COMPUTED_SIZE);
         previewPane.setFile(file);
         previewPaneContainerPane.getChildren().setAll(previewPane.getPane());
      }
   }

   private String formatTime(final FileTime fileTime) {
      final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(fileTime.toInstant(),
            ZoneId.systemDefault());

      return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
            .format(zonedDateTime);
   }

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

   private Label createNameValueLabel() {
      final Label label = new Label();
      label.getStyleClass().add("propertiespreview-namevaluelabel");
      label.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
      return label;
   }
}
