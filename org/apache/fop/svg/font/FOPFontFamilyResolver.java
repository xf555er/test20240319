package org.apache.fop.svg.font;

import org.apache.batik.bridge.FontFamilyResolver;

public interface FOPFontFamilyResolver extends FontFamilyResolver {
   FOPGVTFontFamily resolve(String var1);

   FOPGVTFontFamily getDefault();

   FOPGVTFontFamily getFamilyThatCanDisplay(char var1);
}
