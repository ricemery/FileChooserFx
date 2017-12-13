package com.chainstaysoftware.filechooser;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

class FileMetaDataComparatorTest {
   private final File testDataDir = new File("./src/test/resources/com/chainstaysoftware/filechooser");
   private final File aaa = new File(testDataDir, "aaa");
   private final File bbb = new File(testDataDir, "bbb");
   private final File emptyTxt = new File(testDataDir, "empty.txt");
   private final File emptyXml = new File(testDataDir, "empty.xml");
   private final File dir1 = new File(testDataDir, "dir1");
   private final File dir2 = new File(testDataDir, "dir2");

   @Test
   void testOrderByModificationDate() {
      final FileMetaDataComparator comparator = new FileMetaDataComparator(OrderBy.ModificationDate, OrderDirection.Ascending);

      bbb.setLastModified(System.currentTimeMillis());

      Assertions.assertThat(comparator.compare(aaa, aaa))
         .describedAs("Same file mod date should equal")
         .isEqualTo(0);
      Assertions.assertThat(comparator.compare(aaa, bbb))
         .describedAs("Less than mod date should be less than")
         .isLessThan(0);
      Assertions.assertThat(comparator.compare(bbb, aaa))
         .describedAs("Greater than mod date should be greater than")
         .isGreaterThan(0);
   }

   @Test
   void testOrderByModificationDate_Descending() {
      final FileMetaDataComparator comparator = new FileMetaDataComparator(OrderBy.ModificationDate, OrderDirection.Descending);

      bbb.setLastModified(System.currentTimeMillis());

      Assertions.assertThat(comparator.compare(aaa, aaa))
         .describedAs("Same file mod date should equal")
         .isEqualTo(0);
      Assertions.assertThat(comparator.compare(aaa, bbb))
         .describedAs("Less than mod date should be greater than")
         .isGreaterThan(0);
      Assertions.assertThat(comparator.compare(bbb, aaa))
         .describedAs("Greater than mod date should be less than")
         .isLessThan(0);
   }

   @Test
   void testOrderBySize() {
      final FileMetaDataComparator comparator = new FileMetaDataComparator(OrderBy.Size, OrderDirection.Ascending);

      Assertions.assertThat(comparator.compare(aaa, aaa))
         .describedAs("Same file size should equal")
         .isEqualTo(0);
      Assertions.assertThat(comparator.compare(aaa, bbb))
         .describedAs("Less than size should be less than")
         .isLessThan(0);
      Assertions.assertThat(comparator.compare(bbb, aaa))
         .describedAs("Greater than size should be greater than")
         .isGreaterThan(0);

      Assertions.assertThat(comparator.compare(testDataDir, aaa))
         .describedAs("Directory should be lessthan file")
         .isLessThan(0);
      Assertions.assertThat(comparator.compare(aaa, testDataDir))
         .describedAs("File should be greatethan dir")
         .isGreaterThan(0);
      Assertions.assertThat(comparator.compare(new File(testDataDir, "dir1"), new File(testDataDir, "dir2")))
         .describedAs("Directories should fall back to name compare")
         .isLessThan(0);
      Assertions.assertThat( comparator.compare(new File(testDataDir, "dir1"), new File(testDataDir, "dir1")))
         .describedAs("Directories should fall back to name compare")
         .isEqualTo(0);
      Assertions.assertThat(comparator.compare(new File(testDataDir, "dir2"), new File(testDataDir, "dir1")))
         .describedAs("Directories should fall back to name compare")
         .isGreaterThan(0);
   }

   @Test
   void testOrderBySize_Descending() {
      final FileMetaDataComparator comparator = new FileMetaDataComparator(OrderBy.Size, OrderDirection.Descending);

      Assertions.assertThat(comparator.compare(aaa, aaa))
         .describedAs("Same file size should equal")
         .isEqualTo(0);
      Assertions.assertThat(comparator.compare(aaa, bbb))
         .describedAs("Less than size should be greater than")
         .isGreaterThan(0);
      Assertions.assertThat(comparator.compare(bbb, aaa))
         .describedAs("Greater than size should be less than")
         .isLessThan(0);
   }

