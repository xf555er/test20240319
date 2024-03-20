package org.apache.fop.render.java2d;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontUtil;

public class InstalledFontCollection implements FontCollection {
   private static Log log = LogFactory.getLog(InstalledFontCollection.class);
   private static final Set HARDCODED_FONT_NAMES = new HashSet();
   private final Java2DFontMetrics java2DFontMetrics;

   public InstalledFontCollection(Java2DFontMetrics java2DFontMetrics) {
      this.java2DFontMetrics = java2DFontMetrics;
   }

   public int setup(int start, FontInfo fontInfo) {
      int num = start;
      GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
      Font[] fonts = env.getAllFonts();
      Font[] var6 = fonts;
      int var7 = fonts.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         Font f = var6[var8];
         if (!HARDCODED_FONT_NAMES.contains(f.getName())) {
            if (log.isTraceEnabled()) {
               log.trace("AWT Font: " + f.getFontName() + ", family: " + f.getFamily() + ", PS: " + f.getPSName() + ", Name: " + f.getName() + ", Angle: " + f.getItalicAngle() + ", Style: " + f.getStyle());
            }

            String searchName = FontUtil.stripWhiteSpace(f.getName()).toLowerCase();
            String guessedStyle = FontUtil.guessStyle(searchName);
            int guessedWeight = FontUtil.guessWeight(searchName);
            ++num;
            String fontKey = "F" + num;
            int style = convertToAWTFontStyle(guessedStyle, guessedWeight);
            addFontMetricsMapper(fontInfo, f.getName(), fontKey, this.java2DFontMetrics, style);
            addFontTriplet(fontInfo, f.getName(), "normal", 400, fontKey);
            if (!f.getName().equals(f.getFamily())) {
               addFontTriplet(fontInfo, f.getFamily(), guessedStyle, guessedWeight, fontKey);
            }
         }
      }

      return num;
   }

   private static void addFontTriplet(FontInfo fontInfo, String fontName, String fontStyle, int fontWeight, String fontKey) {
      FontTriplet triplet = FontInfo.createFontKey(fontName, fontStyle, fontWeight);
      fontInfo.addFontProperties(fontKey, triplet);
   }

   private static void addFontMetricsMapper(FontInfo fontInfo, String family, String fontKey, Java2DFontMetrics java2DFontMetrics, int style) {
      FontMetricsMapper metric = new SystemFontMetricsMapper(family, style, java2DFontMetrics);
      fontInfo.addMetrics(fontKey, metric);
   }

   private static int convertToAWTFontStyle(String fontStyle, int fontWeight) {
      int style = 0;
      if (fontWeight >= 700) {
         style |= 1;
      }

      if (!"normal".equals(fontStyle)) {
         style |= 2;
      }

      return style;
   }

   static {
      HARDCODED_FONT_NAMES.add("any");
      HARDCODED_FONT_NAMES.add("sans-serif");
      HARDCODED_FONT_NAMES.add("serif");
      HARDCODED_FONT_NAMES.add("monospace");
      HARDCODED_FONT_NAMES.add("Helvetica");
      HARDCODED_FONT_NAMES.add("Times");
      HARDCODED_FONT_NAMES.add("Courier");
      HARDCODED_FONT_NAMES.add("Symbol");
      HARDCODED_FONT_NAMES.add("ZapfDingbats");
      HARDCODED_FONT_NAMES.add("Times Roman");
      HARDCODED_FONT_NAMES.add("Times-Roman");
      HARDCODED_FONT_NAMES.add("Computer-Modern-Typewriter");
   }
}
