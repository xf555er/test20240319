package org.apache.fop.pdf;

import java.util.List;
import java.util.Set;
import java.util.Vector;

public class PDFAnnotList extends PDFObject {
   private List links = new Vector();

   public void addAnnot(PDFObject link) {
      this.links.add(link);
   }

   public int getCount() {
      return this.links.size();
   }

   public String toPDFString() {
      StringBuffer p = new StringBuffer(128);
      p.append("[\n");

      for(int i = 0; i < this.getCount(); ++i) {
         p.append(((PDFObject)this.links.get(i)).referencePDF());
         p.append("\n");
      }

      p.append("]");
      return p.toString();
   }

   public void getChildren(Set children) {
      PDFDictionary.getChildren(this.links, children);
   }
}
