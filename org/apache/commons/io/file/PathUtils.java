package org.apache.commons.io.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.IOUtils;

public final class PathUtils {
   public static final CopyOption[] EMPTY_COPY_OPTIONS = new CopyOption[0];
   public static final DeleteOption[] EMPTY_DELETE_OPTION_ARRAY = new DeleteOption[0];
   public static final FileVisitOption[] EMPTY_FILE_VISIT_OPTION_ARRAY = new FileVisitOption[0];
   public static final LinkOption[] EMPTY_LINK_OPTION_ARRAY = new LinkOption[0];
   public static final LinkOption[] NOFOLLOW_LINK_OPTION_ARRAY;
   public static final OpenOption[] EMPTY_OPEN_OPTION_ARRAY;
   public static final Path[] EMPTY_PATH_ARRAY;

   private static AccumulatorPathVisitor accumulate(Path directory, int maxDepth, FileVisitOption[] fileVisitOptions) throws IOException {
      return (AccumulatorPathVisitor)visitFileTree(AccumulatorPathVisitor.withLongCounters(), directory, toFileVisitOptionSet(fileVisitOptions), maxDepth);
   }

   public static Counters.PathCounters cleanDirectory(Path directory) throws IOException {
      return cleanDirectory(directory, EMPTY_DELETE_OPTION_ARRAY);
   }

   public static Counters.PathCounters cleanDirectory(Path directory, DeleteOption... deleteOptions) throws IOException {
      return ((CleaningPathVisitor)visitFileTree(new CleaningPathVisitor(Counters.longPathCounters(), deleteOptions, new String[0]), (Path)directory)).getPathCounters();
   }

   public static Counters.PathCounters copyDirectory(Path sourceDirectory, Path targetDirectory, CopyOption... copyOptions) throws IOException {
      Path absoluteSource = sourceDirectory.toAbsolutePath();
      return ((CopyDirectoryVisitor)visitFileTree(new CopyDirectoryVisitor(Counters.longPathCounters(), absoluteSource, targetDirectory, copyOptions), (Path)absoluteSource)).getPathCounters();
   }

   public static Path copyFile(URL sourceFile, Path targetFile, CopyOption... copyOptions) throws IOException {
      InputStream inputStream = sourceFile.openStream();
      Throwable var4 = null;

      Path var5;
      try {
         Files.copy(inputStream, targetFile, copyOptions);
         var5 = targetFile;
      } catch (Throwable var14) {
         var4 = var14;
         throw var14;
      } finally {
         if (inputStream != null) {
            if (var4 != null) {
               try {
                  inputStream.close();
               } catch (Throwable var13) {
                  var4.addSuppressed(var13);
               }
            } else {
               inputStream.close();
            }
         }

      }

      return var5;
   }

   public static Path copyFileToDirectory(Path sourceFile, Path targetDirectory, CopyOption... copyOptions) throws IOException {
      return Files.copy(sourceFile, targetDirectory.resolve(sourceFile.getFileName()), copyOptions);
   }

   public static Path copyFileToDirectory(URL sourceFile, Path targetDirectory, CopyOption... copyOptions) throws IOException {
      InputStream inputStream = sourceFile.openStream();
      Throwable var4 = null;

      Path var5;
      try {
         Files.copy(inputStream, targetDirectory.resolve(sourceFile.getFile()), copyOptions);
         var5 = targetDirectory;
      } catch (Throwable var14) {
         var4 = var14;
         throw var14;
      } finally {
         if (inputStream != null) {
            if (var4 != null) {
               try {
                  inputStream.close();
               } catch (Throwable var13) {
                  var4.addSuppressed(var13);
               }
            } else {
               inputStream.close();
            }
         }

      }

      return var5;
   }

   public static Counters.PathCounters countDirectory(Path directory) throws IOException {
      return ((CountingPathVisitor)visitFileTree(new CountingPathVisitor(Counters.longPathCounters()), (Path)directory)).getPathCounters();
   }

   public static Path createParentDirectories(Path path, FileAttribute... attrs) throws IOException {
      Path parent = path.getParent();
      return parent == null ? null : Files.createDirectories(parent, attrs);
   }

   public static Path current() {
      return Paths.get("");
   }

