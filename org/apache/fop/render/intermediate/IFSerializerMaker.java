package org.apache.fop.render.intermediate;

import org.apache.fop.apps.FOUserAgent;

public class IFSerializerMaker extends AbstractIFDocumentHandlerMaker {
   public IFDocumentHandler makeIFDocumentHandler(IFContext ifContext) {
      IFSerializer handler = new IFSerializer(ifContext);
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
      return new String[]{"application/X-fop-intermediate-format"};
   }
}
