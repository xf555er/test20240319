package org.apache.fop.complexscripts.fonts;

import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.ScriptContextTester;

public abstract class GlyphSubstitutionSubtable extends GlyphSubtable implements GlyphSubstitution {
   private static final GlyphSubstitutionState STATE = new GlyphSubstitutionState();

   protected GlyphSubstitutionSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage) {
      super(id, sequence, flags, format, coverage);
   }

   public int getTableType() {
      return 1;
   }

   public String getTypeName() {
      return GlyphSubstitutionTable.getLookupTypeName(this.getType());
   }

   public boolean isCompatible(GlyphSubtable subtable) {
      return subtable instanceof GlyphSubstitutionSubtable;
   }

   public boolean usesReverseScan() {
      return false;
   }

   public boolean substitute(GlyphSubstitutionState ss) {
      return false;
   }

   public static final GlyphSequence substitute(GlyphSubstitutionState ss, GlyphSubstitutionSubtable[] sta, int sequenceIndex) {
      int sequenceStart = ss.getPosition();

      for(boolean appliedOneShot = false; ss.hasNext(); ss.next()) {
         boolean applied = false;
         if (!appliedOneShot && ss.maybeApplicable()) {
            int i = 0;

            for(int n = sta.length; !applied && i < n; ++i) {
               if (sequenceIndex < 0) {
                  applied = ss.apply(sta[i]);
               } else if (ss.getPosition() == sequenceStart + sequenceIndex) {
                  applied = ss.apply(sta[i]);
                  if (applied) {
                     appliedOneShot = true;
                  }
               }
            }
         }

         if (!applied || !ss.didConsume()) {
            ss.applyDefault();
         }
      }

      return ss.getOutput();
   }

   public static final GlyphSequence substitute(GlyphSequence gs, String script, String language, String feature, GlyphSubstitutionSubtable[] sta, ScriptContextTester sct) {
      synchronized(STATE) {
         return substitute(STATE.reset(gs, script, language, feature, sct), sta, -1);
      }
   }
}
