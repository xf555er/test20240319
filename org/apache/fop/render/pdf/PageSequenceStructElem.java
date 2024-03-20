package org.apache.fop.render.pdf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFObject;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.StructureType;

public class PageSequenceStructElem extends PDFStructElem {
   private static final long serialVersionUID = -9146602678931267198L;
   private List regionBefores = new ArrayList();
   private List regionAfters = new ArrayList();
   private List regionStarts = new ArrayList();
   private List regionEnds = new ArrayList();
   private List footnoteSeparator = new ArrayList();

   PageSequenceStructElem(PDFObject parent, StructureType structureType) {
      super(parent, structureType);
   }

   void addContent(String flowName, PDFStructElem content) {
      if (flowName.equals("xsl-region-before")) {
         this.regionBefores.add(content);
      } else if (flowName.equals("xsl-region-after")) {
         this.regionAfters.add(content);
      } else if (flowName.equals("xsl-region-start")) {
         this.regionStarts.add(content);
      } else if (flowName.equals("xsl-region-end")) {
         this.regionEnds.add(content);
      } else if (flowName.equals("xsl-footnote-separator")) {
         this.footnoteSeparator.add(content);
      } else {
         this.addKid(content);
      }

   }

   protected boolean attachKids() {
      assert !this.kids.isEmpty();

      PDFArray k = new PDFArray();
      this.addRegions(k, this.regionBefores);
      this.addRegions(k, this.regionStarts);
      this.addRegions(k, this.kids);
      this.addRegions(k, this.regionEnds);
      this.addRegions(k, this.footnoteSeparator);
      this.addRegions(k, this.regionAfters);
      this.put("K", k);
      return true;
   }

   private void addRegions(PDFArray k, List regions) {
      if (!regions.isEmpty()) {
         Iterator var3 = regions.iterator();

         while(var3.hasNext()) {
            PDFObject kid = (PDFObject)var3.next();
            k.add(kid);
         }
      }

   }
}
