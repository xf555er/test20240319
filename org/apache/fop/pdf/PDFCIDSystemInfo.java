package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PDFCIDSystemInfo extends PDFObject {
   private String registry;
   private String ordering;
   private int supplement;

   public PDFCIDSystemInfo(String registry, String ordering, int supplement) {
      this.registry = registry;
      this.ordering = ordering;
      this.supplement = supplement;
   }

   public String toPDFString() {
      StringBuffer p = new StringBuffer(64);
      p.setLength(0);
      p.append("/CIDSystemInfo << /Registry (");
      p.append(this.registry);
      p.append(") /Ordering (");
      p.append(this.ordering);
      p.append(") /Supplement ");
      p.append(this.supplement);
      p.append(" >>");
      return p.toString();
   }

   public byte[] toPDF() {
      ByteArrayOutputStream bout = new ByteArrayOutputStream(128);

      try {
         bout.write(encode("<< /Registry "));
         bout.write(this.encodeText(this.registry));
         bout.write(encode(" /Ordering "));
         bout.write(this.encodeText(this.ordering));
         bout.write(encode(" /Supplement "));
         bout.write(encode(Integer.toString(this.supplement)));
         bout.write(encode(" >>"));
      } catch (IOException var3) {
         log.error("Ignored I/O exception", var3);
      }

      return bout.toByteArray();
   }
}
