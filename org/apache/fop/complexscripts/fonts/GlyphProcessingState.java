package org.apache.fop.complexscripts.fonts;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.fop.complexscripts.util.CharAssociation;
import org.apache.fop.complexscripts.util.GlyphContextTester;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.complexscripts.util.GlyphTester;
import org.apache.fop.complexscripts.util.ScriptContextTester;

public class GlyphProcessingState {
   protected GlyphDefinitionTable gdef;
   protected String script;
   protected String language;
   protected String feature;
   protected GlyphSequence igs;
   protected int index;
   protected int indexLast;
   protected int consumed;
   protected int lookupFlags;
   protected int classMatchSet;
   protected ScriptContextTester sct;
   protected GlyphContextTester gct;
   protected GlyphTester ignoreBase;
   protected GlyphTester ignoreLigature;
   protected GlyphTester ignoreMark;
   protected GlyphTester ignoreDefault;
   private GlyphSubtable subtable;

   public GlyphProcessingState() {
   }

   protected GlyphProcessingState(GlyphSequence gs, String script, String language, String feature, ScriptContextTester sct) {
      this.script = script;
      this.language = language;
      this.feature = feature;
      this.igs = gs;
      this.indexLast = gs.getGlyphCount();
      this.sct = sct;
      this.gct = sct != null ? sct.getTester(feature) : null;
      this.ignoreBase = new GlyphTester() {
         public boolean test(int gi, int flags) {
            return GlyphProcessingState.this.isIgnoredBase(gi, flags);
         }
      };
      this.ignoreLigature = new GlyphTester() {
         public boolean test(int gi, int flags) {
            return GlyphProcessingState.this.isIgnoredLigature(gi, flags);
         }
      };
      this.ignoreMark = new GlyphTester() {
         public boolean test(int gi, int flags) {
            return GlyphProcessingState.this.isIgnoredMark(gi, flags);
         }
      };
   }

   protected GlyphProcessingState(GlyphProcessingState s) {
      this(new GlyphSequence(s.igs), s.script, s.language, s.feature, s.sct);
      this.setPosition(s.index);
   }

   protected GlyphProcessingState reset(GlyphSequence gs, String script, String language, String feature, ScriptContextTester sct) {
      this.gdef = null;
      this.script = script;
      this.language = language;
      this.feature = feature;
      this.igs = gs;
      this.index = 0;
      this.indexLast = gs.getGlyphCount();
      this.consumed = 0;
      this.lookupFlags = 0;
      this.classMatchSet = 0;
      this.sct = sct;
      this.gct = sct != null ? sct.getTester(feature) : null;
      this.ignoreBase = new GlyphTester() {
         public boolean test(int gi, int flags) {
            return GlyphProcessingState.this.isIgnoredBase(gi, flags);
         }
      };
      this.ignoreLigature = new GlyphTester() {
         public boolean test(int gi, int flags) {
            return GlyphProcessingState.this.isIgnoredLigature(gi, flags);
         }
      };
      this.ignoreMark = new GlyphTester() {
         public boolean test(int gi, int flags) {
            return GlyphProcessingState.this.isIgnoredMark(gi, flags);
         }
      };
      this.ignoreDefault = null;
      this.subtable = null;
      return this;
   }

   public void setGDEF(GlyphDefinitionTable gdef) {
      if (this.gdef == null) {
         this.gdef = gdef;
      } else if (gdef == null) {
         this.gdef = null;
      }

   }

   public GlyphDefinitionTable getGDEF() {
      return this.gdef;
   }

   public void setLookupFlags(int flags) {
      if (this.lookupFlags == 0) {
         this.lookupFlags = flags;
      } else if (flags == 0) {
         this.lookupFlags = 0;
      }

   }

   public int getLookupFlags() {
      return this.lookupFlags;
   }

   public int getClassMatchSet(int gi) {
      return 0;
   }

