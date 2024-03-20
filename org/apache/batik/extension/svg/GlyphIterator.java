package org.apache.batik.extension.svg;

import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import org.apache.batik.gvt.font.AWTGVTFont;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.GVTLineMetrics;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;

public class GlyphIterator {
   public static final AttributedCharacterIterator.Attribute PREFORMATTED;
   public static final AttributedCharacterIterator.Attribute FLOW_LINE_BREAK;
   public static final AttributedCharacterIterator.Attribute TEXT_COMPOUND_ID;
   public static final AttributedCharacterIterator.Attribute GVT_FONT;
   public static final char SOFT_HYPHEN = '\u00ad';
   public static final char ZERO_WIDTH_SPACE = '\u200b';
   public static final char ZERO_WIDTH_JOINER = '\u200d';
   int idx = -1;
   int chIdx = -1;
   int lineIdx = -1;
   int aciIdx = -1;
   int charCount = -1;
   float adv = 0.0F;
   float adj = 0.0F;
   int runLimit = 0;
   int lineBreakRunLimit = 0;
   int lineBreakCount = 0;
   GVTFont font = null;
   int fontStart = 0;
   float maxAscent = 0.0F;
   float maxDescent = 0.0F;
   float maxFontSize = 0.0F;
   float width = 0.0F;
   char ch = 0;
   int numGlyphs = 0;
   AttributedCharacterIterator aci;
   GVTGlyphVector gv;
   float[] gp;
   FontRenderContext frc;
   int[] leftShiftIdx = null;
   float[] leftShiftAmt = null;
   int leftShift = 0;
   Point2D gvBase = null;

   public GlyphIterator(AttributedCharacterIterator aci, GVTGlyphVector gv) {
      this.aci = aci;
      this.gv = gv;
      this.idx = 0;
      this.chIdx = 0;
      this.lineIdx = 0;
      this.aciIdx = aci.getBeginIndex();
      this.charCount = gv.getCharacterCount(this.idx, this.idx);
      this.ch = aci.first();
      this.frc = gv.getFontRenderContext();
      this.font = (GVTFont)aci.getAttribute(GVT_FONT);
      if (this.font == null) {
         this.font = new AWTGVTFont(aci.getAttributes());
      }

      this.fontStart = this.aciIdx;
      this.maxFontSize = -3.4028235E38F;
      this.maxAscent = -3.4028235E38F;
      this.maxDescent = -3.4028235E38F;
      this.runLimit = aci.getRunLimit(TEXT_COMPOUND_ID);
      this.lineBreakRunLimit = aci.getRunLimit(FLOW_LINE_BREAK);
      Object o = aci.getAttribute(FLOW_LINE_BREAK);
      this.lineBreakCount = o == null ? 0 : 1;
      this.numGlyphs = gv.getNumGlyphs();
      this.gp = gv.getGlyphPositions(0, this.numGlyphs + 1, (float[])null);
      this.gvBase = new Point2D.Float(this.gp[0], this.gp[1]);
      this.adv = this.getCharWidth();
      this.adj = this.getCharAdvance();
   }

   public GlyphIterator(GlyphIterator gi) {
      gi.copy(this);
   }

   public GlyphIterator copy() {
      return new GlyphIterator(this);
   }

   public GlyphIterator copy(GlyphIterator gi) {
      if (gi == null) {
         return new GlyphIterator(this);
      } else {
         gi.idx = this.idx;
         gi.chIdx = this.chIdx;
         gi.aciIdx = this.aciIdx;
         gi.charCount = this.charCount;
         gi.adv = this.adv;
         gi.adj = this.adj;
         gi.runLimit = this.runLimit;
         gi.ch = this.ch;
         gi.numGlyphs = this.numGlyphs;
         gi.gp = this.gp;
         gi.gvBase = this.gvBase;
         gi.lineBreakRunLimit = this.lineBreakRunLimit;
         gi.lineBreakCount = this.lineBreakCount;
         gi.frc = this.frc;
         gi.font = this.font;
         gi.fontStart = this.fontStart;
         gi.maxAscent = this.maxAscent;
         gi.maxDescent = this.maxDescent;
         gi.maxFontSize = this.maxFontSize;
         gi.leftShift = this.leftShift;
         gi.leftShiftIdx = this.leftShiftIdx;
         gi.leftShiftAmt = this.leftShiftAmt;
         return gi;
      }
   }

   public int getGlyphIndex() {
      return this.idx;
   }

   public char getChar() {
      return this.ch;
   }

