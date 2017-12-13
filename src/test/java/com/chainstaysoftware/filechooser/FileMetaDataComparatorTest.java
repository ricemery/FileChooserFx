package com.chainstaysoftware.filechooser;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

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

      MatcherAssert.assertThat("Same file mod date should equal", comparator.compare(aaa, aaa), equalTo(0));
      MatcherAssert.assertThat("Less than mod date should be less than", comparator.compare(aaa, bbb), lessThan(0));
      MatcherAssert.assertThat("Greater than mod date should be greater than", comparator.compare(bbb, aaa), greaterThan(0));
   }

   @Test
   void testOrderByModificationDate_Descending() {
      final FileMetaDataComparator comparator = new FileMetaDataComparator(OrderBy.ModificationDate, OrderDirection.Descending);

      bbb.setLastModified(System.currentTimeMillis());

      MatcherAssert.assertThat("Same file mod date should equal", comparator.compare(aaa, aaa), equalTo(0));
      MatcherAssert.assertThat("Less than mod date should be greater than", comparator.compare(aaa, bbb), greaterThan(0));
      MatcherAssert.assertThat("Greater than mod date should be less than", comparator.compare(bbb, aaa), lessThan(0));
   }

   @Test
   void testOrderBySize() {
      final FileMetaDataComparator comparator = new FileMetaDataComparator(OrderBy.Size, OrderDirection.Ascending);

      MatcherAssert.assertThat("Same file size should equal", comparator.compare(aaa, aaa), equalTo(0));
      MatcherAssert.assertThat("Less than size should be less than", comparator.compare(aaa, bbb), lessThan(0));
      MatcherAssert.assertThat("Greater than size should be greater than", comparator.compare(bbb, aaa), greaterThan(0));

      MatcherAssert.assertThat("Directory should be lessthan file", comparator.compare(testDataDir, aaa), lessThan(0));
      MatcherAssert.assertThat("File should be greatethan dir", comparator.compare(aaa, testDataDir), greaterThan(0));
      MatcherAssert.assertThat("Directories should fall back to name compare",
            comparator.compare(new File(testDataDir, "dir1"), new File(testDataDir, "dir2")), lessThan(0));
      MatcherAssert.assertThat("Directories should fall back to name compare",
            comparator.compare(new File(testDataDir, "dir1"), new File(testDataDir, "dir1")), equalTo(0));
      MatcherAssert.assertThat("Directories should fall back to name compare",
            comparator.compare(new File(testDataDir, "dir2"), new File(testDataDir, "dir1")), greaterThan(0));
   }

   @Test
   void testOrderBySize_Descending() {
      final FileMetaDataComparator comparator = new FileMetaDataComparator(OrderBy.Size, OrderDirection.Descending);

      MatcherAssert.assertThat("Same file size should equal", comparator.compare(aaa, aaa), equalTo(0));
      MatcherAssert.assertThat("Less than size should be greater than", comparator.compare(aaa, bbb), greaterThan(0));
      MatcherAssert.assertThat("Greater than size should be less than", comparator.compare(bbb, aaa), lessThan(0));
   }

   @Test
   void testOrderByType() {
      final FileMetaDataComparator comparator = new FileMetaDataComparator(OrderBy.Type, OrderDirection.Ascending);

      MatcherAssert.assertThat("Same file type date should equal - no extension", comparator.compare(aaa, aaa), equalTo(0));
      MatcherAssert.assertThat("Same file type date should equal - with extension", comparator.compare(emptyTxt, emptyTxt), equalTo(0));
      MatcherAssert.assertThat("Alpha order type - lt", comparator.compare(emptyTxt, emptyXml), lessThan(0));
      MatcherAssert.assertThat("Alpha order type - gt", comparator.compare(emptyXml, emptyTxt), greaterThan(0));

      MatcherAssert.assertThat("Same file type - directory - should equal - no extension", comparator.compare(dir1, dir1), equalTo(0));
      MatcherAssert.assertThat("Alpha order type - directory - lt", comparator.compare(dir1, dir2), lessThan(0));
      MatcherAssert.assertThat("Alpha order type - directory - gt", comparator.compare(dir2, dir1), greaterThan(0));

      MatcherAssert.assertThat("Alpha order type - file and directory - lt - dirs sort before files", comparator.compare(dir1, aaa), lessThan(0));
      MatcherAssert.assertThat("Alpha order type - file and directory - gt - dirs sort before files", comparator.compare(aaa, dir1), greaterThan(0));
   }

   @Test
   void testOrderByType_Descending() {
      final FileMetaDataComparator comparator = new FileMetaDataComparator(OrderBy.Type, OrderDirection.Descending);

      MatcherAssert.assertThat("Same file type date should equal - no extension", comparator.compare(aaa, aaa), equalTo(0));
      MatcherAssert.assertThat("Same file type date should equal - with extension", comparator.compare(emptyTxt, emptyTxt), equalTo(0));
      MatcherAssert.assertThat("Alpha order type - lt", comparator.compare(emptyTxt, emptyXml), greaterThan(0));
      MatcherAssert.assertThat("Alpha order type - gt", comparator.compare(emptyXml, emptyTxt), lessThan(0));

      MatcherAssert.assertThat("Same file type - directory - should equal - no extension", comparator.compare(dir1, dir1), equalTo(0));
      MatcherAssert.assertThat("Alpha order type - directory - lt", comparator.compare(dir1, dir2), greaterThan(0));
      MatcherAssert.assertThat("Alpha order type - directory - gt", comparator.compare(dir2, dir1), lessThan(0));

      MatcherAssert.assertThat("Alpha order type - file and directory - lt - dirs sort before files", comparator.compare(dir1, aaa), greaterThan(0));
      MatcherAssert.assertThat("Alpha order type - file and directory - gt - dirs sort before files", comparator.compare(aaa, dir1), lessThan(0));
   }

   @Test
   void testOrderByName() {
      final FileMetaDataComparator comparator = new FileMetaDataComparator(OrderBy.Name, OrderDirection.Ascending);

      MatcherAssert.assertThat("Same file name should equal", comparator.compare(aaa, aaa), equalTo(0));
      MatcherAssert.assertThat("Less than name should be less than", comparator.compare(aaa, bbb), lessThan(0));
      MatcherAssert.assertThat("Greater than name should be greater than", comparator.compare(bbb, aaa), greaterThan(0));
   }

   @Test
   void testOrderByName_Descending() {
      final FileMetaDataComparator comparator = new FileMetaDataComparator(OrderBy.Name, OrderDirection.Descending);

      MatcherAssert.assertThat("Same file name should equal", comparator.compare(aaa, aaa), equalTo(0));
      MatcherAssert.assertThat("Less than name should be greater than", comparator.compare(aaa, bbb), greaterThan(0));
      MatcherAssert.assertThat("Greater than name should be less than", comparator.compare(bbb, aaa), lessThan(0));
   }
}
