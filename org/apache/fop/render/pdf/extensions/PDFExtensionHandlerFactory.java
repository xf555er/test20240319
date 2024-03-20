package org.apache.fop.render.pdf.extensions;

import org.apache.fop.util.ContentHandlerFactory;
import org.xml.sax.ContentHandler;

public class PDFExtensionHandlerFactory implements ContentHandlerFactory {
   private static final String[] NAMESPACES = new String[]{"apache:fop:extensions:pdf"};

   public String[] getSupportedNamespaces() {
      return NAMESPACES;
   }

   public ContentHandler createContentHandler() {
      return new PDFExtensionHandler();
   }
}
