package org.apache.xmlgraphics.xmp.schemas.pdf;

import java.util.Date;
import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.XMPSchemaAdapter;
import org.apache.xmlgraphics.xmp.XMPSchemaRegistry;

public class PDFVTAdapter extends XMPSchemaAdapter {
   public PDFVTAdapter(Metadata meta, String namespace) {
      super(meta, XMPSchemaRegistry.getInstance().getSchema(namespace));
   }

   public void setVersion(String v) {
      this.setValue("GTS_PDFVTVersion", v);
   }

   public void setModifyDate(Date modifyDate) {
      this.setDateValue("GTS_PDFVTModDate", modifyDate);
   }
}
