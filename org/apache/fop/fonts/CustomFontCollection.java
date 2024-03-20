package org.apache.fop.fonts;

import java.util.Iterator;
import java.util.List;
import org.apache.fop.apps.io.InternalResourceResolver;

public class CustomFontCollection implements FontCollection {
   private final List embedFontInfoList;
   private final InternalResourceResolver uriResolver;
   private final boolean useComplexScripts;

   public CustomFontCollection(InternalResourceResolver fontResolver, List customFonts, boolean useComplexScriptFeatures) {
      this.uriResolver = fontResolver;
      this.embedFontInfoList = customFonts;
      this.useComplexScripts = useComplexScriptFeatures;
   }

   public int setup(int num, FontInfo fontInfo) {
      if (this.embedFontInfoList == null) {
         return num;
      } else {
         String internalName = null;
         Iterator var4 = this.embedFontInfoList.iterator();

         while(var4.hasNext()) {
            EmbedFontInfo embedFontInfo = (EmbedFontInfo)var4.next();
            internalName = "F" + num;
            ++num;
            LazyFont font = new LazyFont(embedFontInfo, this.uriResolver, this.useComplexScripts);
            fontInfo.addMetrics(internalName, font);
            List triplets = embedFontInfo.getFontTriplets();
            Iterator var8 = triplets.iterator();

            while(var8.hasNext()) {
               FontTriplet triplet = (FontTriplet)var8.next();
               fontInfo.addFontProperties(internalName, triplet);
            }
         }

         return num;
      }
   }
}
