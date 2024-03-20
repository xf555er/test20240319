package org.apache.fop.render.bitmap;

import org.apache.fop.render.intermediate.AbstractIFDocumentHandlerMaker;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;

public class PNGDocumentHandlerMaker extends AbstractIFDocumentHandlerMaker {
   private static final String[] MIMES = new String[]{"image/png"};

   public IFDocumentHandler makeIFDocumentHandler(IFContext ifContext) {
      return new PNGDocumentHandler(ifContext);
   }

   public boolean needsOutputStream() {
      return true;
   }

   public String[] getSupportedMimeTypes() {
      return MIMES;
   }
}
