package com.chainstaysoftware.filechooser;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.util.List;

/**
 * WildcardFilter that accepts all directory entries.
 */
public class DirOrWildcardFilter extends WildcardFileFilter {
   /**
    * Construct a new case-sensitive wildcard filter for a single wildcard.
    *
    * @param wildcard the wildcard to match
    * @throws IllegalArgumentException if the pattern is null
    */
   public DirOrWildcardFilter(String wildcard) {
      super(wildcard);
   }

   /**
    * Construct a new wildcard filter for a single wildcard specifying case-sensitivity.
    *
    * @param wildcard        the wildcard to match, not null
    * @param caseSensitivity how to handle case sensitivity, null means case-sensitive
    * @throws IllegalArgumentException if the pattern is null
    */
   public DirOrWildcardFilter(String wildcard, IOCase caseSensitivity) {
      super(wildcard, caseSensitivity);
   }

   /**
    * Construct a new case-sensitive wildcard filter for an array of wildcards.
    * <p>
    * The array is not cloned, so could be changed after constructing the
    * instance. This would be inadvisable however.
    *
    * @param wildcards the array of wildcards to match
    * @throws IllegalArgumentException if the pattern array is null
    */
   public DirOrWildcardFilter(String[] wildcards) {
      super(wildcards);
   }

   /**
    * Construct a new wildcard filter for an array of wildcards specifying case-sensitivity.
    * <p>
    * The array is not cloned, so could be changed after constructing the
    * instance. This would be inadvisable however.
    *
    * @param wildcards       the array of wildcards to match, not null
    * @param caseSensitivity how to handle case sensitivity, null means case-sensitive
    * @throws IllegalArgumentException if the pattern array is null
    */
   public DirOrWildcardFilter(String[] wildcards, IOCase caseSensitivity) {
      super(wildcards, caseSensitivity);
   }

   /**
    * Construct a new case-sensitive wildcard filter for a list of wildcards.
    *
    * @param wildcards the list of wildcards to match, not null
    * @throws IllegalArgumentException if the pattern list is null
    * @throws ClassCastException       if the list does not contain Strings
    */
   public DirOrWildcardFilter(List<String> wildcards) {
      super(wildcards);
   }

   /**
    * Construct a new wildcard filter for a list of wildcards specifying case-sensitivity.
    *
    * @param wildcards       the list of wildcards to match, not null
    * @param caseSensitivity how to handle case sensitivity, null means case-sensitive
    * @throws IllegalArgumentException if the pattern list is null
    * @throws ClassCastException       if the list does not contain Strings
    */
   public DirOrWildcardFilter(List<String> wildcards, IOCase caseSensitivity) {
      super(wildcards, caseSensitivity);
   }

   /**
    * Checks to see if the filename matches one of the wildcards. Or, the
    * filename refers to a directory.
    *
    * @param dir  the file directory (ignored)
    * @param name the filename
    * @return true if the filename matches one of the wildcards
    */
   @Override
   public boolean accept(File dir, String name) {
      return super.accept(dir, name);
   }

   /**
    * Checks to see if the filename matches one of the wildcards. Or, the
    * filename refers to a directory.
    *
    * @param file the file to check
    * @return true if the filename matches one of the wildcards
    */
   @Override
   public boolean accept(File file) {
      return file.isDirectory() || super.accept(file);
   }
}
