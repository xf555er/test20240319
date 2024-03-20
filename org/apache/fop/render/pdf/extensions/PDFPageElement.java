package org.apache.fop.render.pdf.extensions;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class PDFPageElement extends PDFDictionaryElement {
   public static final String ATT_PAGE_NUMBERS = "page-numbers";

   PDFPageElement(FONode parent) {
      super(parent, PDFDictionaryType.Page);
   }

   public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList propertyList) throws FOPException {
      super.processNode(elementName, locator, attlist, propertyList);
      String pageNumbers = attlist.getValue("page-numbers");
      if (pageNumbers != null) {
         this.getDictionaryExtension().setProperty("page-numbers", pageNumbers);
      }

   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      if (this.parent.getNameId() != 68) {
         this.invalidChildError(this.getLocator(), this.parent.getName(), this.getNamespaceURI(), this.getName(), "rule.childOfSPM");
      }

   }
}
