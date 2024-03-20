package org.apache.fop.pdf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PDFPages extends PDFObject {
   protected List kids = new ArrayList();
   protected int count;

   public PDFPages(PDFDocument document) {
      this.setObjectNumber(document);
   }

   public void addPage(PDFPage page) {
      page.setParent(this);
      this.incrementCount();
   }

   public void notifyKidRegistered(PDFPage page) {
      int idx = page.getPageIndex();
      if (idx < 0) {
         this.kids.add(page.makeReference());
      } else {
         while(true) {
            if (idx <= this.kids.size() - 1) {
               if (this.kids.get(idx) != null) {
                  throw new IllegalStateException("A page already exists at index " + idx + " (zero-based).");
               }

               this.kids.set(idx, page.makeReference());
               break;
            }

            this.kids.add((Object)null);
         }
      }

   }

   public int getCount() {
      return this.count;
   }

   public void incrementCount() {
      ++this.count;
   }

   public String toPDFString() {
      StringBuffer sb = new StringBuffer(64);
      sb.append("<< /Type /Pages\n/Count ").append(this.getCount()).append("\n/Kids [");
      Iterator var2 = this.kids.iterator();

      while(var2.hasNext()) {
         Object kid = var2.next();
         if (kid == null) {
            throw new IllegalStateException("Gap in the kids list!");
         }

         sb.append(kid).append(" ");
      }

      sb.append("] >>");
      return sb.toString();
   }
}