   public static Counters.PathCounters delete(Path path) throws IOException {
      return delete(path, EMPTY_DELETE_OPTION_ARRAY);
   }

   public static Counters.PathCounters delete(Path path, DeleteOption... deleteOptions) throws IOException {
      return Files.isDirectory(path, new LinkOption[]{LinkOption.NOFOLLOW_LINKS}) ? deleteDirectory(path, deleteOptions) : deleteFile(path, deleteOptions);
   }

   public static Counters.PathCounters delete(Path path, LinkOption[] linkOptions, DeleteOption... deleteOptions) throws IOException {
      return Files.isDirectory(path, linkOptions) ? deleteDirectory(path, linkOptions, deleteOptions) : deleteFile(path, linkOptions, deleteOptions);
   }

   public static Counters.PathCounters deleteDirectory(Path directory) throws IOException {
      return deleteDirectory(directory, EMPTY_DELETE_OPTION_ARRAY);
   }

   public static Counters.PathCounters deleteDirectory(Path directory, DeleteOption... deleteOptions) throws IOException {
      return ((DeletingPathVisitor)visitFileTree(new DeletingPathVisitor(Counters.longPathCounters(), NOFOLLOW_LINK_OPTION_ARRAY, deleteOptions, new String[0]), (Path)directory)).getPathCounters();
   }

   public static Counters.PathCounters deleteDirectory(Path directory, LinkOption[] linkOptions, DeleteOption... deleteOptions) throws IOException {
      return ((DeletingPathVisitor)visitFileTree(new DeletingPathVisitor(Counters.longPathCounters(), linkOptions, deleteOptions, new String[0]), (Path)directory)).getPathCounters();
   }

   public static Counters.PathCounters deleteFile(Path file) throws IOException {
      return deleteFile(file, EMPTY_DELETE_OPTION_ARRAY);
   }

   public static Counters.PathCounters deleteFile(Path file, DeleteOption... deleteOptions) throws IOException {
      return deleteFile(file, NOFOLLOW_LINK_OPTION_ARRAY, deleteOptions);
   }

   public static Counters.PathCounters deleteFile(Path file, LinkOption[] linkOptions, DeleteOption... deleteOptions) throws NoSuchFileException, IOException {
      if (Files.isDirectory(file, linkOptions)) {
         throw new NoSuchFileException(file.toString());
      } else {
         Counters.PathCounters pathCounts = Counters.longPathCounters();
         boolean exists = Files.exists(file, linkOptions);
         long size = exists && !Files.isSymbolicLink(file) ? Files.size(file) : 0L;
         if (overrideReadOnly(deleteOptions) && exists) {
            setReadOnly(file, false, linkOptions);
         }

         if (Files.deleteIfExists(file)) {
            pathCounts.getFileCounter().increment();
            pathCounts.getByteCounter().add(size);
         }

         return pathCounts;
      }
   }

   public static boolean directoryAndFileContentEquals(Path path1, Path path2) throws IOException {
      return directoryAndFileContentEquals(path1, path2, EMPTY_LINK_OPTION_ARRAY, EMPTY_OPEN_OPTION_ARRAY, EMPTY_FILE_VISIT_OPTION_ARRAY);
   }

   public static boolean directoryAndFileContentEquals(Path path1, Path path2, LinkOption[] linkOptions, OpenOption[] openOptions, FileVisitOption[] fileVisitOption) throws IOException {
      if (path1 == null && path2 == null) {
         return true;
      } else if (path1 != null && path2 != null) {
         if (Files.notExists(path1, new LinkOption[0]) && Files.notExists(path2, new LinkOption[0])) {
            return true;
         } else {
            RelativeSortedPaths relativeSortedPaths = new RelativeSortedPaths(path1, path2, Integer.MAX_VALUE, linkOptions, fileVisitOption);
            if (!relativeSortedPaths.equals) {
               return false;
            } else {
               List fileList1 = relativeSortedPaths.relativeFileList1;
               List fileList2 = relativeSortedPaths.relativeFileList2;
               Iterator var8 = fileList1.iterator();

               Path path;
               do {
                  if (!var8.hasNext()) {
                     return true;
                  }

                  path = (Path)var8.next();
                  int binarySearch = Collections.binarySearch(fileList2, path);
                  if (binarySearch <= -1) {
                     throw new IllegalStateException("Unexpected mismatch.");
                  }
               } while(fileContentEquals(path1.resolve(path), path2.resolve(path), linkOptions, openOptions));

               return false;
            }
         }
      } else {
         return false;
      }
   }

