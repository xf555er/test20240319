package org.apache.fop.svg;

import java.io.File;
import java.net.URI;
import java.util.List;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.fonts.DefaultFontConfig;
import org.apache.fop.fonts.DefaultFontConfigurator;
import org.apache.fop.fonts.FontCacheManagerFactory;
import org.apache.fop.fonts.FontDetectorFactory;
import org.apache.fop.fonts.FontEventListener;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.render.pdf.PDFRendererConfig;

public class PDFDocumentGraphics2DConfigurator {
   public void configure(PDFDocumentGraphics2D graphics, Configuration cfg, boolean useComplexScriptFeatures) throws ConfigurationException {
      PDFDocument pdfDoc = graphics.getPDFDocument();

      try {
         PDFRendererConfig pdfConfig = (new PDFRendererConfig.PDFRendererConfigParser()).build((FOUserAgent)null, cfg);
         pdfDoc.setFilterMap(pdfConfig.getConfigOptions().getFilterMap());
      } catch (FOPException var7) {
         throw new RuntimeException(var7);
      }

      try {
         FontInfo fontInfo = createFontInfo(cfg, useComplexScriptFeatures);
         graphics.setFontInfo(fontInfo);
      } catch (FOPException var6) {
         throw new ConfigurationException("Error while setting up fonts", var6);
      }
   }

   public static FontInfo createFontInfo(Configuration cfg, boolean useComplexScriptFeatures) throws FOPException {
      FontInfo fontInfo = new FontInfo();
      boolean strict = false;
      if (cfg != null) {
         URI thisUri = (new File(".")).getAbsoluteFile().toURI();
         InternalResourceResolver resourceResolver = ResourceResolverFactory.createDefaultInternalResourceResolver(thisUri);
         FontManager fontManager = new FontManager(resourceResolver, FontDetectorFactory.createDefault(), FontCacheManagerFactory.createDefault());
         DefaultFontConfig.DefaultFontConfigParser parser = new DefaultFontConfig.DefaultFontConfigParser();
         DefaultFontConfig fontInfoConfig = parser.parse(cfg, false);
         DefaultFontConfigurator fontInfoConfigurator = new DefaultFontConfigurator(fontManager, (FontEventListener)null, false);
         List fontInfoList = fontInfoConfigurator.configure(fontInfoConfig);
         fontManager.saveCache();
         FontSetup.setup(fontInfo, fontInfoList, resourceResolver, useComplexScriptFeatures);
      } else {
         FontSetup.setup(fontInfo, useComplexScriptFeatures);
      }

      return fontInfo;
   }
}
