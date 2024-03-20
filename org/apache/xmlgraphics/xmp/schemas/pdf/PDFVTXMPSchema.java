package org.apache.xmlgraphics.xmp.schemas.pdf;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPSchema;

public class PDFVTXMPSchema extends XMPSchema {
   public static final String NAMESPACE = "http://www.npes.org/pdfvt/ns/id/";

   public PDFVTXMPSchema() {
      super("http://www.npes.org/pdfvt/ns/id/", "pdfvtid");
   }

   public static PDFVTAdapter getAdapter(Metadata meta) {
      return new PDFVTAdapter(meta, "http://www.npes.org/pdfvt/ns/id/");
   }
}
