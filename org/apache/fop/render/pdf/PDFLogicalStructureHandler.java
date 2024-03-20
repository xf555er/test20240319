package org.apache.fop.render.pdf;

import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFLink;
import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFParentTree;
import org.apache.fop.pdf.PDFStructElem;

public class PDFLogicalStructureHandler {
   private static final PDFName MCR = new PDFName("MCR");
   private static final PDFName OBJR = new PDFName("OBJR");
   private static final MarkedContentInfo ARTIFACT = new MarkedContentInfo((String)null, -1, (PDFStructElem)null);
   private final PDFDocument pdfDoc;
   private final PDFParentTree parentTree = new PDFParentTree();
   private int parentTreeKey;
   private PDFPage currentPage;
   private PDFArray pageParentTreeArray;

   PDFLogicalStructureHandler(PDFDocument pdfDoc) {
      this.pdfDoc = pdfDoc;
   }

   public PDFArray getPageParentTree() {
      return this.pageParentTreeArray;
   }

   public PDFParentTree getParentTree() {
      return this.parentTree;
   }

   public int getNextParentTreeKey() {
      return this.parentTreeKey++;
   }

   void startPage(PDFPage page) {
      this.currentPage = page;
      this.currentPage.setStructParents(this.getNextParentTreeKey());
      this.pageParentTreeArray = new PDFArray();
   }

   void endPage() {
      this.pdfDoc.registerObject(this.pageParentTreeArray);
      this.parentTree.addToNums(this.currentPage.getStructParents(), this.pageParentTreeArray);
   }

   private MarkedContentInfo addToParentTree(PDFStructElem structureTreeElement) {
      PDFStructElem parent;
      for(parent = structureTreeElement; parent instanceof PDFStructElem.Placeholder; parent = parent.getParentStructElem()) {
      }

      this.pageParentTreeArray.add(parent);
      String type = parent.getStructureType().getName().toString();
      int mcid = this.pageParentTreeArray.length() - 1;
      return new MarkedContentInfo(type, mcid, structureTreeElement);
   }

   MarkedContentInfo addTextContentItem(PDFStructElem structElem) {
      if (structElem == null) {
         return ARTIFACT;
      } else {
         MarkedContentInfo mci = this.addToParentTree(structElem);
         PDFDictionary contentItem = new PDFDictionary();
         contentItem.put("Type", MCR);
         contentItem.put("Pg", this.currentPage);
         contentItem.put("MCID", mci.mcid);
         mci.parent.addKid(contentItem);
         return mci;
      }
   }

   MarkedContentInfo addImageContentItem(PDFStructElem structElem) {
      if (structElem == null) {
         return ARTIFACT;
      } else {
         MarkedContentInfo mci = this.addToParentTree(structElem);
         PDFDictionary contentItem = new PDFDictionary();
         contentItem.put("Type", MCR);
         contentItem.put("Pg", this.currentPage);
         contentItem.put("MCID", mci.mcid);
         mci.parent.addKid(contentItem);
         return mci;
      }
   }

   void addLinkContentItem(PDFLink link, PDFStructElem structureTreeElement) {
      int structParent = this.getNextParentTreeKey();
      link.setStructParent(structParent);
      PDFDictionary contentItem = new PDFDictionary();
      contentItem.put("Type", OBJR);
      contentItem.put("Pg", this.currentPage);
      contentItem.put("Obj", link);
      this.parentTree.addToNums(structParent, structureTreeElement);
      structureTreeElement.addKid(contentItem);
   }

   static final class MarkedContentInfo {
      final String tag;
      final int mcid;
      private final PDFStructElem parent;

      private MarkedContentInfo(String tag, int mcid, PDFStructElem parent) {
         this.tag = tag;
         this.mcid = mcid;
         this.parent = parent;
      }

      // $FF: synthetic method
      MarkedContentInfo(String x0, int x1, PDFStructElem x2, Object x3) {
         this(x0, x1, x2);
      }
   }
}
