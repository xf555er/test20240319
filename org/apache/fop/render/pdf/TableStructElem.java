package org.apache.fop.render.pdf;

import org.apache.fop.pdf.PDFObject;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.StructureType;

class TableStructElem extends PDFStructElem {
   private static final long serialVersionUID = -3550873504343680465L;
   private PDFStructElem tableFooter;

   public TableStructElem(PDFObject parent, StructureType structureType) {
      super(parent, structureType);
   }

   void addTableFooter(PDFStructElem footer) {
      assert this.tableFooter == null;

      this.tableFooter = footer;
   }

   protected boolean attachKids() {
      assert !this.kids.isEmpty();

      if (this.tableFooter != null) {
         this.kids.add(this.tableFooter);
      }

      return super.attachKids();
   }
}
