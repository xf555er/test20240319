package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;

public class PDFRectangle implements PDFWritable {
   protected int llx;
   protected int lly;
   protected int urx;
   protected int ury;

   public PDFRectangle(int llx, int lly, int urx, int ury) {
      this.llx = llx;
      this.lly = lly;
      this.urx = urx;
      this.ury = ury;
   }

   public PDFRectangle(int[] array) {
      this.llx = array[0];
      this.lly = array[1];
      this.urx = array[2];
      this.ury = array[3];
   }

   private String format() {
      StringBuilder textBuffer = new StringBuilder(32);
      this.format(textBuffer);
      return textBuffer.toString();
   }

   private void format(StringBuilder textBuffer) {
      textBuffer.append('[').append(this.llx).append(' ').append(this.lly).append(' ').append(this.urx).append(' ').append(this.ury).append(']');
   }

   public String toString() {
      return "PDFRectangle" + this.format();
   }

   public void outputInline(OutputStream out, StringBuilder textBuffer) throws IOException {
      this.format(textBuffer);
   }
}
