package org.apache.fop.complexscripts.fonts;

import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.ScriptContextTester;

public abstract class GlyphPositioningSubtable extends GlyphSubtable implements GlyphPositioning {
   private static final GlyphPositioningState STATE = new GlyphPositioningState();

   protected GlyphPositioningSubtable(String id, int sequence, int flags, int format, GlyphCoverageTable coverage) {
      super(id, sequence, flags, format, coverage);
   }

   public int getTableType() {
      return 2;
   }

   public String getTypeName() {
      return GlyphPositioningTable.getLookupTypeName(this.getType());
   }

   public boolean isCompatible(GlyphSubtable subtable) {
      return subtable instanceof GlyphPositioningSubtable;
   }

   public boolean usesReverseScan() {
      return false;
   }

   public boolean position(GlyphPositioningState ps) {
      return false;
   }

   public static final boolean position(GlyphPositioningState ps, GlyphPositioningSubtable[] sta, int sequenceIndex) {
      int sequenceStart = ps.getPosition();

      for(boolean appliedOneShot = false; ps.hasNext(); ps.next()) {
         boolean applied = false;
         if (!appliedOneShot && ps.maybeApplicable()) {
            int i = 0;

            for(int n = sta.length; !applied && i < n; ++i) {
               if (sequenceIndex < 0) {
                  applied = ps.apply(sta[i]);
               } else if (ps.getPosition() == sequenceStart + sequenceIndex) {
                  applied = ps.apply(sta[i]);
                  if (applied) {
                     appliedOneShot = true;
                  }
               }
            }
         }

         if (!applied || !ps.didConsume()) {
            ps.applyDefault();
         }
      }

      return ps.getAdjusted();
   }

   public static final boolean position(GlyphSequence gs, String script, String language, String feature, int fontSize, GlyphPositioningSubtable[] sta, int[] widths, int[][] adjustments, ScriptContextTester sct) {
      synchronized(STATE) {
         return position(STATE.reset(gs, script, language, feature, fontSize, widths, adjustments, sct), sta, -1);
      }
   }
}
