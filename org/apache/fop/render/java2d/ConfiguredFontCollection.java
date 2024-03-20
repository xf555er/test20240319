package org.apache.fop.render.java2d;

import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontUris;
import org.apache.fop.fonts.LazyFont;

public class ConfiguredFontCollection implements FontCollection {
   private static Log log = LogFactory.getLog(ConfiguredFontCollection.class);
   private final InternalResourceResolver resourceResolver;
   private final List embedFontInfoList;
   private final boolean useComplexScripts;

   public ConfiguredFontCollection(InternalResourceResolver resourceResolver, List customFonts, boolean useComplexScriptFeatures) {
      this.resourceResolver = resourceResolver;
      this.embedFontInfoList = customFonts;
      this.useComplexScripts = useComplexScriptFeatures;
   }

   public int setup(int start, FontInfo fontInfo) {
      int num = start;
      if (this.embedFontInfoList != null && this.embedFontInfoList.size() >= 1) {
         String internalName = null;
         Iterator var5 = this.embedFontInfoList.iterator();

         while(var5.hasNext()) {
            EmbedFontInfo configFontInfo = (EmbedFontInfo)var5.next();
            internalName = "F" + num++;

            try {
               URI fontURI = configFontInfo.getEmbedURI();
               URI metricsURI = configFontInfo.getMetricsURI();
               CustomFontMetricsMapper font;
               if (metricsURI != null) {
                  LazyFont fontMetrics = new LazyFont(configFontInfo, this.resourceResolver, this.useComplexScripts);
                  InputStream fontSource = this.resourceResolver.getResource(fontURI);
                  font = new CustomFontMetricsMapper(fontMetrics, fontSource);
               } else {
                  FontUris fontUris = configFontInfo.getFontUris();
                  CustomFont fontMetrics = FontLoader.loadFont(fontUris, configFontInfo.getSubFontName(), true, configFontInfo.getEmbeddingMode(), configFontInfo.getEncodingMode(), configFontInfo.getKerning(), configFontInfo.getAdvanced(), this.resourceResolver, configFontInfo.getSimulateStyle(), configFontInfo.getEmbedAsType1(), configFontInfo.getUseSVG());
                  font = new CustomFontMetricsMapper(fontMetrics);
               }

               fontInfo.addMetrics(internalName, font);

               FontTriplet triplet;
               for(Iterator var14 = configFontInfo.getFontTriplets().iterator(); var14.hasNext(); fontInfo.addFontProperties(internalName, triplet)) {
                  triplet = (FontTriplet)var14.next();
                  if (log.isDebugEnabled()) {
                     log.debug("Registering: " + triplet + " under " + internalName);
                  }
               }
            } catch (Exception var12) {
               log.warn("Unable to load custom font from file '" + configFontInfo.getEmbedURI() + "'", var12);
            }
         }

         return num;
      } else {
         log.debug("No user configured fonts found.");
         return start;
      }
   }
}
