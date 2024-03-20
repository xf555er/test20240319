package net.jsign.poi.poifs.nio;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.IdentityHashMap;
import net.jsign.poi.util.IOUtils;
import net.jsign.poi.util.POILogFactory;
import net.jsign.poi.util.POILogger;

public class FileBackedDataSource extends DataSource implements Closeable {
   private static final POILogger logger = POILogFactory.getLogger(FileBackedDataSource.class);
   private final FileChannel channel;
   private Long channelSize;
   private final boolean writable;
   private final RandomAccessFile srcFile;
   private final IdentityHashMap buffersToClean;

   public FileBackedDataSource(File file, boolean readOnly) throws FileNotFoundException {
      this(newSrcFile(file, readOnly ? "r" : "rw"), readOnly);
   }

   public FileBackedDataSource(RandomAccessFile srcFile, boolean readOnly) {
      this(srcFile, srcFile.getChannel(), readOnly);
   }

   public FileBackedDataSource(FileChannel channel, boolean readOnly) {
      this((RandomAccessFile)null, channel, readOnly);
   }

   private FileBackedDataSource(RandomAccessFile srcFile, FileChannel channel, boolean readOnly) {
      this.buffersToClean = new IdentityHashMap();
      this.srcFile = srcFile;
      this.channel = channel;
      this.writable = !readOnly;
   }

   public boolean isWriteable() {
      return this.writable;
   }

   public FileChannel getChannel() {
      return this.channel;
   }

   public ByteBuffer read(int length, long position) throws IOException {
      if (position >= this.size()) {
         throw new IndexOutOfBoundsException("Position " + position + " past the end of the file");
      } else {
         Object dst;
         if (this.writable) {
            dst = this.channel.map(MapMode.READ_WRITE, position, (long)length);
            this.buffersToClean.put(dst, dst);
         } else {
            this.channel.position(position);
            dst = ByteBuffer.allocate(length);
            int worked = IOUtils.readFully(this.channel, (ByteBuffer)dst);
            if (worked == -1) {
               throw new IndexOutOfBoundsException("Position " + position + " past the end of the file");
            }
         }

         ((ByteBuffer)dst).position(0);
         return (ByteBuffer)dst;
      }
   }

   public void write(ByteBuffer src, long position) throws IOException {
      this.channel.write(src, position);
      if (this.channelSize != null && position >= this.channelSize) {
         this.channelSize = null;
      }

   }

   public void copyTo(OutputStream stream) throws IOException {
      WritableByteChannel out = Channels.newChannel(stream);
      Throwable var3 = null;

      try {
         this.channel.transferTo(0L, this.channel.size(), out);
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

   public long size() throws IOException {
      if (this.channelSize == null) {
         this.channelSize = this.channel.size();
      }

      return this.channelSize;
   }

   public void releaseBuffer(ByteBuffer buffer) {
      ByteBuffer previous = (ByteBuffer)this.buffersToClean.remove(buffer);
      if (previous != null) {
         unmap(previous);
      }

   }

   public void close() throws IOException {
      this.buffersToClean.forEach((k, v) -> {
         unmap(v);
      });
      this.buffersToClean.clear();
      if (this.srcFile != null) {
         this.srcFile.close();
      } else {
         this.channel.close();
      }

   }

   private static RandomAccessFile newSrcFile(File file, String mode) throws FileNotFoundException {
      if (!file.exists()) {
         throw new FileNotFoundException(file.toString());
      } else {
         return new RandomAccessFile(file, mode);
      }
   }

   private static void unmap(ByteBuffer buffer) {
      if (!buffer.getClass().getName().endsWith("HeapByteBuffer")) {
         if (CleanerUtil.UNMAP_SUPPORTED) {
            try {
               CleanerUtil.getCleaner().freeBuffer(buffer);
            } catch (IOException var2) {
               logger.log(5, "Failed to unmap the buffer", var2);
            }
         } else {
            logger.log(1, CleanerUtil.UNMAP_NOT_SUPPORTED_REASON);
         }

      }
   }
}
