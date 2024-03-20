package org.apache.commons.io.input;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public final class BufferedFileChannelInputStream extends InputStream {
   private final ByteBuffer byteBuffer;
   private final FileChannel fileChannel;
   private static final Class DIRECT_BUFFER_CLASS = getDirectBufferClass();

   private static Class getDirectBufferClass() {
      Class res = null;

      try {
         res = Class.forName("sun.nio.ch.DirectBuffer");
      } catch (ClassNotFoundException | IllegalAccessError var2) {
      }

      return res;
   }

   private static boolean isDirectBuffer(Object object) {
      return DIRECT_BUFFER_CLASS != null && DIRECT_BUFFER_CLASS.isInstance(object);
   }

   public BufferedFileChannelInputStream(File file) throws IOException {
      this((File)file, 8192);
   }

   public BufferedFileChannelInputStream(File file, int bufferSizeInBytes) throws IOException {
      this(file.toPath(), bufferSizeInBytes);
   }

   public BufferedFileChannelInputStream(Path path) throws IOException {
      this((Path)path, 8192);
   }

   public BufferedFileChannelInputStream(Path path, int bufferSizeInBytes) throws IOException {
      Objects.requireNonNull(path, "path");
      this.fileChannel = FileChannel.open(path, StandardOpenOption.READ);
      this.byteBuffer = ByteBuffer.allocateDirect(bufferSizeInBytes);
      this.byteBuffer.flip();
   }

   public synchronized int available() throws IOException {
      return this.byteBuffer.remaining();
   }

   private void clean(ByteBuffer buffer) {
      if (isDirectBuffer(buffer)) {
         this.cleanDirectBuffer(buffer);
      }

   }

   private void cleanDirectBuffer(ByteBuffer buffer) {
      String specVer = System.getProperty("java.specification.version");
      Class clsCleaner;
      Method cleanerMethod;
      if ("1.8".equals(specVer)) {
         try {
            clsCleaner = Class.forName("sun.misc.Cleaner");
            cleanerMethod = DIRECT_BUFFER_CLASS.getMethod("cleaner");
            Object cleaner = cleanerMethod.invoke(buffer);
            if (cleaner != null) {
               Method cleanMethod = clsCleaner.getMethod("clean");
               cleanMethod.invoke(cleaner);
            }
         } catch (ReflectiveOperationException var8) {
            throw new IllegalStateException(var8);
         }
      } else {
         try {
            clsCleaner = Class.forName("sun.misc.Unsafe");
            cleanerMethod = clsCleaner.getMethod("invokeCleaner", ByteBuffer.class);
            Field unsafeField = clsCleaner.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            cleanerMethod.invoke(unsafeField.get((Object)null), buffer);
         } catch (ReflectiveOperationException var7) {
            throw new IllegalStateException(var7);
         }
      }

   }

   public synchronized void close() throws IOException {
      try {
         this.fileChannel.close();
      } finally {
         this.clean(this.byteBuffer);
      }

   }

   public synchronized int read() throws IOException {
      return !this.refill() ? -1 : this.byteBuffer.get() & 255;
   }

   public synchronized int read(byte[] b, int offset, int len) throws IOException {
      if (offset >= 0 && len >= 0 && offset + len >= 0 && offset + len <= b.length) {
         if (!this.refill()) {
            return -1;
         } else {
            len = Math.min(len, this.byteBuffer.remaining());
            this.byteBuffer.get(b, offset, len);
            return len;
         }
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   private boolean refill() throws IOException {
      if (this.byteBuffer.hasRemaining()) {
         return true;
      } else {
         this.byteBuffer.clear();

         int nRead;
         for(nRead = 0; nRead == 0; nRead = this.fileChannel.read(this.byteBuffer)) {
         }

         this.byteBuffer.flip();
         return nRead >= 0;
      }
   }

   public synchronized long skip(long n) throws IOException {
      if (n <= 0L) {
         return 0L;
      } else if ((long)this.byteBuffer.remaining() >= n) {
         this.byteBuffer.position(this.byteBuffer.position() + (int)n);
         return n;
      } else {
         long skippedFromBuffer = (long)this.byteBuffer.remaining();
         long toSkipFromFileChannel = n - skippedFromBuffer;
         this.byteBuffer.position(0);
         this.byteBuffer.flip();
         return skippedFromBuffer + this.skipFromFileChannel(toSkipFromFileChannel);
      }
   }

   private long skipFromFileChannel(long n) throws IOException {
      long currentFilePosition = this.fileChannel.position();
      long size = this.fileChannel.size();
      if (n > size - currentFilePosition) {
         this.fileChannel.position(size);
         return size - currentFilePosition;
      } else {
         this.fileChannel.position(currentFilePosition + n);
         return n;
      }
   }
}
