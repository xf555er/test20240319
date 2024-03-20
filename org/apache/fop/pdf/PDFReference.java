package org.apache.fop.pdf;

import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

public class PDFReference implements PDFWritable {
   private PDFObjectNumber objectNumber;
   private int generation;
   private Reference objReference;

   public PDFReference(PDFObject obj) {
      this.objectNumber = obj.getObjectNumber();
      this.generation = obj.getGeneration();
      this.objReference = new SoftReference(obj);
   }

   public PDFReference(String ref) {
      if (ref == null) {
         throw new NullPointerException("ref must not be null");
      } else {
         String[] parts = ref.split(" ");

         assert parts.length == 3;

         this.objectNumber = new PDFObjectNumber(Integer.parseInt(parts[0]));
         this.generation = Integer.parseInt(parts[1]);

         assert "R".equals(parts[2]);

      }
   }

   public PDFObject getObject() {
      if (this.objReference != null) {
         PDFObject obj = (PDFObject)this.objReference.get();
         if (obj == null) {
            this.objReference = null;
         }

         return obj;
      } else {
         return null;
      }
   }

   public PDFObjectNumber getObjectNumber() {
      return this.objectNumber;
   }

   public int getGeneration() {
      return this.generation;
   }

   public String toString() {
      StringBuilder textBuffer = new StringBuilder();
      this.outputInline((OutputStream)null, textBuffer);
      return textBuffer.toString();
   }

   public void outputInline(OutputStream out, StringBuilder textBuffer) {
      textBuffer.append(this.getObjectNumber().getNumber()).append(' ').append(this.getGeneration()).append(" R");
   }
}
