package org.apache.fop.complexscripts.scripts;

import java.util.List;
import org.apache.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.apache.fop.complexscripts.fonts.GlyphTable;
import org.apache.fop.complexscripts.util.CharAssociation;
import org.apache.fop.complexscripts.util.GlyphContextTester;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.ScriptContextTester;
import org.apache.fop.fonts.MultiByteFont;

public class KhmerScriptProcessor extends IndicScriptProcessor {
   private GlyphSequence unprocessedGS;
   private List associations;
   private int[] chars;
   private ScriptContextTester scriptContextTester = new ScriptContextTester() {
      private GlyphContextTester tester = new GlyphContextTester() {
         public boolean test(String script, String language, String feature, GlyphSequence glyphSequence, int index, int flags) {
            CharAssociation charAssociation = (CharAssociation)KhmerScriptProcessor.this.associations.get(index);
            char vowelSignU = 6075;

            for(int i = charAssociation.getStart(); i < charAssociation.getEnd(); ++i) {
               if (KhmerScriptProcessor.this.chars[i] == vowelSignU) {
                  return false;
               }
            }

            return true;
         }
      };

      public GlyphContextTester getTester(String feature) {
         return this.tester;
      }
   };

   KhmerScriptProcessor(String script) {
      super(script);
   }

   protected Class getSyllabizerClass() {
      return KhmerSyllabizer.class;
   }

   public GlyphSequence reorderCombiningMarks(GlyphDefinitionTable gdef, GlyphSequence glyphSequence, int[] unscaledWidths, int[][] glyphPositionAdjustments, String script, String language) {
      return glyphSequence;
   }

   public CharSequence preProcess(CharSequence charSequence, MultiByteFont font, List associations) {
      this.unprocessedGS = font.charSequenceToGlyphSequence(charSequence, associations);
      return (new KhmerRenderer()).render(charSequence.toString());
   }

   public boolean position(GlyphSequence glyphSequence, String script, String language, int fontSize, GlyphTable.UseSpec[] useSpecs, int[] widths, int[][] adjustments, ScriptContextTester scriptContextTester) {
      glyphSequence.setUnprocessedGS(this.unprocessedGS);
      return super.position(glyphSequence, script, language, fontSize, useSpecs, widths, adjustments, scriptContextTester);
   }

   public GlyphSequence substitute(GlyphSequence glyphSequence, String script, String language, GlyphTable.UseSpec[] useSpecs, ScriptContextTester scriptContextTester) {
      glyphSequence = super.substitute(glyphSequence, script, language, useSpecs, scriptContextTester);
      this.associations = glyphSequence.getAssociations();
      this.chars = glyphSequence.getCharacters().array();
      return glyphSequence;
   }

   public ScriptContextTester getPositioningContextTester() {
      return this.scriptContextTester;
   }

   private static class KhmerSyllabizer extends IndicScriptProcessor.DefaultSyllabizer {
      KhmerSyllabizer(String script, String language) {
         super(script, language);
      }
   }
}