   public int getACIIndex() {
      return this.aciIdx;
   }

   public float getAdv() {
      return this.adv;
   }

   public Point2D getOrigin() {
      return this.gvBase;
   }

   public float getAdj() {
      return this.adj;
   }

   public float getMaxFontSize() {
      if (this.aciIdx >= this.fontStart) {
         int newFS = this.aciIdx + this.charCount;
         this.updateLineMetrics(newFS);
         this.fontStart = newFS;
      }

      return this.maxFontSize;
   }

   public float getMaxAscent() {
      if (this.aciIdx >= this.fontStart) {
         int newFS = this.aciIdx + this.charCount;
         this.updateLineMetrics(newFS);
         this.fontStart = newFS;
      }

      return this.maxAscent;
   }

   public float getMaxDescent() {
      if (this.aciIdx >= this.fontStart) {
         int newFS = this.aciIdx + this.charCount;
         this.updateLineMetrics(newFS);
         this.fontStart = newFS;
      }

      return this.maxDescent;
   }

   public boolean isLastChar() {
      return this.idx == this.numGlyphs - 1;
   }

   public boolean done() {
      return this.idx >= this.numGlyphs;
   }

   public boolean isBreakChar() {
      switch (this.ch) {
         case '\t':
         case ' ':
            return true;
         case '\u00ad':
            return true;
         case '\u200b':
            return true;
         case '\u200d':
            return false;
         default:
            return false;
      }
   }

   protected boolean isPrinting(char tstCH) {
      switch (this.ch) {
         case '\t':
         case ' ':
            return false;
         case '\u00ad':
            return true;
         case '\u200b':
            return false;
         case '\u200d':
            return false;
         default:
            return true;
      }
   }

   public int getLineBreaks() {
      int ret = 0;
      if (this.aciIdx + this.charCount >= this.lineBreakRunLimit) {
         ret = this.lineBreakCount;
         this.aci.setIndex(this.aciIdx + this.charCount);
         this.lineBreakRunLimit = this.aci.getRunLimit(FLOW_LINE_BREAK);
         this.aci.setIndex(this.aciIdx);
         Object o = this.aci.getAttribute(FLOW_LINE_BREAK);
         this.lineBreakCount = o == null ? 0 : 1;
      }

      return ret;
   }

   public void nextChar() {
      float chAdv;
      if (this.ch == 173 || this.ch == 8203 || this.ch == 8205) {
         this.gv.setGlyphVisible(this.idx, false);
         chAdv = this.getCharAdvance();
         this.adj -= chAdv;
         this.addLeftShift(this.idx, chAdv);
      }

      this.aciIdx += this.charCount;
      this.ch = this.aci.setIndex(this.aciIdx);
      ++this.idx;
      this.charCount = this.gv.getCharacterCount(this.idx, this.idx);
      if (this.idx != this.numGlyphs) {
         if (this.aciIdx >= this.runLimit) {
            this.updateLineMetrics(this.aciIdx);
            this.runLimit = this.aci.getRunLimit(TEXT_COMPOUND_ID);
            this.font = (GVTFont)this.aci.getAttribute(GVT_FONT);
            if (this.font == null) {
               this.font = new AWTGVTFont(this.aci.getAttributes());
            }

            this.fontStart = this.aciIdx;
         }

         chAdv = this.getCharAdvance();
         this.adj += chAdv;
         if (this.isPrinting()) {
            this.chIdx = this.idx;
            float chW = this.getCharWidth();
            this.adv = this.adj - (chAdv - chW);
         }

      }
   }

   protected void addLeftShift(int idx, float chAdv) {
      if (this.leftShiftIdx == null) {
         this.leftShiftIdx = new int[1];
         this.leftShiftIdx[0] = idx;
         this.leftShiftAmt = new float[1];
         this.leftShiftAmt[0] = chAdv;
      } else {
         int[] newLeftShiftIdx = new int[this.leftShiftIdx.length + 1];
         System.arraycopy(this.leftShiftIdx, 0, newLeftShiftIdx, 0, this.leftShiftIdx.length);
         newLeftShiftIdx[this.leftShiftIdx.length] = idx;
         this.leftShiftIdx = newLeftShiftIdx;
         float[] newLeftShiftAmt = new float[this.leftShiftAmt.length + 1];
         System.arraycopy(this.leftShiftAmt, 0, newLeftShiftAmt, 0, this.leftShiftAmt.length);
         newLeftShiftAmt[this.leftShiftAmt.length] = chAdv;
         this.leftShiftAmt = newLeftShiftAmt;
      }

   }

