package org.apache.fop.render.pcl;

import org.apache.fop.render.intermediate.AbstractIFDocumentHandlerMaker;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;

public class PCLDocumentHandlerMaker extends AbstractIFDocumentHandlerMaker {
   private static final String[] MIMES = new String[]{"application/x-pcl", "application/vnd.hp-PCL"};

   public IFDocumentHandler makeIFDocumentHandler(IFContext ifContext) {
      return new PCLDocumentHandler(ifContext);
   }

   public boolean needsOutputStream() {
      return true;
   }

   public String[] getSupportedMimeTypes() {
      return MIMES;
   }
}
