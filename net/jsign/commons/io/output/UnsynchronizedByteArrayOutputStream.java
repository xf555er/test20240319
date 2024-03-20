package net.jsign.commons.io.output;

public final class UnsynchronizedByteArrayOutputStream extends AbstractByteArrayOutputStream {
   public UnsynchronizedByteArrayOutputStream() {
      this(1024);
   }

   public UnsynchronizedByteArrayOutputStream(int size) {
      if (size < 0) {
         throw new IllegalArgumentException("Negative initial size: " + size);
      } else {
         this.needNewBuffer(size);
      }
   }

   public void write(byte[] b, int off, int len) {
      if (off >= 0 && off <= b.length && len >= 0 && off + len <= b.length && off + len >= 0) {
         if (len != 0) {
            this.writeImpl(b, off, len);
         }
      } else {
         throw new IndexOutOfBoundsException(String.format("offset=%,d, length=%,d", off, len));
      }
   }

   public void write(int b) {
      this.writeImpl(b);
   }

   public byte[] toByteArray() {
      return this.toByteArrayImpl();
   }
}
