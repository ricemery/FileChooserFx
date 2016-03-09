package com.chainstaysoftware.filechooser;

import com.chainstaysoftware.filechooser.preview.HeadPreviewPane;
import com.chainstaysoftware.filechooser.preview.ImagePreviewPane;
import com.chainstaysoftware.filechooser.preview.PreviewPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.util.HashMap;

public class FileChooserDemo extends Application {
   @Override
   public void start(Stage primaryStage) throws Exception {
      final HashMap<String, Class<? extends PreviewPane>> previewHandlers = new HashMap<>();
      previewHandlers.put("image/png", ImagePreviewPane.class);
      previewHandlers.put("image/jpg", ImagePreviewPane.class);
      previewHandlers.put("text/plain", HeadPreviewPane.class);

      final TextFlow textFlow = new TextFlow();

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

         fileChooser.showOpenDialog(primaryStage,
               fileOptional -> textFlow.getChildren()
                     .add(new Text("File to open - " + fileOptional.toString() + "\r\n"
                                    + "Selected Extension Filter - " + fileChooser.getSelectedExtensionFilter().getDescription() + "\r\n"
                                    + "View Type - " + fileChooser.getViewType() + "\r\n"
                                    + "Sort - " + fileChooser.getOrderBy() + " " + fileChooser.getOrderDirection() + "\r\n"
                                    + "Favorites - " + fileChooser.favoriteDirsProperty() + "\r\n")));
      });

      final Button fileSaveButton = new Button("Save File");
      fileSaveButton.setOnAction(event -> {
         final FileChooserFx fileChooser = new FileChooserFxImpl();
         fileChooser.setTitle("File Save");
         fileChooser.setShowHiddenFiles(false);
         fileChooser.getPreviewHandlers().putAll(previewHandlers);
         fileChooser.setHelpCallback(() -> System.out.println("Help invoked"));
         //fileChooser.setInitialFileName("foo.txt");

         fileChooser.showSaveDialog(primaryStage,
               fileOptional -> textFlow.getChildren()
                     .add(new Text("File to save - " + fileOptional.toString() + "\r\n")));
      });

      final Button dirChooserButton = new Button("Choose Directory");
      dirChooserButton.setOnAction(event -> {
         final DirectoryChooserFx dirChooser = new DirectoryChooserFxImpl();
         dirChooser.setTitle("Directory Chooser");
         dirChooser.setHelpCallback(() -> System.out.println("Help invoked"));
         dirChooser.setViewType(ViewType.ListWithPreview);
         dirChooser.setShowMountPoints(true);

         dirChooser.showDialog(primaryStage,
               fileOptional -> textFlow.getChildren()
                     .add(new Text("Directory to open - " + fileOptional.toString() + "\r\n"
                            + "View Type - " + dirChooser.getViewType() + "\r\n")));
      });

      final ToolBar toolBar = new ToolBar();
      toolBar.getItems().addAll(fileChooserButton, fileSaveButton, dirChooserButton);

      final BorderPane borderPane = new BorderPane();
      borderPane.setTop(toolBar);
      borderPane.setCenter(textFlow);

      primaryStage.setScene(new Scene(borderPane, 800, 600));
      primaryStage.show();
   }

   public static void main(String[] args) {
      Application.launch(args);
   }
}
