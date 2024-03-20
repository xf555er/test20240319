package org.apache.fop.pdf;

public class PDFDPartRoot extends PDFDictionary {
   private PDFArray parts = new PDFArray();
   protected PDFDPart dpart;

   public PDFDPartRoot(PDFDocument document) {
      this.put("Type", new PDFName("DPartRoot"));
      this.dpart = new PDFDPart(this);
      document.registerTrailerObject(this.dpart);
      PDFArray dparts = new PDFArray();
      dparts.add(this.parts);
      this.dpart.put("DParts", dparts);
      this.put("DPartRootNode", this.dpart.makeReference());
      PDFArray nodeNameList = new PDFArray();
      nodeNameList.add(new PDFName("root"));
      nodeNameList.add(new PDFName("record"));
      this.put("NodeNameList", nodeNameList);
   }

   public void add(PDFDPart part) {
      this.parts.add(part);
   }
}
