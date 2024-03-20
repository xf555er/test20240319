package net.jsign;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;

public class ChannelUtils {
   public static void copy(SeekableByteChannel src, WritableByteChannel dest) throws IOException {
      ByteBuffer buffer = ByteBuffer.allocate(1048576);
      src.position(0L);

      while(src.position() < src.size()) {
         buffer.clear();
         src.read(buffer);
         buffer.flip();
         dest.write(buffer);
      }

   }

   public static void copy(SeekableByteChannel src, SeekableByteChannel dest, long length) throws IOException {
      ByteBuffer buffer = ByteBuffer.allocate(1048576);
      long remaining = length;
      long destOffset = dest.position();

      for(long srcOffset = src.position(); remaining > 0L; destOffset += (long)buffer.position()) {
         int avail = (int)Math.min(remaining, (long)buffer.capacity());
         buffer.clear();
         buffer.limit(avail);
         src.position(srcOffset);
         src.read(buffer);
         buffer.flip();
         dest.position(destOffset);
         dest.write(buffer);
         remaining -= (long)buffer.position();
         srcOffset += (long)buffer.position();
      }

   }

   public static void insert(SeekableByteChannel channel, long position, byte[] data) throws IOException {
      if (position > channel.size()) {
         throw new IOException("Cannot insert data after the end of the file");
      } else {
         File backupFile = File.createTempFile("jsign", ".tmp");

         try {
            SeekableByteChannel backupChannel = Files.newByteChannel(backupFile.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
            Throwable var6 = null;

            try {
               copy(channel, backupChannel);
               channel.position(position);
               channel.write(ByteBuffer.wrap(data));
               backupChannel.position(position);
               copy(backupChannel, channel, backupChannel.size() - position);
            } catch (Throwable var22) {
               var6 = var22;
               throw var22;
            } finally {
               if (backupChannel != null) {
                  if (var6 != null) {
                     try {
                        backupChannel.close();
                     } catch (Throwable var21) {
                        var6.addSuppressed(var21);
                     }
                  } else {
                     backupChannel.close();
                  }
               }

            }
         } finally {
            backupFile.delete();
         }

      }
   }

   public static void updateDigest(SeekableByteChannel channel, MessageDigest digest, long startOffset, long endOffset) throws IOException {
      channel.position(startOffset);
      ByteBuffer buffer = ByteBuffer.allocate(8192);

      for(long position = startOffset; position < endOffset; position += (long)buffer.limit()) {
         buffer.clear();
         buffer.limit((int)Math.min((long)buffer.capacity(), endOffset - position));
         channel.read(buffer);
         buffer.rewind();
         digest.update(buffer);
      }

   }

   public static byte[] readNullTerminatedString(ByteChannel channel) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Throwable var2 = null;

      try {
         ByteBuffer buffer = ByteBuffer.allocate(1);

         byte singleChar;
         do {
            buffer.clear();
            buffer.limit(1);
            channel.read(buffer);
            buffer.flip();
            singleChar = buffer.array()[0];
            bos.write(singleChar);
         } while(singleChar != 0);

         byte[] var5 = bos.toByteArray();
         return var5;
      } catch (Throwable var14) {
         var2 = var14;
         throw var14;
      } finally {
         if (bos != null) {
            if (var2 != null) {
               try {
                  bos.close();
               } catch (Throwable var13) {
                  var2.addSuppressed(var13);
               }
            } else {
               bos.close();
            }
         }

      }
   }
}
