package org.apache.fop.render.pdf.extensions;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.pdf.PDFInfo;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class PDFDocumentInformationElement extends PDFDictionaryElement {
   PDFDocumentInformationElement(FONode parent) {
      super(parent, PDFDictionaryType.Info);
   }

   public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList propertyList) throws FOPException {
      this.setLocator(locator);
      super.processNode(elementName, locator, attlist, propertyList);
   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      if (this.parent.getNameId() != 13) {
         this.invalidChildError(this.getLocator(), this.parent.getName(), this.getNamespaceURI(), this.getName(), "rule.childOfDeclarations");
      }

   }

   protected void validateChildNode(Locator loc, String namespaceURI, String localName) throws ValidationException {
      if (!"http://xmlgraphics.apache.org/fop/extensions/pdf".equals(namespaceURI) || !"name".equals(localName)) {
         this.invalidChildError(loc, namespaceURI, localName);
      }

   }

   protected void addChildNode(FONode child) throws FOPException {
      assert child instanceof PDFCollectionEntryElement;

      PDFCollectionEntryElement name = (PDFCollectionEntryElement)child;
      PDFInfo.StandardKey standardKey = PDFInfo.StandardKey.get(name.getExtension().getKey());
      if (standardKey == null) {
         super.addChildNode(child);
      } else {
         PDFExtensionEventProducer eventProducer = (PDFExtensionEventProducer)this.getUserAgent().getEventBroadcaster().getEventProducerFor(PDFExtensionEventProducer.class);
         eventProducer.reservedKeyword(this, this.getLocator(), standardKey.getName());
      }

   }
}
