package net.jsign.poi.util;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Locale;

public final class IOUtils {
   private static final POILogger logger = POILogFactory.getLogger(IOUtils.class);
   private static byte[] SKIP_BYTE_BUFFER;
   private static int BYTE_ARRAY_MAX_OVERRIDE = -1;
   // $FF: synthetic field
   static final boolean $assertionsDisabled = !IOUtils.class.desiredAssertionStatus();

   private IOUtils() {
   }

   private static void checkByteSizeLimit(int length) {
      if (BYTE_ARRAY_MAX_OVERRIDE != -1 && length > BYTE_ARRAY_MAX_OVERRIDE) {
         throwRFE((long)length, BYTE_ARRAY_MAX_OVERRIDE);
      }

   }

   public static byte[] toByteArray(InputStream stream) throws IOException {
      return toByteArray(stream, Integer.MAX_VALUE);
   }

   public static byte[] toByteArray(InputStream stream, int length) throws IOException {
      return toByteArray(stream, length, Integer.MAX_VALUE);
   }

   public static byte[] toByteArray(InputStream stream, int length, int maxLength) throws IOException {
      if ((long)length >= 0L && (long)maxLength >= 0L) {
         if (length != Integer.MAX_VALUE || maxLength != Integer.MAX_VALUE) {
            checkLength((long)length, maxLength);
         }

         int len = Math.min(length, maxLength);
         ByteArrayOutputStream baos = new ByteArrayOutputStream(len == Integer.MAX_VALUE ? 4096 : len);
         byte[] buffer = new byte[4096];
         int totalBytes = 0;

         int readBytes;
         do {
            readBytes = stream.read(buffer, 0, Math.min(buffer.length, len - totalBytes));
            totalBytes += Math.max(readBytes, 0);
            if (readBytes > 0) {
               baos.write(buffer, 0, readBytes);
            }

            checkByteSizeLimit(totalBytes);
         } while(totalBytes < len && readBytes > -1);

         if (maxLength != Integer.MAX_VALUE && totalBytes == maxLength) {
            throw new IOException("MaxLength (" + maxLength + ") reached - stream seems to be invalid.");
         } else if (len != Integer.MAX_VALUE && totalBytes < len) {
            throw new EOFException("unexpected EOF - expected len: " + len + " - actual len: " + totalBytes);
         } else {
            return baos.toByteArray();
         }
      } else {
         throw new RecordFormatException("Can't allocate an array of length < 0");
      }
   }

   private static void checkLength(long length, int maxLength) {
      if (BYTE_ARRAY_MAX_OVERRIDE > 0) {
         if (length > (long)BYTE_ARRAY_MAX_OVERRIDE) {
            throwRFE(length, BYTE_ARRAY_MAX_OVERRIDE);
         }
      } else if (length > (long)maxLength) {
         throwRFE(length, maxLength);
      }

   }

   public static byte[] toByteArray(ByteBuffer buffer, int length) {
      if (buffer.hasArray() && buffer.arrayOffset() == 0) {
         return buffer.array();
      } else {
         checkByteSizeLimit(length);
         byte[] data = new byte[length];
         buffer.get(data);
         return data;
      }
   }

   public static int readFully(ReadableByteChannel channel, ByteBuffer b) throws IOException {
      int total = 0;

      do {
         int got = channel.read(b);
         if (got < 0) {
            return total == 0 ? -1 : total;
         }

         total += got;
      } while(total != b.capacity() && b.position() != b.capacity());

      return total;
   }

   public static long copy(InputStream inp, OutputStream out) throws IOException {
      return copy(inp, out, -1L);
   }

   public static long copy(InputStream inp, OutputStream out, long limit) throws IOException {
      byte[] buff = new byte[4096];
      long totalCount = 0L;
      int readBytes = -1;

      do {
         int todoBytes = (int)(limit < 0L ? (long)buff.length : Math.min(limit - totalCount, (long)buff.length));
         if (todoBytes > 0) {
            readBytes = inp.read(buff, 0, todoBytes);
            if (readBytes > 0) {
               out.write(buff, 0, readBytes);
               totalCount += (long)readBytes;
            }
         }
      } while(readBytes >= 0 && (limit == -1L || totalCount < limit));

      return totalCount;
   }

   public static long skipFully(InputStream input, long toSkip) throws IOException {
      if (toSkip < 0L) {
         throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
      } else if (toSkip == 0L) {
         return 0L;
      } else {
         if (SKIP_BYTE_BUFFER == null) {
            SKIP_BYTE_BUFFER = new byte[2048];
         }

         long remain;
         long n;
         for(remain = toSkip; remain > 0L; remain -= n) {
            n = (long)input.read(SKIP_BYTE_BUFFER, 0, (int)Math.min(remain, 2048L));
            if (n < 0L) {
               break;
            }
         }

         return toSkip == remain ? -1L : toSkip - remain;
      }
   }

   public static byte[] safelyAllocate(long length, int maxLength) {
      safelyAllocateCheck(length, maxLength);
      checkByteSizeLimit((int)length);
      return new byte[(int)length];
   }

   public static void safelyAllocateCheck(long length, int maxLength) {
      if (length < 0L) {
         throw new RecordFormatException("Can't allocate an array of length < 0, but had " + length + " and " + maxLength);
      } else if (length > 2147483647L) {
         throw new RecordFormatException("Can't allocate an array > 2147483647");
      } else {
         checkLength(length, maxLength);
      }
   }

   private static void throwRFE(long length, int maxLength) {
      throw new RecordFormatException(String.format(Locale.ROOT, "Tried to allocate an array of length %,d, but the maximum lenght for this record type is %,d.\nIf the file is not corrupt, please open an issue on bugzilla to request \nincreasing the maximum allowable size for this record type.\nAs a temporary workaround, consider setting a higher override value with IOUtils.setByteArrayMaxOverride()", length, maxLength));
   }
}
