package com.chainstaysoftware.filechooser.preview;


import javafx.scene.layout.Pane;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

class PreviewPaneQueryTest {
   private final Map<String, Class<? extends PreviewPane>> handlersMap
      = Collections.unmodifiableMap(Stream.of(
         new AbstractMap.SimpleEntry<>("text/plain", TxtPreviewPane.class))
      .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey,
         AbstractMap.SimpleEntry::getValue)));

   @Test
   void testQuery() {
      final Class<? extends PreviewPane> txtPaneClass
         = PreviewPaneQuery.query(handlersMap, new File("./src/test/resources/com/chainstaysoftware/filechooser/empty.txt"));
      MatcherAssert.assertThat("Txt pane should extend TxtPreviewPane", txtPaneClass, equalTo(TxtPreviewPane.class));

      final Class<? extends PreviewPane> unknownPaneClass
         = PreviewPaneQuery.query(handlersMap, new File("./src/test/resources/com/chainstaysoftware/filechooser/foo.bar"));
      MatcherAssert.assertThat("Unknown should return null pane", unknownPaneClass, nullValue());

      final Class<? extends PreviewPane> dirPaneClass
         = PreviewPaneQuery.query(handlersMap, new File("./src/test/resources/com/chainstaysoftware/filechooser/dir1"));
      MatcherAssert.assertThat("Directory should return null pane", dirPaneClass, nullValue());
   }

   private class TxtPreviewPane implements PreviewPane {
      @Override
      public void setFile(File file) {
      }

      @Override
      public Pane getPane() {
         return null;
      }
   }
}