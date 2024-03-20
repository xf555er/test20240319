package org.apache.fop.render.afp.extensions;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class AFPPageSegmentElement extends AFPPageSetupElement {
   private static final String ATT_RESOURCE_SRC = "resource-file";

   public AFPPageSegmentElement(FONode parent, String name) {
      super(parent, name);
   }

   private AFPPageSegmentSetup getPageSetupAttachment() {
      return (AFPPageSegmentSetup)this.getExtensionAttachment();
   }

   public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList propertyList) throws FOPException {
      AFPPageSegmentSetup pageSetup = this.getPageSetupAttachment();
      super.processNode(elementName, locator, attlist, propertyList);
      String attr = attlist.getValue("resource-file");
      if (attr != null && attr.length() > 0) {
         pageSetup.setResourceSrc(attr);
      }

   }

   protected ExtensionAttachment instantiateExtensionAttachment() {
      return new AFPPageSegmentSetup(this.getLocalName());
   }

   public static class AFPPageSegmentSetup extends AFPPageSetup {
      private static final long serialVersionUID = 1L;
      private String resourceSrc;

      public AFPPageSegmentSetup(String elementName) {
         super(elementName);
      }

      public String getResourceSrc() {
         return this.resourceSrc;
      }

      public void setResourceSrc(String resourceSrc) {
         this.resourceSrc = resourceSrc.trim();
      }

      public void toSAX(ContentHandler handler) throws SAXException {
         AttributesImpl atts = new AttributesImpl();
         if (this.name != null && this.name.length() > 0) {
            atts.addAttribute("", "name", "name", "CDATA", this.name);
         }

         if (this.value != null && this.value.length() > 0) {
            atts.addAttribute("", "value", "value", "CDATA", this.value);
         }

         if (this.resourceSrc != null && this.resourceSrc.length() > 0) {
            atts.addAttribute("", "resource-file", "resource-file", "CDATA", this.resourceSrc);
         }

         handler.startElement("apache:fop:extensions:afp", this.elementName, this.elementName, atts);
         if (this.content != null && this.content.length() > 0) {
            char[] chars = this.content.toCharArray();
            handler.characters(chars, 0, chars.length);
         }

         handler.endElement("apache:fop:extensions:afp", this.elementName, this.elementName);
      }

      public String toString() {
         return "AFPPageSegmentSetup(element-name=" + this.getElementName() + " name=" + this.getName() + " value=" + this.getValue() + " resource=" + this.getResourceSrc() + ")";
      }
   }
}
