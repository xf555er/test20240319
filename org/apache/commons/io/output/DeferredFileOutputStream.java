package org.apache.commons.io.output;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class DeferredFileOutputStream extends ThresholdingOutputStream {
   private ByteArrayOutputStream memoryOutputStream;
   private OutputStream currentOutputStream;
   private File outputFile;
   private final String prefix;
   private final String suffix;
   private final File directory;
   private boolean closed;

   public DeferredFileOutputStream(int threshold, File outputFile) {
      this(threshold, outputFile, (String)null, (String)null, (File)null, 1024);
   }

   private DeferredFileOutputStream(int threshold, File outputFile, String prefix, String suffix, File directory, int initialBufferSize) {
      super(threshold);
      this.outputFile = outputFile;
      this.prefix = prefix;
      this.suffix = suffix;
      this.directory = directory;
      this.memoryOutputStream = new ByteArrayOutputStream(initialBufferSize);
      this.currentOutputStream = this.memoryOutputStream;
   }

   public DeferredFileOutputStream(int threshold, int initialBufferSize, File outputFile) {
      this(threshold, outputFile, (String)null, (String)null, (File)null, initialBufferSize);
      if (initialBufferSize < 0) {
         throw new IllegalArgumentException("Initial buffer size must be atleast 0.");
      }
   }

   public DeferredFileOutputStream(int threshold, int initialBufferSize, String prefix, String suffix, File directory) {
      this(threshold, (File)null, prefix, suffix, directory, initialBufferSize);
      if (prefix == null) {
         throw new IllegalArgumentException("Temporary file prefix is missing");
      } else if (initialBufferSize < 0) {
         throw new IllegalArgumentException("Initial buffer size must be atleast 0.");
      }
   }

   public DeferredFileOutputStream(int threshold, String prefix, String suffix, File directory) {
      this(threshold, (File)null, prefix, suffix, directory, 1024);
      if (prefix == null) {
         throw new IllegalArgumentException("Temporary file prefix is missing");
      }
   }

   public void close() throws IOException {
      super.close();
      this.closed = true;
   }

   public byte[] getData() {
      return this.memoryOutputStream != null ? this.memoryOutputStream.toByteArray() : null;
   }

   public File getFile() {
      return this.outputFile;
   }

   protected OutputStream getStream() throws IOException {
      return this.currentOutputStream;
   }

   public boolean isInMemory() {
      return !this.isThresholdExceeded();
   }

   protected void thresholdReached() throws IOException {
      if (this.prefix != null) {
         this.outputFile = File.createTempFile(this.prefix, this.suffix, this.directory);
      }

      FileUtils.forceMkdirParent(this.outputFile);
      OutputStream fos = Files.newOutputStream(this.outputFile.toPath());

      try {
         this.memoryOutputStream.writeTo(fos);
      } catch (IOException var3) {
         fos.close();
         throw var3;
      }

      this.currentOutputStream = fos;
      this.memoryOutputStream = null;
   }

   public InputStream toInputStream() throws IOException {
      if (!this.closed) {
         throw new IOException("Stream not closed");
      } else {
         return this.isInMemory() ? this.memoryOutputStream.toInputStream() : Files.newInputStream(this.outputFile.toPath());
      }
   }

   public void writeTo(OutputStream outputStream) throws IOException {
      if (!this.closed) {
         throw new IOException("Stream not closed");
      } else {
         if (this.isInMemory()) {
            this.memoryOutputStream.writeTo(outputStream);
         } else {
            InputStream fis = Files.newInputStream(this.outputFile.toPath());
            Throwable var3 = null;

            try {
               IOUtils.copy(fis, outputStream);
            } catch (Throwable var12) {
               var3 = var12;
               throw var12;
            } finally {
               if (fis != null) {
                  if (var3 != null) {
                     try {
                        fis.close();
                     } catch (Throwable var11) {
                        var3.addSuppressed(var11);
                     }
                  } else {
                     fis.close();
                  }
               }

            }
         }

      }
   }
}
