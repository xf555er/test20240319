package org.apache.fop.complexscripts.scripts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.apache.fop.complexscripts.fonts.GlyphPositioningTable;
import org.apache.fop.complexscripts.fonts.GlyphSubstitutionTable;
import org.apache.fop.complexscripts.fonts.GlyphTable;
import org.apache.fop.complexscripts.util.CharScript;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.ScriptContextTester;
import org.apache.fop.fonts.MultiByteFont;

public abstract class ScriptProcessor {
   private final String script;
   private final Map assembledLookups;

   protected ScriptProcessor(String script) {
      if (script != null && script.length() != 0) {
         this.script = script;
         this.assembledLookups = new HashMap();
      } else {
         throw new IllegalArgumentException("script must be non-empty string");
      }
   }

   public final String getScript() {
      return this.script;
   }

   public abstract String[] getSubstitutionFeatures();

   public String[] getOptionalSubstitutionFeatures() {
      return new String[0];
   }

   public abstract ScriptContextTester getSubstitutionContextTester();

   public final GlyphSequence substitute(GlyphSubstitutionTable gsub, GlyphSequence gs, String script, String language, Map lookups) {
      return this.substitute(gs, script, language, this.assembleLookups(gsub, this.getSubstitutionFeatures(), lookups), this.getSubstitutionContextTester());
   }

   public GlyphSequence substitute(GlyphSequence gs, String script, String language, GlyphTable.UseSpec[] usa, ScriptContextTester sct) {
      assert usa != null;

      GlyphTable.UseSpec[] var6 = usa;
      int var7 = usa.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         GlyphTable.UseSpec us = var6[var8];
         gs = us.substitute(gs, script, language, sct);
      }

      return gs;
   }

   public GlyphSequence reorderCombiningMarks(GlyphDefinitionTable gdef, GlyphSequence gs, int[] unscaledWidths, int[][] gpa, String script, String language) {
      return gs;
   }

   public abstract String[] getPositioningFeatures();

   public String[] getOptionalPositioningFeatures() {
      return new String[0];
   }

   public abstract ScriptContextTester getPositioningContextTester();

   public final boolean position(GlyphPositioningTable gpos, GlyphSequence gs, String script, String language, int fontSize, Map lookups, int[] widths, int[][] adjustments) {
      return this.position(gs, script, language, fontSize, this.assembleLookups(gpos, this.getPositioningFeatures(), lookups), widths, adjustments, this.getPositioningContextTester());
   }

   public boolean position(GlyphSequence gs, String script, String language, int fontSize, GlyphTable.UseSpec[] usa, int[] widths, int[][] adjustments, ScriptContextTester sct) {
      assert usa != null;

      boolean adjusted = false;
      GlyphTable.UseSpec[] var10 = usa;
      int var11 = usa.length;

      for(int var12 = 0; var12 < var11; ++var12) {
         GlyphTable.UseSpec us = var10[var12];
         if (us.position(gs, script, language, fontSize, widths, adjustments, sct)) {
            adjusted = true;
         }
      }

      return adjusted;
   }

   public final GlyphTable.UseSpec[] assembleLookups(GlyphTable table, String[] features, Map lookups) {
      AssembledLookupsKey key = new AssembledLookupsKey(table, features, lookups);
      GlyphTable.UseSpec[] usa;
      return (usa = this.assembledLookupsGet(key)) != null ? usa : this.assembledLookupsPut(key, table.assembleLookups(features, lookups));
   }

   private GlyphTable.UseSpec[] assembledLookupsGet(AssembledLookupsKey key) {
      return (GlyphTable.UseSpec[])this.assembledLookups.get(key);
   }

   private GlyphTable.UseSpec[] assembledLookupsPut(AssembledLookupsKey key, GlyphTable.UseSpec[] usa) {
      this.assembledLookups.put(key, usa);
      return usa;
   }

   public static synchronized ScriptProcessor getInstance(String script, Map processors) {
      ScriptProcessor sp = null;

      assert processors != null;

      if ((sp = (ScriptProcessor)processors.get(script)) == null) {
         processors.put(script, sp = createProcessor(script));
      }

      return sp;
   }

   private static ScriptProcessor createProcessor(String script) {
      ScriptProcessor sp = null;
      int sc = CharScript.scriptCodeFromTag(script);
      if (sc == 160) {
         sp = new ArabicScriptProcessor(script);
      } else if (CharScript.isIndicScript(sc)) {
         sp = IndicScriptProcessor.makeProcessor(script);
      } else {
         sp = new DefaultScriptProcessor(script);
      }

      return (ScriptProcessor)sp;
   }

   public CharSequence preProcess(CharSequence charSequence, MultiByteFont font, List associations) {
      return charSequence;
   }

   private static class AssembledLookupsKey {
      private final GlyphTable table;
      private final String[] features;
      private final Map lookups;

      AssembledLookupsKey(GlyphTable table, String[] features, Map lookups) {
         this.table = table;
         this.features = features;
         this.lookups = lookups;
      }

      public int hashCode() {
         int hc = 0;
         hc = 7 * hc + (hc ^ this.table.hashCode());
         hc = 11 * hc + (hc ^ Arrays.hashCode(this.features));
         hc = 17 * hc + (hc ^ this.lookups.hashCode());
         return hc;
      }

      public boolean equals(Object o) {
         if (o instanceof AssembledLookupsKey) {
            AssembledLookupsKey k = (AssembledLookupsKey)o;
            if (!this.table.equals(k.table)) {
               return false;
            } else {
               return !Arrays.equals(this.features, k.features) ? false : this.lookups.equals(k.lookups);
            }
         } else {
            return false;
         }
      }
   }
}
