package org.apache.fop.render.pdf.extensions;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class PDFArrayElement extends PDFCollectionEntryElement {
   private PDFArrayExtension extension;

   PDFArrayElement(FONode parent) {
      super(parent, PDFObjectType.Array, new PDFArrayExtension());
   }

   public PDFArrayExtension getArrayExtension() {
      PDFCollectionEntryExtension extension = this.getExtension();

      assert extension instanceof PDFArrayExtension;

      return (PDFArrayExtension)extension;
   }

   public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList propertyList) throws FOPException {
      super.processNode(elementName, locator, attlist, propertyList);
   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
   }

   protected void addChildNode(FONode child) throws FOPException {
      PDFArrayExtension extension = this.getArrayExtension();
      if (child instanceof PDFCollectionEntryElement) {
         PDFCollectionEntryExtension entry = ((PDFCollectionEntryElement)child).getExtension();
         if (entry.getKey() == null) {
            extension.addEntry(entry);
         }
      }

   }

   public void endOfNode() throws FOPException {
      super.endOfNode();
   }

   public String getLocalName() {
      return PDFObjectType.Array.elementName();
   }
}
