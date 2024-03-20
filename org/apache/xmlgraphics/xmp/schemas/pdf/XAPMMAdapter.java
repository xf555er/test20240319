package org.apache.xmlgraphics.xmp.schemas.pdf;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPSchemaAdapter;
import org.apache.xmlgraphics.xmp.XMPSchemaRegistry;

public class XAPMMAdapter extends XMPSchemaAdapter {
   public XAPMMAdapter(Metadata meta, String namespace) {
      super(meta, XMPSchemaRegistry.getInstance().getSchema(namespace));
   }

   public void setVersion(String v) {
      this.setValue("VersionID", v);
   }

   public void setRenditionClass(String c) {
      this.setValue("RenditionClass", c);
   }

   public void setInstanceID(String v) {
      this.setValue("InstanceID", v);
   }

   public void setDocumentID(String v) {
      this.setValue("DocumentID", v);
   }
}
