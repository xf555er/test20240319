package org.apache.fop.render.pcl;

import java.util.EnumMap;
import java.util.Map;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.render.RendererConfig;

public final class PCLRendererConfig implements RendererConfig {
   private final Map params;
   private final DefaultFontConfig fontConfig;

   private PCLRendererConfig(DefaultFontConfig fontConfig) {
      this.params = new EnumMap(Java2DRendererOption.class);
      this.fontConfig = fontConfig;
   }

   public DefaultFontConfig getFontInfoConfig() {
      return this.fontConfig;
   }

   public PCLRenderingMode getRenderingMode() {
      return (PCLRenderingMode)this.getParam(Java2DRendererOption.RENDERING_MODE, PCLRenderingMode.class);
   }

   public Boolean isTextRendering() {
      return (Boolean)this.getParam(Java2DRendererOption.TEXT_RENDERING, Boolean.class);
   }

   public Boolean isDisablePjl() {
      return (Boolean)this.getParam(Java2DRendererOption.DISABLE_PJL, Boolean.class);
   }

   public Boolean isColorEnabled() {
      return (Boolean)this.getParam(Java2DRendererOption.MODE_COLOR, Boolean.class);
   }

   public Boolean isOptimizeResources() {
      return (Boolean)this.getParam(Java2DRendererOption.OPTIMIZE_RESOURCES, Boolean.class);
   }

   private Object getParam(Java2DRendererOption option, Class type) {
      assert option.getType().equals(type);

      return type.cast(this.params.get(option));
   }

   private void setParam(Java2DRendererOption option, Object value) {
      assert option.getType().isInstance(value);

      this.params.put(option, value);
   }

   // $FF: synthetic method
   PCLRendererConfig(DefaultFontConfig x0, Object x1) {
      this(x0);
   }

   public static final class PCLRendererConfigParser implements RendererConfig.RendererConfigParser {
      public PCLRendererConfig build(FOUserAgent userAgent, Configuration cfg) throws FOPException {
         PCLRendererConfig config = new PCLRendererConfig((new DefaultFontConfig.DefaultFontConfigParser()).parse(cfg, userAgent.validateStrictly(), new FontEventAdapter(userAgent.getEventBroadcaster())));
         this.configure(cfg, config);
         return config;
      }

      private void configure(Configuration cfg, PCLRendererConfig config) throws FOPException {
         if (cfg != null) {
            Configuration imagesCfg = cfg.getChild("images");
            String imageMode = imagesCfg.getAttribute("mode", (String)null);
            if ("color".equalsIgnoreCase(imageMode)) {
               config.setParam(Java2DRendererOption.MODE_COLOR, true);
            }

            String rendering = cfg.getChild(Java2DRendererOption.RENDERING_MODE.getName()).getValue((String)null);
            if (rendering != null) {
               try {
                  config.setParam(Java2DRendererOption.RENDERING_MODE, PCLRenderingMode.getValueOf(rendering));
               } catch (IllegalArgumentException var7) {
                  throw new FOPException("Valid values for 'rendering' are 'quality', 'speed' and 'bitmap'. Value found: " + rendering);
               }
            }

            String textRendering = cfg.getChild(Java2DRendererOption.TEXT_RENDERING.getName()).getValue((String)null);
            if ("bitmap".equalsIgnoreCase(textRendering)) {
               config.setParam(Java2DRendererOption.TEXT_RENDERING, true);
            } else {
               if (textRendering != null && !"auto".equalsIgnoreCase(textRendering)) {
                  throw new FOPException("Valid values for 'text-rendering' are 'auto' and 'bitmap'. Value found: " + textRendering);
               }

               config.setParam(Java2DRendererOption.TEXT_RENDERING, false);
            }

            config.setParam(Java2DRendererOption.DISABLE_PJL, cfg.getChild(Java2DRendererOption.DISABLE_PJL.getName()).getValueAsBoolean(false));
            config.setParam(Java2DRendererOption.OPTIMIZE_RESOURCES, cfg.getChild(Java2DRendererOption.OPTIMIZE_RESOURCES.getName()).getValueAsBoolean(false));
         }

      }

      public String getMimeType() {
         return "application/x-pcl";
      }
   }
}
