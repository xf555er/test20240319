package org.apache.fop.fonts;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.substitute.FontSubstitutions;

public class FontManager {
   private InternalResourceResolver resourceResolver;
   private final FontDetector fontDetector;
   private FontCacheManager fontCacheManager;
   private FontSubstitutions fontSubstitutions;
   private boolean enableBase14Kerning;
   private FontTriplet.Matcher referencedFontsMatcher;

   public FontManager(InternalResourceResolver resourceResolver, FontDetector fontDetector, FontCacheManager fontCacheManager) {
      this.resourceResolver = resourceResolver;
      this.fontDetector = fontDetector;
      this.fontCacheManager = fontCacheManager;
   }

   public void setResourceResolver(InternalResourceResolver resourceResolver) {
      this.resourceResolver = resourceResolver;
   }

   public InternalResourceResolver getResourceResolver() {
      return this.resourceResolver;
   }

   public boolean isBase14KerningEnabled() {
      return this.enableBase14Kerning;
   }

   public void setBase14KerningEnabled(boolean value) {
      this.enableBase14Kerning = value;
   }

   public void setFontSubstitutions(FontSubstitutions substitutions) {
      this.fontSubstitutions = substitutions;
   }

   protected FontSubstitutions getFontSubstitutions() {
      if (this.fontSubstitutions == null) {
         this.fontSubstitutions = new FontSubstitutions();
      }

      return this.fontSubstitutions;
   }

   public void setCacheFile(URI cacheFileURI) {
      this.fontCacheManager.setCacheFile(this.resourceResolver.resolveFromBase(cacheFileURI));
   }

   public void disableFontCache() {
      this.fontCacheManager = FontCacheManagerFactory.createDisabled();
   }

   public FontCache getFontCache() {
      return this.fontCacheManager.load();
   }

   public void saveCache() throws FOPException {
      this.fontCacheManager.save();
   }

   public void deleteCache() throws FOPException {
      this.fontCacheManager.delete();
   }

   public void setup(FontInfo fontInfo, FontCollection[] fontCollections) {
      int startNum = 1;
      FontCollection[] var4 = fontCollections;
      int var5 = fontCollections.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         FontCollection fontCollection = var4[var6];
         startNum = fontCollection.setup(startNum, fontInfo);
      }

      this.getFontSubstitutions().adjustFontInfo(fontInfo);
   }

   public void setReferencedFontsMatcher(FontTriplet.Matcher matcher) {
      this.referencedFontsMatcher = matcher;
   }

   public FontTriplet.Matcher getReferencedFontsMatcher() {
      return this.referencedFontsMatcher;
   }

   public void updateReferencedFonts(List fontInfoList) {
      FontTriplet.Matcher matcher = this.getReferencedFontsMatcher();
      this.updateReferencedFonts(fontInfoList, matcher);
   }

   public void updateReferencedFonts(List fontInfoList, FontTriplet.Matcher matcher) {
      if (matcher != null) {
         Iterator var3 = fontInfoList.iterator();

         while(true) {
            while(var3.hasNext()) {
               EmbedFontInfo fontInfo = (EmbedFontInfo)var3.next();
               Iterator var5 = fontInfo.getFontTriplets().iterator();

               while(var5.hasNext()) {
                  FontTriplet triplet = (FontTriplet)var5.next();
                  if (matcher.matches(triplet)) {
                     fontInfo.setEmbedded(false);
                     break;
                  }
               }
            }

            return;
         }
      }
   }

   public void autoDetectFonts(boolean autoDetectFonts, FontAdder fontAdder, boolean strict, FontEventListener listener, List fontInfoList) throws FOPException {
      if (autoDetectFonts) {
         this.fontDetector.detect(this, fontAdder, strict, listener, fontInfoList);
      }

   }
}
