package org.apache.xmlgraphics.xmp.schemas.pdf;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPSchemaAdapter;
import org.apache.xmlgraphics.xmp.XMPSchemaRegistry;

public class PDFUAAdapter extends XMPSchemaAdapter {
   private static final String PART = "part";

   public PDFUAAdapter(Metadata meta, String namespace) {
      super(meta, XMPSchemaRegistry.getInstance().getSchema(namespace));
   }

   public void setPart(int value) {
      this.setValue("part", Integer.toString(value));
   }

   public int getPart() {
      return Integer.parseInt(this.getValue("part"));
   }
}
