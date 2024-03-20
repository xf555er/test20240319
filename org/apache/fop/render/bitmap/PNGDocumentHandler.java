package org.apache.fop.render.bitmap;

import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;

public class PNGDocumentHandler extends AbstractBitmapDocumentHandler {
   PNGDocumentHandler(IFContext context) {
      super(context);
   }

   public String getMimeType() {
      return "image/png";
   }

   public String getDefaultExtension() {
      return "png";
   }

   public IFDocumentHandlerConfigurator getConfigurator() {
      return new BitmapRendererConfigurator(this.getUserAgent(), new PNGRendererConfig.PNGRendererConfigParser());
   }
}
