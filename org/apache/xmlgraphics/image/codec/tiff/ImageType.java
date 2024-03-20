package org.apache.xmlgraphics.image.codec.tiff;

import java.awt.color.ColorSpace;
import org.apache.xmlgraphics.image.codec.util.PropertyUtil;

enum ImageType {
   UNSUPPORTED(-1),
   BILEVEL_WHITE_IS_ZERO(0),
   BILEVEL_BLACK_IS_ZERO(1),
   GRAY(1),
   PALETTE(3),
   RGB(2),
   CMYK(5),
   YCBCR(6),
   CIELAB(8),
   GENERIC(1);

   private final int photometricInterpretation;

   private ImageType(int photometricInterpretation) {
      this.photometricInterpretation = photometricInterpretation;
   }

   int getPhotometricInterpretation() {
      return this.photometricInterpretation;
   }

   static ImageType getTypeFromRGB(int mapSize, byte[] r, byte[] g, byte[] b, int dataTypeSize, int numBands) {
      if (numBands == 1) {
         if (dataTypeSize == 1) {
            if (mapSize != 2) {
               throw new IllegalArgumentException(PropertyUtil.getString("TIFFImageEncoder7"));
            }

            if (isBlackZero(r, g, b)) {
               return BILEVEL_BLACK_IS_ZERO;
            }

            if (isWhiteZero(r, g, b)) {
               return BILEVEL_WHITE_IS_ZERO;
            }
         }

         return PALETTE;
      } else {
         return UNSUPPORTED;
      }
   }

   private static boolean rgbIsValueAt(byte[] r, byte[] g, byte[] b, byte value, int i) {
      return r[i] == value && g[i] == value && b[i] == value;
   }

   private static boolean bilevelColorValue(byte[] r, byte[] g, byte[] b, int blackValue, int whiteValue) {
      return rgbIsValueAt(r, g, b, (byte)blackValue, 0) && rgbIsValueAt(r, g, b, (byte)whiteValue, 1);
   }

   private static boolean isBlackZero(byte[] r, byte[] g, byte[] b) {
      return bilevelColorValue(r, g, b, 0, 255);
   }

   private static boolean isWhiteZero(byte[] r, byte[] g, byte[] b) {
      return bilevelColorValue(r, g, b, 255, 0);
   }

   static ImageType getTypeFromColorSpace(ColorSpace colorSpace, TIFFEncodeParam params) {
      switch (colorSpace.getType()) {
         case 1:
            return CIELAB;
         case 2:
         case 4:
         case 7:
         case 8:
         default:
            return GENERIC;
         case 3:
            return YCBCR;
         case 5:
            if (params.getJPEGCompressRGBToYCbCr()) {
               return YCBCR;
            }

            return RGB;
         case 6:
            return GRAY;
         case 9:
            return CMYK;
      }
   }
}