   public static boolean directoryContentEquals(Path path1, Path path2) throws IOException {
      return directoryContentEquals(path1, path2, Integer.MAX_VALUE, EMPTY_LINK_OPTION_ARRAY, EMPTY_FILE_VISIT_OPTION_ARRAY);
   }

   public static boolean directoryContentEquals(Path path1, Path path2, int maxDepth, LinkOption[] linkOptions, FileVisitOption[] fileVisitOptions) throws IOException {
      return (new RelativeSortedPaths(path1, path2, maxDepth, linkOptions, fileVisitOptions)).equals;
   }

   public static boolean fileContentEquals(Path path1, Path path2) throws IOException {
      return fileContentEquals(path1, path2, EMPTY_LINK_OPTION_ARRAY, EMPTY_OPEN_OPTION_ARRAY);
   }

   public static boolean fileContentEquals(Path path1, Path path2, LinkOption[] linkOptions, OpenOption[] openOptions) throws IOException {
      if (path1 == null && path2 == null) {
         return true;
      } else if (path1 != null && path2 != null) {
         Path nPath1 = path1.normalize();
         Path nPath2 = path2.normalize();
         boolean path1Exists = Files.exists(nPath1, linkOptions);
         if (path1Exists != Files.exists(nPath2, linkOptions)) {
            return false;
         } else if (!path1Exists) {
            return true;
         } else if (Files.isDirectory(nPath1, linkOptions)) {
            throw new IOException("Can't compare directories, only files: " + nPath1);
         } else if (Files.isDirectory(nPath2, linkOptions)) {
            throw new IOException("Can't compare directories, only files: " + nPath2);
         } else if (Files.size(nPath1) != Files.size(nPath2)) {
            return false;
         } else if (path1.equals(path2)) {
            return true;
         } else {
            InputStream inputStream1 = Files.newInputStream(nPath1, openOptions);
            Throwable var8 = null;

            Throwable var11;
            try {
               InputStream inputStream2 = Files.newInputStream(nPath2, openOptions);
               Throwable var10 = null;

               try {
                  var11 = IOUtils.contentEquals(inputStream1, inputStream2);
               } catch (Throwable var34) {
                  var11 = var34;
                  var10 = var34;
                  throw var34;
               } finally {
                  if (inputStream2 != null) {
                     if (var10 != null) {
                        try {
                           inputStream2.close();
                        } catch (Throwable var33) {
                           var10.addSuppressed(var33);
                        }
                     } else {
                        inputStream2.close();
                     }
                  }

               }
            } catch (Throwable var36) {
               var8 = var36;
               throw var36;
            } finally {
               if (inputStream1 != null) {
                  if (var8 != null) {
                     try {
                        inputStream1.close();
                     } catch (Throwable var32) {
                        var8.addSuppressed(var32);
                     }
                  } else {
                     inputStream1.close();
                  }
               }

            }

            return (boolean)var11;
         }
      } else {
         return false;
      }
   }

   public static Path[] filter(PathFilter filter, Path... paths) {
      Objects.requireNonNull(filter, "filter");
      return paths == null ? EMPTY_PATH_ARRAY : (Path[])((List)filterPaths(filter, Stream.of(paths), Collectors.toList())).toArray(EMPTY_PATH_ARRAY);
   }

   private static Object filterPaths(PathFilter filter, Stream stream, Collector collector) {
      Objects.requireNonNull(filter, "filter");
      Objects.requireNonNull(collector, "collector");
      return stream == null ? Stream.empty().collect(collector) : stream.filter((p) -> {
         try {
            return p != null && filter.accept(p, readBasicFileAttributes(p)) == FileVisitResult.CONTINUE;
         } catch (IOException var3) {
            return false;
         }
      }).collect(collector);
   }

