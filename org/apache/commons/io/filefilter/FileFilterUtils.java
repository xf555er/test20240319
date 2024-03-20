package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;

public class FileFilterUtils {
   private static final IOFileFilter cvsFilter = notFileFilter(and(directoryFileFilter(), nameFileFilter("CVS")));
   private static final IOFileFilter svnFilter = notFileFilter(and(directoryFileFilter(), nameFileFilter(".svn")));

   public static IOFileFilter ageFileFilter(Date cutoffDate) {
      return new AgeFileFilter(cutoffDate);
   }

   public static IOFileFilter ageFileFilter(Date cutoffDate, boolean acceptOlder) {
      return new AgeFileFilter(cutoffDate, acceptOlder);
   }

   public static IOFileFilter ageFileFilter(File cutoffReference) {
      return new AgeFileFilter(cutoffReference);
   }

   public static IOFileFilter ageFileFilter(File cutoffReference, boolean acceptOlder) {
      return new AgeFileFilter(cutoffReference, acceptOlder);
   }

   public static IOFileFilter ageFileFilter(long cutoff) {
      return new AgeFileFilter(cutoff);
   }

   public static IOFileFilter ageFileFilter(long cutoff, boolean acceptOlder) {
      return new AgeFileFilter(cutoff, acceptOlder);
   }

   public static IOFileFilter and(IOFileFilter... filters) {
      return new AndFileFilter(toList(filters));
   }

   /** @deprecated */
   @Deprecated
   public static IOFileFilter andFileFilter(IOFileFilter filter1, IOFileFilter filter2) {
      return new AndFileFilter(filter1, filter2);
   }

   public static IOFileFilter asFileFilter(FileFilter filter) {
      return new DelegateFileFilter(filter);
   }

   public static IOFileFilter asFileFilter(FilenameFilter filter) {
      return new DelegateFileFilter(filter);
   }

   public static IOFileFilter directoryFileFilter() {
      return DirectoryFileFilter.DIRECTORY;
   }

   public static IOFileFilter falseFileFilter() {
      return FalseFileFilter.FALSE;
   }

   public static IOFileFilter fileFileFilter() {
      return FileFileFilter.INSTANCE;
   }

   public static File[] filter(IOFileFilter filter, File... files) {
      if (filter == null) {
         throw new IllegalArgumentException("file filter is null");
      } else {
         return files == null ? FileUtils.EMPTY_FILE_ARRAY : (File[])((List)filterFiles(filter, Stream.of(files), Collectors.toList())).toArray(FileUtils.EMPTY_FILE_ARRAY);
      }
   }

   private static Object filterFiles(IOFileFilter filter, Stream stream, Collector collector) {
      Objects.requireNonNull(collector, "collector");
      if (filter == null) {
         throw new IllegalArgumentException("file filter is null");
      } else if (stream == null) {
         return Stream.empty().collect(collector);
      } else {
         filter.getClass();
         return stream.filter(filter::accept).collect(collector);
      }
   }

   public static File[] filter(IOFileFilter filter, Iterable files) {
      return (File[])filterList(filter, files).toArray(FileUtils.EMPTY_FILE_ARRAY);
   }

   public static List filterList(IOFileFilter filter, File... files) {
      return Arrays.asList(filter(filter, files));
   }

   public static List filterList(IOFileFilter filter, Iterable files) {
      return files == null ? Collections.emptyList() : (List)filterFiles(filter, StreamSupport.stream(files.spliterator(), false), Collectors.toList());
   }

   public static Set filterSet(IOFileFilter filter, File... files) {
      return new HashSet(Arrays.asList(filter(filter, files)));
   }

   public static Set filterSet(IOFileFilter filter, Iterable files) {
      return files == null ? Collections.emptySet() : (Set)filterFiles(filter, StreamSupport.stream(files.spliterator(), false), Collectors.toSet());
   }

   public static IOFileFilter magicNumberFileFilter(byte[] magicNumber) {
      return new MagicNumberFileFilter(magicNumber);
   }

   public static IOFileFilter magicNumberFileFilter(byte[] magicNumber, long offset) {
      return new MagicNumberFileFilter(magicNumber, offset);
   }

   public static IOFileFilter magicNumberFileFilter(String magicNumber) {
      return new MagicNumberFileFilter(magicNumber);
   }

   public static IOFileFilter magicNumberFileFilter(String magicNumber, long offset) {
      return new MagicNumberFileFilter(magicNumber, offset);
   }

   public static IOFileFilter makeCVSAware(IOFileFilter filter) {
      return filter == null ? cvsFilter : and(filter, cvsFilter);
   }

   public static IOFileFilter makeDirectoryOnly(IOFileFilter filter) {
      return filter == null ? DirectoryFileFilter.DIRECTORY : DirectoryFileFilter.DIRECTORY.and(filter);
   }

   public static IOFileFilter makeFileOnly(IOFileFilter filter) {
      return filter == null ? FileFileFilter.INSTANCE : FileFileFilter.INSTANCE.and(filter);
   }

   public static IOFileFilter makeSVNAware(IOFileFilter filter) {
      return filter == null ? svnFilter : and(filter, svnFilter);
   }

   public static IOFileFilter nameFileFilter(String name) {
      return new NameFileFilter(name);
   }

   public static IOFileFilter nameFileFilter(String name, IOCase caseSensitivity) {
      return new NameFileFilter(name, caseSensitivity);
   }

   public static IOFileFilter notFileFilter(IOFileFilter filter) {
      return filter.negate();
   }

   public static IOFileFilter or(IOFileFilter... filters) {
      return new OrFileFilter(toList(filters));
   }

   /** @deprecated */
   @Deprecated
   public static IOFileFilter orFileFilter(IOFileFilter filter1, IOFileFilter filter2) {
      return new OrFileFilter(filter1, filter2);
   }

   public static IOFileFilter prefixFileFilter(String prefix) {
      return new PrefixFileFilter(prefix);
   }

   public static IOFileFilter prefixFileFilter(String prefix, IOCase caseSensitivity) {
      return new PrefixFileFilter(prefix, caseSensitivity);
   }

   public static IOFileFilter sizeFileFilter(long threshold) {
      return new SizeFileFilter(threshold);
   }

   public static IOFileFilter sizeFileFilter(long threshold, boolean acceptLarger) {
      return new SizeFileFilter(threshold, acceptLarger);
   }

   public static IOFileFilter sizeRangeFileFilter(long minSizeInclusive, long maxSizeInclusive) {
      IOFileFilter minimumFilter = new SizeFileFilter(minSizeInclusive, true);
      IOFileFilter maximumFilter = new SizeFileFilter(maxSizeInclusive + 1L, false);
      return minimumFilter.and(maximumFilter);
   }

   public static IOFileFilter suffixFileFilter(String suffix) {
      return new SuffixFileFilter(suffix);
   }

   public static IOFileFilter suffixFileFilter(String suffix, IOCase caseSensitivity) {
      return new SuffixFileFilter(suffix, caseSensitivity);
   }

   public static List toList(IOFileFilter... filters) {
      if (filters == null) {
         throw new IllegalArgumentException("The filters must not be null");
      } else {
         List list = new ArrayList(filters.length);

         for(int i = 0; i < filters.length; ++i) {
            if (filters[i] == null) {
               throw new IllegalArgumentException("The filter[" + i + "] is null");
            }

            list.add(filters[i]);
         }

         return list;
      }
   }

   public static IOFileFilter trueFileFilter() {
      return TrueFileFilter.TRUE;
   }
}
