package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.preview.HeadPreviewPane;
import com.chainstaysoftware.filechooser.preview.ImagePreviewPane;
import com.chainstaysoftware.filechooser.preview.PreviewPane;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FileChooserDemo extends Application {
   @Override
   public void start(Stage primaryStage)  {
      final HashMap<String, Class<? extends PreviewPane>> previewHandlers = new HashMap<>();
      previewHandlers.put("image/png", ImagePreviewPane.class);
      previewHandlers.put("image/jpg", ImagePreviewPane.class);
      previewHandlers.put("text/plain", HeadPreviewPane.class);

      final TextFlow textFlow = new TextFlow();

      final Button fileChooserButton = createFileChooserButton(primaryStage, previewHandlers, textFlow);
      final Button fileSaveButton = createFileSaveButton(primaryStage, previewHandlers, textFlow, null);

      final Button userButton = new Button("User action");
      final HBox userHbox = new HBox(userButton);
      final Button fileSaveWithUserButton
         = createFileSaveButton(primaryStage, previewHandlers, textFlow, userHbox);

      final Button dirChooserButton = createDirChooserButton(primaryStage, textFlow);

      final ToolBar toolBar = new ToolBar();
      toolBar.getItems().addAll(fileChooserButton, fileSaveButton,
         fileSaveWithUserButton, dirChooserButton);

      final BorderPane borderPane = new BorderPane();
      borderPane.setTop(toolBar);
      borderPane.setCenter(textFlow);

      primaryStage.setScene(new Scene(borderPane, 800, 600));
      primaryStage.show();
   }

   private Button createFileChooserButton(final Stage primaryStage,
                                          final Map<String, Class<? extends PreviewPane>> previewHandlers,
                                          final TextFlow textFlow) {
      final Button fileChooserButton = new Button("Choose File");
      fileChooserButton.setOnAction(event -> {
         final FileChooserFx fileChooser = new FileChooserFxImpl();
         fileChooser.setTitle("File Chooser");
         fileChooser.setShowHiddenFiles(false);
         fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text files (txt)", "*.txt"),
            new FileChooser.ExtensionFilter("XML files", "*.xml"),
            new FileChooser.ExtensionFilter("All files", "*.*"));
         fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("XML files (xml)", "*.xml"));
         fileChooser.getPreviewHandlers().putAll(previewHandlers);
         fileChooser.setHelpCallback(() -> System.out.println("Help invoked"));
         fileChooser.setShowMountPoints(true);
         fileChooser.setFavoriteDirsCallbacks(directory -> System.out.println("Add favorite - " + directory),
            directory -> System.out.println("Remove favorite - " + directory));
         fileChooser.setViewType(ViewType.Icon);
         fileChooser.setWidth(1024);
         fileChooser.setHeight(768);
         fileChooser.setDividerPositions(.15, .30);

         fileChooser.showOpenDialog(primaryStage,
               fileOptional -> textFlow.getChildren()
                     .add(new Text("File to open - " + fileOptional.toString() + "\r\n"
                                    + "Selected Extension Filter - " + fileChooser.getSelectedExtensionFilter().getDescription() + "\r\n"
                                    + "View Type - " + fileChooser.getViewType() + "\r\n"
                                    + "Sort - " + fileChooser.getOrderBy() + " " + fileChooser.getOrderDirection() + "\r\n"
                                    + "Favorites - " + fileChooser.favoriteDirsProperty() + "\r\n"
                                    + "Window size - " + fileChooser.getWidth() + " " + fileChooser.getHeight() + "\r\n"
                                    + "Divider positions - " + Arrays.toString(fileChooser.getDividerPositions()) +  "\r\n")));
      });
      return fileChooserButton;
   }

   private Button createFileSaveButton(final Stage primaryStage,
                                       final Map<String, Class<? extends PreviewPane>> previewHandlers,
                                       final TextFlow textFlow,
                                       final Node userContent) {
      final Button fileSaveButton = new Button();
      if (userContent == null) {
         fileSaveButton.setText("Save File");
      } else {
         fileSaveButton.setText("Save File with user content");
      }

      fileSaveButton.setOnAction(event -> {
         final FileChooserFx fileChooser = new FileChooserFxImpl();
         fileChooser.setShowHiddenFiles(false);
         fileChooser.getPreviewHandlers().putAll(previewHandlers);
         fileChooser.setHelpCallback(() -> System.out.println("Help invoked"));
         //fileChooser.setInitialFileName("foo.txt");

         if (userContent == null) {
            fileChooser.setTitle("File Save");
            fileChooser.showSaveDialog(primaryStage,
               fileOptional -> textFlow.getChildren()
                  .add(new Text("File to save - " + fileOptional.toString() + "\r\n")));
         } else {
            fileChooser.setTitle("File Save with user content");
            fileChooser.showSaveDialog(primaryStage,
               userContent,
               fileOptional -> textFlow.getChildren()
                  .add(new Text("File to save - " + fileOptional.toString() + "\r\n")));
         }
      });
      return fileSaveButton;
   }

   private Button createDirChooserButton(final Stage primaryStage,
                                         final TextFlow textFlow) {
      final Button dirChooserButton = new Button("Choose Directory");
      dirChooserButton.setOnAction(event -> {
         final DirectoryChooserFx dirChooser = new DirectoryChooserFxImpl();
         dirChooser.setTitle("Directory Chooser");
         dirChooser.setHelpCallback(() -> System.out.println("Help invoked"));
         dirChooser.setViewType(ViewType.ListWithPreview);
         dirChooser.setShowMountPoints(true);
         dirChooser.setDividerPosition(.15);

         dirChooser.showDialog(primaryStage,
               fileOptional -> textFlow.getChildren()
                     .add(new Text("Directory to open - " + fileOptional.toString() + "\r\n"
                           + "View Type - " + dirChooser.getViewType() + "\r\n"
                           + "Window size - " + dirChooser.getWidth() + " " + dirChooser.getHeight() + "\r\n"
                           + "Divider position - " + dirChooser.getDividerPosition() + "\r\n")));
      });
      return dirChooserButton;
   }

   public static void main(String[] args) {
      Application.launch(args);
   }
}
