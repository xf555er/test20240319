package org.apache.fop.render.ps;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.DefaultRendererConfigurator;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.intermediate.IFDocumentHandler;

public class PSRendererConfigurator extends DefaultRendererConfigurator {
   public PSRendererConfigurator(FOUserAgent userAgent, RendererConfig.RendererConfigParser rendererConfigParser) {
      super(userAgent, rendererConfigParser);
   }

   private void configure(PSRenderingUtil psUtil, PSRendererConfig psConfig) {
      if (psConfig.isAutoRotateLandscape() != null) {
         psUtil.setAutoRotateLandscape(psConfig.isAutoRotateLandscape());
      }

      if (psConfig.getLanguageLevel() != null) {
         psUtil.setLanguageLevel(psConfig.getLanguageLevel());
      }

      if (psConfig.isOptimizeResources() != null) {
         psUtil.setOptimizeResources(psConfig.isOptimizeResources());
      }

      if (psConfig.isSafeSetPageDevice() != null) {
         psUtil.setSafeSetPageDevice(psConfig.isSafeSetPageDevice());
      }

      if (psConfig.isDscComplianceEnabled() != null) {
         psUtil.setDSCComplianceEnabled(psConfig.isDscComplianceEnabled());
      }

      if (psConfig.getRenderingMode() != null) {
         psUtil.setRenderingMode(psConfig.getRenderingMode());
      }

      if (psConfig.isAcrobatDownsample() != null) {
         psUtil.setAcrobatDownsample(psConfig.isAcrobatDownsample());
      }

   }

   public void configure(IFDocumentHandler documentHandler) throws FOPException {
      PSRendererConfig psConfig = (PSRendererConfig)this.getRendererConfig(documentHandler);
      if (psConfig != null) {
         PSDocumentHandler psDocumentHandler = (PSDocumentHandler)documentHandler;
         PSRenderingUtil psUtil = psDocumentHandler.getPSUtil();
         this.configure(psUtil, psConfig);
      }

   }
}
