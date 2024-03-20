package org.apache.fop.render.pdf.extensions;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class PDFActionElement extends PDFDictionaryElement {
   public static final String ATT_TYPE = "type";

   PDFActionElement(FONode parent) {
      super(parent, PDFDictionaryType.Action);
   }

   public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList propertyList) throws FOPException {
      super.processNode(elementName, locator, attlist, propertyList);
      String type = attlist.getValue("type");
      if (type != null) {
         this.getDictionaryExtension().setProperty("type", type);
      }

   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      if (this.parent.getNameId() != 13) {
         this.invalidChildError(this.getLocator(), this.parent.getName(), this.getNamespaceURI(), this.getName(), "rule.childOfDeclarations");
      }

   }
}
