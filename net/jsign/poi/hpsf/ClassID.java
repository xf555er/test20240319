package net.jsign.poi.hpsf;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

public class ClassID {
   private final byte[] bytes = new byte[16];

   public ClassID(byte[] src, int offset) {
      this.read(src, offset);
   }

   public ClassID() {
      Arrays.fill(this.bytes, (byte)0);
   }

   public byte[] read(byte[] src, int offset) {
      this.bytes[0] = src[3 + offset];
      this.bytes[1] = src[2 + offset];
      this.bytes[2] = src[1 + offset];
      this.bytes[3] = src[0 + offset];
      this.bytes[4] = src[5 + offset];
      this.bytes[5] = src[4 + offset];
      this.bytes[6] = src[7 + offset];
      this.bytes[7] = src[6 + offset];
      System.arraycopy(src, 8 + offset, this.bytes, 8, 8);
      return this.bytes;
   }

   public void write(byte[] dst, int offset) throws ArrayStoreException {
      if (dst.length < 16) {
         throw new ArrayStoreException("Destination byte[] must have room for at least 16 bytes, but has a length of only " + dst.length + ".");
      } else {
         dst[0 + offset] = this.bytes[3];
         dst[1 + offset] = this.bytes[2];
         dst[2 + offset] = this.bytes[1];
         dst[3 + offset] = this.bytes[0];
         dst[4 + offset] = this.bytes[5];
         dst[5 + offset] = this.bytes[4];
         dst[6 + offset] = this.bytes[7];
         dst[7 + offset] = this.bytes[6];
         System.arraycopy(this.bytes, 8, dst, 8 + offset, 8);
      }
   }

   public boolean equals(Object o) {
      return o instanceof ClassID && Arrays.equals(this.bytes, ((ClassID)o).bytes);
   }

   public int hashCode() {
      return this.toString().hashCode();
   }

   public String toString() {
      return "{" + this.toUUIDString() + "}";
   }

   public String toUUIDString() {
      return this.toUUID().toString().toUpperCase(Locale.ROOT);
   }

   public UUID toUUID() {
      long mostSigBits = ByteBuffer.wrap(this.bytes, 0, 8).getLong();
      long leastSigBits = ByteBuffer.wrap(this.bytes, 8, 8).getLong();
      return new UUID(mostSigBits, leastSigBits);
   }
}