   @Test
   void testOrderByType() {
      final FileMetaDataComparator comparator = new FileMetaDataComparator(OrderBy.Type, OrderDirection.Ascending);

      Assertions.assertThat(comparator.compare(aaa, aaa))
         .describedAs("Same file type date should equal - no extension")
         .isEqualTo(0);
      Assertions.assertThat(comparator.compare(emptyTxt, emptyTxt))
         .describedAs("Same file type date should equal - with extension")
         .isEqualTo(0);
      Assertions.assertThat(comparator.compare(emptyTxt, emptyXml))
         .describedAs("Alpha order type - lt")
         .isLessThan(0);
      Assertions.assertThat(comparator.compare(emptyXml, emptyTxt))
         .describedAs("Alpha order type - gt")
         .isGreaterThan(0);

      Assertions.assertThat(comparator.compare(dir1, dir1))
         .describedAs("Same file type - directory - should equal - no extension")
         .isEqualTo(0);
      Assertions.assertThat(comparator.compare(dir1, dir2))
         .describedAs("Alpha order type - directory - lt")
         .isLessThan(0);
      Assertions.assertThat(comparator.compare(dir2, dir1))
         .describedAs("Alpha order type - directory - gt")
         .isGreaterThan(0);

      Assertions.assertThat(comparator.compare(dir1, aaa))
         .describedAs("Alpha order type - file and directory - lt - dirs sort before files")
         .isLessThan(0);
      Assertions.assertThat(comparator.compare(aaa, dir1))
         .describedAs("Alpha order type - file and directory - gt - dirs sort before files")
         .isGreaterThan(0);
   }

   @Test
   void testOrderByType_Descending() {
      final FileMetaDataComparator comparator = new FileMetaDataComparator(OrderBy.Type, OrderDirection.Descending);

      Assertions.assertThat(comparator.compare(aaa, aaa))
         .describedAs("Same file type date should equal - no extension")
         .isEqualTo(0);
      Assertions.assertThat(comparator.compare(emptyTxt, emptyTxt))
         .describedAs("Same file type date should equal - with extension")
         .isEqualTo(0);
      Assertions.assertThat(comparator.compare(emptyTxt, emptyXml))
         .describedAs("Alpha order type - lt")
         .isGreaterThan(0);
      Assertions.assertThat(comparator.compare(emptyXml, emptyTxt))
         .describedAs("Alpha order type - gt")
         .isLessThan(0);

      Assertions.assertThat(comparator.compare(dir1, dir1))
         .describedAs("Same file type - directory - should equal - no extension")
         .isEqualTo(0);
      Assertions.assertThat(comparator.compare(dir1, dir2))
         .describedAs("Alpha order type - directory - lt")
         .isGreaterThan(0);
      Assertions.assertThat(comparator.compare(dir2, dir1))
         .describedAs("Alpha order type - directory - gt")
         .isLessThan(0);

      Assertions.assertThat(comparator.compare(dir1, aaa))
         .describedAs("Alpha order type - file and directory - lt - dirs sort before files")
         .isGreaterThan(0);
      Assertions.assertThat(comparator.compare(aaa, dir1))
         .describedAs("Alpha order type - file and directory - gt - dirs sort before files")
         .isLessThan(0);
   }

   @Test
   void testOrderByName() {
      final FileMetaDataComparator comparator = new FileMetaDataComparator(OrderBy.Name, OrderDirection.Ascending);

      Assertions.assertThat(comparator.compare(aaa, aaa))
         .describedAs("Same file name should equal")
         .isEqualTo(0);
      Assertions.assertThat(comparator.compare(aaa, bbb))
         .describedAs("Less than name should be less than")
         .isLessThan(0);
      Assertions.assertThat(comparator.compare(bbb, aaa))
         .describedAs("Greater than name should be greater than")
         .isGreaterThan(0);
   }

   @Test
   void testOrderByName_Descending() {
      final FileMetaDataComparator comparator = new FileMetaDataComparator(OrderBy.Name, OrderDirection.Descending);

      Assertions.assertThat(comparator.compare(aaa, aaa))
         .describedAs("Same file name should equal")
         .isEqualTo(0);
      Assertions.assertThat(comparator.compare(aaa, bbb))
         .describedAs("Less than name should be greater than")
         .isGreaterThan(0);
      Assertions.assertThat(comparator.compare(bbb, aaa))
         .describedAs("Greater than name should be less than")
         .isLessThan(0);
   }
}
