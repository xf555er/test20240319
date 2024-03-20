package org.apache.commons.io.output;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ByteArrayOutputStream extends AbstractByteArrayOutputStream {
   public ByteArrayOutputStream() {
      this(1024);
   }

   public ByteArrayOutputStream(int size) {
      if (size < 0) {
         throw new IllegalArgumentException("Negative initial size: " + size);
      } else {
         synchronized(this) {
            this.needNewBuffer(size);
         }
      }
   }

   public void write(byte[] b, int off, int len) {
      if (off >= 0 && off <= b.length && len >= 0 && off + len <= b.length && off + len >= 0) {
         if (len != 0) {
            synchronized(this) {
               this.writeImpl(b, off, len);
            }
         }
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   public synchronized void write(int b) {
      this.writeImpl(b);
   }

   public synchronized int write(InputStream in) throws IOException {
      return this.writeImpl(in);
   }

   public synchronized int size() {
      return this.count;
   }

   public synchronized void reset() {
      this.resetImpl();
   }

   public synchronized void writeTo(OutputStream out) throws IOException {
      this.writeToImpl(out);
   }

   public static InputStream toBufferedInputStream(InputStream input) throws IOException {
      return toBufferedInputStream(input, 1024);
   }

   public static InputStream toBufferedInputStream(InputStream input, int size) throws IOException {
      ByteArrayOutputStream output = new ByteArrayOutputStream(size);
      Throwable var3 = null;

      InputStream var4;
      try {
         output.write(input);
         var4 = output.toInputStream();
      } catch (Throwable var13) {
         var3 = var13;
         throw var13;
      } finally {
         if (output != null) {
            if (var3 != null) {
               try {
                  output.close();
               } catch (Throwable var12) {
                  var3.addSuppressed(var12);
               }
            } else {
               output.close();
            }
         }

      }

      return var4;
   }

   public synchronized InputStream toInputStream() {
      return this.toInputStream(ByteArrayInputStream::new);
   }

   public synchronized byte[] toByteArray() {
      return this.toByteArrayImpl();
   }
}
