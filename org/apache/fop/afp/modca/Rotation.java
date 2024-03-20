package org.apache.fop.afp.modca;

public enum Rotation {
   ROTATION_0(0),
   ROTATION_90(45),
   ROTATION_180(90),
   ROTATION_270(135);

   private final byte firstByte;

   public void writeTo(byte[] out, int offset) {
      out[offset] = this.firstByte;
      out[offset + 1] = 0;
   }

   private Rotation(int firstByte) {
      this.firstByte = (byte)firstByte;
   }

   public byte getByte() {
      return this.firstByte;
   }
}
