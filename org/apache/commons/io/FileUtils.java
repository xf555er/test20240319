package org.apache.commons.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;
import org.apache.commons.io.file.AccumulatorPathVisitor;
import org.apache.commons.io.file.Counters;
import org.apache.commons.io.file.PathFilter;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.StandardDeleteOption;
import org.apache.commons.io.filefilter.FileEqualsFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

public class FileUtils {
   public static final long ONE_KB = 1024L;
   public static final BigInteger ONE_KB_BI = BigInteger.valueOf(1024L);
   public static final long ONE_MB = 1048576L;
   public static final BigInteger ONE_MB_BI;
   public static final long ONE_GB = 1073741824L;
   public static final BigInteger ONE_GB_BI;
   public static final long ONE_TB = 1099511627776L;
   public static final BigInteger ONE_TB_BI;
   public static final long ONE_PB = 1125899906842624L;
   public static final BigInteger ONE_PB_BI;
   public static final long ONE_EB = 1152921504606846976L;
   public static final BigInteger ONE_EB_BI;
   public static final BigInteger ONE_ZB;
   public static final BigInteger ONE_YB;
   public static final File[] EMPTY_FILE_ARRAY;

   private static CopyOption[] addCopyAttributes(CopyOption... copyOptions) {
      CopyOption[] actual = (CopyOption[])Arrays.copyOf(copyOptions, copyOptions.length + 1);
      Arrays.sort(actual, 0, copyOptions.length);
      if (Arrays.binarySearch(copyOptions, 0, copyOptions.length, StandardCopyOption.COPY_ATTRIBUTES) >= 0) {
         return copyOptions;
      } else {
         actual[actual.length - 1] = StandardCopyOption.COPY_ATTRIBUTES;
         return actual;
      }
   }

   public static String byteCountToDisplaySize(BigInteger size) {
      Objects.requireNonNull(size, "size");
      String displaySize;
      if (size.divide(ONE_EB_BI).compareTo(BigInteger.ZERO) > 0) {
         displaySize = size.divide(ONE_EB_BI) + " EB";
      } else if (size.divide(ONE_PB_BI).compareTo(BigInteger.ZERO) > 0) {
         displaySize = size.divide(ONE_PB_BI) + " PB";
      } else if (size.divide(ONE_TB_BI).compareTo(BigInteger.ZERO) > 0) {
         displaySize = size.divide(ONE_TB_BI) + " TB";
      } else if (size.divide(ONE_GB_BI).compareTo(BigInteger.ZERO) > 0) {
         displaySize = size.divide(ONE_GB_BI) + " GB";
      } else if (size.divide(ONE_MB_BI).compareTo(BigInteger.ZERO) > 0) {
         displaySize = size.divide(ONE_MB_BI) + " MB";
      } else if (size.divide(ONE_KB_BI).compareTo(BigInteger.ZERO) > 0) {
         displaySize = size.divide(ONE_KB_BI) + " KB";
      } else {
         displaySize = size + " bytes";
      }

      return displaySize;
   }

   public static String byteCountToDisplaySize(long size) {
      return byteCountToDisplaySize(BigInteger.valueOf(size));
   }

   public static Checksum checksum(File file, Checksum checksum) throws IOException {
      requireExistsChecked(file, "file");
      requireFile(file, "file");
      Objects.requireNonNull(checksum, "checksum");
      InputStream inputStream = new CheckedInputStream(Files.newInputStream(file.toPath()), checksum);
      Throwable var3 = null;

      try {
         IOUtils.consume(inputStream);
      } catch (Throwable var12) {
         var3 = var12;
         throw var12;
      } finally {
         if (inputStream != null) {
            if (var3 != null) {
               try {
                  inputStream.close();
               } catch (Throwable var11) {
                  var3.addSuppressed(var11);
               }
            } else {
               inputStream.close();
            }
         }

      }

      return checksum;
   }

   public static long checksumCRC32(File file) throws IOException {
      return checksum(file, new CRC32()).getValue();
   }

   public static void cleanDirectory(File directory) throws IOException {
      File[] files = listFiles(directory, (FileFilter)null);
      List causeList = new ArrayList();
      File[] var3 = files;
      int var4 = files.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         File file = var3[var5];

         try {
            forceDelete(file);
         } catch (IOException var8) {
            causeList.add(var8);
         }
      }

