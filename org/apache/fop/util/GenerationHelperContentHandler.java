package org.apache.fop.util;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class GenerationHelperContentHandler extends DelegatingContentHandler {
   private static final Attributes EMPTY_ATTS = new AttributesImpl();
   private String mainNamespace;
   private Object contentHandlerContext;

   public GenerationHelperContentHandler(ContentHandler handler, String mainNamespace, Object contentHandlerContext) {
      super(handler);
      this.mainNamespace = mainNamespace;
      this.contentHandlerContext = contentHandlerContext;
   }

   public String getMainNamespace() {
      return this.mainNamespace;
   }

   public void setMainNamespace(String namespaceURI) {
      this.mainNamespace = namespaceURI;
   }

   public Object getContentHandlerContext() {
      return this.contentHandlerContext;
   }

   public void startElement(String localName, Attributes atts) throws SAXException {
      this.getDelegateContentHandler().startElement(this.getMainNamespace(), localName, localName, atts);
   }

   public void startElement(String localName) throws SAXException {
      this.startElement(localName, EMPTY_ATTS);
   }

   public void startElement(org.apache.xmlgraphics.util.QName qName, Attributes atts) throws SAXException {
      this.getDelegateContentHandler().startElement(qName.getNamespaceURI(), qName.getLocalName(), qName.getQName(), atts);
   }

   public void startElement(org.apache.xmlgraphics.util.QName qName) throws SAXException {
      this.startElement(qName, EMPTY_ATTS);
   }

   public void endElement(String localName) throws SAXException {
      this.getDelegateContentHandler().endElement(this.getMainNamespace(), localName, localName);
   }

   public void endElement(org.apache.xmlgraphics.util.QName qName) throws SAXException {
      this.getDelegateContentHandler().endElement(qName.getNamespaceURI(), qName.getLocalName(), qName.getQName());
   }

   public void element(String localName, Attributes atts) throws SAXException {
      this.getDelegateContentHandler().startElement(this.getMainNamespace(), localName, localName, atts);
      this.getDelegateContentHandler().endElement(this.getMainNamespace(), localName, localName);
   }

   public void element(org.apache.xmlgraphics.util.QName qName, Attributes atts) throws SAXException {
      this.getDelegateContentHandler().startElement(qName.getNamespaceURI(), qName.getLocalName(), qName.getQName(), atts);
      this.getDelegateContentHandler().endElement(qName.getNamespaceURI(), qName.getLocalName(), qName.getQName());
   }
}
