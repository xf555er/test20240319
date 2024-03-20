package org.apache.fop.render.bitmap;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.render.RendererConfig;

public final class PNGRendererConfig extends BitmapRendererConfig {
   private PNGRendererConfig(DefaultFontConfig fontConfig) {
      super(fontConfig);
   }

   // $FF: synthetic method
   PNGRendererConfig(DefaultFontConfig x0, Object x1) {
      this(x0);
   }

   public static class PNGRendererConfigParser implements RendererConfig.RendererConfigParser {
      public PNGRendererConfig build(FOUserAgent userAgent, Configuration cfg) throws FOPException {
         return new PNGRendererConfig((new DefaultFontConfig.DefaultFontConfigParser()).parse(cfg, userAgent.validateStrictly(), new FontEventAdapter(userAgent.getEventBroadcaster())));
      }

      public String getMimeType() {
         return "image/png";
      }
   }
}
