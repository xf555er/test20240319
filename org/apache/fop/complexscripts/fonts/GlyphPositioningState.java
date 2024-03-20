package org.apache.fop.complexscripts.fonts;

import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.ScriptContextTester;

public class GlyphPositioningState extends GlyphProcessingState {
   private int fontSize;
   private int[] widths;
   private int[][] adjustments;
   private boolean adjusted;

   public GlyphPositioningState() {
   }

   public GlyphPositioningState(GlyphSequence gs, String script, String language, String feature, int fontSize, int[] widths, int[][] adjustments, ScriptContextTester sct) {
      super(gs, script, language, feature, sct);
      this.fontSize = fontSize;
      this.widths = widths;
      this.adjustments = adjustments;
   }

   public GlyphPositioningState(GlyphPositioningState ps) {
      super(ps);
      this.fontSize = ps.fontSize;
      this.widths = ps.widths;
      this.adjustments = ps.adjustments;
   }

   public GlyphPositioningState reset(GlyphSequence gs, String script, String language, String feature, int fontSize, int[] widths, int[][] adjustments, ScriptContextTester sct) {
      super.reset(gs, script, language, feature, sct);
      this.fontSize = fontSize;
      this.widths = widths;
      this.adjustments = adjustments;
      this.adjusted = false;
      return this;
   }

   public int getWidth(int gi) {
      return this.widths != null && gi < this.widths.length ? this.widths[gi] : 0;
   }

   public boolean adjust(GlyphPositioningTable.Value v) {
      return this.adjust(v, 0);
   }

   public boolean adjust(GlyphPositioningTable.Value v, int offset) {
      assert v != null;

      if (this.index + offset < this.indexLast) {
         return v.adjust(this.adjustments[this.index + offset], this.fontSize);
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   public int[] getAdjustment() {
      return this.getAdjustment(0);
   }

   public int[] getAdjustment(int offset) throws IndexOutOfBoundsException {
      if (this.index + offset < this.indexLast) {
         return this.adjustments[this.index + offset];
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   public boolean apply(GlyphPositioningSubtable st) {
      assert st != null;

      this.updateSubtableState(st);
      boolean applied = st.position(this);
      return applied;
   }

   public boolean apply(GlyphTable.RuleLookup[] lookups, int nig) {
      if (lookups != null && lookups.length > 0) {
         GlyphTable.RuleLookup[] var3 = lookups;
         int var4 = lookups.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            GlyphTable.RuleLookup l = var3[var5];
            if (l != null) {
               GlyphTable.LookupTable lt = l.getLookup();
               if (lt != null) {
                  GlyphPositioningState ps = new GlyphPositioningState(this);
                  if (lt.position(ps, l.getSequenceIndex())) {
                     this.setAdjusted(true);
                  }
               }
            }
         }

         this.consume(nig);
         return true;
      } else {
         return false;
      }
   }

   public void applyDefault() {
      super.applyDefault();
   }

   public void setAdjusted(boolean adjusted) {
      this.adjusted = adjusted;
   }

   public boolean getAdjusted() {
      return this.adjusted;
   }
}
