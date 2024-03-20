package org.apache.fop.pdf;

public class PDFCIELabColorSpace extends PDFArray implements PDFColorSpace {
   public PDFCIELabColorSpace(float[] whitePoint, float[] blackPoint) {
      this.add(new PDFName("Lab"));
      PDFDictionary dict = new PDFDictionary();
      dict.put("WhitePoint", this.toPDFArray("White point", whitePoint));
      if (whitePoint[1] != 1.0F) {
         throw new IllegalArgumentException("The white point's Y coordinate must be 1.0");
      } else {
         if (blackPoint != null) {
            dict.put("BlackPoint", this.toPDFArray("Black point", blackPoint));
         }

         dict.put("Range", new PDFArray(dict, new int[]{-128, 128, -128, 128}));
         this.add(dict);
      }
   }

   private PDFArray toPDFArray(String name, float[] whitePoint) {
      PDFArray wp = new PDFArray();
      if (whitePoint != null && whitePoint.length == 3) {
         for(int i = 0; i < 3; ++i) {
            wp.add((double)whitePoint[i]);
         }

         return wp;
      } else {
         throw new IllegalArgumentException(name + " must be given an have 3 components");
      }
   }

   public String getName() {
      return "CS" + this.getObjectNumber();
   }

   public int getNumComponents() {
      return 3;
   }

   public boolean isCMYKColorSpace() {
      return false;
   }

   public boolean isDeviceColorSpace() {
      return false;
   }

   public boolean isGrayColorSpace() {
      return false;
   }

   public boolean isRGBColorSpace() {
      return false;
   }
}
