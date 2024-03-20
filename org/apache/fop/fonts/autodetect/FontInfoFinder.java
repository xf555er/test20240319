package org.apache.fop.fonts.autodetect;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.EncodingMode;
import org.apache.fop.fonts.FontCache;
import org.apache.fop.fonts.FontEventListener;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontUris;
import org.apache.fop.fonts.FontUtil;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OFFontLoader;
import org.apache.fop.fonts.truetype.TTFFile;

public class FontInfoFinder {
   private final Log log = LogFactory.getLog(FontInfoFinder.class);
   private FontEventListener eventListener;
   private final Pattern quotePattern = Pattern.compile("'");

   public void setEventListener(FontEventListener listener) {
      this.eventListener = listener;
   }

   private void generateTripletsFromFont(CustomFont customFont, Collection triplets) {
      if (this.log.isTraceEnabled()) {
         this.log.trace("Font: " + customFont.getFullName() + ", family: " + customFont.getFamilyNames() + ", PS: " + customFont.getFontName() + ", EmbedName: " + customFont.getEmbedFontName());
      }

      String strippedName = this.stripQuotes(customFont.getStrippedFontName());
      String fullName = this.stripQuotes(customFont.getFullName());
      String searchName = fullName.toLowerCase();
      String style = this.guessStyle(customFont, searchName);
      int guessedWeight = FontUtil.guessWeight(searchName);
      int weight = guessedWeight;
      triplets.add(new FontTriplet(fullName, "normal", 400));
      if (!fullName.equals(strippedName)) {
         triplets.add(new FontTriplet(strippedName, "normal", 400));
      }

      Set familyNames = customFont.getFamilyNames();
      Iterator var10 = familyNames.iterator();

      while(var10.hasNext()) {
         String familyName = (String)var10.next();
         familyName = this.stripQuotes(familyName);
         if (!fullName.equals(familyName)) {
            int priority = fullName.startsWith(familyName) ? fullName.length() - familyName.length() : fullName.length();
            triplets.add(new FontTriplet(familyName, style, weight, priority));
         }
      }

   }

   private String stripQuotes(String name) {
      return this.quotePattern.matcher(name).replaceAll("");
   }

   private String guessStyle(CustomFont customFont, String fontName) {
      String style = "normal";
      if (customFont.getItalicAngle() > 0) {
         style = "italic";
      } else {
         style = FontUtil.guessStyle(fontName);
      }

      return style;
   }

   private EmbedFontInfo getFontInfoFromCustomFont(URI fontUri, CustomFont customFont, FontCache fontCache, InternalResourceResolver resourceResolver) {
      FontUris fontUris = new FontUris(fontUri, (URI)null);
      List fontTripletList = new ArrayList();
      this.generateTripletsFromFont(customFont, fontTripletList);
      String subFontName = null;
      if (customFont instanceof MultiByteFont) {
         subFontName = ((MultiByteFont)customFont).getTTCName();
      }

      EmbedFontInfo fontInfo = new EmbedFontInfo(fontUris, customFont.isKerningEnabled(), customFont.isAdvancedEnabled(), fontTripletList, subFontName);
      fontInfo.setPostScriptName(customFont.getFontName());
      if (fontCache != null) {
         fontCache.addFont(fontInfo, resourceResolver);
      }

      return fontInfo;
   }

   public EmbedFontInfo[] find(URI fontURI, InternalResourceResolver resourceResolver, FontCache fontCache) {
      URI embedUri = resourceResolver.resolveFromBase(fontURI);
      String embedStr = embedUri.toASCIIString();
      boolean useKerning = true;
      boolean useAdvanced = true;
      long fileLastModified = -1L;
      EmbedFontInfo[] fontInfos;
      if (fontCache != null) {
         fileLastModified = FontCache.getLastModified(fontURI);
         if (fontCache.containsFont(embedStr)) {
            fontInfos = fontCache.getFontInfos(embedStr, fileLastModified);
            if (fontInfos != null) {
               return fontInfos;
            }
         } else if (fontCache.isFailedFont(embedStr, fileLastModified)) {
            if (this.log.isDebugEnabled()) {
               this.log.debug("Skipping font file that failed to load previously: " + embedUri);
            }

            return null;
         }
      }

      fontInfos = null;
      FontUris fontUris;
      CustomFont customFont;
      if (fontURI.toASCIIString().toLowerCase().endsWith(".ttc")) {
         fontUris = null;
         InputStream in = null;

         List ttcNames;
         label189: {
            FontFileReader reader;
            try {
               try {
                  in = resourceResolver.getResource(fontURI);
                  TTFFile ttf = new TTFFile(false, false);
                  reader = new FontFileReader(in);
                  ttcNames = ttf.getTTCnames(reader);
                  break label189;
               } catch (Exception var22) {
                  if (this.eventListener != null) {
                     this.eventListener.fontLoadingErrorAtAutoDetection(this, fontURI.toASCIIString(), var22);
                  }
               }

               reader = null;
            } finally {
               IOUtils.closeQuietly((InputStream)in);
            }

            return reader;
         }

         ArrayList embedFontInfoList = new ArrayList();
         Iterator var29 = ttcNames.iterator();

         while(true) {
            while(true) {
               if (!var29.hasNext()) {
                  return (EmbedFontInfo[])embedFontInfoList.toArray(new EmbedFontInfo[embedFontInfoList.size()]);
               }

               String fontName = (String)var29.next();
               if (this.log.isDebugEnabled()) {
                  this.log.debug("Loading " + fontName);
               }

               try {
                  OFFontLoader ttfLoader = new OFFontLoader(fontURI, fontName, true, EmbeddingMode.AUTO, EncodingMode.AUTO, useKerning, useAdvanced, resourceResolver, false, false, true);
                  customFont = ttfLoader.getFont();
                  if (this.eventListener != null) {
                     customFont.setEventListener(this.eventListener);
                  }
                  break;
               } catch (Exception var21) {
                  if (fontCache != null) {
                     fontCache.registerFailedFont(embedUri.toASCIIString(), fileLastModified);
                  }

                  if (this.eventListener != null) {
                     this.eventListener.fontLoadingErrorAtAutoDetection(this, embedUri.toASCIIString(), var21);
                  }
               }
            }

            EmbedFontInfo fi = this.getFontInfoFromCustomFont(fontURI, customFont, fontCache, resourceResolver);
            if (fi != null) {
               embedFontInfoList.add(fi);
            }
         }
      } else {
         try {
            fontUris = new FontUris(fontURI, (URI)null);
            customFont = FontLoader.loadFont(fontUris, (String)null, true, EmbeddingMode.AUTO, EncodingMode.AUTO, useKerning, useAdvanced, resourceResolver, false, false, true);
            if (this.eventListener != null) {
               customFont.setEventListener(this.eventListener);
            }
         } catch (Exception var24) {
            if (fontCache != null) {
               fontCache.registerFailedFont(embedUri.toASCIIString(), fileLastModified);
            }

            if (this.eventListener != null) {
               this.eventListener.fontLoadingErrorAtAutoDetection(this, embedUri.toASCIIString(), var24);
            }

            return null;
         }

         EmbedFontInfo fi = this.getFontInfoFromCustomFont(fontURI, customFont, fontCache, resourceResolver);
         return fi != null ? new EmbedFontInfo[]{fi} : null;
      }
   }
}
