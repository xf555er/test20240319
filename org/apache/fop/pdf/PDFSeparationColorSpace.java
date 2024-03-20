package org.apache.fop.pdf;

public class PDFSeparationColorSpace extends PDFArray implements PDFColorSpace {
   public PDFSeparationColorSpace(String colorName, PDFFunction tintFunction) {
      this.add(new PDFName("Separation"));
      this.add(new PDFName(colorName));
      this.add(new PDFName("DeviceRGB"));
      this.add(new PDFReference(tintFunction));
   }

   public String getName() {
      return this.getColorName().toString();
   }

   public PDFName getColorName() {
      return (PDFName)this.get(1);
   }

   public PDFReference getTintFunction() {
      return (PDFReference)this.get(2);
   }

   public int getNumComponents() {
      return 1;
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
