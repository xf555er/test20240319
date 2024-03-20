package org.apache.fop.render.txt;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.DefaultRendererConfigurator;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererConfig;

public class TXTRendererConfigurator extends DefaultRendererConfigurator {
   public TXTRendererConfigurator(FOUserAgent userAgent, RendererConfig.RendererConfigParser rendererConfigParser) {
      super(userAgent, rendererConfigParser);
   }

   public void configure(Renderer renderer) throws FOPException {
      TxtRendererConfig config = (TxtRendererConfig)this.getRendererConfig(renderer);
      if (config != null) {
         TXTRenderer txtRenderer = (TXTRenderer)renderer;
         txtRenderer.setEncoding(config.getEncoding());
      }

   }
}
