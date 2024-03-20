package org.apache.fop.fonts;

import java.util.Iterator;
import java.util.List;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.base14.Courier;
import org.apache.fop.fonts.base14.CourierBold;
import org.apache.fop.fonts.base14.CourierBoldOblique;
import org.apache.fop.fonts.base14.CourierOblique;
import org.apache.fop.fonts.base14.Helvetica;
import org.apache.fop.fonts.base14.HelveticaBold;
import org.apache.fop.fonts.base14.HelveticaBoldOblique;
import org.apache.fop.fonts.base14.HelveticaOblique;
import org.apache.fop.fonts.base14.Symbol;
import org.apache.fop.fonts.base14.TimesBold;
import org.apache.fop.fonts.base14.TimesBoldItalic;
import org.apache.fop.fonts.base14.TimesItalic;
import org.apache.fop.fonts.base14.TimesRoman;
import org.apache.fop.fonts.base14.ZapfDingbats;

public final class FontSetup {
   private FontSetup() {
   }

   public static void setup(FontInfo fontInfo, boolean base14Kerning) {
      setup(fontInfo, (List)null, (InternalResourceResolver)null, base14Kerning);
   }

   public static void setup(FontInfo fontInfo, List embedFontInfoList, InternalResourceResolver resourceResolver, boolean base14Kerning) {
      fontInfo.addMetrics("F1", new Helvetica(base14Kerning));
      fontInfo.addMetrics("F2", new HelveticaOblique(base14Kerning));
      fontInfo.addMetrics("F3", new HelveticaBold(base14Kerning));
      fontInfo.addMetrics("F4", new HelveticaBoldOblique(base14Kerning));
      fontInfo.addMetrics("F5", new TimesRoman(base14Kerning));
      fontInfo.addMetrics("F6", new TimesItalic(base14Kerning));
      fontInfo.addMetrics("F7", new TimesBold(base14Kerning));
      fontInfo.addMetrics("F8", new TimesBoldItalic(base14Kerning));
      fontInfo.addMetrics("F9", new Courier(base14Kerning));
      fontInfo.addMetrics("F10", new CourierOblique(base14Kerning));
      fontInfo.addMetrics("F11", new CourierBold(base14Kerning));
      fontInfo.addMetrics("F12", new CourierBoldOblique(base14Kerning));
      fontInfo.addMetrics("F13", new Symbol());
      fontInfo.addMetrics("F14", new ZapfDingbats());
      fontInfo.addFontProperties("F5", (String)"any", "normal", 400);
      fontInfo.addFontProperties("F6", (String)"any", "italic", 400);
      fontInfo.addFontProperties("F6", (String)"any", "oblique", 400);
      fontInfo.addFontProperties("F7", (String)"any", "normal", 700);
      fontInfo.addFontProperties("F8", (String)"any", "italic", 700);
      fontInfo.addFontProperties("F8", (String)"any", "oblique", 700);
      fontInfo.addFontProperties("F1", (String)"sans-serif", "normal", 400);
      fontInfo.addFontProperties("F2", (String)"sans-serif", "oblique", 400);
      fontInfo.addFontProperties("F2", (String)"sans-serif", "italic", 400);
      fontInfo.addFontProperties("F3", (String)"sans-serif", "normal", 700);
      fontInfo.addFontProperties("F4", (String)"sans-serif", "oblique", 700);
      fontInfo.addFontProperties("F4", (String)"sans-serif", "italic", 700);
      fontInfo.addFontProperties("F1", (String)"SansSerif", "normal", 400);
      fontInfo.addFontProperties("F2", (String)"SansSerif", "oblique", 400);
      fontInfo.addFontProperties("F2", (String)"SansSerif", "italic", 400);
      fontInfo.addFontProperties("F3", (String)"SansSerif", "normal", 700);
      fontInfo.addFontProperties("F4", (String)"SansSerif", "oblique", 700);
      fontInfo.addFontProperties("F4", (String)"SansSerif", "italic", 700);
      fontInfo.addFontProperties("F5", (String)"serif", "normal", 400);
      fontInfo.addFontProperties("F6", (String)"serif", "oblique", 400);
      fontInfo.addFontProperties("F6", (String)"serif", "italic", 400);
      fontInfo.addFontProperties("F7", (String)"serif", "normal", 700);
      fontInfo.addFontProperties("F8", (String)"serif", "oblique", 700);
      fontInfo.addFontProperties("F8", (String)"serif", "italic", 700);
      fontInfo.addFontProperties("F9", (String)"monospace", "normal", 400);
      fontInfo.addFontProperties("F10", (String)"monospace", "oblique", 400);
      fontInfo.addFontProperties("F10", (String)"monospace", "italic", 400);
      fontInfo.addFontProperties("F11", (String)"monospace", "normal", 700);
      fontInfo.addFontProperties("F12", (String)"monospace", "oblique", 700);
      fontInfo.addFontProperties("F12", (String)"monospace", "italic", 700);
      fontInfo.addFontProperties("F9", (String)"Monospaced", "normal", 400);
      fontInfo.addFontProperties("F10", (String)"Monospaced", "oblique", 400);
      fontInfo.addFontProperties("F10", (String)"Monospaced", "italic", 400);
      fontInfo.addFontProperties("F11", (String)"Monospaced", "normal", 700);
      fontInfo.addFontProperties("F12", (String)"Monospaced", "oblique", 700);
      fontInfo.addFontProperties("F12", (String)"Monospaced", "italic", 700);
      fontInfo.addFontProperties("F1", (String)"Helvetica", "normal", 400);
      fontInfo.addFontProperties("F2", (String)"Helvetica", "oblique", 400);
      fontInfo.addFontProperties("F2", (String)"Helvetica", "italic", 400);
      fontInfo.addFontProperties("F3", (String)"Helvetica", "normal", 700);
      fontInfo.addFontProperties("F4", (String)"Helvetica", "oblique", 700);
      fontInfo.addFontProperties("F4", (String)"Helvetica", "italic", 700);
      fontInfo.addFontProperties("F5", (String)"Times", "normal", 400);
      fontInfo.addFontProperties("F6", (String)"Times", "oblique", 400);
      fontInfo.addFontProperties("F6", (String)"Times", "italic", 400);
      fontInfo.addFontProperties("F7", (String)"Times", "normal", 700);
      fontInfo.addFontProperties("F8", (String)"Times", "oblique", 700);
      fontInfo.addFontProperties("F8", (String)"Times", "italic", 700);
      fontInfo.addFontProperties("F9", (String)"Courier", "normal", 400);
      fontInfo.addFontProperties("F10", (String)"Courier", "oblique", 400);
      fontInfo.addFontProperties("F10", (String)"Courier", "italic", 400);
      fontInfo.addFontProperties("F11", (String)"Courier", "normal", 700);
      fontInfo.addFontProperties("F12", (String)"Courier", "oblique", 700);
      fontInfo.addFontProperties("F12", (String)"Courier", "italic", 700);
      fontInfo.addFontProperties("F13", (String)"Symbol", "normal", 400);
      fontInfo.addFontProperties("F14", (String)"ZapfDingbats", "normal", 400);
      fontInfo.addFontProperties("F5", (String)"Times-Roman", "normal", 400);
      fontInfo.addFontProperties("F6", (String)"Times-Roman", "oblique", 400);
      fontInfo.addFontProperties("F6", (String)"Times-Roman", "italic", 400);
      fontInfo.addFontProperties("F7", (String)"Times-Roman", "normal", 700);
      fontInfo.addFontProperties("F8", (String)"Times-Roman", "oblique", 700);
      fontInfo.addFontProperties("F8", (String)"Times-Roman", "italic", 700);
      fontInfo.addFontProperties("F5", (String)"Times Roman", "normal", 400);
      fontInfo.addFontProperties("F6", (String)"Times Roman", "oblique", 400);
      fontInfo.addFontProperties("F6", (String)"Times Roman", "italic", 400);
      fontInfo.addFontProperties("F7", (String)"Times Roman", "normal", 700);
      fontInfo.addFontProperties("F8", (String)"Times Roman", "oblique", 700);
      fontInfo.addFontProperties("F8", (String)"Times Roman", "italic", 700);
      fontInfo.addFontProperties("F9", (String)"Computer-Modern-Typewriter", "normal", 400);
      int startNum = true;
      addConfiguredFonts(fontInfo, embedFontInfoList, 15, resourceResolver, base14Kerning);
   }

   private static void addConfiguredFonts(FontInfo fontInfo, List embedFontInfoList, int num, InternalResourceResolver resourceResolver, boolean base14Kerning) {
      if (embedFontInfoList != null) {
         assert resourceResolver != null;

         String internalName = null;
         Iterator var6 = embedFontInfoList.iterator();

         while(var6.hasNext()) {
            EmbedFontInfo embedFontInfo = (EmbedFontInfo)var6.next();
            internalName = "F" + num;
            ++num;
            LazyFont font = new LazyFont(embedFontInfo, resourceResolver, false);
            fontInfo.addMetrics(internalName, font);
            List triplets = embedFontInfo.getFontTriplets();
            Iterator var10 = triplets.iterator();

            while(var10.hasNext()) {
               FontTriplet triplet = (FontTriplet)var10.next();
               fontInfo.addFontProperties(internalName, triplet);
            }
         }

      }
   }
}