   protected void updateLineMetrics(int end) {
      GVTLineMetrics glm = this.font.getLineMetrics((CharacterIterator)this.aci, this.fontStart, end, this.frc);
      float ascent = glm.getAscent();
      float descent = glm.getDescent();
      float fontSz = this.font.getSize();
      if (ascent > this.maxAscent) {
         this.maxAscent = ascent;
      }

      if (descent > this.maxDescent) {
         this.maxDescent = descent;
      }

      if (fontSz > this.maxFontSize) {
         this.maxFontSize = fontSz;
      }

   }

   public LineInfo newLine(Point2D.Float loc, float lineWidth, boolean partial, Point2D.Float verticalAlignOffset) {
      if (this.ch == 173) {
         this.gv.setGlyphVisible(this.idx, true);
      }

      int lsi = 0;
      int nextLSI;
      if (this.leftShiftIdx != null) {
         nextLSI = this.leftShiftIdx[lsi];
      } else {
         nextLSI = this.idx + 1;
      }

      for(int ci = this.lineIdx; ci <= this.idx; ++ci) {
         if (ci == nextLSI) {
            this.leftShift = (int)((float)this.leftShift + this.leftShiftAmt[lsi++]);
            if (lsi < this.leftShiftIdx.length) {
               nextLSI = this.leftShiftIdx[lsi];
            }
         }

         this.gv.setGlyphPosition(ci, new Point2D.Float(this.gp[2 * ci] - (float)this.leftShift, this.gp[2 * ci + 1]));
      }

      this.leftShiftIdx = null;
      this.leftShiftAmt = null;
      int hideIdx;
      float lineInfoChW;
      if (this.chIdx == 0 && !this.isPrinting()) {
         lineInfoChW = 0.0F;
         hideIdx = 0;
      } else {
         lineInfoChW = this.getCharWidth(this.chIdx);
         hideIdx = this.chIdx + 1;
      }

      int lineInfoIdx = this.idx + 1;
      float lineInfoAdv = this.adv;

      float lineInfoAdj;
      for(lineInfoAdj = this.adj; !this.done(); lineInfoAdj += this.adj) {
         this.adv = 0.0F;
         this.adj = 0.0F;
         if (this.ch == 8203 || this.ch == 8205) {
            this.gv.setGlyphVisible(this.idx, false);
         }

         this.ch = 0;
         this.nextChar();
         if (this.isPrinting()) {
            break;
         }

         lineInfoIdx = this.idx + 1;
      }

      for(int i = hideIdx; i < lineInfoIdx; ++i) {
         this.gv.setGlyphVisible(i, false);
      }

      this.maxAscent = -3.4028235E38F;
      this.maxDescent = -3.4028235E38F;
      this.maxFontSize = -3.4028235E38F;
      LineInfo ret = new LineInfo(loc, this.aci, this.gv, this.lineIdx, lineInfoIdx, lineInfoAdj, lineInfoAdv, lineInfoChW, lineWidth, partial, verticalAlignOffset);
      this.lineIdx = this.idx;
      return ret;
   }

   public boolean isPrinting() {
      return this.aci.getAttribute(PREFORMATTED) == Boolean.TRUE ? true : this.isPrinting(this.ch);
   }

   public float getCharAdvance() {
      return this.getCharAdvance(this.idx);
   }

   public float getCharWidth() {
      return this.getCharWidth(this.idx);
   }

   protected float getCharAdvance(int gvIdx) {
      return this.gp[2 * gvIdx + 2] - this.gp[2 * gvIdx];
   }

   protected float getCharWidth(int gvIdx) {
      Rectangle2D lcBound = this.gv.getGlyphVisualBounds(gvIdx).getBounds2D();
      Point2D lcLoc = this.gv.getGlyphPosition(gvIdx);
      return (float)(lcBound.getX() + lcBound.getWidth() - lcLoc.getX());
   }

   static {
      PREFORMATTED = GVTAttributedCharacterIterator.TextAttribute.PREFORMATTED;
      FLOW_LINE_BREAK = GVTAttributedCharacterIterator.TextAttribute.FLOW_LINE_BREAK;
      TEXT_COMPOUND_ID = GVTAttributedCharacterIterator.TextAttribute.TEXT_COMPOUND_ID;
      GVT_FONT = GVTAttributedCharacterIterator.TextAttribute.GVT_FONT;
   }
}
