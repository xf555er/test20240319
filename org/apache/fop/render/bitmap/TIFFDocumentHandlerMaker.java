package org.apache.fop.render.bitmap;

import org.apache.fop.render.intermediate.AbstractIFDocumentHandlerMaker;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;

public class TIFFDocumentHandlerMaker extends AbstractIFDocumentHandlerMaker {
   private static final String[] MIMES = new String[]{"image/tiff"};

   public IFDocumentHandler makeIFDocumentHandler(IFContext ifContext) {
      return new TIFFDocumentHandler(ifContext);
   }

   public boolean needsOutputStream() {
      return true;
   }

   public String[] getSupportedMimeTypes() {
      return MIMES;
   }
}
