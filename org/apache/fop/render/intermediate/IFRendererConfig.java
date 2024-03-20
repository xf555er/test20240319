package org.apache.fop.render.intermediate;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.FontConfig;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.render.RendererConfig;

public final class IFRendererConfig implements RendererConfig {
   private final DefaultFontConfig fontConfig;

   private IFRendererConfig(DefaultFontConfig fontConfig) {
      this.fontConfig = fontConfig;
   }

   public FontConfig getFontInfoConfig() {
      return this.fontConfig;
   }

   // $FF: synthetic method
   IFRendererConfig(DefaultFontConfig x0, Object x1) {
      this(x0);
   }

   public static final class IFRendererConfigParser implements RendererConfig.RendererConfigParser {
      public RendererConfig build(FOUserAgent userAgent, Configuration cfg) throws FOPException {
         return new IFRendererConfig((new DefaultFontConfig.DefaultFontConfigParser()).parse(cfg, userAgent.validateStrictly(), new FontEventAdapter(userAgent.getEventBroadcaster())));
      }

      public String getMimeType() {
         return "application/X-fop-intermediate-format";
      }
   }
}
