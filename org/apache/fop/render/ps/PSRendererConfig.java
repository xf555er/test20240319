package org.apache.fop.render.ps;

import java.util.EnumMap;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.util.LogUtil;

public final class PSRendererConfig implements RendererConfig {
   private final EnumMap params;
   private final DefaultFontConfig fontConfig;

   private PSRendererConfig(DefaultFontConfig fontConfig) {
      this.params = new EnumMap(PSRendererOption.class);
      this.fontConfig = fontConfig;
   }

   public DefaultFontConfig getFontInfoConfig() {
      return this.fontConfig;
   }

   public Boolean isAutoRotateLandscape() {
      return (Boolean)this.params.get(PSRendererOption.AUTO_ROTATE_LANDSCAPE);
   }

   public Integer getLanguageLevel() {
      return (Integer)this.params.get(PSRendererOption.LANGUAGE_LEVEL);
   }

   public Boolean isOptimizeResources() {
      return (Boolean)this.params.get(PSRendererOption.OPTIMIZE_RESOURCES);
   }

   public Boolean isSafeSetPageDevice() {
      return (Boolean)this.params.get(PSRendererOption.SAFE_SET_PAGE_DEVICE);
   }

   public Boolean isDscComplianceEnabled() {
      return (Boolean)this.params.get(PSRendererOption.DSC_COMPLIANT);
   }

   public PSRenderingMode getRenderingMode() {
      return (PSRenderingMode)this.params.get(PSRendererOption.RENDERING_MODE);
   }

   public Boolean isAcrobatDownsample() {
      return (Boolean)this.params.get(PSRendererOption.ACROBAT_DOWNSAMPLE);
   }

   // $FF: synthetic method
   PSRendererConfig(DefaultFontConfig x0, Object x1) {
      this(x0);
   }

   private static final class ParserHelper {
      private PSRendererConfig config;

      private ParserHelper(Configuration cfg, FOUserAgent userAgent) throws ConfigurationException, FOPException {
         this.config = new PSRendererConfig((new DefaultFontConfig.DefaultFontConfigParser()).parse(cfg, userAgent.validateStrictly(), new FontEventAdapter(userAgent.getEventBroadcaster())));
         if (cfg != null) {
            this.setBoolConfigParam(cfg, PSRendererOption.AUTO_ROTATE_LANDSCAPE);
            this.setConfigParameter(PSRendererOption.LANGUAGE_LEVEL, cfg.getChild(PSRendererOption.LANGUAGE_LEVEL.getName()).getValueAsInteger((Integer)PSRendererOption.LANGUAGE_LEVEL.getDefaultValue()));
            this.setBoolConfigParam(cfg, PSRendererOption.OPTIMIZE_RESOURCES);
            this.setBoolConfigParam(cfg, PSRendererOption.SAFE_SET_PAGE_DEVICE);
            this.setBoolConfigParam(cfg, PSRendererOption.DSC_COMPLIANT);
            this.setBoolConfigParam(cfg, PSRendererOption.ACROBAT_DOWNSAMPLE);
            Configuration child = cfg.getChild("rendering");
            if (child != null) {
               this.config.params.put(PSRendererOption.RENDERING_MODE, PSRenderingMode.valueOf(child.getValue(PSRendererOption.RENDERING_MODE.getDefaultValue().toString()).toUpperCase(Locale.ENGLISH)));
            }
         }

      }

      private void setConfigParameter(PSRendererOption option, Object value) {
         this.config.params.put(option, value != null ? value : option.getDefaultValue());
      }

      private void setBoolConfigParam(Configuration cfg, PSRendererOption option) {
         this.setConfigParameter(option, cfg.getChild(option.getName()).getValueAsBoolean((Boolean)option.getDefaultValue()));
      }

      // $FF: synthetic method
      ParserHelper(Configuration x0, FOUserAgent x1, Object x2) throws ConfigurationException, FOPException {
         this(x0, x1);
      }
   }

   public static final class PSRendererConfigParser implements RendererConfig.RendererConfigParser {
      private static final Log LOG = LogFactory.getLog(PSRendererConfigParser.class);

      public PSRendererConfig build(FOUserAgent userAgent, Configuration cfg) throws FOPException {
         PSRendererConfig config = null;

         try {
            config = (new ParserHelper(cfg, userAgent)).config;
         } catch (ConfigurationException var5) {
            LogUtil.handleException(LOG, var5, false);
         }

         return config;
      }

      public String getMimeType() {
         return "application/postscript";
      }
   }
}
