package org.apache.fop.apps;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfiguration;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
import org.apache.fop.fonts.FontManagerConfigurator;
import org.apache.fop.hyphenation.HyphenationTreeCache;
import org.apache.fop.hyphenation.Hyphenator;
import org.apache.fop.util.LogUtil;
import org.apache.xmlgraphics.image.loader.spi.ImageImplRegistry;
import org.apache.xmlgraphics.image.loader.util.Penalty;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.xml.sax.SAXException;

public class FopConfParser {
   private static final String PREFER_RENDERER = "prefer-renderer";
   private static final String TABLE_BORDER_OVERPAINT = "table-border-overpaint";
   private final Log log;
   private final FopFactoryBuilder fopFactoryBuilder;

   public FopConfParser(InputStream fopConfStream, EnvironmentProfile enviro) throws SAXException, IOException {
      this(fopConfStream, enviro.getDefaultBaseURI(), enviro);
   }

   public FopConfParser(InputStream fopConfStream, URI defaultBaseURI, ResourceResolver resourceResolver) throws SAXException, IOException {
      this(fopConfStream, defaultBaseURI, EnvironmentalProfileFactory.createDefault(defaultBaseURI, resourceResolver));
   }

   public FopConfParser(InputStream fopConfStream, URI defaultBaseURI) throws SAXException, IOException {
      this(fopConfStream, defaultBaseURI, ResourceResolverFactory.createDefaultResourceResolver());
   }

   public FopConfParser(File fopConfFile) throws SAXException, IOException {
      this(fopConfFile, ResourceResolverFactory.createDefaultResourceResolver());
   }

   public FopConfParser(File fopConfFile, URI defaultBaseURI) throws SAXException, IOException {
      this(new FileInputStream(fopConfFile), fopConfFile.toURI(), (EnvironmentProfile)EnvironmentalProfileFactory.createDefault(defaultBaseURI, ResourceResolverFactory.createDefaultResourceResolver()));
   }

   public FopConfParser(File fopConfFile, ResourceResolver resourceResolver) throws SAXException, IOException {
      this(new FileInputStream(fopConfFile), fopConfFile.getParentFile().toURI(), (ResourceResolver)resourceResolver);
   }

   public FopConfParser(InputStream fopConfStream, URI baseURI, EnvironmentProfile enviro) throws SAXException, IOException {
      this.log = LogFactory.getLog(FopConfParser.class);
      DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();

      DefaultConfiguration cfg;
      try {
         cfg = cfgBuilder.build(fopConfStream);
      } catch (ConfigurationException var7) {
         throw new FOPException(var7);
      }

      this.fopFactoryBuilder = (new FopFactoryBuilder(enviro)).setConfiguration(cfg);
      this.configure(baseURI, enviro.getResourceResolver(), cfg);
   }

