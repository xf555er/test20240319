package org.apache.fop.accessibility;

import java.util.Locale;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public final class StructureTree2SAXEventAdapter implements StructureTreeEventHandler {
   private final ContentHandler contentHandler;

   private StructureTree2SAXEventAdapter(ContentHandler currentContentHandler) {
      this.contentHandler = currentContentHandler;
   }

   public static StructureTreeEventHandler newInstance(ContentHandler contentHandler) {
      return new StructureTree2SAXEventAdapter(contentHandler);
   }

   public void startPageSequence(Locale locale, String role) {
      try {
         AttributesImpl attributes = new AttributesImpl();
         if (role != null) {
            attributes.addAttribute("", "type", "type", "CDATA", role);
         }

         this.contentHandler.startPrefixMapping("foi", "http://xmlgraphics.apache.org/fop/internal");
         this.contentHandler.startPrefixMapping("fox", "http://xmlgraphics.apache.org/fop/extensions");
         this.contentHandler.startElement("http://xmlgraphics.apache.org/fop/intermediate", "structure-tree", "structure-tree", attributes);
      } catch (SAXException var4) {
         throw new RuntimeException(var4);
      }
   }

   public void endPageSequence() {
      try {
         this.contentHandler.endElement("http://xmlgraphics.apache.org/fop/intermediate", "structure-tree", "structure-tree");
         this.contentHandler.endPrefixMapping("fox");
         this.contentHandler.endPrefixMapping("foi");
      } catch (SAXException var2) {
         throw new RuntimeException(var2);
      }
   }

   public StructureTreeElement startNode(String name, Attributes attributes, StructureTreeElement parent) {
      try {
         if (name.equals("#PCDATA")) {
            name = "marked-content";
            this.contentHandler.startElement("http://xmlgraphics.apache.org/fop/intermediate", name, name, attributes);
         } else {
            this.contentHandler.startElement("http://www.w3.org/1999/XSL/Format", name, "fo:" + name, attributes);
         }

         return null;
      } catch (SAXException var5) {
         throw new RuntimeException(var5);
      }
   }

   public void endNode(String name) {
      try {
         this.contentHandler.endElement("http://www.w3.org/1999/XSL/Format", name, "fo:" + name);
      } catch (SAXException var3) {
         throw new RuntimeException(var3);
      }
   }

   public StructureTreeElement startImageNode(String name, Attributes attributes, StructureTreeElement parent) {
      return this.startNode(name, attributes, (StructureTreeElement)null);
   }

   public StructureTreeElement startReferencedNode(String name, Attributes attributes, StructureTreeElement parent) {
      return this.startNode(name, attributes, (StructureTreeElement)null);
   }
}
