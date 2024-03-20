package org.apache.fop.fonts.substitute;

import org.apache.fop.apps.FOPException;
import org.apache.fop.configuration.Configuration;

public class FontSubstitutionsConfigurator {
   private Configuration cfg;

   public FontSubstitutionsConfigurator(Configuration cfg) {
      this.cfg = cfg;
   }

   private static FontQualifier getQualfierFromConfiguration(Configuration cfg) throws FOPException {
      String fontFamily = cfg.getAttribute("font-family", (String)null);
      if (fontFamily == null) {
         throw new FOPException("substitution qualifier must have a font-family");
      } else {
         FontQualifier qualifier = new FontQualifier();
         qualifier.setFontFamily(fontFamily);
         String fontWeight = cfg.getAttribute("font-weight", (String)null);
         if (fontWeight != null) {
            qualifier.setFontWeight(fontWeight);
         }

         String fontStyle = cfg.getAttribute("font-style", (String)null);
         if (fontStyle != null) {
            qualifier.setFontStyle(fontStyle);
         }

         return qualifier;
      }
   }

   public void configure(FontSubstitutions substitutions) throws FOPException {
      Configuration[] substitutionCfgs = this.cfg.getChildren("substitution");
      Configuration[] var3 = substitutionCfgs;
      int var4 = substitutionCfgs.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Configuration substitutionCfg = var3[var5];
         Configuration fromCfg = substitutionCfg.getChild("from", false);
         if (fromCfg == null) {
            throw new FOPException("'substitution' element without child 'from' element");
         }

         Configuration toCfg = substitutionCfg.getChild("to", false);
         if (fromCfg == null) {
            throw new FOPException("'substitution' element without child 'to' element");
         }

         FontQualifier fromQualifier = getQualfierFromConfiguration(fromCfg);
         FontQualifier toQualifier = getQualfierFromConfiguration(toCfg);
         FontSubstitution substitution = new FontSubstitution(fromQualifier, toQualifier);
         substitutions.add(substitution);
      }

   }
}
