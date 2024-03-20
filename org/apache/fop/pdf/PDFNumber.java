package org.apache.fop.pdf;

import org.apache.xmlgraphics.util.DoubleFormatUtil;

public class PDFNumber extends PDFObject {
   private Number number;

   public PDFNumber() {
      this.number = 0;
   }

   public PDFNumber(Number number) {
      this.number = number;
   }

   public Number getNumber() {
      return this.number;
   }

   public void setNumber(Number number) {
      this.number = number;
   }

   public static String doubleOut(Double doubleDown) {
      return doubleOut(doubleDown);
   }

   public static String doubleOut(double doubleDown) {
      return doubleOut(doubleDown, 6);
   }

   public static String doubleOut(double doubleDown, int dec) {
      if (dec >= 0 && dec <= 16) {
         StringBuffer buf = new StringBuffer();
         DoubleFormatUtil.formatDouble(doubleDown, dec, dec, buf);
         return buf.toString();
      } else {
         throw new IllegalArgumentException("Parameter dec must be between 1 and 16");
      }
   }

   public static StringBuffer doubleOut(double doubleDown, int dec, StringBuffer buf) {
      if (dec >= 0 && dec <= 16) {
         DoubleFormatUtil.formatDouble(doubleDown, dec, dec, buf);
         return buf;
      } else {
         throw new IllegalArgumentException("Parameter dec must be between 1 and 16");
      }
   }

   protected String toPDFString() {
      if (this.getNumber() == null) {
         throw new IllegalArgumentException("The number of this PDFNumber must not be empty");
      } else {
         StringBuffer sb = new StringBuffer(64);
         sb.append(doubleOut(this.getNumber().doubleValue(), 10));
         return sb.toString();
      }
   }
}