   private void configure(URI baseURI, ResourceResolver resourceResolver, Configuration cfg) throws FOPException {
      if (this.log.isDebugEnabled()) {
         this.log.debug("Initializing FopFactory Configuration");
      }

      boolean strict;
      if (cfg.getChild("strict-validation", false) != null) {
         try {
            strict = cfg.getChild("strict-validation").getValueAsBoolean();
            this.fopFactoryBuilder.setStrictFOValidation(strict);
         } catch (ConfigurationException var13) {
            LogUtil.handleException(this.log, var13, false);
         }
      }

      strict = false;
      if (cfg.getChild("strict-configuration", false) != null) {
         try {
            strict = cfg.getChild("strict-configuration").getValueAsBoolean();
            this.fopFactoryBuilder.setStrictUserConfigValidation(strict);
         } catch (ConfigurationException var12) {
            LogUtil.handleException(this.log, var12, false);
         }
      }

      if (cfg.getChild("accessibility", false) != null) {
         try {
            this.fopFactoryBuilder.setAccessibility(cfg.getChild("accessibility").getValueAsBoolean());
            this.fopFactoryBuilder.setKeepEmptyTags(cfg.getChild("accessibility").getAttributeAsBoolean("keep-empty-tags", true));
         } catch (ConfigurationException var11) {
            LogUtil.handleException(this.log, var11, false);
         }
      }

      if (cfg.getChild("base", false) != null) {
         try {
            URI confUri = InternalResourceResolver.getBaseURI(cfg.getChild("base").getValue((String)null));
            this.fopFactoryBuilder.setBaseURI(baseURI.resolve(confUri));
         } catch (URISyntaxException var10) {
            LogUtil.handleException(this.log, var10, strict);
         }
      }

      float targetRes;
      if (cfg.getChild("source-resolution", false) != null) {
         targetRes = cfg.getChild("source-resolution").getValueAsFloat(72.0F);
         this.fopFactoryBuilder.setSourceResolution(targetRes);
         if (this.log.isDebugEnabled()) {
            this.log.debug("source-resolution set to: " + targetRes + "dpi");
         }
      }

      if (cfg.getChild("target-resolution", false) != null) {
         targetRes = cfg.getChild("target-resolution").getValueAsFloat(72.0F);
         this.fopFactoryBuilder.setTargetResolution(targetRes);
         if (this.log.isDebugEnabled()) {
            this.log.debug("target-resolution set to: " + targetRes + "dpi");
         }
      }

      if (cfg.getChild("break-indent-inheritance", false) != null) {
         try {
            this.fopFactoryBuilder.setBreakIndentInheritanceOnReferenceAreaBoundary(cfg.getChild("break-indent-inheritance").getValueAsBoolean());
         } catch (ConfigurationException var9) {
            LogUtil.handleException(this.log, var9, strict);
         }
      }

      Configuration pageConfig = cfg.getChild("default-page-settings");
      String pageWidth;
      if (pageConfig.getAttribute("height", (String)null) != null) {
         pageWidth = pageConfig.getAttribute("height", "11in");
         this.fopFactoryBuilder.setPageHeight(pageWidth);
         if (this.log.isInfoEnabled()) {
            this.log.info("Default page-height set to: " + pageWidth);
         }
      }

      if (pageConfig.getAttribute("width", (String)null) != null) {
         pageWidth = pageConfig.getAttribute("width", "8.26in");
         this.fopFactoryBuilder.setPageWidth(pageWidth);
         if (this.log.isInfoEnabled()) {
            this.log.info("Default page-width set to: " + pageWidth);
         }
      }

      if (cfg.getChild("complex-scripts") != null) {
         Configuration csConfig = cfg.getChild("complex-scripts");
         this.fopFactoryBuilder.setComplexScriptFeatures(!csConfig.getAttributeAsBoolean("disabled", false));
      }

      this.setHyphenationBase(cfg, resourceResolver, baseURI, this.fopFactoryBuilder);
      this.setHyphPatNames(cfg, this.fopFactoryBuilder, strict);
      if (cfg.getChild("prefer-renderer", false) != null) {
         try {
            this.fopFactoryBuilder.setPreferRenderer(cfg.getChild("prefer-renderer").getValueAsBoolean());
         } catch (ConfigurationException var8) {
            LogUtil.handleException(this.log, var8, strict);
         }
      }

      if (cfg.getChild("table-border-overpaint", false) != null) {
         try {
            this.fopFactoryBuilder.setTableBorderOverpaint(cfg.getChild("table-border-overpaint").getValueAsBoolean());
         } catch (ConfigurationException var7) {
            LogUtil.handleException(this.log, var7, false);
         }
      }

      (new FontManagerConfigurator(cfg, baseURI, this.fopFactoryBuilder.getBaseURI(), resourceResolver)).configure(this.fopFactoryBuilder.getFontManager(), strict);
      this.configureImageLoading(cfg.getChild("image-loading", false), strict);
   }

   private void setHyphenationBase(Configuration cfg, ResourceResolver resourceResolver, URI baseURI, FopFactoryBuilder fopFactoryBuilder) throws FOPException {
      if (cfg.getChild("hyphenation-base", false) != null) {
         try {
            URI fontBase = InternalResourceResolver.getBaseURI(cfg.getChild("hyphenation-base").getValue((String)null));
            fopFactoryBuilder.setHyphenBaseResourceResolver(ResourceResolverFactory.createInternalResourceResolver(baseURI.resolve(fontBase), resourceResolver));
         } catch (URISyntaxException var6) {
            LogUtil.handleException(this.log, var6, true);
         }
      } else {
         fopFactoryBuilder.setHyphenBaseResourceResolver(ResourceResolverFactory.createInternalResourceResolver(fopFactoryBuilder.getBaseURI(), resourceResolver));
      }

   }

