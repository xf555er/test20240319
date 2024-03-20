package org.apache.xmlgraphics.xmp.schemas.pdf;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPSchema;

public class PDFXXMPSchema extends XMPSchema {
   public static final String NAMESPACE = "http://www.npes.org/pdfx/ns/id/";

   public PDFXXMPSchema() {
      super("http://www.npes.org/pdfx/ns/id/", "pdfxid");
   }

   public static PDFXAdapter getAdapter(Metadata meta) {
      return new PDFXAdapter(meta, "http://www.npes.org/pdfx/ns/id/");
   }
}
