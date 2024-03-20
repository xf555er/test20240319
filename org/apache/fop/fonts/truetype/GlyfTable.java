package org.apache.fop.fonts.truetype;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class GlyfTable {
   private final OFMtxEntry[] mtxTab;
   private final long tableOffset;
   private final Set remappedComposites;
   protected final Map subset;
   private final FontFileReader in;
   private Set compositeGlyphs = new TreeSet();
   protected Set composedGlyphs = new TreeSet();

   public GlyfTable(FontFileReader in, OFMtxEntry[] metrics, OFDirTabEntry dirTableEntry, Map glyphs) throws IOException {
      this.mtxTab = metrics;
      this.tableOffset = dirTableEntry.getOffset();
      this.remappedComposites = new HashSet();
      this.subset = glyphs;
      this.in = in;
   }

   protected void populateGlyphsWithComposites() throws IOException {
      Iterator var1 = this.subset.keySet().iterator();

      int compositeGlyph;
      while(var1.hasNext()) {
         compositeGlyph = (Integer)var1.next();
         this.scanGlyphsRecursively(compositeGlyph);
      }

      this.addAllComposedGlyphsToSubset();
      var1 = this.compositeGlyphs.iterator();

      while(var1.hasNext()) {
         compositeGlyph = (Integer)var1.next();
         long offset = this.tableOffset + this.mtxTab[compositeGlyph].getOffset() + 10L;
         if (!this.remappedComposites.contains(offset)) {
            this.remapComposite(offset);
         }
      }

   }

   private void scanGlyphsRecursively(int indexInOriginal) throws IOException {
      if (!this.subset.containsKey(indexInOriginal)) {
         this.composedGlyphs.add(indexInOriginal);
      }

      if (this.isComposite(indexInOriginal)) {
         this.compositeGlyphs.add(indexInOriginal);
         Set composedGlyphs = this.retrieveComposedGlyphs(indexInOriginal);
         Iterator var3 = composedGlyphs.iterator();

         while(var3.hasNext()) {
            Integer composedGlyph = (Integer)var3.next();
            this.scanGlyphsRecursively(composedGlyph);
         }
      }

   }

   protected void addAllComposedGlyphsToSubset() {
      int newIndex = this.subset.size();
      Iterator var2 = this.composedGlyphs.iterator();

      while(var2.hasNext()) {
         int composedGlyph = (Integer)var2.next();
         this.subset.put(composedGlyph, newIndex++);
      }

   }

   private void remapComposite(long glyphOffset) throws IOException {
      long currentGlyphOffset = glyphOffset;
      this.remappedComposites.add(glyphOffset);
      int flags = false;

      int flags;
      do {
         flags = this.in.readTTFUShort(currentGlyphOffset);
         int glyphIndex = this.in.readTTFUShort(currentGlyphOffset + 2L);
         Integer indexInSubset = (Integer)this.subset.get(glyphIndex);

         assert indexInSubset != null;

         this.in.writeTTFUShort(currentGlyphOffset + 2L, indexInSubset);
         currentGlyphOffset += (long)(4 + GlyfTable.GlyfFlags.getOffsetToNextComposedGlyf(flags));
      } while(GlyfTable.GlyfFlags.hasMoreComposites(flags));

   }

   public boolean isComposite(int indexInOriginal) throws IOException {
      int numberOfContours = this.in.readTTFShort(this.tableOffset + this.mtxTab[indexInOriginal].getOffset());
      return numberOfContours < 0;
   }

   public Set retrieveComposedGlyphs(int indexInOriginal) throws IOException {
      Set composedGlyphs = new HashSet();
      long offset = this.tableOffset + this.mtxTab[indexInOriginal].getOffset() + 10L;
      int flags = false;

      int flags;
      do {
         flags = this.in.readTTFUShort(offset);
         composedGlyphs.add(this.in.readTTFUShort(offset + 2L));
         offset += (long)(4 + GlyfTable.GlyfFlags.getOffsetToNextComposedGlyf(flags));
      } while(GlyfTable.GlyfFlags.hasMoreComposites(flags));

      return composedGlyphs;
   }

   private static enum GlyfFlags {
      ARG_1_AND_2_ARE_WORDS(4, 2),
      ARGS_ARE_XY_VALUES,
      ROUND_XY_TO_GRID,
      WE_HAVE_A_SCALE(2),
      RESERVED,
      MORE_COMPONENTS,
      WE_HAVE_AN_X_AND_Y_SCALE(4),
      WE_HAVE_A_TWO_BY_TWO(8),
      WE_HAVE_INSTRUCTIONS,
      USE_MY_METRICS,
      OVERLAP_COMPOUND,
      SCALED_COMPONENT_OFFSET,
      UNSCALED_COMPONENT_OFFSET;

      private final int bitMask;
      private final int argsCountIfSet;
      private final int argsCountIfNotSet;

      private GlyfFlags(int argsCountIfSet, int argsCountIfNotSet) {
         this.bitMask = 1 << this.ordinal();
         this.argsCountIfSet = argsCountIfSet;
         this.argsCountIfNotSet = argsCountIfNotSet;
      }

      private GlyfFlags(int argsCountIfSet) {
         this(argsCountIfSet, 0);
      }

      private GlyfFlags() {
         this(0, 0);
      }

      static int getOffsetToNextComposedGlyf(int flags) {
         int offset = 0;
         GlyfFlags[] var2 = values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            GlyfFlags flag = var2[var4];
            offset += (flags & flag.bitMask) > 0 ? flag.argsCountIfSet : flag.argsCountIfNotSet;
         }

         return offset;
      }

      static boolean hasMoreComposites(int flags) {
         return (flags & MORE_COMPONENTS.bitMask) > 0;
      }
   }
}
