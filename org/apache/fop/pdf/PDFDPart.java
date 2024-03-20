package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class PDFDPart extends PDFDictionary {
   private List pages = new ArrayList();
   private PDFDictionary parent;

   public PDFDPart(PDFDictionary parent) {
      this.parent = parent;
      this.put("Type", new PDFName("DPart"));
   }

   public void addPage(PDFPage p) {
      this.pages.add(p);
   }

   public int output(OutputStream stream) throws IOException {
      this.put("Parent", this.parent.makeReference());
      if (!this.pages.isEmpty()) {
         this.put("Start", ((PDFPage)this.pages.get(0)).makeReference());
         this.put("End", ((PDFPage)this.pages.get(this.pages.size() - 1)).makeReference());
      }

      return super.output(stream);
   }
}