   public static List getAclEntryList(Path sourcePath) throws IOException {
      AclFileAttributeView fileAttributeView = (AclFileAttributeView)Files.getFileAttributeView(sourcePath, AclFileAttributeView.class);
      return fileAttributeView == null ? null : fileAttributeView.getAcl();
   }

   public static boolean isDirectory(Path path, LinkOption... options) {
      return path != null && Files.isDirectory(path, options);
   }

   public static boolean isEmpty(Path path) throws IOException {
      return Files.isDirectory(path, new LinkOption[0]) ? isEmptyDirectory(path) : isEmptyFile(path);
   }

   public static boolean isEmptyDirectory(Path directory) throws IOException {
      DirectoryStream directoryStream = Files.newDirectoryStream(directory);
      Throwable var2 = null;

      boolean var3;
      try {
         var3 = !directoryStream.iterator().hasNext();
      } catch (Throwable var12) {
         var2 = var12;
         throw var12;
      } finally {
         if (directoryStream != null) {
            if (var2 != null) {
               try {
                  directoryStream.close();
               } catch (Throwable var11) {
                  var2.addSuppressed(var11);
               }
            } else {
               directoryStream.close();
            }
         }

      }

      return var3;
   }

   public static boolean isEmptyFile(Path file) throws IOException {
      return Files.size(file) <= 0L;
   }

   public static boolean isNewer(Path file, long timeMillis, LinkOption... options) throws IOException {
      Objects.requireNonNull(file, "file");
      if (Files.notExists(file, new LinkOption[0])) {
         return false;
      } else {
         return Files.getLastModifiedTime(file, options).toMillis() > timeMillis;
      }
   }

   public static boolean isRegularFile(Path path, LinkOption... options) {
      return path != null && Files.isRegularFile(path, options);
   }

   public static DirectoryStream newDirectoryStream(Path dir, PathFilter pathFilter) throws IOException {
      return Files.newDirectoryStream(dir, new DirectoryStreamFilter(pathFilter));
   }

   private static boolean overrideReadOnly(DeleteOption... deleteOptions) {
      return deleteOptions == null ? false : Stream.of(deleteOptions).anyMatch((e) -> {
         return e == StandardDeleteOption.OVERRIDE_READ_ONLY;
      });
   }

   public static BasicFileAttributes readBasicFileAttributes(Path path) throws IOException {
      return Files.readAttributes(path, BasicFileAttributes.class);
   }

   public static BasicFileAttributes readBasicFileAttributesUnchecked(Path path) {
      try {
         return readBasicFileAttributes(path);
      } catch (IOException var2) {
         throw new UncheckedIOException(var2);
      }
   }

   static List relativize(Collection collection, Path parent, boolean sort, Comparator comparator) {
      Stream var10000 = collection.stream();
      parent.getClass();
      Stream stream = var10000.map(parent::relativize);
      if (sort) {
         stream = comparator == null ? stream.sorted() : stream.sorted(comparator);
      }

      return (List)stream.collect(Collectors.toList());
   }

   public static Path setReadOnly(Path path, boolean readOnly, LinkOption... linkOptions) throws IOException {
      List causeList = new ArrayList(2);
      DosFileAttributeView fileAttributeView = (DosFileAttributeView)Files.getFileAttributeView(path, DosFileAttributeView.class, linkOptions);
      if (fileAttributeView != null) {
         try {
            fileAttributeView.setReadOnly(readOnly);
            return path;
         } catch (IOException var10) {
            causeList.add(var10);
         }
      }

      PosixFileAttributeView posixFileAttributeView = (PosixFileAttributeView)Files.getFileAttributeView(path, PosixFileAttributeView.class, linkOptions);
      if (posixFileAttributeView != null) {
         PosixFileAttributes readAttributes = posixFileAttributeView.readAttributes();
         Set permissions = readAttributes.permissions();
         permissions.remove(PosixFilePermission.OWNER_WRITE);
         permissions.remove(PosixFilePermission.GROUP_WRITE);
         permissions.remove(PosixFilePermission.OTHERS_WRITE);

         try {
            return Files.setPosixFilePermissions(path, permissions);
         } catch (IOException var9) {
            causeList.add(var9);
         }
      }

      if (!causeList.isEmpty()) {
         throw new IOExceptionList(path.toString(), causeList);
      } else {
         throw new IOException(String.format("No DosFileAttributeView or PosixFileAttributeView for '%s' (linkOptions=%s)", path, Arrays.toString(linkOptions)));
      }
   }

