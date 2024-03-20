package org.apache.fop.render.pdf;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.DefaultRendererConfigurator;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.intermediate.IFDocumentHandler;

public class PDFRendererConfigurator extends DefaultRendererConfigurator {
   public PDFRendererConfigurator(FOUserAgent userAgent, RendererConfig.RendererConfigParser rendererConfigParser) {
      super(userAgent, rendererConfigParser);
   }

   public void configure(IFDocumentHandler documentHandler) throws FOPException {
      ((PDFDocumentHandler)documentHandler).mergeRendererOptionsConfig(((PDFRendererConfig)this.getRendererConfig(documentHandler)).getConfigOptions());
   }
}
