package org.apache.fop.render.pdf.extensions;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.extensions.ExtensionAttachment;

public abstract class AbstractPDFExtensionElement extends FONode {
   protected PDFExtensionAttachment attachment;

   public AbstractPDFExtensionElement(FONode parent) {
      super(parent);
   }

   public String getNamespaceURI() {
      return "http://xmlgraphics.apache.org/fop/extensions/pdf";
   }

   public String getNormalNamespacePrefix() {
      return "pdf";
   }

   public ExtensionAttachment getExtensionAttachment() {
      if (this.attachment == null) {
         this.attachment = (PDFExtensionAttachment)this.instantiateExtensionAttachment();
      }

      return this.attachment;
   }

   protected ExtensionAttachment instantiateExtensionAttachment() {
      return null;
   }
}
