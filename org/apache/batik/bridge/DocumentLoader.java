package org.apache.batik.bridge;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDocumentFactory;
import org.apache.batik.dom.util.DocumentDescriptor;
import org.apache.batik.util.CleanerThread;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

public class DocumentLoader {
   protected SVGDocumentFactory documentFactory;
   protected HashMap cacheMap = new HashMap();
   protected UserAgent userAgent;

   protected DocumentLoader() {
   }

   public DocumentLoader(UserAgent userAgent) {
      this.userAgent = userAgent;
      this.documentFactory = new SAXSVGDocumentFactory(userAgent.getXMLParserClassName(), true);
      this.documentFactory.setValidating(userAgent.isXMLParserValidating());
   }

   public Document checkCache(String uri) {
      int n = uri.lastIndexOf(47);
      if (n == -1) {
         n = 0;
      }

      n = uri.indexOf(35, n);
      if (n != -1) {
         uri = uri.substring(0, n);
      }

      DocumentState state;
      synchronized(this.cacheMap) {
         state = (DocumentState)this.cacheMap.get(uri);
      }

      return state != null ? state.getDocument() : null;
   }

   public Document loadDocument(String uri) throws IOException {
      Document ret = this.checkCache(uri);
      if (ret != null) {
         return ret;
      } else {
         SVGDocument document = this.documentFactory.createSVGDocument(uri);
         DocumentDescriptor desc = this.documentFactory.getDocumentDescriptor();
         DocumentState state = new DocumentState(uri, document, desc);
         synchronized(this.cacheMap) {
            this.cacheMap.put(uri, state);
         }

         return state.getDocument();
      }
   }

   public Document loadDocument(String uri, InputStream is) throws IOException {
      Document ret = this.checkCache(uri);
      if (ret != null) {
         return ret;
      } else {
         SVGDocument document = this.documentFactory.createSVGDocument(uri, is);
         DocumentDescriptor desc = this.documentFactory.getDocumentDescriptor();
         DocumentState state = new DocumentState(uri, document, desc);
         synchronized(this.cacheMap) {
            this.cacheMap.put(uri, state);
         }

         return state.getDocument();
      }
   }

   public UserAgent getUserAgent() {
      return this.userAgent;
   }

   public void dispose() {
      synchronized(this.cacheMap) {
         this.cacheMap.clear();
      }
   }

   public int getLineNumber(Element e) {
      String uri = ((SVGDocument)e.getOwnerDocument()).getURL();
      DocumentState state;
      synchronized(this.cacheMap) {
         state = (DocumentState)this.cacheMap.get(uri);
      }

      return state == null ? -1 : state.desc.getLocationLine(e);
   }

   private class DocumentState extends CleanerThread.SoftReferenceCleared {
      private String uri;
      private DocumentDescriptor desc;

      public DocumentState(String uri, Document document, DocumentDescriptor desc) {
         super(document);
         this.uri = uri;
         this.desc = desc;
      }

      public void cleared() {
         synchronized(DocumentLoader.this.cacheMap) {
            DocumentLoader.this.cacheMap.remove(this.uri);
         }
      }

      public DocumentDescriptor getDocumentDescriptor() {
         return this.desc;
      }

      public String getURI() {
         return this.uri;
      }

      public Document getDocument() {
         return (Document)this.get();
      }
   }
}
