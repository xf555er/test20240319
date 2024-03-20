package net.jsign.commons.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Objects;
import net.jsign.commons.io.function.IOConsumer;
import net.jsign.commons.io.output.StringBuilderWriter;
import net.jsign.commons.io.output.ThresholdingOutputStream;
import net.jsign.commons.io.output.UnsynchronizedByteArrayOutputStream;

public class IOUtils {
   public static final char DIR_SEPARATOR;
   public static final byte[] EMPTY_BYTE_ARRAY;
   /** @deprecated */
   @Deprecated
   public static final String LINE_SEPARATOR;
   public static final String LINE_SEPARATOR_UNIX;
   public static final String LINE_SEPARATOR_WINDOWS;
   private static final ThreadLocal SKIP_BYTE_BUFFER;
   private static final ThreadLocal SKIP_CHAR_BUFFER;

   public static byte[] byteArray() {
      return byteArray(8192);
   }

   public static byte[] byteArray(int size) {
      return new byte[size];
   }

   private static char[] charArray() {
      return charArray(8192);
   }

   private static char[] charArray(int size) {
      return new char[size];
   }

   public static void close(Closeable closeable, IOConsumer consumer) throws IOException {
      if (closeable != null) {
         try {
            closeable.close();
         } catch (IOException var3) {
            if (consumer != null) {
               consumer.accept(var3);
            }
         }
      }

   }

   public static int copy(InputStream inputStream, OutputStream outputStream) throws IOException {
      long count = copyLarge(inputStream, outputStream);
      return count > 2147483647L ? -1 : (int)count;
   }

   public static long copy(InputStream inputStream, OutputStream outputStream, int bufferSize) throws IOException {
      return copyLarge(inputStream, outputStream, byteArray(bufferSize));
   }

   public static void copy(InputStream input, Writer writer, Charset inputCharset) throws IOException {
      InputStreamReader reader = new InputStreamReader(input, Charsets.toCharset(inputCharset));
      copy((Reader)reader, (Writer)writer);
   }

   public static int copy(Reader reader, Writer writer) throws IOException {
      long count = copyLarge(reader, writer);
      return count > 2147483647L ? -1 : (int)count;
   }

   public static long copyLarge(InputStream inputStream, OutputStream outputStream) throws IOException {
      return copy(inputStream, outputStream, 8192);
   }

   public static long copyLarge(InputStream inputStream, OutputStream outputStream, byte[] buffer) throws IOException {
      Objects.requireNonNull(inputStream, "inputStream");
      Objects.requireNonNull(outputStream, "outputStream");

      long count;
      int n;
      for(count = 0L; -1 != (n = inputStream.read(buffer)); count += (long)n) {
         outputStream.write(buffer, 0, n);
      }

      return count;
   }

   public static long copyLarge(Reader reader, Writer writer) throws IOException {
      return copyLarge(reader, writer, getCharArray());
   }

   public static long copyLarge(Reader reader, Writer writer, char[] buffer) throws IOException {
      long count;
      int n;
      for(count = 0L; -1 != (n = reader.read(buffer)); count += (long)n) {
         writer.write(buffer, 0, n);
      }

      return count;
   }

   static char[] getCharArray() {
      return (char[])SKIP_CHAR_BUFFER.get();
   }

   public static int length(byte[] array) {
      return array == null ? 0 : array.length;
   }

   public static int length(Object[] array) {
      return array == null ? 0 : array.length;
   }

   public static byte[] toByteArray(InputStream inputStream) throws IOException {
      UnsynchronizedByteArrayOutputStream ubaOutput = new UnsynchronizedByteArrayOutputStream();
      Throwable var2 = null;

      Object var5;
      try {
         ThresholdingOutputStream thresholdOuput = new ThresholdingOutputStream(Integer.MAX_VALUE, (os) -> {
            throw new IllegalArgumentException(String.format("Cannot read more than %,d into a byte array", Integer.MAX_VALUE));
         }, (os) -> {
            return ubaOutput;
         });
         Throwable var4 = null;

         try {
            copy((InputStream)inputStream, (OutputStream)thresholdOuput);
            var5 = ubaOutput.toByteArray();
         } catch (Throwable var28) {
            var5 = var28;
            var4 = var28;
            throw var28;
         } finally {
            if (thresholdOuput != null) {
               if (var4 != null) {
                  try {
                     thresholdOuput.close();
                  } catch (Throwable var27) {
                     var4.addSuppressed(var27);
                  }
               } else {
                  thresholdOuput.close();
               }
            }

         }
      } catch (Throwable var30) {
         var2 = var30;
         throw var30;
      } finally {
         if (ubaOutput != null) {
            if (var2 != null) {
               try {
                  ubaOutput.close();
               } catch (Throwable var26) {
                  var2.addSuppressed(var26);
               }
            } else {
               ubaOutput.close();
            }
         }

      }

      return (byte[])var5;
   }

   public static byte[] toByteArray(InputStream input, int size) throws IOException {
      if (size < 0) {
         throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);
      } else if (size == 0) {
         return EMPTY_BYTE_ARRAY;
      } else {
         byte[] data = byteArray(size);

         int offset;
         int read;
         for(offset = 0; offset < size && (read = input.read(data, offset, size - offset)) != -1; offset += read) {
         }

         if (offset != size) {
            throw new IOException("Unexpected read size, current: " + offset + ", expected: " + size);
         } else {
            return data;
         }
      }
   }

   public static byte[] toByteArray(InputStream input, long size) throws IOException {
      if (size > 2147483647L) {
         throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size);
      } else {
         return toByteArray(input, (int)size);
      }
   }

   public static String toString(InputStream input, Charset charset) throws IOException {
      StringBuilderWriter sw = new StringBuilderWriter();
      Throwable var3 = null;

      String var4;
      try {
         copy(input, sw, charset);
         var4 = sw.toString();
      } catch (Throwable var13) {
         var3 = var13;
         throw var13;
      } finally {
         if (sw != null) {
            if (var3 != null) {
               try {
                  sw.close();
               } catch (Throwable var12) {
                  var3.addSuppressed(var12);
               }
            } else {
               sw.close();
            }
         }

      }

      return var4;
   }

   static {
      DIR_SEPARATOR = File.separatorChar;
      EMPTY_BYTE_ARRAY = new byte[0];
      LINE_SEPARATOR = System.lineSeparator();
      LINE_SEPARATOR_UNIX = StandardLineSeparator.LF.getString();
      LINE_SEPARATOR_WINDOWS = StandardLineSeparator.CRLF.getString();
      SKIP_BYTE_BUFFER = ThreadLocal.withInitial(IOUtils::byteArray);
      SKIP_CHAR_BUFFER = ThreadLocal.withInitial(IOUtils::charArray);
   }
}
