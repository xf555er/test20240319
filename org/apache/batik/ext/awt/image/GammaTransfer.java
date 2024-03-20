package org.apache.batik.ext.awt.image;

public class GammaTransfer implements TransferFunction {
   public byte[] lutData;
   public float amplitude;
   public float exponent;
   public float offset;

   public GammaTransfer(float amplitude, float exponent, float offset) {
      this.amplitude = amplitude;
      this.exponent = exponent;
      this.offset = offset;
   }

   private void buildLutData() {
      this.lutData = new byte[256];

      for(int j = 0; j <= 255; ++j) {
         int v = (int)Math.round(255.0 * ((double)this.amplitude * Math.pow((double)((float)j / 255.0F), (double)this.exponent) + (double)this.offset));
         if (v > 255) {
            v = -1;
         } else if (v < 0) {
            v = 0;
         }

         this.lutData[j] = (byte)(v & 255);
      }

   }

   public byte[] getLookupTable() {
      this.buildLutData();
      return this.lutData;
   }
}
