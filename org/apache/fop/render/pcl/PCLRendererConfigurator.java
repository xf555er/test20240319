package org.apache.fop.render.pcl;

import java.util.ArrayList;
import java.util.List;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.render.PrintRendererConfigurator;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.java2d.Base14FontCollection;
import org.apache.fop.render.java2d.ConfiguredFontCollection;
import org.apache.fop.render.java2d.InstalledFontCollection;
import org.apache.fop.render.java2d.Java2DFontMetrics;

public class PCLRendererConfigurator extends PrintRendererConfigurator {
   public PCLRendererConfigurator(FOUserAgent userAgent, RendererConfig.RendererConfigParser rendererConfigParser) {
      super(userAgent, rendererConfigParser);
   }

   public void configure(IFDocumentHandler documentHandler) throws FOPException {
      PCLRendererConfig pdfConfig = (PCLRendererConfig)this.getRendererConfig(documentHandler);
      if (pdfConfig != null) {
         PCLDocumentHandler pclDocumentHandler = (PCLDocumentHandler)documentHandler;
         PCLRenderingUtil pclUtil = pclDocumentHandler.getPCLUtil();
         this.configure(pdfConfig, pclUtil);
      }

   }

   private void configure(PCLRendererConfig config, PCLRenderingUtil pclUtil) throws FOPException {
      if (config.getRenderingMode() != null) {
         pclUtil.setRenderingMode(config.getRenderingMode());
      }

      if (config.isDisablePjl() != null) {
         pclUtil.setPJLDisabled(config.isDisablePjl());
      }

      if (config.isTextRendering() != null) {
         pclUtil.setAllTextAsBitmaps(config.isTextRendering());
      }

      if (config.isColorEnabled() != null) {
         pclUtil.setColorEnabled(config.isColorEnabled());
      }

      if (config.isOptimizeResources() != null) {
         pclUtil.setOptimizeResources(config.isOptimizeResources());
      }

   }

   protected List getDefaultFontCollection() {
      List fontCollections = new ArrayList();
      Java2DFontMetrics java2DFontMetrics = new Java2DFontMetrics();
      fontCollections.add(new Base14FontCollection(java2DFontMetrics));
      fontCollections.add(new InstalledFontCollection(java2DFontMetrics));
      return fontCollections;
   }

   protected FontCollection createCollectionFromFontList(InternalResourceResolver resourceResolver, List fontList) {
      return new ConfiguredFontCollection(resourceResolver, fontList, this.userAgent.isComplexScriptFeaturesEnabled());
   }
}
