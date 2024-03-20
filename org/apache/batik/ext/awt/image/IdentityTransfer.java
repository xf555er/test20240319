package org.apache.batik.ext.awt.image;

public class IdentityTransfer implements TransferFunction {
   public static byte[] lutData = new byte[256];

   public byte[] getLookupTable() {
      return lutData;
   }

   static {
      for(int j = 0; j <= 255; ++j) {
         lutData[j] = (byte)j;
      }

   }
}
