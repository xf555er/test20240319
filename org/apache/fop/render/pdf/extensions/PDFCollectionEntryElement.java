package org.apache.fop.render.pdf.extensions;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class PDFCollectionEntryElement extends AbstractPDFExtensionElement {
   public static final String ATT_KEY = "key";
   private PDFCollectionEntryExtension extension;
   private StringBuffer characters;

   PDFCollectionEntryElement(FONode parent, PDFObjectType type, PDFCollectionEntryExtension extension) {
      super(parent);
      this.extension = extension;
   }

   PDFCollectionEntryElement(FONode parent, PDFObjectType type) {
      this(parent, type, createExtension(type));
   }

   private static PDFCollectionEntryExtension createExtension(PDFObjectType type) {
      return (PDFCollectionEntryExtension)(type == PDFObjectType.Reference ? new PDFReferenceExtension() : new PDFCollectionEntryExtension(type));
   }

   public PDFCollectionEntryExtension getExtension() {
      return this.extension;
   }

   public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList propertyList) throws FOPException {
      if (this.parent instanceof PDFDictionaryElement) {
         String key = attlist.getValue("key");
         if (key == null) {
            this.missingPropertyError("key");
         } else if (key.length() == 0) {
            this.invalidPropertyValueError("key", key, (Exception)null);
         } else {
            this.extension.setKey(key);
         }
      }

   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      if (this.parent instanceof PDFDictionaryElement && !PDFDictionaryType.hasValueOfElementName(this.parent.getLocalName())) {
         this.invalidChildError(this.getLocator(), this.parent.getName(), this.getNamespaceURI(), this.getName(), (String)null);
      }

   }

   protected void characters(char[] data, int start, int length, PropertyList pList, Locator locator) throws FOPException {
      if (this.capturePCData(this.extension.getType())) {
         if (this.characters == null) {
            this.characters = new StringBuffer(length < 16 ? 16 : length);
         }

         this.characters.append(data, start, length);
      }

   }

   private boolean capturePCData(PDFObjectType type) {
      if (type == PDFObjectType.Array) {
         return false;
      } else if (type == PDFObjectType.Dictionary) {
         return false;
      } else {
         return type != PDFObjectType.Reference;
      }
   }

   public void endOfNode() throws FOPException {
      if (this.capturePCData(this.extension.getType())) {
         String value;
         if (this.extension.getType() == PDFObjectType.Boolean) {
            value = this.characters != null ? this.characters.toString() : "";
            if (!value.equals("true") && !value.equals("false")) {
               this.invalidPropertyValueError("<value>", value, (Exception)null);
            }

            this.extension.setValue(Boolean.valueOf(value));
         } else if (this.extension.getType() == PDFObjectType.Name) {
            value = this.characters != null ? this.characters.toString() : "";
            if (value.length() == 0) {
               this.invalidPropertyValueError("<value>", value, (Exception)null);
            }

            this.extension.setValue(value);
         } else if (this.extension.getType() == PDFObjectType.Number) {
            value = this.characters != null ? this.characters.toString() : "";

            try {
               double d = Double.parseDouble(value);
               if (Math.abs(Math.floor(d) - d) < 1.0E-10) {
                  this.extension.setValue((long)d);
               } else {
                  this.extension.setValue(d);
               }
            } catch (NumberFormatException var4) {
               this.invalidPropertyValueError("<value>", value, (Exception)null);
            }
         } else if (this.extension.getType() == PDFObjectType.String) {
            value = this.characters != null ? this.characters.toString() : "";
            this.extension.setValue(value);
         }
      }

      super.endOfNode();
   }

   public String getLocalName() {
      return this.extension.getType().elementName();
   }
}
