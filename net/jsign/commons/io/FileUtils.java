package net.jsign.commons.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Objects;

public class FileUtils {
   public static final BigInteger ONE_KB_BI = BigInteger.valueOf(1024L);
   public static final BigInteger ONE_MB_BI;
   public static final BigInteger ONE_GB_BI;
   public static final BigInteger ONE_TB_BI;
   public static final BigInteger ONE_PB_BI;
   public static final BigInteger ONE_EB_BI;
   public static final BigInteger ONE_ZB;
   public static final BigInteger ONE_YB;
   public static final File[] EMPTY_FILE_ARRAY;

   public static File createParentDirectories(File file) throws IOException {
      return mkdirs(getParentFile(file));
   }

   private static File getParentFile(File file) {
      return file == null ? null : file.getParentFile();
   }

   private static File mkdirs(File directory) throws IOException {
      if (directory != null && !directory.mkdirs() && !directory.isDirectory()) {
         throw new IOException("Cannot create directory '" + directory + "'.");
      } else {
         return directory;
      }
   }

   public static FileInputStream openInputStream(File file) throws IOException {
      Objects.requireNonNull(file, "file");
      return new FileInputStream(file);
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
         var5 = fileLength > 0L ? IOUtils.toByteArray(inputStream, fileLength) : IOUtils.toByteArray(inputStream);
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

   private static void requireCanWrite(File file, String name) {
      Objects.requireNonNull(file, "file");
      if (!file.canWrite()) {
         throw new IllegalArgumentException("File parameter '" + name + " is not writable: '" + file + "'");
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

   public static void writeByteArrayToFile(File file, byte[] data) throws IOException {
      writeByteArrayToFile(file, data, false);
   }

   public static void writeByteArrayToFile(File file, byte[] data, boolean append) throws IOException {
      writeByteArrayToFile(file, data, 0, data.length, append);
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
