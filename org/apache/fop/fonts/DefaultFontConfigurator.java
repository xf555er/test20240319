package org.apache.fop.fonts;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.autodetect.FontFileFinder;
import org.apache.fop.fonts.autodetect.FontInfoFinder;
import org.apache.fop.util.LogUtil;

public class DefaultFontConfigurator implements FontConfigurator {
   protected static final Log log = LogFactory.getLog(DefaultFontConfigurator.class);
   private final FontManager fontManager;
   private final InternalResourceResolver resourceResolver;
   private final FontEventListener listener;
   private final boolean strict;

   public DefaultFontConfigurator(FontManager fontManager, FontEventListener listener, boolean strict) {
      this.fontManager = fontManager;
      this.resourceResolver = fontManager.getResourceResolver();
      this.listener = listener;
      this.strict = strict;
   }

   public List configure(FontConfig fontInfoConfig) throws FOPException {
      List fontInfoList = new ArrayList();
      if (fontInfoConfig != null) {
         assert fontInfoConfig instanceof DefaultFontConfig;

         DefaultFontConfig adobeFontInfoConfig = (DefaultFontConfig)fontInfoConfig;
         long start = 0L;
         if (log.isDebugEnabled()) {
            log.debug("Starting font configuration...");
            start = System.currentTimeMillis();
         }

         FontAdder fontAdder = new FontAdder(this.fontManager, this.resourceResolver, this.listener);
         this.fontManager.autoDetectFonts(adobeFontInfoConfig.isAutoDetectFonts(), fontAdder, this.strict, this.listener, fontInfoList);
         this.addDirectories(adobeFontInfoConfig, fontAdder, fontInfoList);
         FontCache fontCache = this.fontManager.getFontCache();

         try {
            this.addFonts(adobeFontInfoConfig, fontCache, fontInfoList);
         } catch (URISyntaxException var10) {
            LogUtil.handleException(log, var10, this.strict);
         }

         this.fontManager.updateReferencedFonts(fontInfoList);
         List referencedFonts = adobeFontInfoConfig.getReferencedFontFamily();
         if (referencedFonts.size() > 0) {
            FontTriplet.Matcher matcher = FontManagerConfigurator.createFontsMatcher(referencedFonts, this.strict);
            this.fontManager.updateReferencedFonts(fontInfoList, matcher);
         }

         this.fontManager.saveCache();
         if (log.isDebugEnabled()) {
            log.debug("Finished font configuration in " + (System.currentTimeMillis() - start) + "ms");
         }
      }

      return Collections.unmodifiableList(fontInfoList);
   }

   private void addDirectories(DefaultFontConfig fontInfoConfig, FontAdder fontAdder, List fontInfoList) throws FOPException {
      List directories = fontInfoConfig.getDirectories();
      Iterator var5 = directories.iterator();

      while(var5.hasNext()) {
         DefaultFontConfig.Directory directory = (DefaultFontConfig.Directory)var5.next();
         FontFileFinder fontFileFinder = new FontFileFinder(directory.isRecursive() ? -1 : 1, this.listener);

         try {
            List fontURLList = fontFileFinder.find(directory.getDirectory());
            fontAdder.add(fontURLList, fontInfoList);
         } catch (IOException var10) {
            LogUtil.handleException(log, var10, this.strict);
         } catch (URISyntaxException var11) {
            LogUtil.handleException(log, var11, this.strict);
         }
      }

   }

   private void addFonts(DefaultFontConfig fontInfoConfig, FontCache fontCache, List fontInfoList) throws FOPException, URISyntaxException {
      List fonts = fontInfoConfig.getFonts();
      Iterator var5 = fonts.iterator();

      while(var5.hasNext()) {
         DefaultFontConfig.Font font = (DefaultFontConfig.Font)var5.next();
         EmbedFontInfo embedFontInfo = this.getFontInfo(font, fontCache);
         if (embedFontInfo != null) {
            fontInfoList.add(embedFontInfo);
         }
      }

   }

   private EmbedFontInfo getFontInfo(DefaultFontConfig.Font font, FontCache fontCache) throws FOPException, URISyntaxException {
      String embed = font.getEmbedURI();
      String metrics = font.getMetrics();
      String afm = font.getAfm();
      String pfm = font.getPfm();
      URI embedUri = InternalResourceResolver.cleanURI(embed);
      URI metricsUri = metrics == null ? null : InternalResourceResolver.cleanURI(metrics);
      URI afmUri = afm == null ? null : InternalResourceResolver.cleanURI(afm);
      URI pfmUri = pfm == null ? null : InternalResourceResolver.cleanURI(pfm);
      FontUris fontUris = afmUri == null && pfmUri == null ? new FontUris(embedUri, metricsUri) : new FontUris(embedUri, metricsUri, afmUri, pfmUri);
      String subFont = font.getSubFont();
      List tripletList = font.getTripletList();
      if (tripletList.size() == 0) {
         URI fontUri = this.resourceResolver.resolveFromBase(embedUri);
         FontInfoFinder finder = new FontInfoFinder();
         finder.setEventListener(this.listener);
         EmbedFontInfo[] infos = finder.find(fontUri, this.resourceResolver, fontCache);
         return infos[0];
      } else {
         EncodingMode encodingMode = EncodingMode.getValue(font.getEncodingMode());
         EmbeddingMode embeddingMode = EmbeddingMode.getValue(font.getEmbeddingMode());
         EmbedFontInfo embedFontInfo = new EmbedFontInfo(fontUris, font.isKerning(), font.isAdvanced(), tripletList, subFont, encodingMode, embeddingMode, font.getSimulateStyle(), font.getEmbedAsType1(), font.getUseSVG());
         if (fontCache != null && !fontCache.containsFont(embedFontInfo)) {
            fontCache.addFont(embedFontInfo, this.resourceResolver);
         }

         if (log.isDebugEnabled()) {
            URI embedFile = embedFontInfo.getEmbedURI();
            log.debug("Adding font " + (embedFile != null ? embedFile + ", " : "") + "metrics URI " + embedFontInfo.getMetricsURI());
            Iterator var18 = tripletList.iterator();

            while(var18.hasNext()) {
               FontTriplet triplet = (FontTriplet)var18.next();
               log.debug("  Font triplet " + triplet.getName() + ", " + triplet.getStyle() + ", " + triplet.getWeight());
            }
         }

         return embedFontInfo;
      }
   }
}
