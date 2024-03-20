package org.apache.fop.render.java2d;

import java.util.EnumMap;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.render.RendererConfig;

public final class Java2DRendererConfig implements RendererConfig {
   private final EnumMap params;
   private final DefaultFontConfig fontConfig;

   private Java2DRendererConfig(DefaultFontConfig fontConfig) {
      this.params = new EnumMap(Java2DRendererOption.class);
      this.fontConfig = fontConfig;
   }

   public DefaultFontConfig getFontInfoConfig() {
      return this.fontConfig;
   }

   public Boolean isPageBackgroundTransparent() {
      return (Boolean)Boolean.class.cast(this.params.get(Java2DRendererOption.JAVA2D_TRANSPARENT_PAGE_BACKGROUND));
   }

   // $FF: synthetic method
   Java2DRendererConfig(DefaultFontConfig x0, Object x1) {
      this(x0);
   }

   public static class Java2DRendererConfigParser implements RendererConfig.RendererConfigParser {
      private final String mimeType;

      public Java2DRendererConfigParser(String mimeType) {
         this.mimeType = mimeType;
      }

      public Java2DRendererConfig build(FOUserAgent userAgent, Configuration cfg) throws FOPException {
         Java2DRendererConfig config = new Java2DRendererConfig((new DefaultFontConfig.DefaultFontConfigParser()).parse(cfg, userAgent.validateStrictly(), new FontEventAdapter(userAgent.getEventBroadcaster())));
         boolean value = cfg.getChild(Java2DRendererOption.JAVA2D_TRANSPARENT_PAGE_BACKGROUND.getName(), true).getValueAsBoolean(false);
         config.params.put(Java2DRendererOption.JAVA2D_TRANSPARENT_PAGE_BACKGROUND, value);
         return config;
      }

      public String getMimeType() {
         return this.mimeType;
      }
   }
}
