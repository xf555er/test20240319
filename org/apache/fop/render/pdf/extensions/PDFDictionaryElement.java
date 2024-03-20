package org.apache.fop.render.pdf.extensions;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class PDFDictionaryElement extends PDFCollectionEntryElement {
   public static final String ATT_ID = "id";

   PDFDictionaryElement(FONode parent, PDFDictionaryType type) {
      super(parent, PDFObjectType.Dictionary, createExtension(type));
   }

   private static PDFDictionaryExtension createExtension(PDFDictionaryType type) {
      if (type == PDFDictionaryType.Action) {
         return new PDFActionExtension();
      } else if (type == PDFDictionaryType.Catalog) {
         return new PDFCatalogExtension();
      } else if (type == PDFDictionaryType.Layer) {
         return new PDFLayerExtension();
      } else if (type == PDFDictionaryType.Navigator) {
         return new PDFNavigatorExtension();
      } else if (type == PDFDictionaryType.Page) {
         return new PDFPageExtension();
      } else {
         return (PDFDictionaryExtension)(type == PDFDictionaryType.Info ? new PDFDocumentInformationExtension() : new PDFDictionaryExtension(type));
      }
   }

   public PDFDictionaryExtension getDictionaryExtension() {
      PDFCollectionEntryExtension extension = this.getExtension();

      assert extension instanceof PDFDictionaryExtension;

      return (PDFDictionaryExtension)extension;
   }

   public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList propertyList) throws FOPException {
      PDFDictionaryExtension extension = this.getDictionaryExtension();
      String key;
      if (extension.usesIDAttribute()) {
         key = attlist.getValue("id");
         if (key != null) {
            extension.setProperty("id", key);
         }
      }

      if (extension.getDictionaryType() == PDFDictionaryType.Dictionary) {
         key = attlist.getValue("key");
         if (key == null) {
            if (this.parent instanceof PDFDictionaryElement) {
               this.missingPropertyError("key");
            }
         } else if (key.length() == 0) {
            this.invalidPropertyValueError("key", key, (Exception)null);
         } else {
            extension.setKey(key);
         }
      }

   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      String localName = this.getLocalName();
      if (!localName.equals("action") && !localName.equals("catalog") && !localName.equals("layer") && !localName.equals("navigator") && !localName.equals("page") && !localName.equals("info") && !localName.equals("vt") && !localName.equals("pagepiece")) {
         if (!localName.equals("dictionary")) {
            throw new IllegalStateException("unknown name: " + localName);
         }

         if (!PDFDictionaryType.hasValueOfElementName(this.parent.getLocalName()) && !PDFObjectType.Array.elementName().equals(this.parent.getLocalName())) {
            this.invalidChildError(this.getLocator(), this.parent.getName(), this.getNamespaceURI(), this.getName(), (String)null);
         }
      }

   }

   protected void addChildNode(FONode child) throws FOPException {
      PDFDictionaryExtension extension = this.getDictionaryExtension();
      if (child instanceof PDFDictionaryElement) {
         PDFDictionaryExtension entry = ((PDFDictionaryElement)child).getDictionaryExtension();
         if (entry.getDictionaryType() == PDFDictionaryType.Dictionary) {
            extension.addEntry(entry);
         }
      } else if (child instanceof PDFCollectionEntryElement) {
         PDFCollectionEntryExtension entry = ((PDFCollectionEntryElement)child).getExtension();
         extension.addEntry(entry);
      }

   }

   public void endOfNode() throws FOPException {
      super.endOfNode();
   }

   public String getLocalName() {
      PDFDictionaryExtension extension = this.getDictionaryExtension();
      return extension.getDictionaryType().elementName();
   }

   protected ExtensionAttachment instantiateExtensionAttachment() {
      return new PDFDictionaryAttachment(this.getDictionaryExtension());
   }
}
