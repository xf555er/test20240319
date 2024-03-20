package org.apache.fop.fonts;

import java.awt.Rectangle;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.apache.fop.complexscripts.fonts.GlyphPositioningTable;
import org.apache.fop.complexscripts.fonts.GlyphSubstitutionTable;
import org.apache.fop.complexscripts.fonts.GlyphTable;
import org.apache.fop.complexscripts.fonts.Positionable;
import org.apache.fop.complexscripts.fonts.Substitutable;
import org.apache.fop.complexscripts.util.CharAssociation;
import org.apache.fop.complexscripts.util.CharNormalize;
import org.apache.fop.complexscripts.util.GlyphSequence;
import org.apache.fop.fonts.truetype.SVGGlyphData;
import org.apache.fop.util.CharUtilities;

public class MultiByteFont extends CIDFont implements Substitutable, Positionable {
   private static final Log log = LogFactory.getLog(MultiByteFont.class);
   private String ttcName;
   private String encoding = "Identity-H";
   private int defaultWidth;
   private CIDFontType cidType;
   protected final CIDSet cidSet;
   private GlyphDefinitionTable gdef;
   private GlyphSubstitutionTable gsub;
   private GlyphPositioningTable gpos;
   private int numMapped;
   private int numUnmapped;
   private int nextPrivateUse;
   private int firstPrivate;
   private int lastPrivate;
   private int firstUnmapped;
   private int lastUnmapped;
   protected Rectangle[] boundingBoxes;
   private boolean isOTFFile;
   private static final int NUM_MOST_LIKELY_GLYPHS = 256;
   private int[] mostLikelyGlyphs;
   private LinkedHashMap usedGlyphNames;

   public MultiByteFont(InternalResourceResolver resourceResolver, EmbeddingMode embeddingMode) {
      super(resourceResolver);
      this.cidType = CIDFontType.CIDTYPE2;
      this.nextPrivateUse = 57344;
      this.mostLikelyGlyphs = new int[256];
      this.usedGlyphNames = new LinkedHashMap();
      this.setFontType(FontType.TYPE0);
      this.setEmbeddingMode(embeddingMode);
      if (embeddingMode != EmbeddingMode.FULL) {
         this.cidSet = new CIDSubset(this);
      } else {
         this.cidSet = new CIDFull(this);
      }

   }

   public int getDefaultWidth() {
      return this.defaultWidth;
   }

   public String getRegistry() {
      return "Adobe";
   }

   public String getOrdering() {
      return "UCS";
   }

   public int getSupplement() {
      return 0;
   }

   public CIDFontType getCIDType() {
      return this.cidType;
   }

   public void setIsOTFFile(boolean isOTFFile) {
      this.isOTFFile = isOTFFile;
   }

   public boolean isOTFFile() {
      return this.isOTFFile;
   }

   public void setCIDType(CIDFontType cidType) {
      this.cidType = cidType;
   }

   public String getEmbedFontName() {
      return this.isEmbeddable() ? FontUtil.stripWhiteSpace(super.getFontName()) : super.getFontName();
   }

   public boolean isEmbeddable() {
      return this.getEmbedFileURI() != null || this.getEmbedResourceName() != null;
   }

   public boolean isSubsetEmbedded() {
      return this.getEmbeddingMode() != EmbeddingMode.FULL;
   }

   public CIDSet getCIDSet() {
      return this.cidSet;
   }

   public void mapUsedGlyphName(int gid, String value) {
      this.usedGlyphNames.put(gid, value);
   }

   public LinkedHashMap getUsedGlyphNames() {
      return this.usedGlyphNames;
   }

   public String getEncodingName() {
      return this.encoding;
   }

   public int getWidth(int i, int size) {
      if (this.isEmbeddable()) {
         int glyphIndex = this.cidSet.getOriginalGlyphIndex(i);
         return size * this.width[glyphIndex];
      } else {
         return size * this.width[i];
      }
   }

   public int[] getWidths() {
      int[] arr = new int[this.width.length];
      System.arraycopy(this.width, 0, arr, 0, this.width.length);
      return arr;
   }

