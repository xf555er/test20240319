package org.apache.fop.svg.font;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import org.apache.batik.bridge.FontFace;
import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.Typeface;

public class FOPFontFamilyResolverImpl implements FOPFontFamilyResolver {
   private final FontInfo fontInfo;

   public FOPFontFamilyResolverImpl(FontInfo fontInfo) {
      this.fontInfo = fontInfo;
   }

   public FOPGVTFontFamily resolve(String familyName) {
      return this.resolve(familyName, new GVTFontFace(familyName));
   }

   public FOPGVTFontFamily resolve(String familyName, FontFace fontFace) {
      return this.resolve(familyName, (GVTFontFace)FontFace.createFontFace(familyName, fontFace));
   }

   private FOPGVTFontFamily resolve(String familyName, GVTFontFace fontFace) {
      FOPGVTFontFamily gvtFontFamily = null;
      FontTriplet triplet = this.fontInfo.fontLookup((String)familyName, "normal", 400);
      if (this.fontInfo.hasFont(familyName, "normal", 400)) {
         gvtFontFamily = new FOPGVTFontFamily(this.fontInfo, familyName, triplet, fontFace);
      }

      return gvtFontFamily;
   }

   public GVTFontFamily loadFont(InputStream in, FontFace fontFace) throws Exception {
      throw new UnsupportedOperationException("Not implemented");
   }

   public FOPGVTFontFamily getDefault() {
      return this.resolve("any");
   }

   public FOPGVTFontFamily getFamilyThatCanDisplay(char c) {
      Map fonts = this.fontInfo.getFonts();
      Iterator var3 = fonts.values().iterator();

      Typeface font;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         font = (Typeface)var3.next();
      } while(!font.hasChar(c));

      String fontFamily = (String)font.getFamilyNames().iterator().next();
      return new FOPGVTFontFamily(this.fontInfo, fontFamily, new FontTriplet(fontFamily, "normal", 400), new GVTFontFace(fontFamily));
   }
}
