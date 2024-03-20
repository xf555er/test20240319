package org.apache.fop.render.pdf.extensions;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.URISpecification;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class PDFEmbeddedFileElement extends AbstractPDFExtensionElement {
   protected static final String ELEMENT = "embedded-file";

   PDFEmbeddedFileElement(FONode parent) {
      super(parent);
   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      if (this.parent.getNameId() != 13) {
         this.invalidChildError(this.getLocator(), this.parent.getName(), this.getNamespaceURI(), this.getName(), "rule.childOfDeclarations");
      }

   }

   public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList propertyList) throws FOPException {
      PDFEmbeddedFileAttachment embeddedFile = (PDFEmbeddedFileAttachment)this.getExtensionAttachment();
      String desc = attlist.getValue("description");
      if (desc != null && desc.length() > 0) {
         embeddedFile.setDesc(desc);
      }

      String src = attlist.getValue("src");
      src = URISpecification.getURL(src);
      if (src != null && src.length() > 0) {
         embeddedFile.setSrc(src);
      } else {
         this.missingPropertyError("src");
      }

      String filename = attlist.getValue("filename");
      if (filename == null || filename.length() == 0) {
         try {
            URI uri = new URI(src);
            String path = uri.getPath();
            int idx = path.lastIndexOf(47);
            if (idx > 0) {
               filename = path.substring(idx + 1);
            } else {
               filename = path;
            }

            embeddedFile.setFilename(filename);
         } catch (URISyntaxException var12) {
            this.missingPropertyError("name");
         }
      }

      embeddedFile.setFilename(filename);
   }

   public String getLocalName() {
      return "embedded-file";
   }

   protected ExtensionAttachment instantiateExtensionAttachment() {
      return new PDFEmbeddedFileAttachment();
   }
}
