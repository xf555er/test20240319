package org.apache.fop.render.bitmap;

import java.util.EnumMap;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.render.RendererConfigOption;
import org.apache.xmlgraphics.image.writer.Endianness;

public final class TIFFRendererConfig extends BitmapRendererConfig {
   private final EnumMap params;

   private TIFFRendererConfig(DefaultFontConfig fontConfig) {
      super(fontConfig);
      this.params = new EnumMap(TIFFRendererOption.class);
   }

   public TIFFCompressionValue getCompressionType() {
      return (TIFFCompressionValue)this.params.get(TIFFRendererConfig.TIFFRendererOption.COMPRESSION);
   }

   public Boolean isSingleStrip() {
      return (Boolean)this.params.get(TIFFRendererConfig.TIFFRendererOption.SINGLE_STRIP);
   }

   public Endianness getEndianness() {
      return (Endianness)this.params.get(TIFFRendererConfig.TIFFRendererOption.ENDIANNESS);
   }

   // $FF: synthetic method
   TIFFRendererConfig(DefaultFontConfig x0, Object x1) {
      this(x0);
   }

   public static final class TIFFRendererConfigParser extends BitmapRendererConfig.BitmapRendererConfigParser {
      private TIFFRendererConfig config;

      public TIFFRendererConfigParser() {
         super("image/tiff");
      }

      private void setParam(TIFFRendererOption option, Object value) {
         this.config.params.put(option, value != null ? value : option.getDefaultValue());
      }

      private String getValue(Configuration cfg, TIFFRendererOption option) {
         return cfg.getChild(option.getName()).getValue((String)null);
      }

      public TIFFRendererConfig build(FOUserAgent userAgent, Configuration cfg) throws FOPException {
         this.config = new TIFFRendererConfig((new DefaultFontConfig.DefaultFontConfigParser()).parse(cfg, userAgent.validateStrictly(), new FontEventAdapter(userAgent.getEventBroadcaster())));
         super.build(this.config, userAgent, cfg);
         if (cfg != null) {
            this.setParam(TIFFRendererConfig.TIFFRendererOption.COMPRESSION, TIFFCompressionValue.getType(this.getValue(cfg, TIFFRendererConfig.TIFFRendererOption.COMPRESSION)));
            this.setParam(TIFFRendererConfig.TIFFRendererOption.SINGLE_STRIP, Boolean.valueOf(this.getValue(cfg, TIFFRendererConfig.TIFFRendererOption.SINGLE_STRIP)));
            this.setParam(TIFFRendererConfig.TIFFRendererOption.ENDIANNESS, Endianness.getEndianType(this.getValue(cfg, TIFFRendererConfig.TIFFRendererOption.ENDIANNESS)));
         }

         return this.config;
      }
   }

   public static enum TIFFRendererOption implements RendererConfigOption {
      COMPRESSION("compression", TIFFCompressionValue.PACKBITS),
      SINGLE_STRIP("single-strip", Boolean.FALSE),
      ENDIANNESS("endianness", Endianness.DEFAULT);

      private final String name;
      private final Object defaultValue;

      private TIFFRendererOption(String name, Object defaultValue) {
         this.name = name;
         this.defaultValue = defaultValue;
      }

      public String getName() {
         return this.name;
      }

      public Object getDefaultValue() {
         return this.defaultValue;
      }
   }
}