   private void setHyphPatNames(Configuration cfg, FopFactoryBuilder builder, boolean strict) throws FOPException {
      Configuration[] hyphPatConfig = cfg.getChildren("hyphenation-pattern");
      if (hyphPatConfig.length != 0) {
         Map hyphPatNames = new HashMap();
         Configuration[] var6 = hyphPatConfig;
         int var7 = hyphPatConfig.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            Configuration aHyphPatConfig = var6[var8];
            StringBuffer error = new StringBuffer();
            String location = aHyphPatConfig.getLocation();
            String lang = aHyphPatConfig.getAttribute("lang", (String)null);
            if (lang == null) {
               addError("The lang attribute of a hyphenation-pattern configuration element must exist (" + location + ")", error);
            } else if (!lang.matches("[a-zA-Z]{2}")) {
               addError("The lang attribute of a hyphenation-pattern configuration element must consist of exactly two letters (" + location + ")", error);
            }

            lang = lang.toLowerCase(Locale.getDefault());
            String country = aHyphPatConfig.getAttribute("country", (String)null);
            if ("".equals(country)) {
               country = null;
            }

            if (country != null) {
               if (!country.matches("[a-zA-Z]{2}")) {
                  addError("The country attribute of a hyphenation-pattern configuration element must consist of exactly two letters (" + location + ")", error);
               }

               country = country.toUpperCase(Locale.getDefault());
            }

            String filename = aHyphPatConfig.getValue((String)null);
            if (filename == null) {
               addError("The value of a hyphenation-pattern configuration element may not be empty (" + location + ")", error);
            }

            if (error.length() != 0) {
               LogUtil.handleError(this.log, error.toString(), strict);
            } else {
               String llccKey = HyphenationTreeCache.constructLlccKey(lang, country);
               String extension = aHyphPatConfig.getAttribute("extension", (String)null);
               if ("xml".equals(extension)) {
                  hyphPatNames.put(llccKey, filename + Hyphenator.XMLTYPE);
               } else if ("hyp".equals(extension)) {
                  hyphPatNames.put(llccKey, filename + Hyphenator.HYPTYPE);
               } else {
                  hyphPatNames.put(llccKey, filename);
               }

               if (this.log.isDebugEnabled()) {
                  this.log.debug("Using hyphenation pattern filename " + filename + " for lang=\"" + lang + "\"" + (country != null ? ", country=\"" + country + "\"" : ""));
               }
            }
         }

         builder.setHyphPatNames(hyphPatNames);
      }

   }

   private static void addError(String message, StringBuffer error) {
      if (error.length() != 0) {
         error.append(". ");
      }

      error.append(message);
   }

   private void configureImageLoading(Configuration parent, boolean strict) throws FOPException {
      if (parent != null) {
         ImageImplRegistry registry = this.fopFactoryBuilder.getImageManager().getRegistry();
         Configuration[] penalties = parent.getChildren("penalty");

         try {
            Configuration[] var5 = penalties;
            int var6 = penalties.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               Configuration penaltyCfg = var5[var7];
               String className = penaltyCfg.getAttribute("class");
               String value = penaltyCfg.getAttribute("value");
               Penalty p = null;
               if (value.toUpperCase(Locale.getDefault()).startsWith("INF")) {
                  p = Penalty.INFINITE_PENALTY;
               } else {
                  try {
                     p = Penalty.toPenalty(Integer.parseInt(value));
                  } catch (NumberFormatException var13) {
                     LogUtil.handleException(this.log, var13, strict);
                  }
               }

               if (p != null) {
                  registry.setAdditionalPenalty(className, p);
               }
            }
         } catch (ConfigurationException var14) {
            LogUtil.handleException(this.log, var14, strict);
         }

      }
   }

   public FopFactoryBuilder getFopFactoryBuilder() {
      return this.fopFactoryBuilder;
   }
}
