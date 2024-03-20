package org.apache.fop.svg.font;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.batik.bridge.FontFace;
import org.apache.batik.bridge.FontFamilyResolver;
import org.apache.batik.gvt.font.GVTFontFamily;

public class AggregatingFontFamilyResolver implements FontFamilyResolver {
   private final List resolvers;

   public AggregatingFontFamilyResolver(FontFamilyResolver... resolvers) {
      this.resolvers = Arrays.asList(resolvers);
   }

   public GVTFontFamily resolve(String familyName) {
      Iterator var2 = this.resolvers.iterator();

      GVTFontFamily family;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         FontFamilyResolver resolver = (FontFamilyResolver)var2.next();
         family = resolver.resolve(familyName);
      } while(family == null);

      return family;
   }

   public GVTFontFamily resolve(String familyName, FontFace fontFace) {
      Iterator var3 = this.resolvers.iterator();

      GVTFontFamily family;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         FontFamilyResolver resolver = (FontFamilyResolver)var3.next();
         family = resolver.resolve(familyName, fontFace);
      } while(family == null);

      return family;
   }

   public GVTFontFamily loadFont(InputStream in, FontFace fontFace) throws Exception {
      Iterator var3 = this.resolvers.iterator();

      while(var3.hasNext()) {
         FontFamilyResolver resolver = (FontFamilyResolver)var3.next();

         try {
            return resolver.loadFont(in, fontFace);
         } catch (Exception var6) {
         }
      }

      return null;
   }

   public GVTFontFamily getDefault() {
      return this.resolve("any");
   }

   public GVTFontFamily getFamilyThatCanDisplay(char c) {
      Iterator var2 = this.resolvers.iterator();

      GVTFontFamily family;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         FontFamilyResolver resolver = (FontFamilyResolver)var2.next();
         family = resolver.getFamilyThatCanDisplay(c);
      } while(family == null);

      return family;
   }
}
