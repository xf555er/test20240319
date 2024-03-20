package org.apache.fop.render.pdf.extensions;

import java.util.Iterator;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.util.GenerationHelperContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class PDFDictionaryAttachment extends PDFExtensionAttachment {
   private static final long serialVersionUID = -5576832955238384505L;
   private PDFDictionaryExtension extension;

   public PDFDictionaryAttachment(PDFDictionaryExtension extension) {
      this.extension = extension;
   }

   public PDFDictionaryExtension getExtension() {
      return this.extension;
   }

   public void toSAX(ContentHandler handler) throws SAXException {
      int pageNumber = 0;
      if (this.extension instanceof PDFPageExtension && handler instanceof GenerationHelperContentHandler) {
         Object context = ((GenerationHelperContentHandler)handler).getContentHandlerContext();
         if (context instanceof IFContext) {
            int pageIndex = ((IFContext)context).getPageIndex();
            if (pageIndex >= 0 && ((PDFPageExtension)this.extension).matchesPageNumber(pageIndex + 1)) {
               pageNumber = pageIndex + 1;
            } else {
               pageNumber = -1;
            }
         }
      }

      if (pageNumber >= 0) {
         this.toSAX(handler, this.extension);
      }

   }

   private void toSAX(ContentHandler handler, PDFDictionaryExtension dictionary) throws SAXException {
      AttributesImpl attributes = new AttributesImpl();
      String ln = dictionary.getElementName();
      String qn = "pdf:" + ln;
      attributes = extractIFAttributes(attributes, dictionary);
      handler.startElement("apache:fop:extensions:pdf", ln, qn, attributes);
      Iterator var6 = dictionary.getEntries().iterator();

      while(var6.hasNext()) {
         PDFCollectionEntryExtension entry = (PDFCollectionEntryExtension)var6.next();
         this.toSAX(handler, entry);
      }

      handler.endElement("apache:fop:extensions:pdf", ln, qn);
   }

   private void toSAX(ContentHandler handler, PDFArrayExtension array) throws SAXException {
      AttributesImpl attributes = new AttributesImpl();
      String ln = array.getElementName();
      String qn = "pdf:" + ln;
      attributes = extractIFAttributes(attributes, array);
      handler.startElement("apache:fop:extensions:pdf", ln, qn, attributes);
      Iterator var6 = array.getEntries().iterator();

      while(var6.hasNext()) {
         PDFCollectionEntryExtension entry = (PDFCollectionEntryExtension)var6.next();
         this.toSAX(handler, entry);
      }

      handler.endElement("apache:fop:extensions:pdf", ln, qn);
   }

   private void toSAX(ContentHandler handler, PDFCollectionEntryExtension entry) throws SAXException {
      if (entry instanceof PDFDictionaryExtension) {
         this.toSAX(handler, (PDFDictionaryExtension)entry);
      } else if (entry instanceof PDFArrayExtension) {
         this.toSAX(handler, (PDFArrayExtension)entry);
      } else {
         AttributesImpl attributes = new AttributesImpl();
         String ln = entry.getElementName();
         String qn = "pdf:" + ln;
         attributes = extractIFAttributes(attributes, entry);
         handler.startElement("apache:fop:extensions:pdf", ln, qn, attributes);
         if (!(entry instanceof PDFReferenceExtension)) {
            char[] characters = entry.getValueAsXMLEscapedString().toCharArray();
            if (characters.length > 0) {
               handler.characters(characters, 0, characters.length);
            }
         }

         handler.endElement("apache:fop:extensions:pdf", ln, qn);
      }

   }

   private static AttributesImpl extractIFAttributes(AttributesImpl attributes, PDFDictionaryExtension dictionary) {
      PDFDictionaryType type = dictionary.getDictionaryType();
      String keyName;
      String key;
      if (dictionary.usesIDAttribute()) {
         keyName = "id";
         key = dictionary.getProperty("id");
         if (key != null) {
            attributes.addAttribute("", keyName, keyName, "ID", key);
         }
      }

      if (type == PDFDictionaryType.Action) {
         keyName = "type";
         key = dictionary.getProperty("type");
         if (key != null) {
            attributes.addAttribute("", keyName, keyName, "CDATA", key);
         }
      } else if (type == PDFDictionaryType.Page) {
         keyName = "page-numbers";
         key = dictionary.getProperty(keyName);
         if (key != null) {
            attributes.addAttribute("", keyName, keyName, "CDATA", key);
         }
      } else if (type == PDFDictionaryType.Dictionary) {
         keyName = "key";
         key = dictionary.getKey();
         if (key != null) {
            attributes.addAttribute("", keyName, keyName, "CDATA", key);
         }
      }

      return attributes;
   }

   private static AttributesImpl extractIFAttributes(AttributesImpl attributes, PDFArrayExtension array) {
      String keyName = "key";
      String key = array.getKey();
      if (key != null) {
         attributes.addAttribute("", keyName, keyName, "CDATA", key);
      }

      return attributes;
   }

   private static AttributesImpl extractIFAttributes(AttributesImpl attributes, PDFCollectionEntryExtension entry) {
      String keyName = "key";
      String key = entry.getKey();
      if (key != null) {
         attributes.addAttribute("", keyName, keyName, "CDATA", key);
      }

      if (entry instanceof PDFReferenceExtension) {
         String refid = ((PDFReferenceExtension)entry).getReferenceId();
         if (refid != null) {
            String refidName = "refid";
            attributes.addAttribute("", refidName, refidName, "IDREF", refid);
         }
      }

      return attributes;
   }
}