      if (!causeList.isEmpty()) {
         throw new IOExceptionList(directory.toString(), causeList);
      }
   }

   private static void cleanDirectoryOnExit(File directory) throws IOException {
      File[] files = listFiles(directory, (FileFilter)null);
      List causeList = new ArrayList();
      File[] var3 = files;
      int var4 = files.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         File file = var3[var5];

         try {
            forceDeleteOnExit(file);
         } catch (IOException var8) {
            causeList.add(var8);
         }
      }

      if (!causeList.isEmpty()) {
         throw new IOExceptionList(causeList);
      }
   }

   public static boolean contentEquals(File file1, File file2) throws IOException {
      if (file1 == null && file2 == null) {
         return true;
      } else if (file1 != null && file2 != null) {
         boolean file1Exists = file1.exists();
         if (file1Exists != file2.exists()) {
            return false;
         } else if (!file1Exists) {
            return true;
         } else {
            requireFile(file1, "file1");
            requireFile(file2, "file2");
            if (file1.length() != file2.length()) {
               return false;
            } else if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
               return true;
            } else {
               InputStream input1 = Files.newInputStream(file1.toPath());
               Throwable var4 = null;

               Throwable var7;
               try {
                  InputStream input2 = Files.newInputStream(file2.toPath());
                  Throwable var6 = null;

                  try {
                     var7 = IOUtils.contentEquals(input1, input2);
                  } catch (Throwable var30) {
                     var7 = var30;
                     var6 = var30;
                     throw var30;
                  } finally {
                     if (input2 != null) {
                        if (var6 != null) {
                           try {
                              input2.close();
                           } catch (Throwable var29) {
                              var6.addSuppressed(var29);
                           }
                        } else {
                           input2.close();
                        }
                     }

                  }
               } catch (Throwable var32) {
                  var4 = var32;
                  throw var32;
               } finally {
                  if (input1 != null) {
                     if (var4 != null) {
                        try {
                           input1.close();
                        } catch (Throwable var28) {
                           var4.addSuppressed(var28);
                        }
                     } else {
                        input1.close();
                     }
                  }

               }

               return (boolean)var7;
            }
         }
      } else {
         return false;
      }
   }

   public static boolean contentEqualsIgnoreEOL(File file1, File file2, String charsetName) throws IOException {
      if (file1 == null && file2 == null) {
         return true;
      } else if (file1 != null && file2 != null) {
         boolean file1Exists = file1.exists();
         if (file1Exists != file2.exists()) {
            return false;
         } else if (!file1Exists) {
            return true;
         } else {
            requireFile(file1, "file1");
            requireFile(file2, "file2");
            if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
               return true;
            } else {
               Charset charset = Charsets.toCharset(charsetName);
               Reader input1 = new InputStreamReader(Files.newInputStream(file1.toPath()), charset);
               Throwable var6 = null;

               Throwable var9;
               try {
                  Reader input2 = new InputStreamReader(Files.newInputStream(file2.toPath()), charset);
                  Throwable var8 = null;

                  try {
                     var9 = IOUtils.contentEqualsIgnoreEOL(input1, input2);
                  } catch (Throwable var32) {
                     var9 = var32;
                     var8 = var32;
                     throw var32;
                  } finally {
                     if (input2 != null) {
                        if (var8 != null) {
                           try {
                              input2.close();
                           } catch (Throwable var31) {
                              var8.addSuppressed(var31);
                           }
                        } else {
                           input2.close();
                        }
                     }

                  }
               } catch (Throwable var34) {
                  var6 = var34;
                  throw var34;
               } finally {
                  if (input1 != null) {
                     if (var6 != null) {
                        try {
                           input1.close();
                        } catch (Throwable var30) {
                           var6.addSuppressed(var30);
                        }
                     } else {
                        input1.close();
                     }
                  }

               }

               return (boolean)var9;
            }
         }
      } else {
         return false;
      }
   }

   public static File[] convertFileCollectionToFileArray(Collection files) {
      return (File[])files.toArray(EMPTY_FILE_ARRAY);
   }

   public static void copyDirectory(File srcDir, File destDir) throws IOException {
      copyDirectory(srcDir, destDir, true);
   }

   public static void copyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws IOException {
      copyDirectory(srcDir, destDir, (FileFilter)null, preserveFileDate);
   }

   public static void copyDirectory(File srcDir, File destDir, FileFilter filter) throws IOException {
      copyDirectory(srcDir, destDir, filter, true);
   }

   public static void copyDirectory(File srcDir, File destDir, FileFilter filter, boolean preserveFileDate) throws IOException {
      copyDirectory(srcDir, destDir, filter, preserveFileDate, StandardCopyOption.REPLACE_EXISTING);
   }

   public static void copyDirectory(File srcDir, File destDir, FileFilter fileFilter, boolean preserveFileDate, CopyOption... copyOptions) throws IOException {
      requireFileCopy(srcDir, destDir);
      requireDirectory(srcDir, "srcDir");
      requireCanonicalPathsNotEquals(srcDir, destDir);
      List exclusionList = null;
      String srcDirCanonicalPath = srcDir.getCanonicalPath();
      String destDirCanonicalPath = destDir.getCanonicalPath();
      if (destDirCanonicalPath.startsWith(srcDirCanonicalPath)) {
         File[] srcFiles = listFiles(srcDir, fileFilter);
         if (srcFiles.length > 0) {
            exclusionList = new ArrayList(srcFiles.length);
            File[] var9 = srcFiles;
            int var10 = srcFiles.length;

            for(int var11 = 0; var11 < var10; ++var11) {
               File srcFile = var9[var11];
               File copiedFile = new File(destDir, srcFile.getName());
               exclusionList.add(copiedFile.getCanonicalPath());
            }
         }
      }

      doCopyDirectory(srcDir, destDir, fileFilter, exclusionList, preserveFileDate, preserveFileDate ? addCopyAttributes(copyOptions) : copyOptions);
   }

   public static void copyDirectoryToDirectory(File sourceDir, File destinationDir) throws IOException {
      requireDirectoryIfExists(sourceDir, "sourceDir");
      requireDirectoryIfExists(destinationDir, "destinationDir");
      copyDirectory(sourceDir, new File(destinationDir, sourceDir.getName()), true);
   }

   public static void copyFile(File srcFile, File destFile) throws IOException {
      copyFile(srcFile, destFile, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
   }

   public static void copyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
      copyFile(srcFile, destFile, preserveFileDate ? new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING} : new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
   }

   public static void copyFile(File srcFile, File destFile, boolean preserveFileDate, CopyOption... copyOptions) throws IOException {
      copyFile(srcFile, destFile, preserveFileDate ? addCopyAttributes(copyOptions) : copyOptions);
   }

   public static void copyFile(File srcFile, File destFile, CopyOption... copyOptions) throws IOException {
      requireFileCopy(srcFile, destFile);
      requireFile(srcFile, "srcFile");
      requireCanonicalPathsNotEquals(srcFile, destFile);
      createParentDirectories(destFile);
      requireFileIfExists(destFile, "destFile");
      if (destFile.exists()) {
         requireCanWrite(destFile, "destFile");
      }

      Files.copy(srcFile.toPath(), destFile.toPath(), copyOptions);
      requireEqualSizes(srcFile, destFile, srcFile.length(), destFile.length());
   }

   public static long copyFile(File input, OutputStream output) throws IOException {
      InputStream fis = Files.newInputStream(input.toPath());
      Throwable var3 = null;

      long var4;
      try {
         var4 = IOUtils.copyLarge(fis, output);
      } catch (Throwable var14) {
         var3 = var14;
         throw var14;
      } finally {
         if (fis != null) {
            if (var3 != null) {
               try {
                  fis.close();
               } catch (Throwable var13) {
                  var3.addSuppressed(var13);
               }
            } else {
               fis.close();
            }
         }

      }

      return var4;
   }

   public static void copyFileToDirectory(File srcFile, File destDir) throws IOException {
      copyFileToDirectory(srcFile, destDir, true);
   }

   public static void copyFileToDirectory(File sourceFile, File destinationDir, boolean preserveFileDate) throws IOException {
      Objects.requireNonNull(sourceFile, "sourceFile");
      requireDirectoryIfExists(destinationDir, "destinationDir");
      copyFile(sourceFile, new File(destinationDir, sourceFile.getName()), preserveFileDate);
   }

   public static void copyInputStreamToFile(InputStream source, File destination) throws IOException {
      InputStream inputStream = source;
      Throwable var3 = null;

      try {
         copyToFile(inputStream, destination);
      } catch (Throwable var12) {
         var3 = var12;
         throw var12;
      } finally {
         if (source != null) {
            if (var3 != null) {
               try {
                  inputStream.close();
               } catch (Throwable var11) {
                  var3.addSuppressed(var11);
               }
            } else {
               source.close();
            }
         }

      }

   }

   public static void copyToDirectory(File sourceFile, File destinationDir) throws IOException {
      Objects.requireNonNull(sourceFile, "sourceFile");
      if (sourceFile.isFile()) {
         copyFileToDirectory(sourceFile, destinationDir);
      } else {
         if (!sourceFile.isDirectory()) {
            throw new FileNotFoundException("The source " + sourceFile + " does not exist");
         }

         copyDirectoryToDirectory(sourceFile, destinationDir);
      }

   }

   public static void copyToDirectory(Iterable sourceIterable, File destinationDir) throws IOException {
      Objects.requireNonNull(sourceIterable, "sourceIterable");
      Iterator var2 = sourceIterable.iterator();

      while(var2.hasNext()) {
         File src = (File)var2.next();
         copyFileToDirectory(src, destinationDir);
      }

   }

   public static void copyToFile(InputStream inputStream, File file) throws IOException {
      OutputStream out = openOutputStream(file);
      Throwable var3 = null;

      try {
         IOUtils.copy((InputStream)inputStream, (OutputStream)out);
      } catch (Throwable var12) {
         var3 = var12;
         throw var12;
      } finally {
         if (out != null) {
            if (var3 != null) {
               try {
                  out.close();
               } catch (Throwable var11) {
                  var3.addSuppressed(var11);
               }
            } else {
               out.close();
            }
         }

      }

   }

   public static void copyURLToFile(URL source, File destination) throws IOException {
      InputStream stream = source.openStream();
      Throwable var3 = null;

      try {
         copyInputStreamToFile(stream, destination);
      } catch (Throwable var12) {
         var3 = var12;
         throw var12;
      } finally {
         if (stream != null) {
            if (var3 != null) {
               try {
                  stream.close();
               } catch (Throwable var11) {
                  var3.addSuppressed(var11);
               }
            } else {
               stream.close();
            }
         }

      }

   }

   public static void copyURLToFile(URL source, File destination, int connectionTimeoutMillis, int readTimeoutMillis) throws IOException {
      URLConnection connection = source.openConnection();
      connection.setConnectTimeout(connectionTimeoutMillis);
      connection.setReadTimeout(readTimeoutMillis);
      InputStream stream = connection.getInputStream();
      Throwable var6 = null;

      try {
         copyInputStreamToFile(stream, destination);
      } catch (Throwable var15) {
         var6 = var15;
         throw var15;
      } finally {
         if (stream != null) {
            if (var6 != null) {
               try {
                  stream.close();
               } catch (Throwable var14) {
                  var6.addSuppressed(var14);
               }
            } else {
               stream.close();
            }
         }

      }

   }

   public static File createParentDirectories(File file) throws IOException {
      return mkdirs(getParentFile(file));
   }

   static String decodeUrl(String url) {
      String decoded = url;
      if (url != null && url.indexOf(37) >= 0) {
         int n = url.length();
         StringBuilder buffer = new StringBuilder();
         ByteBuffer bytes = ByteBuffer.allocate(n);
         int i = 0;

         while(true) {
            while(true) {
               if (i >= n) {
                  decoded = buffer.toString();
                  return decoded;
               }

               if (url.charAt(i) != '%') {
                  break;
               }

               try {
                  while(true) {
                     byte octet = (byte)Integer.parseInt(url.substring(i + 1, i + 3), 16);
                     bytes.put(octet);
                     i += 3;
                     if (i >= n || url.charAt(i) != '%') {
                        break;
                     }
                  }
               } catch (RuntimeException var10) {
                  break;
               } finally {
                  if (bytes.position() > 0) {
                     bytes.flip();
                     buffer.append(StandardCharsets.UTF_8.decode(bytes).toString());
                     bytes.clear();
                  }

               }
            }

            buffer.append(url.charAt(i++));
         }
      } else {
         return decoded;
      }
   }

   public static File delete(File file) throws IOException {
      Objects.requireNonNull(file, "file");
      Files.delete(file.toPath());
      return file;
   }

   public static void deleteDirectory(File directory) throws IOException {
      Objects.requireNonNull(directory, "directory");
      if (directory.exists()) {
         if (!isSymlink(directory)) {
            cleanDirectory(directory);
         }

         delete(directory);
      }
   }

   private static void deleteDirectoryOnExit(File directory) throws IOException {
      if (directory.exists()) {
         directory.deleteOnExit();
         if (!isSymlink(directory)) {
            cleanDirectoryOnExit(directory);
         }

      }
   }

   public static boolean deleteQuietly(File file) {
      if (file == null) {
         return false;
      } else {
         try {
            if (file.isDirectory()) {
               cleanDirectory(file);
            }
         } catch (Exception var3) {
         }

         try {
            return file.delete();
         } catch (Exception var2) {
            return false;
         }
      }
   }

   public static boolean directoryContains(File directory, File child) throws IOException {
      requireDirectoryExists(directory, "directory");
      if (child == null) {
         return false;
      } else {
         return directory.exists() && child.exists() ? FilenameUtils.directoryContains(directory.getCanonicalPath(), child.getCanonicalPath()) : false;
      }
   }

   private static void doCopyDirectory(File srcDir, File destDir, FileFilter fileFilter, List exclusionList, boolean preserveDirDate, CopyOption... copyOptions) throws IOException {
      File[] srcFiles = listFiles(srcDir, fileFilter);
      requireDirectoryIfExists(destDir, "destDir");
      mkdirs(destDir);
      requireCanWrite(destDir, "destDir");
      File[] var7 = srcFiles;
      int var8 = srcFiles.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         File srcFile = var7[var9];
         File dstFile = new File(destDir, srcFile.getName());
         if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
            if (srcFile.isDirectory()) {
               doCopyDirectory(srcFile, dstFile, fileFilter, exclusionList, preserveDirDate, copyOptions);
            } else {
               copyFile(srcFile, dstFile, copyOptions);
            }
         }
      }

      if (preserveDirDate) {
         setLastModified(srcDir, destDir);
      }

   }

   public static void forceDelete(File file) throws IOException {
      Objects.requireNonNull(file, "file");

      Counters.PathCounters deleteCounters;
      try {
         deleteCounters = PathUtils.delete(file.toPath(), PathUtils.EMPTY_LINK_OPTION_ARRAY, StandardDeleteOption.OVERRIDE_READ_ONLY);
      } catch (IOException var3) {
         throw new IOException("Cannot delete file: " + file, var3);
      }

      if (deleteCounters.getFileCounter().get() < 1L && deleteCounters.getDirectoryCounter().get() < 1L) {
         throw new FileNotFoundException("File does not exist: " + file);
      }
   }

   public static void forceDeleteOnExit(File file) throws IOException {
      Objects.requireNonNull(file, "file");
      if (file.isDirectory()) {
         deleteDirectoryOnExit(file);
      } else {
         file.deleteOnExit();
      }

   }

   public static void forceMkdir(File directory) throws IOException {
      mkdirs(directory);
   }

   public static void forceMkdirParent(File file) throws IOException {
      Objects.requireNonNull(file, "file");
      File parent = getParentFile(file);
      if (parent != null) {
         forceMkdir(parent);
      }
   }

   public static File getFile(File directory, String... names) {
      Objects.requireNonNull(directory, "directory");
      Objects.requireNonNull(names, "names");
      File file = directory;
      String[] var3 = names;
      int var4 = names.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String name = var3[var5];
         file = new File(file, name);
      }

      return file;
   }

   public static File getFile(String... names) {
      Objects.requireNonNull(names, "names");
      File file = null;
      String[] var2 = names;
      int var3 = names.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String name = var2[var4];
         if (file == null) {
            file = new File(name);
         } else {
            file = new File(file, name);
         }
      }

      return file;
   }

   private static File getParentFile(File file) {
      return file == null ? null : file.getParentFile();
   }

   public static File getTempDirectory() {
      return new File(getTempDirectoryPath());
   }

   public static String getTempDirectoryPath() {
      return System.getProperty("java.io.tmpdir");
   }

   public static File getUserDirectory() {
      return new File(getUserDirectoryPath());
   }

   public static String getUserDirectoryPath() {
      return System.getProperty("user.home");
   }

   public static boolean isDirectory(File file, LinkOption... options) {
      return file != null && Files.isDirectory(file.toPath(), options);
   }

   public static boolean isEmptyDirectory(File directory) throws IOException {
      return PathUtils.isEmptyDirectory(directory.toPath());
   }

   public static boolean isFileNewer(File file, ChronoLocalDate chronoLocalDate) {
      return isFileNewer(file, chronoLocalDate, LocalTime.now());
   }

   public static boolean isFileNewer(File file, ChronoLocalDate chronoLocalDate, LocalTime localTime) {
      Objects.requireNonNull(chronoLocalDate, "chronoLocalDate");
      Objects.requireNonNull(localTime, "localTime");
      return isFileNewer(file, chronoLocalDate.atTime(localTime));
   }

   public static boolean isFileNewer(File file, ChronoLocalDateTime chronoLocalDateTime) {
      return isFileNewer(file, chronoLocalDateTime, ZoneId.systemDefault());
   }

   public static boolean isFileNewer(File file, ChronoLocalDateTime chronoLocalDateTime, ZoneId zoneId) {
      Objects.requireNonNull(chronoLocalDateTime, "chronoLocalDateTime");
      Objects.requireNonNull(zoneId, "zoneId");
      return isFileNewer(file, chronoLocalDateTime.atZone(zoneId));
   }

   public static boolean isFileNewer(File file, ChronoZonedDateTime chronoZonedDateTime) {
      Objects.requireNonNull(chronoZonedDateTime, "chronoZonedDateTime");
      return isFileNewer(file, chronoZonedDateTime.toInstant());
   }

   public static boolean isFileNewer(File file, Date date) {
      Objects.requireNonNull(date, "date");
      return isFileNewer(file, date.getTime());
   }

   public static boolean isFileNewer(File file, File reference) {
      requireExists(reference, "reference");
      return isFileNewer(file, lastModifiedUnchecked(reference));
   }

   public static boolean isFileNewer(File file, Instant instant) {
      Objects.requireNonNull(instant, "instant");
      return isFileNewer(file, instant.toEpochMilli());
   }

   public static boolean isFileNewer(File file, long timeMillis) {
      Objects.requireNonNull(file, "file");
      return file.exists() && lastModifiedUnchecked(file) > timeMillis;
   }

   public static boolean isFileOlder(File file, ChronoLocalDate chronoLocalDate) {
      return isFileOlder(file, chronoLocalDate, LocalTime.now());
   }

   public static boolean isFileOlder(File file, ChronoLocalDate chronoLocalDate, LocalTime localTime) {
      Objects.requireNonNull(chronoLocalDate, "chronoLocalDate");
      Objects.requireNonNull(localTime, "localTime");
      return isFileOlder(file, chronoLocalDate.atTime(localTime));
   }

   public static boolean isFileOlder(File file, ChronoLocalDateTime chronoLocalDateTime) {
      return isFileOlder(file, chronoLocalDateTime, ZoneId.systemDefault());
   }

   public static boolean isFileOlder(File file, ChronoLocalDateTime chronoLocalDateTime, ZoneId zoneId) {
      Objects.requireNonNull(chronoLocalDateTime, "chronoLocalDateTime");
      Objects.requireNonNull(zoneId, "zoneId");
      return isFileOlder(file, chronoLocalDateTime.atZone(zoneId));
   }

   public static boolean isFileOlder(File file, ChronoZonedDateTime chronoZonedDateTime) {
      Objects.requireNonNull(chronoZonedDateTime, "chronoZonedDateTime");
      return isFileOlder(file, chronoZonedDateTime.toInstant());
   }

   public static boolean isFileOlder(File file, Date date) {
      Objects.requireNonNull(date, "date");
      return isFileOlder(file, date.getTime());
   }

   public static boolean isFileOlder(File file, File reference) {
      requireExists(reference, "reference");
      return isFileOlder(file, lastModifiedUnchecked(reference));
   }

   public static boolean isFileOlder(File file, Instant instant) {
      Objects.requireNonNull(instant, "instant");
      return isFileOlder(file, instant.toEpochMilli());
   }

   public static boolean isFileOlder(File file, long timeMillis) {
      Objects.requireNonNull(file, "file");
      return file.exists() && lastModifiedUnchecked(file) < timeMillis;
   }

   public static boolean isRegularFile(File file, LinkOption... options) {
      return file != null && Files.isRegularFile(file.toPath(), options);
   }

   public static boolean isSymlink(File file) {
      return file != null && Files.isSymbolicLink(file.toPath());
   }

   public static Iterator iterateFiles(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
      return listFiles(directory, fileFilter, dirFilter).iterator();
   }

   public static Iterator iterateFiles(File directory, String[] extensions, boolean recursive) {
      try {
         return StreamIterator.iterator(streamFiles(directory, recursive, extensions));
      } catch (IOException var4) {
         throw new UncheckedIOException(directory.toString(), var4);
      }
   }

   public static Iterator iterateFilesAndDirs(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
      return listFilesAndDirs(directory, fileFilter, dirFilter).iterator();
   }

   public static long lastModified(File file) throws IOException {
      return Files.getLastModifiedTime((Path)Objects.requireNonNull(file.toPath(), "file")).toMillis();
   }

   public static long lastModifiedUnchecked(File file) {
      try {
         return lastModified(file);
      } catch (IOException var2) {
         throw new UncheckedIOException(file.toString(), var2);
      }
   }

   public static LineIterator lineIterator(File file) throws IOException {
      return lineIterator(file, (String)null);
   }

   public static LineIterator lineIterator(File file, String charsetName) throws IOException {
      InputStream inputStream = null;

      try {
         inputStream = openInputStream(file);
         return IOUtils.lineIterator(inputStream, (String)charsetName);
      } catch (RuntimeException | IOException var4) {
         IOUtils.closeQuietly(inputStream, var4::addSuppressed);
         throw var4;
      }
   }

   private static AccumulatorPathVisitor listAccumulate(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) throws IOException {
      boolean isDirFilterSet = dirFilter != null;
      FileEqualsFileFilter rootDirFilter = new FileEqualsFileFilter(directory);
      PathFilter dirPathFilter = isDirFilterSet ? rootDirFilter.or(dirFilter) : rootDirFilter;
      AccumulatorPathVisitor visitor = new AccumulatorPathVisitor(Counters.noopPathCounters(), fileFilter, (PathFilter)dirPathFilter);
      Files.walkFileTree(directory.toPath(), Collections.emptySet(), toMaxDepth(isDirFilterSet), visitor);
      return visitor;
   }

   private static File[] listFiles(File directory, FileFilter fileFilter) throws IOException {
      requireDirectoryExists(directory, "directory");
      File[] files = fileFilter == null ? directory.listFiles() : directory.listFiles(fileFilter);
      if (files == null) {
         throw new IOException("Unknown I/O error listing contents of directory: " + directory);
      } else {
         return files;
      }
   }

   public static Collection listFiles(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
      try {
         AccumulatorPathVisitor visitor = listAccumulate(directory, fileFilter, dirFilter);
         return (Collection)visitor.getFileList().stream().map(Path::toFile).collect(Collectors.toList());
      } catch (IOException var4) {
         throw new UncheckedIOException(directory.toString(), var4);
      }
   }

   public static Collection listFiles(File directory, String[] extensions, boolean recursive) {
      try {
         return toList(streamFiles(directory, recursive, extensions));
      } catch (IOException var4) {
         throw new UncheckedIOException(directory.toString(), var4);
      }
   }

   public static Collection listFilesAndDirs(File directory, IOFileFilter fileFilter, IOFileFilter dirFilter) {
      try {
         AccumulatorPathVisitor visitor = listAccumulate(directory, fileFilter, dirFilter);
         List list = visitor.getFileList();
         list.addAll(visitor.getDirList());
         return (Collection)list.stream().map(Path::toFile).collect(Collectors.toList());
      } catch (IOException var5) {
         throw new UncheckedIOException(directory.toString(), var5);
      }
   }

   private static File mkdirs(File directory) throws IOException {
      if (directory != null && !directory.mkdirs() && !directory.isDirectory()) {
         throw new IOException("Cannot create directory '" + directory + "'.");
      } else {
         return directory;
      }
   }

   public static void moveDirectory(File srcDir, File destDir) throws IOException {
      validateMoveParameters(srcDir, destDir);
      requireDirectory(srcDir, "srcDir");
      requireAbsent(destDir, "destDir");
      if (!srcDir.renameTo(destDir)) {
         if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath() + File.separator)) {
            throw new IOException("Cannot move directory: " + srcDir + " to a subdirectory of itself: " + destDir);
         }

         copyDirectory(srcDir, destDir);
         deleteDirectory(srcDir);
         if (srcDir.exists()) {
            throw new IOException("Failed to delete original directory '" + srcDir + "' after copy to '" + destDir + "'");
         }
      }

   }

   public static void moveDirectoryToDirectory(File src, File destDir, boolean createDestDir) throws IOException {
      validateMoveParameters(src, destDir);
      if (!destDir.isDirectory()) {
         if (destDir.exists()) {
            throw new IOException("Destination '" + destDir + "' is not a directory");
         }

         if (!createDestDir) {
            throw new FileNotFoundException("Destination directory '" + destDir + "' does not exist [createDestDir=" + false + "]");
         }

         mkdirs(destDir);
      }

      moveDirectory(src, new File(destDir, src.getName()));
   }

   public static void moveFile(File srcFile, File destFile) throws IOException {
      moveFile(srcFile, destFile, StandardCopyOption.COPY_ATTRIBUTES);
   }

   public static void moveFile(File srcFile, File destFile, CopyOption... copyOptions) throws IOException {
      validateMoveParameters(srcFile, destFile);
      requireFile(srcFile, "srcFile");
      requireAbsent(destFile, (String)null);
      boolean rename = srcFile.renameTo(destFile);
      if (!rename) {
         copyFile(srcFile, destFile, copyOptions);
         if (!srcFile.delete()) {
            deleteQuietly(destFile);
            throw new IOException("Failed to delete original file '" + srcFile + "' after copy to '" + destFile + "'");
         }
      }

   }

   public static void moveFileToDirectory(File srcFile, File destDir, boolean createDestDir) throws IOException {
      validateMoveParameters(srcFile, destDir);
      if (!destDir.exists() && createDestDir) {
         mkdirs(destDir);
      }

      requireExistsChecked(destDir, "destDir");
      requireDirectory(destDir, "destDir");
      moveFile(srcFile, new File(destDir, srcFile.getName()));
   }

   public static void moveToDirectory(File src, File destDir, boolean createDestDir) throws IOException {
      validateMoveParameters(src, destDir);
      if (src.isDirectory()) {
         moveDirectoryToDirectory(src, destDir, createDestDir);
      } else {
         moveFileToDirectory(src, destDir, createDestDir);
      }

   }

   public static FileInputStream openInputStream(File file) throws IOException {
      Objects.requireNonNull(file, "file");
      return new FileInputStream(file);
   }

   public static FileOutputStream openOutputStream(File file) throws IOException {
      return openOutputStream(file, false);
   }

   public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
      Objects.requireNonNull(file, "file");
      if (file.exists()) {
         requireFile(file, "file");
         requireCanWrite(file, "file");
      } else {
         createParentDirectories(file);
      }

      return new FileOutputStream(file, append);
   }

   public static byte[] readFileToByteArray(File file) throws IOException {
      InputStream inputStream = openInputStream(file);
      Throwable var2 = null;

      byte[] var5;
      try {
         long fileLength = file.length();
         var5 = fileLength > 0L ? IOUtils.toByteArray(inputStream, fileLength) : IOUtils.toByteArray((InputStream)inputStream);
      } catch (Throwable var14) {
         var2 = var14;
         throw var14;
      } finally {
         if (inputStream != null) {
            if (var2 != null) {
               try {
                  inputStream.close();
               } catch (Throwable var13) {
                  var2.addSuppressed(var13);
               }
            } else {
               inputStream.close();
            }
         }

      }

      return var5;
   }

   /** @deprecated */
   @Deprecated
   public static String readFileToString(File file) throws IOException {
      return readFileToString(file, Charset.defaultCharset());
   }

   public static String readFileToString(File file, Charset charsetName) throws IOException {
      InputStream inputStream = openInputStream(file);
      Throwable var3 = null;

      String var4;
      try {
         var4 = IOUtils.toString((InputStream)inputStream, (Charset)Charsets.toCharset(charsetName));
      } catch (Throwable var13) {
         var3 = var13;
         throw var13;
      } finally {
         if (inputStream != null) {
            if (var3 != null) {
               try {
                  inputStream.close();
               } catch (Throwable var12) {
                  var3.addSuppressed(var12);
               }
            } else {
               inputStream.close();
            }
         }

      }

      return var4;
   }

   public static String readFileToString(File file, String charsetName) throws IOException {
      return readFileToString(file, Charsets.toCharset(charsetName));
   }

   /** @deprecated */
   @Deprecated
   public static List readLines(File file) throws IOException {
      return readLines(file, Charset.defaultCharset());
   }

   public static List readLines(File file, Charset charset) throws IOException {
      InputStream inputStream = openInputStream(file);
      Throwable var3 = null;

      List var4;
      try {
         var4 = IOUtils.readLines(inputStream, (Charset)Charsets.toCharset(charset));
      } catch (Throwable var13) {
         var3 = var13;
         throw var13;
      } finally {
         if (inputStream != null) {
            if (var3 != null) {
               try {
                  inputStream.close();
               } catch (Throwable var12) {
                  var3.addSuppressed(var12);
               }
            } else {
               inputStream.close();
            }
         }

      }

      return var4;
   }

   public static List readLines(File file, String charsetName) throws IOException {
      return readLines(file, Charsets.toCharset(charsetName));
   }

   private static void requireAbsent(File file, String name) throws FileExistsException {
      if (file.exists()) {
         throw new FileExistsException(String.format("File element in parameter '%s' already exists: '%s'", name, file));
      }
   }

   private static void requireCanonicalPathsNotEquals(File file1, File file2) throws IOException {
      String canonicalPath = file1.getCanonicalPath();
      if (canonicalPath.equals(file2.getCanonicalPath())) {
         throw new IllegalArgumentException(String.format("File canonical paths are equal: '%s' (file1='%s', file2='%s')", canonicalPath, file1, file2));
      }
   }

   private static void requireCanWrite(File file, String name) {
      Objects.requireNonNull(file, "file");
      if (!file.canWrite()) {
         throw new IllegalArgumentException("File parameter '" + name + " is not writable: '" + file + "'");
      }
   }

   private static File requireDirectory(File directory, String name) {
      Objects.requireNonNull(directory, name);
      if (!directory.isDirectory()) {
         throw new IllegalArgumentException("Parameter '" + name + "' is not a directory: '" + directory + "'");
      } else {
         return directory;
      }
   }

   private static File requireDirectoryExists(File directory, String name) {
      requireExists(directory, name);
      requireDirectory(directory, name);
      return directory;
   }

   private static File requireDirectoryIfExists(File directory, String name) {
      Objects.requireNonNull(directory, name);
      if (directory.exists()) {
         requireDirectory(directory, name);
      }

      return directory;
   }

   private static void requireEqualSizes(File srcFile, File destFile, long srcLen, long dstLen) throws IOException {
      if (srcLen != dstLen) {
         throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "' Expected length: " + srcLen + " Actual: " + dstLen);
      }
   }

   private static File requireExists(File file, String fileParamName) {
      Objects.requireNonNull(file, fileParamName);
      if (!file.exists()) {
         throw new IllegalArgumentException("File system element for parameter '" + fileParamName + "' does not exist: '" + file + "'");
      } else {
         return file;
      }
   }

   private static File requireExistsChecked(File file, String fileParamName) throws FileNotFoundException {
      Objects.requireNonNull(file, fileParamName);
      if (!file.exists()) {
         throw new FileNotFoundException("File system element for parameter '" + fileParamName + "' does not exist: '" + file + "'");
      } else {
         return file;
      }
   }

   private static File requireFile(File file, String name) {
      Objects.requireNonNull(file, name);
      if (!file.isFile()) {
         throw new IllegalArgumentException("Parameter '" + name + "' is not a file: " + file);
      } else {
         return file;
      }
   }

   private static void requireFileCopy(File source, File destination) throws FileNotFoundException {
      requireExistsChecked(source, "source");
      Objects.requireNonNull(destination, "destination");
   }

   private static File requireFileIfExists(File file, String name) {
      Objects.requireNonNull(file, name);
      return file.exists() ? requireFile(file, name) : file;
   }

   private static void setLastModified(File sourceFile, File targetFile) throws IOException {
      Objects.requireNonNull(sourceFile, "sourceFile");
      setLastModified(targetFile, lastModified(sourceFile));
   }

   private static void setLastModified(File file, long timeMillis) throws IOException {
      Objects.requireNonNull(file, "file");
      if (!file.setLastModified(timeMillis)) {
         throw new IOException(String.format("Failed setLastModified(%s) on '%s'", timeMillis, file));
      }
   }

   public static long sizeOf(File file) {
      requireExists(file, "file");
      return file.isDirectory() ? sizeOfDirectory0(file) : file.length();
   }

   private static long sizeOf0(File file) {
      Objects.requireNonNull(file, "file");
      return file.isDirectory() ? sizeOfDirectory0(file) : file.length();
   }

   public static BigInteger sizeOfAsBigInteger(File file) {
      requireExists(file, "file");
      return file.isDirectory() ? sizeOfDirectoryBig0(file) : BigInteger.valueOf(file.length());
   }

   private static BigInteger sizeOfBig0(File file) {
      Objects.requireNonNull(file, "fileOrDir");
      return file.isDirectory() ? sizeOfDirectoryBig0(file) : BigInteger.valueOf(file.length());
   }

   public static long sizeOfDirectory(File directory) {
      return sizeOfDirectory0(requireDirectoryExists(directory, "directory"));
   }

   private static long sizeOfDirectory0(File directory) {
      Objects.requireNonNull(directory, "directory");
      File[] files = directory.listFiles();
      if (files == null) {
         return 0L;
      } else {
         long size = 0L;
         File[] var4 = files;
         int var5 = files.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            File file = var4[var6];
            if (!isSymlink(file)) {
               size += sizeOf0(file);
               if (size < 0L) {
                  break;
               }
            }
         }

         return size;
      }
   }

   public static BigInteger sizeOfDirectoryAsBigInteger(File directory) {
      return sizeOfDirectoryBig0(requireDirectoryExists(directory, "directory"));
   }

   private static BigInteger sizeOfDirectoryBig0(File directory) {
      Objects.requireNonNull(directory, "directory");
      File[] files = directory.listFiles();
      if (files == null) {
         return BigInteger.ZERO;
      } else {
         BigInteger size = BigInteger.ZERO;
         File[] var3 = files;
         int var4 = files.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            File file = var3[var5];
            if (!isSymlink(file)) {
               size = size.add(sizeOfBig0(file));
            }
         }

         return size;
      }
   }

   public static Stream streamFiles(File directory, boolean recursive, String... extensions) throws IOException {
      IOFileFilter filter = extensions == null ? FileFileFilter.INSTANCE : FileFileFilter.INSTANCE.and(new SuffixFileFilter(toSuffixes(extensions)));
      return PathUtils.walk(directory.toPath(), filter, toMaxDepth(recursive), false, FileVisitOption.FOLLOW_LINKS).map(Path::toFile);
   }

   public static File toFile(URL url) {
      if (url != null && "file".equalsIgnoreCase(url.getProtocol())) {
         String filename = url.getFile().replace('/', File.separatorChar);
         return new File(decodeUrl(filename));
      } else {
         return null;
      }
   }

   public static File[] toFiles(URL... urls) {
      if (IOUtils.length((Object[])urls) == 0) {
         return EMPTY_FILE_ARRAY;
      } else {
         File[] files = new File[urls.length];

         for(int i = 0; i < urls.length; ++i) {
            URL url = urls[i];
            if (url != null) {
               if (!"file".equalsIgnoreCase(url.getProtocol())) {
                  throw new IllegalArgumentException("Can only convert file URL to a File: " + url);
               }

               files[i] = toFile(url);
            }
         }

         return files;
      }
   }

   private static List toList(Stream stream) {
      return (List)stream.collect(Collectors.toList());
   }

   private static int toMaxDepth(boolean recursive) {
      return recursive ? Integer.MAX_VALUE : 1;
   }

   private static String[] toSuffixes(String... extensions) {
      Objects.requireNonNull(extensions, "extensions");
      String[] suffixes = new String[extensions.length];

      for(int i = 0; i < extensions.length; ++i) {
         suffixes[i] = "." + extensions[i];
      }

      return suffixes;
   }

   public static void touch(File file) throws IOException {
      Objects.requireNonNull(file, "file");
      if (!file.exists()) {
         openOutputStream(file).close();
      }

      setLastModified(file, System.currentTimeMillis());
   }

   public static URL[] toURLs(File... files) throws IOException {
      Objects.requireNonNull(files, "files");
      URL[] urls = new URL[files.length];

      for(int i = 0; i < urls.length; ++i) {
         urls[i] = files[i].toURI().toURL();
      }

      return urls;
   }

   private static void validateMoveParameters(File source, File destination) throws FileNotFoundException {
      Objects.requireNonNull(source, "source");
      Objects.requireNonNull(destination, "destination");
      if (!source.exists()) {
         throw new FileNotFoundException("Source '" + source + "' does not exist");
      }
   }

   public static boolean waitFor(File file, int seconds) {
      Objects.requireNonNull(file, "file");
      long finishAtMillis = System.currentTimeMillis() + (long)seconds * 1000L;
      boolean wasInterrupted = false;

      try {
         while(!file.exists()) {
            long remainingMillis = finishAtMillis - System.currentTimeMillis();
            if (remainingMillis < 0L) {
               boolean var7 = false;
               return var7;
            }

            try {
               Thread.sleep(Math.min(100L, remainingMillis));
            } catch (InterruptedException var12) {
               wasInterrupted = true;
            } catch (Exception var13) {
               return true;
            }
         }

         return true;
      } finally {
         if (wasInterrupted) {
            Thread.currentThread().interrupt();
         }

      }
   }

   /** @deprecated */
   @Deprecated
   public static void write(File file, CharSequence data) throws IOException {
      write(file, data, Charset.defaultCharset(), false);
   }

   /** @deprecated */
   @Deprecated
   public static void write(File file, CharSequence data, boolean append) throws IOException {
      write(file, data, Charset.defaultCharset(), append);
   }

   public static void write(File file, CharSequence data, Charset charset) throws IOException {
      write(file, data, charset, false);
   }

   public static void write(File file, CharSequence data, Charset charset, boolean append) throws IOException {
      writeStringToFile(file, Objects.toString(data, (String)null), charset, append);
   }

   public static void write(File file, CharSequence data, String charsetName) throws IOException {
      write(file, data, charsetName, false);
   }

   public static void write(File file, CharSequence data, String charsetName, boolean append) throws IOException {
      write(file, data, Charsets.toCharset(charsetName), append);
   }

   public static void writeByteArrayToFile(File file, byte[] data) throws IOException {
      writeByteArrayToFile(file, data, false);
   }

   public static void writeByteArrayToFile(File file, byte[] data, boolean append) throws IOException {
      writeByteArrayToFile(file, data, 0, data.length, append);
   }

   public static void writeByteArrayToFile(File file, byte[] data, int off, int len) throws IOException {
      writeByteArrayToFile(file, data, off, len, false);
   }

   public static void writeByteArrayToFile(File file, byte[] data, int off, int len, boolean append) throws IOException {
      OutputStream out = openOutputStream(file, append);
      Throwable var6 = null;

      try {
         out.write(data, off, len);
      } catch (Throwable var15) {
         var6 = var15;
         throw var15;
      } finally {
         if (out != null) {
            if (var6 != null) {
               try {
                  out.close();
               } catch (Throwable var14) {
                  var6.addSuppressed(var14);
               }
            } else {
               out.close();
            }
         }

      }

   }

   public static void writeLines(File file, Collection lines) throws IOException {
      writeLines(file, (String)null, lines, (String)null, false);
   }

   public static void writeLines(File file, Collection lines, boolean append) throws IOException {
      writeLines(file, (String)null, lines, (String)null, append);
   }

   public static void writeLines(File file, Collection lines, String lineEnding) throws IOException {
      writeLines(file, (String)null, lines, lineEnding, false);
   }

   public static void writeLines(File file, Collection lines, String lineEnding, boolean append) throws IOException {
      writeLines(file, (String)null, lines, lineEnding, append);
   }

   public static void writeLines(File file, String charsetName, Collection lines) throws IOException {
      writeLines(file, charsetName, lines, (String)null, false);
   }

   public static void writeLines(File file, String charsetName, Collection lines, boolean append) throws IOException {
      writeLines(file, charsetName, lines, (String)null, append);
   }

   public static void writeLines(File file, String charsetName, Collection lines, String lineEnding) throws IOException {
      writeLines(file, charsetName, lines, lineEnding, false);
   }

   public static void writeLines(File file, String charsetName, Collection lines, String lineEnding, boolean append) throws IOException {
      OutputStream out = new BufferedOutputStream(openOutputStream(file, append));
      Throwable var6 = null;

      try {
         IOUtils.writeLines(lines, lineEnding, out, (String)charsetName);
      } catch (Throwable var15) {
         var6 = var15;
         throw var15;
      } finally {
         if (out != null) {
            if (var6 != null) {
               try {
                  out.close();
               } catch (Throwable var14) {
                  var6.addSuppressed(var14);
               }
            } else {
               out.close();
            }
         }

      }

   }

   /** @deprecated */
   @Deprecated
   public static void writeStringToFile(File file, String data) throws IOException {
      writeStringToFile(file, data, Charset.defaultCharset(), false);
   }

   /** @deprecated */
   @Deprecated
   public static void writeStringToFile(File file, String data, boolean append) throws IOException {
      writeStringToFile(file, data, Charset.defaultCharset(), append);
   }

   public static void writeStringToFile(File file, String data, Charset charset) throws IOException {
      writeStringToFile(file, data, charset, false);
   }

   public static void writeStringToFile(File file, String data, Charset charset, boolean append) throws IOException {
      OutputStream out = openOutputStream(file, append);
      Throwable var5 = null;

      try {
         IOUtils.write((String)data, (OutputStream)out, (Charset)charset);
      } catch (Throwable var14) {
         var5 = var14;
         throw var14;
      } finally {
         if (out != null) {
            if (var5 != null) {
               try {
                  out.close();
               } catch (Throwable var13) {
                  var5.addSuppressed(var13);
               }
            } else {
               out.close();
            }
         }

      }

   }

   public static void writeStringToFile(File file, String data, String charsetName) throws IOException {
      writeStringToFile(file, data, charsetName, false);
   }

   public static void writeStringToFile(File file, String data, String charsetName, boolean append) throws IOException {
      writeStringToFile(file, data, Charsets.toCharset(charsetName), append);
   }

   static {
      ONE_MB_BI = ONE_KB_BI.multiply(ONE_KB_BI);
      ONE_GB_BI = ONE_KB_BI.multiply(ONE_MB_BI);
      ONE_TB_BI = ONE_KB_BI.multiply(ONE_GB_BI);
      ONE_PB_BI = ONE_KB_BI.multiply(ONE_TB_BI);
      ONE_EB_BI = ONE_KB_BI.multiply(ONE_PB_BI);
      ONE_ZB = BigInteger.valueOf(1024L).multiply(BigInteger.valueOf(1152921504606846976L));
      ONE_YB = ONE_KB_BI.multiply(ONE_ZB);
      EMPTY_FILE_ARRAY = new File[0];
   }
}
