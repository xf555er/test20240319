package org.apache.fop.render.java2d;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.DefaultRendererConfigurator;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererConfig;

public class Java2DRendererConfigurator extends DefaultRendererConfigurator {
   public Java2DRendererConfigurator(FOUserAgent userAgent, RendererConfig.RendererConfigParser rendererConfigParser) {
      super(userAgent, rendererConfigParser);
   }

   public void configure(Renderer renderer) throws FOPException {
      Java2DRendererConfig config = (Java2DRendererConfig)this.getRendererConfig(renderer);
      if (config != null) {
         Java2DRenderer java2dRenderer = (Java2DRenderer)renderer;
         if (config.isPageBackgroundTransparent() != null) {
            java2dRenderer.setTransparentPageBackground(config.isPageBackgroundTransparent());
         }

         super.configure(renderer);
      }

   }
}
