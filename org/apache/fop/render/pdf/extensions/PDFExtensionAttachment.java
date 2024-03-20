package org.apache.fop.render.pdf.extensions;

import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.xmlgraphics.util.XMLizable;

public abstract class PDFExtensionAttachment implements ExtensionAttachment, XMLizable {
   public static final String CATEGORY = "apache:fop:extensions:pdf";
   public static final String PREFIX = "pdf";

   public String getPrefix() {
      return "pdf";
   }

   public String getCategory() {
      return "apache:fop:extensions:pdf";
   }
}