   public Rectangle getBoundingBox(int glyphIndex, int size) {
      int index = this.isEmbeddable() ? this.cidSet.getOriginalGlyphIndex(glyphIndex) : glyphIndex;
      Rectangle bbox = this.boundingBoxes[index];
      return new Rectangle(bbox.x * size, bbox.y * size, bbox.width * size, bbox.height * size);
   }

   public int findGlyphIndex(int c) {
      int idx = c;
      int retIdx = 0;
      if (c < 256 && this.mostLikelyGlyphs[c] != 0) {
         return this.mostLikelyGlyphs[c];
      } else {
         Iterator var4 = this.cmap.iterator();

         while(var4.hasNext()) {
            CMapSegment i = (CMapSegment)var4.next();
            if (retIdx == 0 && i.getUnicodeStart() <= idx && i.getUnicodeEnd() >= idx) {
               retIdx = i.getGlyphStartIndex() + idx - i.getUnicodeStart();
               if (idx < 256) {
                  this.mostLikelyGlyphs[idx] = retIdx;
               }

               if (retIdx != 0) {
                  break;
               }
            }
         }

         return retIdx;
      }
   }

   protected synchronized void addPrivateUseMapping(int pu, int gi) {
      assert this.findGlyphIndex(pu) == 0;

      this.cmap.add(new CMapSegment(pu, pu, gi));
   }

   private int createPrivateUseMapping(int gi) {
      while(this.nextPrivateUse < 63744 && this.findGlyphIndex(this.nextPrivateUse) != 0) {
         ++this.nextPrivateUse;
      }

      if (this.nextPrivateUse < 63744) {
         int pu = this.nextPrivateUse;
         this.addPrivateUseMapping(pu, gi);
         if (this.firstPrivate == 0) {
            this.firstPrivate = pu;
         }

         this.lastPrivate = pu;
         ++this.numMapped;
         if (log.isDebugEnabled()) {
            log.debug("Create private use mapping from " + CharUtilities.format(pu) + " to glyph index " + gi + " in font '" + this.getFullName() + "'");
         }

         return pu;
      } else {
         if (this.firstUnmapped == 0) {
            this.firstUnmapped = gi;
         }

         this.lastUnmapped = gi;
         ++this.numUnmapped;
         log.warn("Exhausted private use area: unable to map " + this.numUnmapped + " glyphs in glyph index range [" + this.firstUnmapped + "," + this.lastUnmapped + "] (inclusive) of font '" + this.getFullName() + "'");
         return 0;
      }
   }

   private int findCharacterFromGlyphIndex(int gi, boolean augment) {
      int cc = 0;
      Iterator var4 = this.cmap.iterator();

      while(var4.hasNext()) {
         CMapSegment segment = (CMapSegment)var4.next();
         int s = segment.getGlyphStartIndex();
         int e = s + (segment.getUnicodeEnd() - segment.getUnicodeStart());
         if (gi >= s && gi <= e) {
            cc = segment.getUnicodeStart() + (gi - s);
            break;
         }
      }

      if (cc == 0 && augment) {
         cc = this.createPrivateUseMapping(gi);
      }

      return cc;
   }

   private int findCharacterFromGlyphIndex(int gi) {
      return this.findCharacterFromGlyphIndex(gi, true);
   }

   protected BitSet getGlyphIndices() {
      BitSet bitset = new BitSet();
      bitset.set(0);
      bitset.set(1);
      bitset.set(2);
      Iterator var2 = this.cmap.iterator();

      while(var2.hasNext()) {
         CMapSegment i = (CMapSegment)var2.next();
         int start = i.getUnicodeStart();
         int end = i.getUnicodeEnd();
         int glyphIndex = i.getGlyphStartIndex();

         while(start++ < end + 1) {
            bitset.set(glyphIndex++);
         }
      }

      return bitset;
   }

   protected char[] getChars() {
      char[] chars = new char[this.width.length];
      Iterator var2 = this.cmap.iterator();

      while(var2.hasNext()) {
         CMapSegment i = (CMapSegment)var2.next();
         int start = i.getUnicodeStart();
         int end = i.getUnicodeEnd();

         for(int glyphIndex = i.getGlyphStartIndex(); start < end + 1; chars[glyphIndex++] = (char)(start++)) {
         }
      }

      return chars;
   }

