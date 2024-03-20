package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;

public final class PDFNull implements PDFWritable {
   public static final PDFNull INSTANCE = new PDFNull();

   private PDFNull() {
   }

   public String toString() {
      return "null";
   }

   public void outputInline(OutputStream out, StringBuilder textBuffer) throws IOException {
      textBuffer.append(this.toString());
   }
}
