package org.apache.xmlgraphics.xmp.schemas.pdf;

import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPSchema;

public class XAPMMXMPSchema extends XMPSchema {
   public static final String NAMESPACE = "http://ns.adobe.com/xap/1.0/mm/";

   public XAPMMXMPSchema() {
      super("http://ns.adobe.com/xap/1.0/mm/", "xmpMM");
   }

   public static XAPMMAdapter getAdapter(Metadata meta) {
      return new XAPMMAdapter(meta, "http://ns.adobe.com/xap/1.0/mm/");
   }
}
