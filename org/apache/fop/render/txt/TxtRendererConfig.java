package org.apache.fop.render.txt;

import java.util.EnumMap;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.RendererConfigOption;

public final class TxtRendererConfig implements RendererConfig {
   private final EnumMap params;
   private final DefaultFontConfig fontConfig;

   private TxtRendererConfig(DefaultFontConfig fontConfig) {
      this.params = new EnumMap(TxtRendererOption.class);
      this.fontConfig = fontConfig;
   }

   public DefaultFontConfig getFontInfoConfig() {
      return this.fontConfig;
   }

   public String getEncoding() {
      return (String)this.params.get(TxtRendererConfig.TxtRendererOption.ENCODING);
   }

   // $FF: synthetic method
   TxtRendererConfig(DefaultFontConfig x0, Object x1) {
      this(x0);
   }

   public static final class TxtRendererConfigParser implements RendererConfig.RendererConfigParser {
      public TxtRendererConfig build(FOUserAgent userAgent, Configuration cfg) throws FOPException {
         TxtRendererConfig config = new TxtRendererConfig((new DefaultFontConfig.DefaultFontConfigParser()).parse(cfg, userAgent.validateStrictly(), new FontEventAdapter(userAgent.getEventBroadcaster())));
         if (cfg != null) {
            TxtRendererOption option = TxtRendererConfig.TxtRendererOption.ENCODING;
            String value = cfg.getChild(option.getName(), true).getValue((String)null);
            config.params.put(option, value != null ? value : option.getDefaultValue());
         }

         return config;
      }

      public String getMimeType() {
         return "text/plain";
      }
   }

   public static enum TxtRendererOption implements RendererConfigOption {
      ENCODING("encoding", "UTF-8");

      private final String name;
      private final Object defaultValue;

      private TxtRendererOption(String name, Object defaultValue) {
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
