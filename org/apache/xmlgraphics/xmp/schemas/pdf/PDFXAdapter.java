package org.apache.xmlgraphics.xmp.schemas.pdf;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPSchemaAdapter;
import org.apache.xmlgraphics.xmp.XMPSchemaRegistry;

public class PDFXAdapter extends XMPSchemaAdapter {
   public PDFXAdapter(Metadata meta, String namespace) {
      super(meta, XMPSchemaRegistry.getInstance().getSchema(namespace));
   }

   public void setVersion(String v) {
      this.setValue("GTS_PDFXVersion", v);
   }
}
