package org.apache.fop.render.pdf.extensions;

import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.util.ContentHandlerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

public class PDFExtensionHandler extends DefaultHandler implements ContentHandlerFactory.ObjectSource {
   protected static final Log log = LogFactory.getLog(PDFExtensionHandler.class);
   private PDFExtensionAttachment returnedObject;
   private ContentHandlerFactory.ObjectBuiltListener listener;
   private Attributes lastAttributes;
   private Stack collections = new Stack();
   private boolean captureContent;
   private StringBuffer characters;

   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if ("apache:fop:extensions:pdf".equals(uri)) {
         if (localName.equals("embedded-file")) {
            this.lastAttributes = new AttributesImpl(attributes);
         } else {
            String key;
            String refid;
            if (PDFDictionaryType.Action.elementName().equals(localName)) {
               PDFActionExtension action = new PDFActionExtension();
               key = attributes.getValue("id");
               if (key != null) {
                  action.setProperty("id", key);
               }

               refid = attributes.getValue("type");
               if (refid != null) {
                  action.setProperty("type", refid);
               }

               this.collections.push(action);
            } else if (PDFObjectType.Array.elementName().equals(localName)) {
               PDFArrayExtension array = new PDFArrayExtension();
               key = attributes.getValue("key");
               if (key != null) {
                  array.setKey(key);
               }

               this.collections.push(array);
            } else if (PDFDictionaryType.Catalog.elementName().equals(localName)) {
               PDFCatalogExtension catalog = new PDFCatalogExtension();
               this.collections.push(catalog);
            } else if (PDFDictionaryType.Dictionary.elementName().equals(localName)) {
               PDFDictionaryExtension dictionary = new PDFDictionaryExtension();
               key = attributes.getValue("key");
               if (key != null) {
                  dictionary.setKey(key);
               }

               this.collections.push(dictionary);
            } else if (PDFDictionaryType.Layer.elementName().equals(localName)) {
               PDFLayerExtension layer = new PDFLayerExtension();
               key = attributes.getValue("id");
               if (key != null) {
                  layer.setProperty("id", key);
               }

               this.collections.push(layer);
            } else if (PDFDictionaryType.Navigator.elementName().equals(localName)) {
               PDFNavigatorExtension navigator = new PDFNavigatorExtension();
               key = attributes.getValue("id");
               if (key != null) {
                  navigator.setProperty("id", key);
               }

               this.collections.push(navigator);
            } else if (PDFDictionaryType.Page.elementName().equals(localName)) {
               PDFPageExtension page = new PDFPageExtension();
               key = attributes.getValue("page-numbers");
               if (key != null) {
                  page.setProperty("page-numbers", key);
               }

               this.collections.push(page);
            } else if (PDFDictionaryType.Info.elementName().equals(localName)) {
               PDFDocumentInformationExtension info = new PDFDocumentInformationExtension();
               this.collections.push(info);
            } else if (PDFDictionaryType.VT.elementName().equals(localName)) {
               PDFVTExtension dictionary = new PDFVTExtension();
               this.collections.push(dictionary);
            } else if (PDFDictionaryType.PagePiece.elementName().equals(localName)) {
               PDFPagePieceExtension dictionary = new PDFPagePieceExtension();
               this.collections.push(dictionary);
            } else {
               if (!PDFObjectType.hasValueOfElementName(localName)) {
                  throw new SAXException("Unhandled element " + localName + " in namespace: " + uri);
               }

               Object entry;
               if (PDFObjectType.Reference.elementName().equals(localName)) {
                  entry = new PDFReferenceExtension();
               } else {
                  entry = new PDFCollectionEntryExtension(PDFObjectType.valueOfElementName(localName));
               }

               key = attributes.getValue("key");
               if (key != null) {
                  ((PDFCollectionEntryExtension)entry).setKey(key);
               }

               if (entry instanceof PDFReferenceExtension) {
                  refid = attributes.getValue("refid");
                  if (refid != null) {
                     ((PDFReferenceExtension)entry).setReferenceId(refid);
                  }
               }

               if (!this.collections.empty()) {
                  PDFCollectionExtension collection = (PDFCollectionExtension)this.collections.peek();
                  collection.addEntry((PDFCollectionEntryExtension)entry);
                  if (!(entry instanceof PDFReferenceExtension)) {
                     this.captureContent = true;
                  }
               }
            }
         }
      } else {
         log.warn("Unhandled element " + localName + " in namespace: " + uri);
      }

   }

   public void characters(char[] data, int start, int length) throws SAXException {
      if (this.captureContent) {
         if (this.characters == null) {
            this.characters = new StringBuffer(length < 16 ? 16 : length);
         }

         this.characters.append(data, start, length);
      }

   }

   public void endElement(String uri, String localName, String qName) throws SAXException {
      if ("apache:fop:extensions:pdf".equals(uri)) {
         if ("embedded-file".equals(localName)) {
            String name = this.lastAttributes.getValue("filename");
            String src = this.lastAttributes.getValue("src");
            String desc = this.lastAttributes.getValue("description");
            this.lastAttributes = null;
            this.returnedObject = new PDFEmbeddedFileAttachment(name, src, desc);
         } else {
            PDFCollectionExtension collectionOuter;
            if (PDFDictionaryType.hasValueOfElementName(localName)) {
               if (this.collections.empty() || !(this.collections.peek() instanceof PDFDictionaryExtension)) {
                  throw new SAXException(new IllegalStateException("collections stack is empty or not a dictionary"));
               }

               PDFDictionaryExtension dictionary = (PDFDictionaryExtension)this.collections.pop();
               if (!this.collections.empty()) {
                  collectionOuter = (PDFCollectionExtension)this.collections.peek();
                  collectionOuter.addEntry(dictionary);
               } else {
                  if (dictionary.getDictionaryType() == PDFDictionaryType.Dictionary) {
                     throw new SAXException(new IllegalStateException("generic dictionary not permitted at outer level"));
                  }

                  this.returnedObject = new PDFDictionaryAttachment(dictionary);
               }
            } else if (PDFObjectType.Array.elementName().equals(localName)) {
               if (this.collections.empty() || !(this.collections.peek() instanceof PDFArrayExtension)) {
                  throw new SAXException(new IllegalStateException("collections stack is empty or not an array"));
               }

               PDFArrayExtension array = (PDFArrayExtension)this.collections.pop();
               if (this.collections.empty()) {
                  throw new SAXException(new IllegalStateException("array not permitted at outer level"));
               }

               collectionOuter = (PDFCollectionExtension)this.collections.peek();
               collectionOuter.addEntry(array);
            } else if (PDFObjectType.hasValueOfElementName(localName)) {
               if (this.collections.empty()) {
                  throw new SAXException(new IllegalStateException("entry not permitted at outer level"));
               }

               PDFCollectionExtension collection = (PDFCollectionExtension)this.collections.peek();
               PDFCollectionEntryExtension entry = collection.getLastEntry();
               if (entry == null) {
                  throw new SAXException(new IllegalStateException("no current entry"));
               }

               if (this.characters != null) {
                  entry.setValue(this.characters.toString());
                  this.characters = null;
               }
            }
         }
      }

      this.captureContent = false;
   }

   public void endDocument() throws SAXException {
      if (this.listener != null) {
         this.listener.notifyObjectBuilt(this.getObject());
      }

   }

   public Object getObject() {
      return this.returnedObject;
   }

   public void setObjectBuiltListener(ContentHandlerFactory.ObjectBuiltListener listener) {
      this.listener = listener;
   }
}
