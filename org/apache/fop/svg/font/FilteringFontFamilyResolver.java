package org.apache.fop.svg.font;

import java.io.InputStream;
import org.apache.batik.bridge.FontFace;
import org.apache.batik.gvt.font.GVTFontFamily;

public class FilteringFontFamilyResolver implements FOPFontFamilyResolver {
   private final FOPFontFamilyResolver delegate;

   public FilteringFontFamilyResolver(FOPFontFamilyResolver delegate) {
      this.delegate = delegate;
   }

   public FOPGVTFontFamily resolve(String familyName) {
      return this.delegate.resolve(familyName);
   }

   public GVTFontFamily resolve(String familyName, FontFace fontFace) {
      return this.delegate.resolve(familyName, fontFace);
   }

   public GVTFontFamily loadFont(InputStream in, FontFace fontFace) throws Exception {
      return this.delegate.loadFont(in, fontFace);
   }

   public FOPGVTFontFamily getDefault() {
      return this.delegate.getDefault();
   }

   public FOPGVTFontFamily getFamilyThatCanDisplay(char c) {
      return this.delegate.getFamilyThatCanDisplay(c);
   }
}
