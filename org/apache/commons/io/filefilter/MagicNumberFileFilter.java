package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;

public class MagicNumberFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = -547733176983104172L;
   private final byte[] magicNumbers;
   private final long byteOffset;

   public MagicNumberFileFilter(byte[] magicNumber) {
      this(magicNumber, 0L);
   }

   public MagicNumberFileFilter(byte[] magicNumber, long offset) {
      if (magicNumber == null) {
         throw new IllegalArgumentException("The magic number cannot be null");
      } else if (magicNumber.length == 0) {
         throw new IllegalArgumentException("The magic number must contain at least one byte");
      } else if (offset < 0L) {
         throw new IllegalArgumentException("The offset cannot be negative");
      } else {
         this.magicNumbers = IOUtils.byteArray(magicNumber.length);
         System.arraycopy(magicNumber, 0, this.magicNumbers, 0, magicNumber.length);
         this.byteOffset = offset;
      }
   }

   public MagicNumberFileFilter(String magicNumber) {
      this(magicNumber, 0L);
   }

   public MagicNumberFileFilter(String magicNumber, long offset) {
      if (magicNumber == null) {
         throw new IllegalArgumentException("The magic number cannot be null");
      } else if (magicNumber.isEmpty()) {
         throw new IllegalArgumentException("The magic number must contain at least one byte");
      } else if (offset < 0L) {
         throw new IllegalArgumentException("The offset cannot be negative");
      } else {
         this.magicNumbers = magicNumber.getBytes(Charset.defaultCharset());
         this.byteOffset = offset;
      }
   }

   public boolean accept(File file) {
      if (file != null && file.isFile() && file.canRead()) {
         try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            Throwable var3 = null;

            boolean var6;
            try {
               byte[] fileBytes = IOUtils.byteArray(this.magicNumbers.length);
               randomAccessFile.seek(this.byteOffset);
               int read = randomAccessFile.read(fileBytes);
               if (read != this.magicNumbers.length) {
                  var6 = false;
                  return var6;
               }

               var6 = Arrays.equals(this.magicNumbers, fileBytes);
            } catch (Throwable var17) {
               var3 = var17;
               throw var17;
            } finally {
               if (randomAccessFile != null) {
                  if (var3 != null) {
                     try {
                        randomAccessFile.close();
                     } catch (Throwable var16) {
                        var3.addSuppressed(var16);
                     }
                  } else {
                     randomAccessFile.close();
                  }
               }

            }

            return var6;
         } catch (IOException var19) {
         }
      }

      return false;
   }

   public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
      if (file != null && Files.isRegularFile(file, new LinkOption[0]) && Files.isReadable(file)) {
         try {
            FileChannel fileChannel = FileChannel.open(file);
            Throwable var4 = null;

            FileVisitResult var7;
            try {
               ByteBuffer byteBuffer = ByteBuffer.allocate(this.magicNumbers.length);
               int read = fileChannel.read(byteBuffer);
               if (read == this.magicNumbers.length) {
                  var7 = toFileVisitResult(Arrays.equals(this.magicNumbers, byteBuffer.array()), file);
                  return var7;
               }

               var7 = FileVisitResult.TERMINATE;
            } catch (Throwable var18) {
               var4 = var18;
               throw var18;
            } finally {
               if (fileChannel != null) {
                  if (var4 != null) {
                     try {
                        fileChannel.close();
                     } catch (Throwable var17) {
                        var4.addSuppressed(var17);
                     }
                  } else {
                     fileChannel.close();
                  }
               }

            }

            return var7;
         } catch (IOException var20) {
         }
      }

      return FileVisitResult.TERMINATE;
   }

   public String toString() {
      StringBuilder builder = new StringBuilder(super.toString());
      builder.append("(");
      builder.append(new String(this.magicNumbers, Charset.defaultCharset()));
      builder.append(",");
      builder.append(this.byteOffset);
      builder.append(")");
      return builder.toString();
   }
}
