package org.apache.fop.complexscripts.scripts;

import org.apache.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.apache.fop.complexscripts.util.CharAssociation;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.ScriptContextTester;

public class DefaultScriptProcessor extends ScriptProcessor {
   private static final String[] GSUB_FEATURES = new String[]{"ccmp", "liga", "locl"};
   private static final String[] GPOS_FEATURES = new String[]{"kern", "mark", "mkmk"};

   DefaultScriptProcessor(String script) {
      super(script);
   }

   public String[] getSubstitutionFeatures() {
      return GSUB_FEATURES;
   }

   public ScriptContextTester getSubstitutionContextTester() {
      return null;
   }

   public String[] getPositioningFeatures() {
      return GPOS_FEATURES;
   }

   public ScriptContextTester getPositioningContextTester() {
      return null;
   }

   public GlyphSequence reorderCombiningMarks(GlyphDefinitionTable gdef, GlyphSequence gs, int[] unscaledWidths, int[][] gpa, String script, String language) {
      int ng = gs.getGlyphCount();
      int[] ga = gs.getGlyphArray(false);
      int nm = 0;

      for(int i = 0; i < ng; ++i) {
         int var10000 = ga[i];
         var10000 = unscaledWidths[i];
         if (this.isReorderedMark(gdef, ga, unscaledWidths, i)) {
            ++nm;
         }
      }

      if (nm > 0 && ng - nm > 0) {
         CharAssociation[] aa = gs.getAssociations(0, -1);
         int[] nga = new int[ng];
         int[][] npa = gpa != null ? new int[ng][] : (int[][])null;
         CharAssociation[] naa = new CharAssociation[ng];
         int k = 0;
         CharAssociation ba = null;
         int bg = -1;
         int[] bpa = null;

         for(int i = 0; i < ng; ++i) {
            int gid = ga[i];
            int[] pa = gpa != null ? gpa[i] : null;
            CharAssociation ca = aa[i];
            if (this.isReorderedMark(gdef, ga, unscaledWidths, i)) {
               nga[k] = gid;
               naa[k] = ca;
               if (npa != null) {
                  npa[k] = pa;
               }

               ++k;
            } else {
               if (bg != -1) {
                  nga[k] = bg;
                  naa[k] = ba;
                  if (npa != null) {
                     npa[k] = bpa;
                  }

                  ++k;
                  bg = -1;
                  ba = null;
                  bpa = null;
               }

               if (bg == -1) {
                  bg = gid;
                  ba = ca;
                  bpa = pa;
               }
            }
         }

         if (bg != -1) {
            nga[k] = bg;
            naa[k] = ba;
            if (npa != null) {
               npa[k] = bpa;
            }

            ++k;
         }

         assert k == ng;

         if (npa != null) {
            System.arraycopy(npa, 0, gpa, 0, ng);
         }

         return new GlyphSequence(gs, (int[])null, nga, (int[])null, (CharAssociation[])null, naa, (CharAssociation[])null);
      } else {
         return gs;
      }
   }

   protected boolean isReorderedMark(GlyphDefinitionTable gdef, int[] glyphs, int[] unscaledWidths, int index) {
      return gdef.isGlyphClass(glyphs[index], 3) && unscaledWidths[index] != 0;
   }
}
