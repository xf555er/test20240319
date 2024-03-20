package org.apache.fop.render.afp.extensions;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.util.ContentHandlerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

public class AFPExtensionHandler extends DefaultHandler implements ContentHandlerFactory.ObjectSource {
   protected static final Log log = LogFactory.getLog(AFPExtensionHandler.class);
   private StringBuffer content = new StringBuffer();
   private Attributes lastAttributes;
   private AFPExtensionAttachment returnedObject;
   private ContentHandlerFactory.ObjectBuiltListener listener;

   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      boolean handled = false;
      if ("apache:fop:extensions:afp".equals(uri)) {
         this.lastAttributes = new AttributesImpl(attributes);
         handled = true;
         if (!localName.equals("no-operation") && !localName.equals("tag-logical-element") && !localName.equals("include-page-overlay") && !localName.equals("include-page-segment") && !localName.equals("include-form-map") && !localName.equals("invoke-medium-map")) {
            handled = false;
         }
      }

      if (!handled) {
         if ("apache:fop:extensions:afp".equals(uri)) {
            throw new SAXException("Unhandled element " + localName + " in namespace: " + uri);
         }

         log.warn("Unhandled element " + localName + " in namespace: " + uri);
      }

   }

   public void endElement(String uri, String localName, String qName) throws SAXException {
      if ("apache:fop:extensions:afp".equals(uri)) {
         AFPIncludeFormMap pageSetupExtn;
         String name;
         String value;
         if ("include-form-map".equals(localName)) {
            pageSetupExtn = new AFPIncludeFormMap();
            name = this.lastAttributes.getValue("name");
            pageSetupExtn.setName(name);
            value = this.lastAttributes.getValue("src");

            try {
               pageSetupExtn.setSrc(new URI(value));
            } catch (URISyntaxException var9) {
               throw new SAXException("Invalid URI: " + value, var9);
            }

            this.returnedObject = pageSetupExtn;
         } else if ("include-page-overlay".equals(localName)) {
            this.returnedObject = new AFPPageOverlay();
            String name = this.lastAttributes.getValue("name");
            if (name != null) {
               this.returnedObject.setName(name);
            }
         } else {
            String placement;
            if ("include-page-segment".equals(localName)) {
               pageSetupExtn = null;
               AFPPageSegmentElement.AFPPageSegmentSetup pageSetupExtn = new AFPPageSegmentElement.AFPPageSegmentSetup(localName);
               this.returnedObject = pageSetupExtn;
               name = this.lastAttributes.getValue("name");
               if (name != null) {
                  this.returnedObject.setName(name);
               }

               value = this.lastAttributes.getValue("value");
               if (value != null && pageSetupExtn != null) {
                  pageSetupExtn.setValue(value);
               }

               placement = this.lastAttributes.getValue("resource-file");
               if (placement != null && pageSetupExtn != null) {
                  pageSetupExtn.setResourceSrc(placement);
               }

               if (this.content.length() > 0 && pageSetupExtn != null) {
                  pageSetupExtn.setContent(this.content.toString());
                  this.content.setLength(0);
               }
            } else {
               AFPPageSetup pageSetupExtn = null;
               if ("invoke-medium-map".equals(localName)) {
                  this.returnedObject = new AFPInvokeMediumMap();
               } else {
                  pageSetupExtn = new AFPPageSetup(localName);
                  this.returnedObject = pageSetupExtn;
               }

               name = this.lastAttributes.getValue("name");
               if (name != null) {
                  this.returnedObject.setName(name);
               }

               value = this.lastAttributes.getValue("value");
               if (value != null && pageSetupExtn != null) {
                  pageSetupExtn.setValue(value);
               }

               placement = this.lastAttributes.getValue("placement");
               if (placement != null && placement.length() > 0) {
                  pageSetupExtn.setPlacement(ExtensionPlacement.fromXMLValue(placement));
               }

               String encoding = this.lastAttributes.getValue("encoding");
               if (encoding != null && pageSetupExtn != null) {
                  pageSetupExtn.setEncoding(Integer.parseInt(encoding));
               }

               if (this.content.length() > 0 && pageSetupExtn != null) {
                  pageSetupExtn.setContent(this.content.toString());
                  this.content.setLength(0);
               }
            }
         }
      }

   }

   public void characters(char[] ch, int start, int length) throws SAXException {
      this.content.append(ch, start, length);
   }

   public void endDocument() throws SAXException {
      if (this.listener != null) {
         this.listener.notifyObjectBuilt(this.getObject());
      }

   }

   public Object getObject() {
      return this.returnedObject;
   }

   public void setObjectBuiltListener(ContentHandlerFactory.ObjectBuiltListener listen) {
      this.listener = listen;
   }
}