   public char mapChar(char c) {
      this.notifyMapOperation();
      int glyphIndex = this.findGlyphIndex(c);
      if (glyphIndex == 0) {
         this.warnMissingGlyph(c);
         if (!this.isOTFFile) {
            glyphIndex = this.findGlyphIndex(35);
         }
      }

      if (this.isEmbeddable()) {
         glyphIndex = this.cidSet.mapChar(glyphIndex, c);
      }

      if (this.isCID() && glyphIndex > 256) {
         this.mapUnencodedChar(c);
      }

      return (char)glyphIndex;
   }

   public int mapCodePoint(int cp) {
      this.notifyMapOperation();
      int glyphIndex = this.findGlyphIndex(cp);
      if (glyphIndex == 0) {
         char[] var3 = Character.toChars(cp);
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            char ch = var3[var5];
            this.warnMissingGlyph(ch);
         }

         if (!this.isOTFFile) {
            glyphIndex = this.findGlyphIndex(35);
         }
      }

      if (this.isEmbeddable()) {
         glyphIndex = this.cidSet.mapCodePoint(glyphIndex, cp);
      }

      return (char)glyphIndex;
   }

   public boolean hasChar(char c) {
      return this.hasCodePoint(c);
   }

   public boolean hasCodePoint(int cp) {
      return this.findGlyphIndex(cp) != 0;
   }

   public void setDefaultWidth(int defaultWidth) {
      this.defaultWidth = defaultWidth;
   }

   public String getTTCName() {
      return this.ttcName;
   }

   public void setTTCName(String ttcName) {
      this.ttcName = ttcName;
   }

   public void setWidthArray(int[] wds) {
      this.width = wds;
   }

   public void setBBoxArray(Rectangle[] boundingBoxes) {
      this.boundingBoxes = boundingBoxes;
   }

   public Map getUsedGlyphs() {
      return this.cidSet.getGlyphs();
   }

   public char getUnicodeFromGID(int glyphIndex) {
      return this.cidSet.getUnicodeFromGID(glyphIndex);
   }

   public int getGIDFromChar(char ch) {
      return this.cidSet.getGIDFromChar(ch);
   }

   public void setGDEF(GlyphDefinitionTable gdef) {
      if (this.gdef != null && gdef != null) {
         throw new IllegalStateException("font already associated with GDEF table");
      } else {
         this.gdef = gdef;
      }
   }

   public GlyphDefinitionTable getGDEF() {
      return this.gdef;
   }

   public void setGSUB(GlyphSubstitutionTable gsub) {
      if (this.gsub != null && gsub != null) {
         throw new IllegalStateException("font already associated with GSUB table");
      } else {
         this.gsub = gsub;
      }
   }

   public GlyphSubstitutionTable getGSUB() {
      return this.gsub;
   }

   public void setGPOS(GlyphPositioningTable gpos) {
      if (this.gpos != null && gpos != null) {
         throw new IllegalStateException("font already associated with GPOS table");
      } else {
         this.gpos = gpos;
      }
   }

   public GlyphPositioningTable getGPOS() {
      return this.gpos;
   }

   public boolean performsSubstitution() {
      return this.gsub != null;
   }

   public CharSequence performSubstitution(CharSequence charSequence, String script, String language, List associations, boolean retainControls) {
      if (this.gsub != null) {
         charSequence = this.gsub.preProcess(charSequence, script, this, associations);
         GlyphSequence glyphSequence = this.charSequenceToGlyphSequence(charSequence, associations);
         GlyphSequence glyphSequenceSubstituted = this.gsub.substitute(glyphSequence, script, language);
         if (associations != null) {
            associations.clear();
            associations.addAll(glyphSequenceSubstituted.getAssociations());
         }

         if (!retainControls) {
            glyphSequenceSubstituted = elideControls(glyphSequenceSubstituted);
         }

         return this.mapGlyphsToChars(glyphSequenceSubstituted);
      } else {
         return charSequence;
      }
   }

   public GlyphSequence charSequenceToGlyphSequence(CharSequence charSequence, List associations) {
      CharSequence normalizedCharSequence = this.normalize(charSequence, associations);
      return this.mapCharsToGlyphs(normalizedCharSequence, associations);
   }

   public CharSequence reorderCombiningMarks(CharSequence cs, int[][] gpa, String script, String language, List associations) {
      if (this.gdef != null) {
         GlyphSequence igs = this.mapCharsToGlyphs(cs, associations);
         GlyphSequence ogs = this.gdef.reorderCombiningMarks(igs, this.getUnscaledWidths(igs), gpa, script, language);
         if (associations != null) {
            associations.clear();
            associations.addAll(ogs.getAssociations());
         }

         CharSequence ocs = this.mapGlyphsToChars(ogs);
         return ocs;
      } else {
         return cs;
      }
   }

   protected int[] getUnscaledWidths(GlyphSequence gs) {
      int[] widths = new int[gs.getGlyphCount()];
      int i = 0;

      for(int n = widths.length; i < n; ++i) {
         if (i < this.width.length) {
            widths[i] = this.width[i];
         }
      }

      return widths;
   }

   public boolean performsPositioning() {
      return this.gpos != null;
   }

   public int[][] performPositioning(CharSequence cs, String script, String language, int fontSize) {
      if (this.gpos != null) {
         GlyphSequence gs = this.mapCharsToGlyphs(cs, (List)null);
         int[][] adjustments = new int[gs.getGlyphCount()][4];
         return this.gpos.position(gs, script, language, fontSize, this.width, adjustments) ? this.scaleAdjustments(adjustments, fontSize) : (int[][])null;
      } else {
         return (int[][])null;
      }
   }

   public int[][] performPositioning(CharSequence cs, String script, String language) {
      throw new UnsupportedOperationException();
   }

   private int[][] scaleAdjustments(int[][] adjustments, int fontSize) {
      if (adjustments == null) {
         return (int[][])null;
      } else {
         int[][] var3 = adjustments;
         int var4 = adjustments.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            int[] gpa = var3[var5];

            for(int k = 0; k < 4; ++k) {
               gpa[k] = gpa[k] * fontSize / 1000;
            }
         }

         return adjustments;
      }
   }

   private GlyphSequence mapCharsToGlyphs(CharSequence cs, List associations) {
      IntBuffer cb = IntBuffer.allocate(cs.length());
      IntBuffer gb = IntBuffer.allocate(cs.length());
      int giMissing = this.findGlyphIndex(35);
      int i = 0;

      for(int n = cs.length(); i < n; ++i) {
         int cc = cs.charAt(i);
         if (cc >= 55296 && cc < 56320) {
            if (i + 1 >= n) {
               throw new IllegalArgumentException("ill-formed UTF-16 sequence, contains isolated high surrogate at end of sequence");
            }

            ++i;
            int sl = cs.charAt(i);
            if (sl < '\udc00' || sl >= '\ue000') {
               throw new IllegalArgumentException("ill-formed UTF-16 sequence, contains isolated high surrogate at index " + i);
            }

            cc = 65536 + (cc - '\ud800' << 10) + (sl - '\udc00' << 0);
         } else if (cc >= 56320 && cc < 57344) {
            throw new IllegalArgumentException("ill-formed UTF-16 sequence, contains isolated low surrogate at index " + i);
         }

         this.notifyMapOperation();
         int gi = this.findGlyphIndex(cc);
         if (gi == 0) {
            this.warnMissingGlyph((char)cc);
            gi = giMissing;
         }

         cb.put(cc);
         gb.put(gi);
      }

      cb.flip();
      gb.flip();
      ArrayList associations;
      if (associations != null && associations.size() == cs.length()) {
         associations = new ArrayList(associations);
      } else {
         associations = null;
      }

      return new GlyphSequence(cb, gb, associations);
   }

   private CharSequence mapGlyphsToChars(GlyphSequence gs) {
      int ng = gs.getGlyphCount();
      int ccMissing = 35;
      List chars = new ArrayList(gs.getUTF16CharacterCount());
      int i = 0;

      for(int n = ng; i < n; ++i) {
         int gi = gs.getGlyph(i);
         int cc = this.findCharacterFromGlyphIndex(gi);
         if (cc == 0 || cc > 1114111) {
            cc = ccMissing;
            log.warn("Unable to map glyph index " + gi + " to Unicode scalar in font '" + this.getFullName() + "', substituting missing character '" + (char)ccMissing + "'");
         }

         if (cc > 65535) {
            cc -= 65536;
            int sh = (cc >> 10 & 1023) + '\ud800';
            int sl = (cc >> 0 & 1023) + '\udc00';
            chars.add((char)sh);
            chars.add((char)sl);
         } else {
            chars.add((char)cc);
         }
      }

      CharBuffer cb = CharBuffer.allocate(chars.size());
      Iterator var12 = chars.iterator();

      while(var12.hasNext()) {
         char c = (Character)var12.next();
         cb.put(c);
      }

      cb.flip();
      return cb;
   }

   private CharSequence normalize(CharSequence cs, List associations) {
      return this.hasDecomposable(cs) ? this.decompose(cs, associations) : cs;
   }

   private boolean hasDecomposable(CharSequence cs) {
      int i = 0;

      for(int n = cs.length(); i < n; ++i) {
         int cc = cs.charAt(i);
         if (CharNormalize.isDecomposable(cc)) {
            return true;
         }
      }

      return false;
   }

   private CharSequence decompose(CharSequence cs, List associations) {
      StringBuffer sb = new StringBuffer(cs.length());
      int[] daBuffer = new int[CharNormalize.maximumDecompositionLength()];
      int i = 0;

      for(int n = cs.length(); i < n; ++i) {
         int cc = cs.charAt(i);
         int[] da = CharNormalize.decompose(cc, daBuffer);
         int[] var9 = da;
         int var10 = da.length;

         for(int var11 = 0; var11 < var10; ++var11) {
            int aDa = var9[var11];
            if (aDa <= 0) {
               break;
            }

            sb.append((char)aDa);
         }
      }

      return sb;
   }

   private static GlyphSequence elideControls(GlyphSequence gs) {
      if (!hasElidableControl(gs)) {
         return gs;
      } else {
         int[] ca = gs.getCharacterArray(false);
         IntBuffer ngb = IntBuffer.allocate(gs.getGlyphCount());
         List nal = new ArrayList(gs.getGlyphCount());
         int i = 0;

         for(int n = gs.getGlyphCount(); i < n; ++i) {
            CharAssociation a = gs.getAssociation(i);
            int s = a.getStart();

            int e;
            for(e = a.getEnd(); s < e; ++s) {
               int ch = ca[s];
               if (!isElidableControl(ch)) {
                  break;
               }
            }

            if (s != e) {
               ngb.put(gs.getGlyph(i));
               nal.add(a);
            }
         }

         ngb.flip();
         return new GlyphSequence(gs.getCharacters(), ngb, nal, gs.getPredications());
      }
   }

   private static boolean hasElidableControl(GlyphSequence gs) {
      int[] ca = gs.getCharacterArray(false);
      int[] var2 = ca;
      int var3 = ca.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         int ch = var2[var4];
         if (isElidableControl(ch)) {
            return true;
         }
      }

      return false;
   }

   private static boolean isElidableControl(int ch) {
      if (ch < 32) {
         return true;
      } else if (ch >= 128 && ch < 160) {
         return true;
      } else if (ch >= 8192 && ch <= 8303) {
         if (ch >= 8203 && ch <= 8207) {
            return true;
         } else if (ch >= 8232 && ch <= 8238) {
            return true;
         } else if (ch >= 8294) {
            return true;
         } else {
            return ch == 8288;
         }
      } else {
         return false;
      }
   }

   public boolean hasFeature(int tableType, String script, String language, String feature) {
      Object table;
      if (tableType == 1) {
         table = this.getGSUB();
      } else if (tableType == 2) {
         table = this.getGPOS();
      } else if (tableType == 5) {
         table = this.getGDEF();
      } else {
         table = null;
      }

      return table != null && ((GlyphTable)table).hasFeature(script, language, feature);
   }

   public Map getWidthsMap() {
      return null;
   }

   public InputStream getCmapStream() {
      return null;
   }

   public SVGGlyphData getSVG(char c) {
      int gid = this.findGlyphIndex(c);
      return (SVGGlyphData)this.svgs.get(gid);
   }
}