   public void setIgnoreDefault(GlyphTester ignoreDefault) {
      if (this.ignoreDefault == null) {
         this.ignoreDefault = ignoreDefault;
      } else if (ignoreDefault == null) {
         this.ignoreDefault = null;
      }

   }

   public GlyphTester getIgnoreDefault() {
      return this.ignoreDefault;
   }

   public void updateSubtableState(GlyphSubtable st) {
      if (this.subtable != st) {
         this.setGDEF(st.getGDEF());
         this.setLookupFlags(st.getFlags());
         this.setIgnoreDefault(this.getIgnoreTester(this.getLookupFlags()));
         this.subtable = st;
      }

   }

   public int getPosition() {
      return this.index;
   }

   public void setPosition(int index) throws IndexOutOfBoundsException {
      if (index >= 0 && index <= this.indexLast) {
         this.index = index;
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   public int getLastPosition() {
      return this.indexLast;
   }

   public boolean hasNext() {
      return this.hasNext(1);
   }

   public boolean hasNext(int count) {
      return this.index + count <= this.indexLast;
   }

   public int next() {
      if (this.index < this.indexLast) {
         if (this.consumed == 0) {
            this.consumed = 1;
         }

         this.index += this.consumed;
         this.consumed = 0;
         if (this.index > this.indexLast) {
            this.index = this.indexLast;
         }
      }

      return this.index;
   }

   public boolean hasPrev() {
      return this.hasPrev(1);
   }

   public boolean hasPrev(int count) {
      return this.index - count >= 0;
   }

   public int prev() {
      if (this.index > 0) {
         if (this.consumed == 0) {
            this.consumed = 1;
         }

         this.index -= this.consumed;
         this.consumed = 0;
         if (this.index < 0) {
            this.index = 0;
         }
      }

      return this.index;
   }

   public int consume(int count) throws IndexOutOfBoundsException {
      if (this.consumed + count <= this.indexLast) {
         this.consumed += count;
         return this.consumed;
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   public boolean didConsume() {
      return this.consumed > 0;
   }

   public GlyphSequence getInput() {
      return this.igs;
   }

   public int getGlyph(int offset) throws IndexOutOfBoundsException {
      int i = this.index + offset;
      if (i >= 0 && i < this.indexLast) {
         return this.igs.getGlyph(i);
      } else {
         throw new IndexOutOfBoundsException("attempting index at " + i);
      }
   }

   public int getUnprocessedGlyph(int offset) throws IndexOutOfBoundsException {
      int i = this.index + offset;
      if (i >= 0 && i < this.indexLast) {
         return this.igs.getUnprocessedGlyph(i);
      } else {
         throw new IndexOutOfBoundsException("Attempting to process glyph at index " + i);
      }
   }

   public int getGlyph() throws IndexOutOfBoundsException {
      return this.getGlyph(0);
   }

   public void setGlyph(int offset, int glyph) throws IndexOutOfBoundsException {
      int i = this.index + offset;
      if (i >= 0 && i < this.indexLast) {
         this.igs.setGlyph(i, glyph);
      } else {
         throw new IndexOutOfBoundsException("attempting index at " + i);
      }
   }

   public CharAssociation getAssociation(int offset) throws IndexOutOfBoundsException {
      int i = this.index + offset;
      if (i >= 0 && i < this.indexLast) {
         return this.igs.getAssociation(i);
      } else {
         throw new IndexOutOfBoundsException("attempting index at " + i);
      }
   }

   public CharAssociation getAssociation() throws IndexOutOfBoundsException {
      return this.getAssociation(0);
   }

   public int[] getGlyphs(int offset, int count, boolean reverseOrder, GlyphTester ignoreTester, int[] glyphs, int[] counts) throws IndexOutOfBoundsException {
      if (count < 0) {
         count = this.getGlyphsAvailable(offset, reverseOrder, ignoreTester)[0];
      }

      int start = this.index + offset;
      if (start < 0) {
         throw new IndexOutOfBoundsException("will attempt index at " + start);
      } else if (!reverseOrder && start + count > this.indexLast) {
         throw new IndexOutOfBoundsException("will attempt index at " + (start + count));
      } else if (reverseOrder && start + 1 < count) {
         throw new IndexOutOfBoundsException("will attempt index at " + (start - count));
      } else {
         if (glyphs == null) {
            glyphs = new int[count];
         } else if (glyphs.length != count) {
            throw new IllegalArgumentException("glyphs array is non-null, but its length (" + glyphs.length + "), is not equal to count (" + count + ")");
         }

         return !reverseOrder ? this.getGlyphsForward(start, count, ignoreTester, glyphs, counts) : this.getGlyphsReverse(start, count, ignoreTester, glyphs, counts);
      }
   }

   private int[] getGlyphsForward(int start, int count, GlyphTester ignoreTester, int[] glyphs, int[] counts) throws IndexOutOfBoundsException {
      int counted = 0;
      int ignored = 0;
      int i = start;

      for(int n = this.indexLast; i < n && counted < count; ++i) {
         int gi = this.getGlyph(i - this.index);
         if (gi == 65535) {
            ++ignored;
         } else if (ignoreTester != null && ignoreTester.test(gi, this.getLookupFlags())) {
            ++ignored;
         } else {
            glyphs[counted++] = gi;
         }
      }

      if (counts != null && counts.length > 1) {
         counts[0] = counted;
         counts[1] = ignored;
      }

      return glyphs;
   }

   private int[] getGlyphsReverse(int start, int count, GlyphTester ignoreTester, int[] glyphs, int[] counts) throws IndexOutOfBoundsException {
      int counted = 0;
      int ignored = 0;

      for(int i = start; i >= 0 && counted < count; --i) {
         int gi = this.getGlyph(i - this.index);
         if (gi == 65535) {
            ++ignored;
         } else if (ignoreTester != null && ignoreTester.test(gi, this.getLookupFlags())) {
            ++ignored;
         } else {
            glyphs[counted++] = gi;
         }
      }

      if (counts != null && counts.length > 1) {
         counts[0] = counted;
         counts[1] = ignored;
      }

      return glyphs;
   }

   public int[] getGlyphs(int offset, int count, int[] glyphs, int[] counts) throws IndexOutOfBoundsException {
      return this.getGlyphs(offset, count, offset < 0, this.ignoreDefault, glyphs, counts);
   }

   public int[] getGlyphs() throws IndexOutOfBoundsException {
      return this.getGlyphs(0, this.indexLast - this.index, false, (GlyphTester)null, (int[])null, (int[])null);
   }

   public int[] getIgnoredGlyphs(int offset, int count, boolean reverseOrder, GlyphTester ignoreTester, int[] glyphs, int[] counts) throws IndexOutOfBoundsException {
      return this.getGlyphs(offset, count, reverseOrder, new NotGlyphTester(ignoreTester), glyphs, counts);
   }

   public int[] getIgnoredGlyphs(int offset, int count) throws IndexOutOfBoundsException {
      return this.getIgnoredGlyphs(offset, count, offset < 0, this.ignoreDefault, (int[])null, (int[])null);
   }

   public boolean isIgnoredGlyph(int offset, GlyphTester ignoreTester) throws IndexOutOfBoundsException {
      return ignoreTester != null && ignoreTester.test(this.getGlyph(offset), this.getLookupFlags());
   }

   public boolean isIgnoredGlyph(int offset) throws IndexOutOfBoundsException {
      return this.isIgnoredGlyph(offset, this.ignoreDefault);
   }

   public boolean isIgnoredGlyph() throws IndexOutOfBoundsException {
      return this.isIgnoredGlyph(this.getPosition());
   }

   public int[] getGlyphsAvailable(int offset, boolean reverseOrder, GlyphTester ignoreTester) throws IndexOutOfBoundsException {
      int start = this.index + offset;
      if (start >= 0 && start <= this.indexLast) {
         return !reverseOrder ? this.getGlyphsAvailableForward(start, ignoreTester) : this.getGlyphsAvailableReverse(start, ignoreTester);
      } else {
         return new int[]{0, 0};
      }
   }

   private int[] getGlyphsAvailableForward(int start, GlyphTester ignoreTester) throws IndexOutOfBoundsException {
      int counted = 0;
      int ignored = 0;
      if (ignoreTester == null) {
         counted = this.indexLast - start;
      } else {
         int i = start;

         for(int n = this.indexLast; i < n; ++i) {
            int gi = this.getGlyph(i - this.index);
            if (gi == 65535) {
               ++ignored;
            } else if (ignoreTester.test(gi, this.getLookupFlags())) {
               ++ignored;
            } else {
               ++counted;
            }
         }
      }

      return new int[]{counted, ignored};
   }

   private int[] getGlyphsAvailableReverse(int start, GlyphTester ignoreTester) throws IndexOutOfBoundsException {
      int counted = 0;
      int ignored = 0;
      if (ignoreTester == null) {
         counted = start + 1;
      } else {
         for(int i = start; i >= 0; --i) {
            int gi = this.getGlyph(i - this.index);
            if (gi == 65535) {
               ++ignored;
            } else if (ignoreTester.test(gi, this.getLookupFlags())) {
               ++ignored;
            } else {
               ++counted;
            }
         }
      }

      return new int[]{counted, ignored};
   }

   public int[] getGlyphsAvailable(int offset, boolean reverseOrder) throws IndexOutOfBoundsException {
      return this.getGlyphsAvailable(offset, reverseOrder, this.ignoreDefault);
   }

   public int[] getGlyphsAvailable(int offset) throws IndexOutOfBoundsException {
      return this.getGlyphsAvailable(offset, offset < 0);
   }

   public CharAssociation[] getAssociations(int offset, int count, boolean reverseOrder, GlyphTester ignoreTester, CharAssociation[] associations, int[] counts) throws IndexOutOfBoundsException {
      if (count < 0) {
         count = this.getGlyphsAvailable(offset, reverseOrder, ignoreTester)[0];
      }

      int start = this.index + offset;
      if (start < 0) {
         throw new IndexOutOfBoundsException("will attempt index at " + start);
      } else if (!reverseOrder && start + count > this.indexLast) {
         throw new IndexOutOfBoundsException("will attempt index at " + (start + count));
      } else if (reverseOrder && start + 1 < count) {
         throw new IndexOutOfBoundsException("will attempt index at " + (start - count));
      } else {
         if (associations == null) {
            associations = new CharAssociation[count];
         } else if (associations.length != count) {
            throw new IllegalArgumentException("associations array is non-null, but its length (" + associations.length + "), is not equal to count (" + count + ")");
         }

         return !reverseOrder ? this.getAssociationsForward(start, count, ignoreTester, associations, counts) : this.getAssociationsReverse(start, count, ignoreTester, associations, counts);
      }
   }

   private CharAssociation[] getAssociationsForward(int start, int count, GlyphTester ignoreTester, CharAssociation[] associations, int[] counts) throws IndexOutOfBoundsException {
      int counted = 0;
      int ignored = 0;
      int i = start;
      int n = this.indexLast;

      for(int k = 0; i < n; ++i) {
         int gi = this.getGlyph(i - this.index);
         if (gi == 65535) {
            ++ignored;
         } else if (ignoreTester != null && ignoreTester.test(gi, this.getLookupFlags())) {
            ++ignored;
         } else {
            if (k >= count) {
               break;
            }

            associations[k++] = this.getAssociation(i - this.index);
            ++counted;
         }
      }

      if (counts != null && counts.length > 1) {
         counts[0] = counted;
         counts[1] = ignored;
      }

      return associations;
   }

   private CharAssociation[] getAssociationsReverse(int start, int count, GlyphTester ignoreTester, CharAssociation[] associations, int[] counts) throws IndexOutOfBoundsException {
      int counted = 0;
      int ignored = 0;
      int i = start;

      for(int k = 0; i >= 0; --i) {
         int gi = this.getGlyph(i - this.index);
         if (gi == 65535) {
            ++ignored;
         } else if (ignoreTester != null && ignoreTester.test(gi, this.getLookupFlags())) {
            ++ignored;
         } else {
            if (k >= count) {
               break;
            }

            associations[k++] = this.getAssociation(i - this.index);
            ++counted;
         }
      }

      if (counts != null && counts.length > 1) {
         counts[0] = counted;
         counts[1] = ignored;
      }

      return associations;
   }

   public CharAssociation[] getAssociations(int offset, int count) throws IndexOutOfBoundsException {
      return this.getAssociations(offset, count, offset < 0, this.ignoreDefault, (CharAssociation[])null, (int[])null);
   }

   public CharAssociation[] getIgnoredAssociations(int offset, int count, boolean reverseOrder, GlyphTester ignoreTester, CharAssociation[] associations, int[] counts) throws IndexOutOfBoundsException {
      return this.getAssociations(offset, count, reverseOrder, new NotGlyphTester(ignoreTester), associations, counts);
   }

   public CharAssociation[] getIgnoredAssociations(int offset, int count) throws IndexOutOfBoundsException {
      return this.getIgnoredAssociations(offset, count, offset < 0, this.ignoreDefault, (CharAssociation[])null, (int[])null);
   }

   public boolean replaceInput(int offset, int count, GlyphSequence gs, int gsOffset, int gsCount) throws IndexOutOfBoundsException {
      int nig = this.igs != null ? this.igs.getGlyphCount() : 0;
      int position = this.getPosition() + offset;
      if (position < 0) {
         position = 0;
      } else if (position > nig) {
         position = nig;
      }

      if (count < 0 || position + count > nig) {
         count = nig - position;
      }

      int nrg = gs != null ? gs.getGlyphCount() : 0;
      if (gsOffset < 0) {
         gsOffset = 0;
      } else if (gsOffset > nrg) {
         gsOffset = nrg;
      }

      if (gsCount < 0 || gsOffset + gsCount > nrg) {
         gsCount = nrg - gsOffset;
      }

      int ng = nig + gsCount - count;
      IntBuffer gb = IntBuffer.allocate(ng);
      List al = new ArrayList(ng);
      int i = 0;

      int n;
      for(n = position; i < n; ++i) {
         gb.put(this.igs.getGlyph(i));
         al.add(this.igs.getAssociation(i));
      }

      i = gsOffset;

      for(n = gsOffset + gsCount; i < n; ++i) {
         gb.put(gs.getGlyph(i));
         al.add(gs.getAssociation(i));
      }

      i = position + count;

      for(n = nig; i < n; ++i) {
         gb.put(this.igs.getGlyph(i));
         al.add(this.igs.getAssociation(i));
      }

      gb.flip();

      assert this.igs != null;

      if (this.igs.compareGlyphs(gb) != 0) {
         this.igs = new GlyphSequence(this.igs.getCharacters(), gb, al);
         this.indexLast = gb.limit();
         return true;
      } else {
         return false;
      }
   }

   public boolean replaceInput(int offset, int count, GlyphSequence gs) throws IndexOutOfBoundsException {
      return this.replaceInput(offset, count, gs, 0, gs.getGlyphCount());
   }

   public int erase(int offset, int[] glyphs) throws IndexOutOfBoundsException {
      int start = this.index + offset;
      if (start >= 0 && start <= this.indexLast) {
         int erased = 0;
         int i = start - this.index;

         for(int n = this.indexLast - start; i < n; ++i) {
            int gi = this.getGlyph(i);
            if (gi == glyphs[erased]) {
               this.setGlyph(i, 65535);
               ++erased;
            }
         }

         return erased;
      } else {
         throw new IndexOutOfBoundsException("will attempt index at " + start);
      }
   }

   public boolean maybeApplicable() {
      return this.gct == null ? true : this.gct.test(this.script, this.language, this.feature, this.igs, this.index, this.getLookupFlags());
   }

   public void applyDefault() {
      ++this.consumed;
   }

   public boolean isBase(int gi) {
      return this.gdef != null ? this.gdef.isGlyphClass(gi, 1) : false;
   }

   public boolean isIgnoredBase(int gi, int flags) {
      return (flags & 2) != 0 && this.isBase(gi);
   }

   public boolean isLigature(int gi) {
      return this.gdef != null ? this.gdef.isGlyphClass(gi, 2) : false;
   }

   public boolean isIgnoredLigature(int gi, int flags) {
      return (flags & 4) != 0 && this.isLigature(gi);
   }

   public boolean isMark(int gi) {
      return this.gdef != null ? this.gdef.isGlyphClass(gi, 3) : false;
   }

   public boolean isIgnoredMark(int gi, int flags) {
      if ((flags & 8) != 0) {
         return this.isMark(gi);
      } else if ((flags & '\uff00') != 0) {
         int lac = (flags & '\uff00') >> 8;
         int gac = this.gdef.getMarkAttachClass(gi);
         return gac != lac;
      } else {
         return false;
      }
   }

   public GlyphTester getIgnoreTester(int flags) {
      if ((flags & 2) != 0) {
         return (flags & 12) == 0 ? this.ignoreBase : this.getCombinedIgnoreTester(flags);
      } else if ((flags & 4) != 0) {
         return (flags & 10) == 0 ? this.ignoreLigature : this.getCombinedIgnoreTester(flags);
      } else if ((flags & 8) != 0) {
         return (flags & 6) == 0 ? this.ignoreMark : this.getCombinedIgnoreTester(flags);
      } else {
         return null;
      }
   }

   public GlyphTester getCombinedIgnoreTester(int flags) {
      GlyphTester[] gta = new GlyphTester[3];
      int ngt = 0;
      if ((flags & 2) != 0) {
         gta[ngt++] = this.ignoreBase;
      }

      if ((flags & 4) != 0) {
         gta[ngt++] = this.ignoreLigature;
      }

      if ((flags & 8) != 0) {
         gta[ngt++] = this.ignoreMark;
      }

      return this.getCombinedOrTester(gta, ngt);
   }

   public GlyphTester getCombinedOrTester(GlyphTester[] gta, int ngt) {
      return ngt > 0 ? new CombinedOrGlyphTester(gta, ngt) : null;
   }

   public GlyphTester getCombinedAndTester(GlyphTester[] gta, int ngt) {
      return ngt > 0 ? new CombinedAndGlyphTester(gta, ngt) : null;
   }

   private static class NotGlyphTester implements GlyphTester {
      private GlyphTester gt;

      NotGlyphTester(GlyphTester gt) {
         this.gt = gt;
      }

      public boolean test(int gi, int flags) {
         return this.gt == null || !this.gt.test(gi, flags);
      }
   }

   private static class CombinedAndGlyphTester implements GlyphTester {
      private GlyphTester[] gta;
      private int ngt;

      CombinedAndGlyphTester(GlyphTester[] gta, int ngt) {
         this.gta = gta;
         this.ngt = ngt;
      }

      public boolean test(int gi, int flags) {
         int i = 0;

         for(int n = this.ngt; i < n; ++i) {
            GlyphTester gt = this.gta[i];
            if (gt != null && !gt.test(gi, flags)) {
               return false;
            }
         }

         return true;
      }
   }

   private static class CombinedOrGlyphTester implements GlyphTester {
      private GlyphTester[] gta;
      private int ngt;

      CombinedOrGlyphTester(GlyphTester[] gta, int ngt) {
         this.gta = gta;
         this.ngt = ngt;
      }

      public boolean test(int gi, int flags) {
         int i = 0;

         for(int n = this.ngt; i < n; ++i) {
            GlyphTester gt = this.gta[i];
            if (gt != null && gt.test(gi, flags)) {
               return true;
            }
         }

         return false;
      }
   }
}
