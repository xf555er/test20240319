package org.apache.xmlgraphics.xmp;

import java.util.HashMap;
import java.util.Map;
import org.apache.xmlgraphics.xmp.schemas.DublinCoreSchema;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.AdobePDFSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.PDFAXMPSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.PDFUAXMPSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.PDFVTXMPSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.PDFXXMPSchema;
import org.apache.xmlgraphics.xmp.schemas.pdf.XAPMMXMPSchema;

public final class XMPSchemaRegistry {
   private static XMPSchemaRegistry instance = new XMPSchemaRegistry();
   private Map schemas = new HashMap();

   private XMPSchemaRegistry() {
      this.init();
   }

   public static XMPSchemaRegistry getInstance() {
      return instance;
   }

   private void init() {
      this.addSchema(new DublinCoreSchema());
      this.addSchema(new PDFAXMPSchema());
      this.addSchema(new XMPBasicSchema());
      this.addSchema(new AdobePDFSchema());
      this.addSchema(new PDFXXMPSchema());
      this.addSchema(new PDFVTXMPSchema());
      this.addSchema(new XAPMMXMPSchema());
      this.addSchema(new PDFUAXMPSchema());
   }

   public void addSchema(XMPSchema schema) {
      this.schemas.put(schema.getNamespace(), schema);
   }

   public XMPSchema getSchema(String namespace) {
      return (XMPSchema)this.schemas.get(namespace);
   }
}
