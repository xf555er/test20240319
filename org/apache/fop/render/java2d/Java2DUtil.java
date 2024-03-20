package org.apache.fop.render.java2d;

import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.util.CharUtilities;

public final class Java2DUtil {
   private Java2DUtil() {
   }

   public static FontInfo buildDefaultJava2DBasedFontInfo(FontInfo fontInfo, FOUserAgent userAgent) {
      Java2DFontMetrics java2DFontMetrics = new Java2DFontMetrics();
      FontManager fontManager = userAgent.getFontManager();
      FontCollection[] fontCollections = new FontCollection[]{new Base14FontCollection(java2DFontMetrics), new InstalledFontCollection(java2DFontMetrics)};
      FontInfo fi = fontInfo != null ? fontInfo : new FontInfo();
      fi.setEventListener(new FontEventAdapter(userAgent.getEventBroadcaster()));
      fontManager.setup(fi, fontCollections);
      return fi;
   }

   public static GlyphVector createGlyphVector(String text, Graphics2D g2d, Font font, FontInfo fontInfo) {
      MultiByteFont multiByteFont = getMultiByteFont(font.getFontName(), fontInfo);
      return multiByteFont == null ? createGlyphVector(text, g2d) : createGlyphVectorMultiByteFont(text, g2d, multiByteFont);
   }

   private static GlyphVector createGlyphVector(String text, Graphics2D g2d) {
      StringBuilder sb = new StringBuilder(text.length());
      Iterator var3 = CharUtilities.codepointsIter(text).iterator();

      while(var3.hasNext()) {
         int cp = (Integer)var3.next();
         sb.appendCodePoint(cp <= 65535 ? cp : 35);
      }

      return g2d.getFont().createGlyphVector(g2d.getFontRenderContext(), sb.toString());
   }

   private static GlyphVector createGlyphVectorMultiByteFont(String text, Graphics2D g2d, MultiByteFont multiByteFont) {
      int[] glyphCodes = new int[text.length()];
      int currentIdx = 0;

      int cp;
      for(Iterator var5 = CharUtilities.codepointsIter(text).iterator(); var5.hasNext(); glyphCodes[currentIdx++] = multiByteFont.findGlyphIndex(cp)) {
         cp = (Integer)var5.next();
      }

      if (currentIdx != text.length()) {
         glyphCodes = Arrays.copyOf(glyphCodes, currentIdx);
      }

      return g2d.getFont().createGlyphVector(g2d.getFontRenderContext(), glyphCodes);
   }

   private static MultiByteFont getMultiByteFont(String fontName, FontInfo fontInfo) {
      Typeface tf = (Typeface)fontInfo.getFonts().get(fontName);
      if (tf instanceof CustomFontMetricsMapper) {
         tf = ((CustomFontMetricsMapper)tf).getRealFont();
      }

      if (tf instanceof LazyFont) {
         tf = ((LazyFont)tf).getRealFont();
      }

      return tf instanceof MultiByteFont ? (MultiByteFont)tf : null;
   }
}
