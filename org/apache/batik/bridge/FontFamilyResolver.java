package org.apache.batik.bridge;

import java.io.InputStream;
import org.apache.batik.gvt.font.GVTFontFamily;

public interface FontFamilyResolver {
   GVTFontFamily resolve(String var1);

   GVTFontFamily resolve(String var1, FontFace var2);

   GVTFontFamily loadFont(InputStream var1, FontFace var2) throws Exception;

   GVTFontFamily getDefault();

   GVTFontFamily getFamilyThatCanDisplay(char var1);
}
