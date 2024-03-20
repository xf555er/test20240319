package org.apache.fop.pdf;

public class PDFObjectNumber {
   private int num;
   private PDFDocument doc;

   public PDFObjectNumber() {
   }

   public PDFObjectNumber(int num) {
      this.num = num;
   }

   public void setDocument(PDFDocument doc) {
      this.doc = doc;
   }

   public int getNumber() {
      if (this.num == 0 && this.doc != null) {
         this.num = ++this.doc.objectcount;
      }

      return this.num;
   }

   public String toString() {
      return String.valueOf(this.getNumber());
   }
}
