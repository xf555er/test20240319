package org.apache.fop.render.bitmap;

import java.awt.Color;
import java.util.EnumMap;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.java2d.Java2DRendererConfig;
import org.apache.fop.util.ColorUtil;

public class BitmapRendererConfig implements RendererConfig {
   private final EnumMap params = new EnumMap(BitmapRendererOption.class);
   private final DefaultFontConfig fontConfig;

   BitmapRendererConfig(DefaultFontConfig fontConfig) {
      this.fontConfig = fontConfig;
   }

   public DefaultFontConfig getFontInfoConfig() {
      return this.fontConfig;
   }

   public Color getBackgroundColor() {
      return (Color)this.get(BitmapRendererOption.BACKGROUND_COLOR);
   }

   public Boolean hasAntiAliasing() {
      return (Boolean)this.get(BitmapRendererOption.ANTI_ALIASING);
   }

   public Boolean isRenderHighQuality() {
      return (Boolean)this.get(BitmapRendererOption.RENDERING_QUALITY);
   }

   public Integer getColorMode() {
      return (Integer)this.get(BitmapRendererOption.COLOR_MODE);
   }

   public boolean hasTransparentBackround() {
      Object result = this.get(BitmapRendererOption.JAVA2D_TRANSPARENT_PAGE_BACKGROUND);
      return (Boolean)((Boolean)(result != null ? result : BitmapRendererOption.JAVA2D_TRANSPARENT_PAGE_BACKGROUND.getDefaultValue()));
   }

   private Object get(BitmapRendererOption option) {
      return this.params.get(option);
   }

   public static class BitmapRendererConfigParser implements RendererConfig.RendererConfigParser {
      private final String mimeType;

      public BitmapRendererConfigParser(String mimeType) {
         this.mimeType = mimeType;
      }

      private void setParam(BitmapRendererConfig config, BitmapRendererOption option, Object value) {
         config.params.put(option, value != null ? value : option.getDefaultValue());
      }

      void build(BitmapRendererConfig config, FOUserAgent userAgent, Configuration cfg) throws FOPException {
         if (cfg != null) {
            Java2DRendererConfig j2dConfig = (new Java2DRendererConfig.Java2DRendererConfigParser((String)null)).build(userAgent, cfg);
            Boolean isTransparent = j2dConfig.isPageBackgroundTransparent();
            isTransparent = isTransparent == null ? (Boolean)BitmapRendererOption.JAVA2D_TRANSPARENT_PAGE_BACKGROUND.getDefaultValue() : isTransparent;
            this.setParam(config, BitmapRendererOption.JAVA2D_TRANSPARENT_PAGE_BACKGROUND, isTransparent);
            String background = this.getValue(cfg, BitmapRendererOption.BACKGROUND_COLOR);
            if (isTransparent) {
               config.params.put(BitmapRendererOption.BACKGROUND_COLOR, (Object)null);
            } else {
               this.setParam(config, BitmapRendererOption.BACKGROUND_COLOR, ColorUtil.parseColorString(userAgent, background));
            }

            this.setParam(config, BitmapRendererOption.ANTI_ALIASING, this.getChild(cfg, BitmapRendererOption.ANTI_ALIASING).getValueAsBoolean((Boolean)BitmapRendererOption.ANTI_ALIASING.getDefaultValue()));
            String optimization = this.getValue(cfg, BitmapRendererOption.RENDERING_QUALITY_ELEMENT);
            this.setParam(config, BitmapRendererOption.RENDERING_QUALITY, BitmapRendererOption.getValue(optimization) != BitmapRendererOption.RENDERING_SPEED);
            String color = this.getValue(cfg, BitmapRendererOption.COLOR_MODE);
            this.setParam(config, BitmapRendererOption.COLOR_MODE, this.getBufferedImageIntegerFromColor(BitmapRendererOption.getValue(color)));
         }

      }

      public BitmapRendererConfig build(FOUserAgent userAgent, Configuration cfg) throws FOPException {
         BitmapRendererConfig config = new BitmapRendererConfig((new DefaultFontConfig.DefaultFontConfigParser()).parse(cfg, userAgent.validateStrictly(), new FontEventAdapter(userAgent.getEventBroadcaster())));
         this.build(config, userAgent, cfg);
         return config;
      }

      private Integer getBufferedImageIntegerFromColor(BitmapRendererOption option) {
         if (option == null) {
            return null;
         } else {
            switch (option) {
               case COLOR_MODE_RGBA:
                  return 2;
               case COLOR_MODE_RGB:
                  return 1;
               case COLOR_MODE_GRAY:
                  return 10;
               case COLOR_MODE_BINARY:
               case COLOR_MODE_BILEVEL:
                  return 12;
               default:
                  return null;
            }
         }
      }

      private Configuration getChild(Configuration cfg, BitmapRendererOption option) {
         return cfg.getChild(option.getName());
      }

      private String getValue(Configuration cfg, BitmapRendererOption option) {
         Object defaultValue = option.getDefaultValue();
         Object result = cfg.getChild(option.getName()).getValue((String)null);
         if (result == null || "".equals(result)) {
            result = defaultValue;
         }

         if (result == null) {
            return null;
         } else {
            return result instanceof Color ? ColorUtil.colorToString((Color)result) : result.toString();
         }
      }

      public String getMimeType() {
         return this.mimeType;
      }
   }
}
