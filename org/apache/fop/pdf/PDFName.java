package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import org.apache.commons.io.output.CountingOutputStream;

public class PDFName extends PDFObject implements Serializable {
   private static final long serialVersionUID = -968412396459739925L;
   private String name;
   private static final String ESCAPED_NAME_CHARS = "/()<>[]%#";
   private static final char[] DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

   public PDFName(String name) {
      this.name = escapeName(name);
   }

   static String escapeName(String name) {
      StringBuilder sb = new StringBuilder(Math.min(16, name.length() + 4));
      boolean skipFirst = false;
      sb.append('/');
      if (name.startsWith("/")) {
         skipFirst = true;
      }

      int i = skipFirst ? 1 : 0;

      for(int c = name.length(); i < c; ++i) {
         char ch = name.charAt(i);
         if (ch >= '!' && ch <= '~' && "/()<>[]%#".indexOf(ch) < 0) {
            sb.append(ch);
         } else {
            sb.append('#');
            toHex(ch, sb);
         }
      }

      return sb.toString();
   }

   private static void toHex(char ch, StringBuilder sb) {
      if (ch >= 256) {
         throw new IllegalArgumentException("Only 8-bit characters allowed by this implementation");
      } else {
         sb.append(DIGITS[ch >>> 4 & 15]);
         sb.append(DIGITS[ch & 15]);
      }
   }

   public String toString() {
      return this.name;
   }

   public String getName() {
      return this.name.substring(1);
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof PDFName)) {
         return false;
      } else {
         PDFName other = (PDFName)obj;
         return this.name.equals(other.name);
      }
   }

   public int hashCode() {
      return this.name.hashCode();
   }

   public int output(OutputStream stream) throws IOException {
      CountingOutputStream cout = new CountingOutputStream(stream);
      StringBuilder textBuffer = new StringBuilder(64);
      textBuffer.append(this.toString());
      PDFDocument.flushTextBuffer(textBuffer, cout);
      return cout.getCount();
   }

   public void outputInline(OutputStream out, StringBuilder textBuffer) throws IOException {
      if (this.hasObjectNumber()) {
         textBuffer.append(this.referencePDF());
      } else {
         textBuffer.append(this.toString());
      }

   }
}