   static Set toFileVisitOptionSet(FileVisitOption... fileVisitOptions) {
      return (Set)(fileVisitOptions == null ? EnumSet.noneOf(FileVisitOption.class) : (Set)Stream.of(fileVisitOptions).collect(Collectors.toSet()));
   }

   public static FileVisitor visitFileTree(FileVisitor visitor, Path directory) throws IOException {
      Files.walkFileTree(directory, visitor);
      return visitor;
   }

   public static FileVisitor visitFileTree(FileVisitor visitor, Path start, Set options, int maxDepth) throws IOException {
      Files.walkFileTree(start, options, maxDepth, visitor);
      return visitor;
   }

   public static FileVisitor visitFileTree(FileVisitor visitor, String first, String... more) throws IOException {
      return visitFileTree(visitor, Paths.get(first, more));
   }

   public static FileVisitor visitFileTree(FileVisitor visitor, URI uri) throws IOException {
      return visitFileTree(visitor, Paths.get(uri));
   }

   public static Stream walk(Path start, PathFilter pathFilter, int maxDepth, boolean readAttributes, FileVisitOption... options) throws IOException {
      return Files.walk(start, maxDepth, options).filter((path) -> {
         return pathFilter.accept(path, readAttributes ? readBasicFileAttributesUnchecked(path) : null) == FileVisitResult.CONTINUE;
      });
   }

   private PathUtils() {
   }

   static {
      NOFOLLOW_LINK_OPTION_ARRAY = new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
      EMPTY_OPEN_OPTION_ARRAY = new OpenOption[0];
      EMPTY_PATH_ARRAY = new Path[0];
   }

   private static class RelativeSortedPaths {
      final boolean equals;
      final List relativeFileList1;
      final List relativeFileList2;

      private RelativeSortedPaths(Path dir1, Path dir2, int maxDepth, LinkOption[] linkOptions, FileVisitOption[] fileVisitOptions) throws IOException {
         List tmpRelativeFileList1 = null;
         List tmpRelativeFileList2 = null;
         if (dir1 == null && dir2 == null) {
            this.equals = true;
         } else if (dir1 == null ^ dir2 == null) {
            this.equals = false;
         } else {
            boolean parentDirNotExists1 = Files.notExists(dir1, linkOptions);
            boolean parentDirNotExists2 = Files.notExists(dir2, linkOptions);
            if (!parentDirNotExists1 && !parentDirNotExists2) {
               AccumulatorPathVisitor visitor1 = PathUtils.accumulate(dir1, maxDepth, fileVisitOptions);
               AccumulatorPathVisitor visitor2 = PathUtils.accumulate(dir2, maxDepth, fileVisitOptions);
               if (visitor1.getDirList().size() == visitor2.getDirList().size() && visitor1.getFileList().size() == visitor2.getFileList().size()) {
                  List tmpRelativeDirList1 = visitor1.relativizeDirectories(dir1, true, (Comparator)null);
                  List tmpRelativeDirList2 = visitor2.relativizeDirectories(dir2, true, (Comparator)null);
                  if (!tmpRelativeDirList1.equals(tmpRelativeDirList2)) {
                     this.equals = false;
                  } else {
                     tmpRelativeFileList1 = visitor1.relativizeFiles(dir1, true, (Comparator)null);
                     tmpRelativeFileList2 = visitor2.relativizeFiles(dir2, true, (Comparator)null);
                     this.equals = tmpRelativeFileList1.equals(tmpRelativeFileList2);
                  }
               } else {
                  this.equals = false;
               }
            } else {
               this.equals = parentDirNotExists1 && parentDirNotExists2;
            }
         }

         this.relativeFileList1 = tmpRelativeFileList1;
         this.relativeFileList2 = tmpRelativeFileList2;
      }

      // $FF: synthetic method
      RelativeSortedPaths(Path x0, Path x1, int x2, LinkOption[] x3, FileVisitOption[] x4, Object x5) throws IOException {
         this(x0, x1, x2, x3, x4);
      }
   }
}
