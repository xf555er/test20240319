package org.apache.fop.complexscripts.fonts;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.fop.complexscripts.util.CharAssociation;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.GlyphTester;
import org.apache.fop.complexscripts.util.ScriptContextTester;

public class GlyphSubstitutionState extends GlyphProcessingState {
   private int[] alternatesIndex;
   private IntBuffer ogb;
   private List oal;
   private boolean predications;

   public GlyphSubstitutionState() {
   }

   public GlyphSubstitutionState(GlyphSequence gs, String script, String language, String feature, ScriptContextTester sct) {
      super(gs, script, language, feature, sct);
      this.ogb = IntBuffer.allocate(gs.getGlyphCount());
      this.oal = new ArrayList(gs.getGlyphCount());
      this.predications = gs.getPredications();
   }

   public GlyphSubstitutionState(GlyphSubstitutionState ss) {
      super(ss);
      this.ogb = IntBuffer.allocate(this.indexLast);
      this.oal = new ArrayList(this.indexLast);
   }

   public GlyphSubstitutionState reset(GlyphSequence gs, String script, String language, String feature, ScriptContextTester sct) {
      super.reset(gs, script, language, feature, sct);
      this.alternatesIndex = null;
      this.ogb = IntBuffer.allocate(gs.getGlyphCount());
      this.oal = new ArrayList(gs.getGlyphCount());
      this.predications = gs.getPredications();
      return this;
   }

   public void setAlternates(int[] alternates) {
      this.alternatesIndex = alternates;
   }

   public int getAlternatesIndex(int ci) {
      if (this.alternatesIndex == null) {
         return 0;
      } else {
         return ci >= 0 && ci <= this.alternatesIndex.length ? this.alternatesIndex[ci] : 0;
      }
   }

   public void putGlyph(int glyph, CharAssociation a, Object predication) {
      if (!this.ogb.hasRemaining()) {
         this.ogb = growBuffer(this.ogb);
      }

      this.ogb.put(glyph);
      if (this.predications && predication != null) {
         a.setPredication(this.feature, predication);
      }

      this.oal.add(a);
   }

   public void putGlyphs(int[] glyphs, CharAssociation[] associations, Object predication) {
      assert glyphs != null;

      assert associations != null;

      assert associations.length >= glyphs.length;

      int i = 0;

      for(int n = glyphs.length; i < n; ++i) {
         this.putGlyph(glyphs[i], associations[i], predication);
      }

   }

   public GlyphSequence getOutput() {
      int position = this.ogb.position();
      if (position > 0) {
         this.ogb.limit(position);
         this.ogb.rewind();
         return new GlyphSequence(this.igs.getCharacters(), this.ogb, this.oal);
      } else {
         return this.igs;
      }
   }

   public boolean apply(GlyphSubstitutionSubtable st) {
      assert st != null;

      this.updateSubtableState(st);
      boolean applied = st.substitute(this);
      return applied;
   }

   public boolean apply(GlyphTable.RuleLookup[] lookups, int nig) {
      int nlg = this.indexLast - (this.index + nig);
      int nog = 0;
      if (lookups != null && lookups.length > 0) {
         GlyphTable.RuleLookup[] var5 = lookups;
         int var6 = lookups.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            GlyphTable.RuleLookup l = var5[var7];
            if (l != null) {
               GlyphTable.LookupTable lt = l.getLookup();
               if (lt != null) {
                  GlyphSubstitutionState ss = new GlyphSubstitutionState(this);
                  GlyphSequence gs = lt.substitute(ss, l.getSequenceIndex());
                  if (this.replaceInput(0, -1, gs)) {
                     nog = gs.getGlyphCount() - nlg;
                  }
               }
            }
         }

         this.putGlyphs(this.getGlyphs(0, nog, false, (GlyphTester)null, (int[])null, (int[])null), this.getAssociations(0, nog, false, (GlyphTester)null, (CharAssociation[])null, (int[])null), (Object)null);
         this.consume(nog);
         return true;
      } else {
         return false;
      }
   }

   public void applyDefault() {
      super.applyDefault();
      int gi = this.getGlyph();
      if (gi != 65535) {
         this.putGlyph(gi, this.getAssociation(), (Object)null);
      }

   }

   private static IntBuffer growBuffer(IntBuffer ib) {
      int capacity = ib.capacity();
      int capacityNew = capacity * 2;
      IntBuffer ibNew = IntBuffer.allocate(capacityNew);
      ib.rewind();
      return ibNew.put(ib);
   }
}
