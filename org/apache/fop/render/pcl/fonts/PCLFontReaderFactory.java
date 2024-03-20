package org.apache.fop.render.pcl.fonts;

import java.io.IOException;
import org.apache.fop.fonts.CIDFontType;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.render.java2d.CustomFontMetricsMapper;
import org.apache.fop.render.pcl.fonts.truetype.PCLTTFFontReader;

public final class PCLFontReaderFactory {
   private PCLFontReaderFactory() {
   }

   public static PCLFontReader createInstance(Typeface font) throws IOException {
      return font.getFontType() != FontType.TRUETYPE && !isCIDType2(font) ? null : new PCLTTFFontReader(font);
   }

   private static boolean isCIDType2(Typeface font) {
      CustomFontMetricsMapper fontMetrics = (CustomFontMetricsMapper)font;
      CustomFont customFont = (CustomFont)fontMetrics.getRealFont();
      if (customFont instanceof MultiByteFont) {
         return ((MultiByteFont)customFont).getCIDType() == CIDFontType.CIDTYPE2;
      } else {
         return false;
      }
   }
}
