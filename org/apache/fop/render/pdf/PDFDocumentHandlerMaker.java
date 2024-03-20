package org.apache.fop.render.pdf;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.intermediate.AbstractIFDocumentHandlerMaker;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;

public class PDFDocumentHandlerMaker extends AbstractIFDocumentHandlerMaker {
   private static final String[] MIMES = new String[]{"application/pdf"};

   public IFDocumentHandler makeIFDocumentHandler(IFContext ifContext) {
      PDFDocumentHandler handler = new PDFDocumentHandler(ifContext);
      FOUserAgent ua = ifContext.getUserAgent();
      if (ua.isAccessibilityEnabled()) {
         ua.setStructureTreeEventHandler(handler.getStructureTreeEventHandler());
      }

      return handler;
   }

   public boolean needsOutputStream() {
      return true;
   }

   public String[] getSupportedMimeTypes() {
      return MIMES;
   }
}
