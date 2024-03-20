package org.apache.fop.afp.svg;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.fonts.DoubleByteFont;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.svg.font.FOPFontFamilyResolverImpl;
import org.apache.fop.svg.font.FOPGVTFontFamily;
import org.apache.fop.svg.font.FilteringFontFamilyResolver;

public class AFPFontFamilyResolver extends FilteringFontFamilyResolver {
   private final FontInfo fontInfo;
   private final AFPEventProducer eventProducer;

   public AFPFontFamilyResolver(FontInfo fontInfo, EventBroadcaster eventBroadCaster) {
      super(new FOPFontFamilyResolverImpl(fontInfo));
      this.fontInfo = fontInfo;
      this.eventProducer = AFPEventProducer.Provider.get(eventBroadCaster);
   }

   public FOPGVTFontFamily resolve(String familyName) {
      FOPGVTFontFamily fopGVTFontFamily = super.resolve(familyName);
      if (fopGVTFontFamily != null && fopGVTFontFamily.deriveFont(1.0F, (Map)(new HashMap())).getFont().getFontMetrics() instanceof DoubleByteFont) {
         this.notifyDBFontRejection(fopGVTFontFamily.getFamilyName());
         fopGVTFontFamily = null;
      }

      return fopGVTFontFamily;
   }

   public FOPGVTFontFamily getFamilyThatCanDisplay(char c) {
      Map fonts = this.fontInfo.getFonts();
      Iterator var3 = fonts.values().iterator();

      while(var3.hasNext()) {
         Typeface font = (Typeface)var3.next();
         if (font.hasChar(c) && !(font instanceof DoubleByteFont)) {
            String fontFamily = (String)font.getFamilyNames().iterator().next();
            if (!(font instanceof DoubleByteFont)) {
               return new FOPGVTFontFamily(this.fontInfo, fontFamily, new FontTriplet(fontFamily, "normal", 400), new GVTFontFace(fontFamily));
            }

            this.notifyDBFontRejection(font.getFontName());
         }
      }

      return null;
   }

   private void notifyDBFontRejection(String fontFamily) {
      this.eventProducer.invalidDBFontInSVG(this, fontFamily);
   }
}
