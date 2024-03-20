package org.apache.fop.render.pdf;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.pdf.PDFEncryptionParams;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.RendererConfigOption;
import org.apache.fop.util.LogUtil;

public final class PDFRendererConfig implements RendererConfig {
   private static final Log LOG = LogFactory.getLog(PDFRendererConfig.class);
   private final PDFRendererOptionsConfig configOption;
   private final DefaultFontConfig fontConfig;

   private PDFRendererConfig(DefaultFontConfig fontConfig, PDFRendererOptionsConfig config) {
      this.fontConfig = fontConfig;
      this.configOption = config;
   }

   public PDFRendererOptionsConfig getConfigOptions() {
      return this.configOption;
   }

   public DefaultFontConfig getFontInfoConfig() {
      return this.fontConfig;
   }

   // $FF: synthetic method
   PDFRendererConfig(DefaultFontConfig x0, PDFRendererOptionsConfig x1, Object x2) {
      this(x0, x1);
   }

   private static final class ParserHelper {
      private final Map configOptions;
      private PDFEncryptionParams encryptionConfig;
      private PDFRendererConfig pdfConfig;

      private ParserHelper(Configuration cfg, FOUserAgent userAgent, boolean strict) throws FOPException {
         this.configOptions = new EnumMap(PDFRendererOption.class);
         if (cfg != null) {
            this.configure(cfg, userAgent, strict);
         }

         if (userAgent == null) {
            this.pdfConfig = new PDFRendererConfig((new DefaultFontConfig.DefaultFontConfigParser()).parse(cfg, strict), new PDFRendererOptionsConfig(this.configOptions, this.encryptionConfig));
         } else {
            this.pdfConfig = new PDFRendererConfig((new DefaultFontConfig.DefaultFontConfigParser()).parse(cfg, strict, new FontEventAdapter(userAgent.getEventBroadcaster())), new PDFRendererOptionsConfig(this.configOptions, this.encryptionConfig));
         }

      }

      private void parseAndPut(PDFRendererOption option, Configuration cfg) {
         this.put(option, option.parse(this.parseConfig(cfg, option)));
      }

      private void put(PDFRendererOption option, Object value) {
         if (value != null && !value.equals(option.getDefaultValue())) {
            this.configOptions.put(option, value);
         }

      }

      private void configure(Configuration cfg, FOUserAgent userAgent, boolean strict) throws FOPException {
         try {
            this.buildFilterMapFromConfiguration(cfg);
            this.parseAndPut(PDFRendererOption.PDF_A_MODE, cfg);
            this.parseAndPut(PDFRendererOption.PDF_UA_MODE, cfg);
            this.parseAndPut(PDFRendererOption.PDF_X_MODE, cfg);
            this.parseAndPut(PDFRendererOption.PDF_VT_MODE, cfg);
            this.configureEncryptionParams(cfg, userAgent, strict);
            this.parseAndPut(PDFRendererOption.OUTPUT_PROFILE, cfg);
            this.parseAndPut(PDFRendererOption.DISABLE_SRGB_COLORSPACE, cfg);
            this.parseAndPut(PDFRendererOption.MERGE_FONTS, cfg);
            this.parseAndPut(PDFRendererOption.LINEARIZATION, cfg);
            this.parseAndPut(PDFRendererOption.FORM_XOBJECT, cfg);
            this.parseAndPut(PDFRendererOption.VERSION, cfg);
         } catch (ConfigurationException var5) {
            LogUtil.handleException(PDFRendererConfig.LOG, var5, strict);
         }

      }

      private void configureEncryptionParams(Configuration cfg, FOUserAgent userAgent, boolean strict) {
         Configuration encryptCfg = cfg.getChild("encryption-params", false);
         if (encryptCfg != null) {
            this.encryptionConfig = new PDFEncryptionParams();
            this.encryptionConfig.setOwnerPassword(this.parseConfig(encryptCfg, PDFEncryptionOption.OWNER_PASSWORD));
            this.encryptionConfig.setUserPassword(this.parseConfig(encryptCfg, PDFEncryptionOption.USER_PASSWORD));
            this.encryptionConfig.setAllowPrint(!this.doesValueExist(encryptCfg, PDFEncryptionOption.NO_PRINT));
            this.encryptionConfig.setAllowCopyContent(!this.doesValueExist(encryptCfg, PDFEncryptionOption.NO_COPY_CONTENT));
            this.encryptionConfig.setAllowEditContent(!this.doesValueExist(encryptCfg, PDFEncryptionOption.NO_EDIT_CONTENT));
            this.encryptionConfig.setAllowEditAnnotations(!this.doesValueExist(encryptCfg, PDFEncryptionOption.NO_ANNOTATIONS));
            this.encryptionConfig.setAllowFillInForms(!this.doesValueExist(encryptCfg, PDFEncryptionOption.NO_FILLINFORMS));
            this.encryptionConfig.setAllowAccessContent(!this.doesValueExist(encryptCfg, PDFEncryptionOption.NO_ACCESSCONTENT));
            this.encryptionConfig.setAllowAssembleDocument(!this.doesValueExist(encryptCfg, PDFEncryptionOption.NO_ASSEMBLEDOC));
            this.encryptionConfig.setAllowPrintHq(!this.doesValueExist(encryptCfg, PDFEncryptionOption.NO_PRINTHQ));
            this.encryptionConfig.setEncryptMetadata(this.getConfigValue(encryptCfg, PDFEncryptionOption.ENCRYPT_METADATA, true));
            String encryptionLength = this.parseConfig(encryptCfg, PDFEncryptionOption.ENCRYPTION_LENGTH);
            if (encryptionLength != null) {
               int validatedLength = this.checkEncryptionLength(Integer.parseInt(encryptionLength), userAgent);
               this.encryptionConfig.setEncryptionLengthInBits(validatedLength);
            }
         }

      }

