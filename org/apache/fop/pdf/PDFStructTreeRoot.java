package org.apache.fop.pdf;

public class PDFStructTreeRoot extends StructureHierarchyMember {
   PDFStructTreeRoot(PDFParentTree parentTree) {
      this.put("Type", new PDFName("StructTreeRoot"));
      this.put("K", new PDFArray());
      this.put("ParentTree", parentTree);
   }

   public PDFArray getKids() {
      return (PDFArray)this.get("K");
   }

   public void addKid(PDFObject kid) {
      this.getKids().add(kid);
   }
}
