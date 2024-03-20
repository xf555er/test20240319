package org.apache.fop.fonts.truetype;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OTFSubSetWriter extends OTFFile {
   protected int currentPos;
   protected ByteArrayOutputStream output = new ByteArrayOutputStream();

   public OTFSubSetWriter() throws IOException {
   }

   public static byte[] concatArray(byte[] a, byte[] b) {
      int aLen = a.length;
      int bLen = b.length;
      byte[] c = new byte[aLen + bLen];
      System.arraycopy(a, 0, c, 0, aLen);
      System.arraycopy(b, 0, c, aLen, bLen);
      return c;
   }

   protected void writeByte(int b) {
      this.output.write(b);
      ++this.currentPos;
   }

   protected void writeCard16(int s) {
      byte b1 = (byte)(s >> 8 & 255);
      byte b2 = (byte)(s & 255);
      this.writeByte(b1);
      this.writeByte(b2);
   }

   protected void writeThreeByteNumber(int s) {
      byte b1 = (byte)(s >> 16 & 255);
      byte b2 = (byte)(s >> 8 & 255);
      byte b3 = (byte)(s & 255);
      this.writeByte(b1);
      this.writeByte(b2);
      this.writeByte(b3);
   }

   protected void writeULong(int s) {
      byte b1 = (byte)(s >> 24 & 255);
      byte b2 = (byte)(s >> 16 & 255);
      byte b3 = (byte)(s >> 8 & 255);
      byte b4 = (byte)(s & 255);
      this.writeByte(b1);
      this.writeByte(b2);
      this.writeByte(b3);
      this.writeByte(b4);
   }

   protected void writeBytes(byte[] out) {
      byte[] var2 = out;
      int var3 = out.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         byte anOut = var2[var4];
         this.writeByte(anOut);
      }

   }

   public byte[] getFontSubset() {
      return this.output.toByteArray();
   }
}
