package org.apache.fop.svg;

import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.batik.bridge.SVGFontFamily;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.font.FOPGVTFont;
import org.apache.fop.svg.font.FOPGVTFontFamily;

public final class ACIUtils {
   private static final Log LOG = LogFactory.getLog(ACIUtils.class);

   private ACIUtils() {
   }

   public static Font[] findFontsForBatikACI(AttributedCharacterIterator aci, FontInfo fontInfo) {
      List fonts = new ArrayList();
      List gvtFonts = (List)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.GVT_FONT_FAMILIES);
      String style = toStyle((Float)aci.getAttribute(TextAttribute.POSTURE));
      int weight = toCSSWeight((Float)aci.getAttribute(TextAttribute.WEIGHT));
      float fontSize = (Float)aci.getAttribute(TextAttribute.SIZE);
      String firstFontFamily = null;
      GVTFont gvtFont = (GVTFont)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.GVT_FONT);
      if (gvtFont != null) {
         String gvtFontFamily = gvtFont.getFamilyName();
         if (gvtFont instanceof FOPGVTFont) {
            Font font = ((FOPGVTFont)gvtFont).getFont();
            if (LOG.isDebugEnabled()) {
               LOG.debug("Found a font that matches the GVT font: " + gvtFontFamily + ", " + weight + ", " + style + " -> " + font);
            }

            fonts.add(font);
         }

         firstFontFamily = gvtFontFamily;
      }

      if (gvtFonts != null) {
         boolean haveInstanceOfSVGFontFamily = false;
         Iterator var14 = gvtFonts.iterator();

         while(var14.hasNext()) {
            GVTFontFamily fontFamily = (GVTFontFamily)var14.next();
            if (fontFamily instanceof SVGFontFamily) {
               haveInstanceOfSVGFontFamily = true;
            } else if (fontFamily instanceof FOPGVTFontFamily) {
               Font font = ((FOPGVTFontFamily)fontFamily).deriveFont(fontSize, aci).getFont();
               if (LOG.isDebugEnabled()) {
                  LOG.debug("Found a font that matches the GVT font family: " + fontFamily.getFamilyName() + ", " + weight + ", " + style + " -> " + font);
               }

               fonts.add(font);
            }

            if (firstFontFamily == null) {
               firstFontFamily = fontFamily.getFamilyName();
            }
         }

         if (fonts.isEmpty() && haveInstanceOfSVGFontFamily) {
            fontInfo.notifyStrokingSVGTextAsShapes(firstFontFamily);
            return null;
         }
      }

      return fonts.isEmpty() ? null : (Font[])fonts.toArray(new Font[fonts.size()]);
   }

   public static int toCSSWeight(Float weight) {
      if (weight == null) {
         return 400;
      } else if (weight <= TextAttribute.WEIGHT_EXTRA_LIGHT) {
         return 100;
      } else if (weight <= TextAttribute.WEIGHT_LIGHT) {
         return 200;
      } else if (weight <= TextAttribute.WEIGHT_DEMILIGHT) {
         return 300;
      } else if (weight <= TextAttribute.WEIGHT_REGULAR) {
         return 400;
      } else if (weight <= TextAttribute.WEIGHT_SEMIBOLD) {
         return 500;
      } else if (weight < TextAttribute.WEIGHT_BOLD) {
         return 600;
      } else if (weight == TextAttribute.WEIGHT_BOLD) {
         return 700;
      } else if (weight <= TextAttribute.WEIGHT_HEAVY) {
         return 800;
      } else {
         return weight <= TextAttribute.WEIGHT_EXTRABOLD ? 900 : 900;
      }
   }

   public static String toStyle(Float posture) {
      return posture != null && (double)posture > 0.0 ? "italic" : "normal";
   }

   public static void dumpAttrs(AttributedCharacterIterator aci) {
      aci.first();
      Set entries = aci.getAttributes().entrySet();
      Iterator var2 = entries.iterator();

      while(var2.hasNext()) {
         Map.Entry entry = (Map.Entry)var2.next();
         if (entry.getValue() != null) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
         }
      }

      int start = aci.getBeginIndex();
      System.out.print("AttrRuns: ");

      while(aci.current() != '\uffff') {
         int end = aci.getRunLimit();
         System.out.print("" + (end - start) + ", ");
         aci.setIndex(end);
         if (start == end) {
            break;
         }

         start = end;
      }

      System.out.println("");
   }
}