      private void buildFilterMapFromConfiguration(Configuration cfg) throws ConfigurationException, FOPException {
         Configuration[] filterLists = cfg.getChildren(PDFRendererOption.FILTER_LIST.getName());
         Map filterMap = new HashMap();
         Configuration[] var4 = filterLists;
         int var5 = filterLists.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            Configuration filters = var4[var6];
            String type = filters.getAttribute("type", "default");
            List filterList = new ArrayList();
            Configuration[] var10 = filters.getChildren("value");
            int j = var10.length;

            for(int var12 = 0; var12 < j; ++var12) {
               Configuration nameCfg = var10[var12];
               filterList.add(nameCfg.getValue());
            }

            if (!filterList.isEmpty() && PDFRendererConfig.LOG.isDebugEnabled()) {
               StringBuffer debug = new StringBuffer("Adding PDF filter");
               if (filterList.size() != 1) {
                  debug.append("s");
               }

               debug.append(" for type ").append(type).append(": ");

               for(j = 0; j < filterList.size(); ++j) {
                  if (j != 0) {
                     debug.append(", ");
                  }

                  debug.append((String)filterList.get(j));
               }

               PDFRendererConfig.LOG.debug(debug.toString());
            }

            if (filterMap.get(type) != null) {
               throw new ConfigurationException("A filterList of type '" + type + "' has already been defined");
            }

            filterMap.put(type, filterList);
         }

         this.put(PDFRendererOption.FILTER_LIST, filterMap);
      }

      private String parseConfig(Configuration cfg, RendererConfigOption option) {
         Configuration child = cfg.getChild(option.getName());
         String value = child.getValue((String)null);
         if (value != null && !"".equals(value)) {
            return value;
         } else {
            Object v = option.getDefaultValue();
            return v == null ? null : v.toString();
         }
      }

      private boolean doesValueExist(Configuration cfg, RendererConfigOption option) {
         return cfg.getChild(option.getName(), false) != null;
      }

      private boolean getConfigValue(Configuration cfg, RendererConfigOption option, boolean defaultTo) {
         if (cfg.getChild(option.getName(), false) != null) {
            Configuration child = cfg.getChild(option.getName());

            try {
               return child.getValueAsBoolean();
            } catch (ConfigurationException var6) {
               return defaultTo;
            }
         } else {
            return defaultTo;
         }
      }

      private int checkEncryptionLength(int encryptionLength, FOUserAgent userAgent) {
         int correctEncryptionLength = encryptionLength;
         if (encryptionLength < 40) {
            correctEncryptionLength = 40;
         } else if (encryptionLength > 256) {
            correctEncryptionLength = 256;
         } else if (encryptionLength > 128 && encryptionLength < 256) {
            correctEncryptionLength = 128;
         } else if (encryptionLength % 8 != 0) {
            correctEncryptionLength = Math.round((float)encryptionLength / 8.0F) * 8;
         }

         if (correctEncryptionLength != encryptionLength && userAgent != null) {
            PDFEventProducer.Provider.get(userAgent.getEventBroadcaster()).incorrectEncryptionLength(this, encryptionLength, correctEncryptionLength);
         }

         return correctEncryptionLength;
      }

      // $FF: synthetic method
      ParserHelper(Configuration x0, FOUserAgent x1, boolean x2, Object x3) throws FOPException {
         this(x0, x1, x2);
      }
   }

   public static final class PDFRendererConfigParser implements RendererConfig.RendererConfigParser {
      public PDFRendererConfig build(FOUserAgent userAgent, Configuration cfg) throws FOPException {
         boolean strict = userAgent != null ? userAgent.validateUserConfigStrictly() : false;
         return (new ParserHelper(cfg, userAgent, strict)).pdfConfig;
      }

      public String getMimeType() {
         return "application/pdf";
      }
   }
}
