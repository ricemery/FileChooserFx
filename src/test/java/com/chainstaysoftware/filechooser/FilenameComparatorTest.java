package com.chainstaysoftware.filechooser;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

public class FilenameComparatorTest {
   private final File testDataDir = new File("./src/test/resources/com/chainstaysoftware/filechooser");
   private final File aaa = new File(testDataDir, "aaa");
   private final File bbb = new File(testDataDir, "bbb");
   private final File emptyTxt = new File(testDataDir, "empty.txt");
   private final File emptyXml = new File(testDataDir, "empty.xml");
   private final File dir1 = new File(testDataDir, "dir1");
   private final File dir2 = new File(testDataDir, "dir2");

   @Test
   public void testOrderByModificationDate() {
      final FilenameComparator comparator = new FilenameComparator(OrderBy.ModificationDate, OrderDirection.Ascending);

      bbb.setLastModified(System.currentTimeMillis());

      Assert.assertThat("Same file mod date should equal", comparator.compare(aaa, aaa), equalTo(0));
      Assert.assertThat("Less than mod date should be less than", comparator.compare(aaa, bbb), lessThan(0));
      Assert.assertThat("Greater than mod date should be greater than", comparator.compare(bbb, aaa), greaterThan(0));
   }

   @Test
   public void testOrderByModificationDate_Descending() {
      final FilenameComparator comparator = new FilenameComparator(OrderBy.ModificationDate, OrderDirection.Descending);

      bbb.setLastModified(System.currentTimeMillis());

      Assert.assertThat("Same file mod date should equal", comparator.compare(aaa, aaa), equalTo(0));
      Assert.assertThat("Less than mod date should be greater than", comparator.compare(aaa, bbb), greaterThan(0));
      Assert.assertThat("Greater than mod date should be less than", comparator.compare(bbb, aaa), lessThan(0));
   }

   @Test
   public void testOrderBySize() {
      final FilenameComparator comparator = new FilenameComparator(OrderBy.Size, OrderDirection.Ascending);

      Assert.assertThat("Same file size should equal", comparator.compare(aaa, aaa), equalTo(0));
      Assert.assertThat("Less than size should be less than", comparator.compare(aaa, bbb), lessThan(0));
      Assert.assertThat("Greater than size should be greater than", comparator.compare(bbb, aaa), greaterThan(0));
   }

   @Test
   public void testOrderBySize_Descending() {
      final FilenameComparator comparator = new FilenameComparator(OrderBy.Size, OrderDirection.Descending);

      Assert.assertThat("Same file size should equal", comparator.compare(aaa, aaa), equalTo(0));
      Assert.assertThat("Less than size should be greater than", comparator.compare(aaa, bbb), greaterThan(0));
      Assert.assertThat("Greater than size should be less than", comparator.compare(bbb, aaa), lessThan(0));
   }

   @Test
   public void testOrderByType() {
      final FilenameComparator comparator = new FilenameComparator(OrderBy.Type, OrderDirection.Ascending);

      Assert.assertThat("Same file type date should equal - no extension", comparator.compare(aaa, aaa), equalTo(0));
      Assert.assertThat("Same file type date should equal - with extension", comparator.compare(emptyTxt, emptyTxt), equalTo(0));
      Assert.assertThat("Alpha order type - lt", comparator.compare(emptyTxt, emptyXml), lessThan(0));
      Assert.assertThat("Alpha order type - gt", comparator.compare(emptyXml, emptyTxt), greaterThan(0));

      Assert.assertThat("Same file type - directory - should equal - no extension", comparator.compare(dir1, dir1), equalTo(0));
      Assert.assertThat("Alpha order type - directory - lt", comparator.compare(dir1, dir2), lessThan(0));
      Assert.assertThat("Alpha order type - directory - gt", comparator.compare(dir2, dir1), greaterThan(0));

      Assert.assertThat("Alpha order type - file and directory - lt - dirs sort before files", comparator.compare(dir1, aaa), lessThan(0));
      Assert.assertThat("Alpha order type - file and directory - gt - dirs sort before files", comparator.compare(aaa, dir1), greaterThan(0));
   }

   @Test
   public void testOrderByType_Descending() {
      final FilenameComparator comparator = new FilenameComparator(OrderBy.Type, OrderDirection.Descending);

      Assert.assertThat("Same file type date should equal - no extension", comparator.compare(aaa, aaa), equalTo(0));
      Assert.assertThat("Same file type date should equal - with extension", comparator.compare(emptyTxt, emptyTxt), equalTo(0));
      Assert.assertThat("Alpha order type - lt", comparator.compare(emptyTxt, emptyXml), greaterThan(0));
      Assert.assertThat("Alpha order type - gt", comparator.compare(emptyXml, emptyTxt), lessThan(0));

      Assert.assertThat("Same file type - directory - should equal - no extension", comparator.compare(dir1, dir1), equalTo(0));
      Assert.assertThat("Alpha order type - directory - lt", comparator.compare(dir1, dir2), greaterThan(0));
      Assert.assertThat("Alpha order type - directory - gt", comparator.compare(dir2, dir1), lessThan(0));

      Assert.assertThat("Alpha order type - file and directory - lt - dirs sort before files", comparator.compare(dir1, aaa), greaterThan(0));
      Assert.assertThat("Alpha order type - file and directory - gt - dirs sort before files", comparator.compare(aaa, dir1), lessThan(0));
   }

   @Test
   public void testOrderByName() {
      final FilenameComparator comparator = new FilenameComparator(OrderBy.Name, OrderDirection.Ascending);

      Assert.assertThat("Same file name should equal", comparator.compare(aaa, aaa), equalTo(0));
      Assert.assertThat("Less than name should be less than", comparator.compare(aaa, bbb), lessThan(0));
      Assert.assertThat("Greater than name should be greater than", comparator.compare(bbb, aaa), greaterThan(0));
   }

   @Test
   public void testOrderByName_Descending() {
      final FilenameComparator comparator = new FilenameComparator(OrderBy.Name, OrderDirection.Descending);

      Assert.assertThat("Same file name should equal", comparator.compare(aaa, aaa), equalTo(0));
      Assert.assertThat("Less than name should be greater than", comparator.compare(aaa, bbb), greaterThan(0));
      Assert.assertThat("Greater than name should be less than", comparator.compare(bbb, aaa), lessThan(0));
   }
}
