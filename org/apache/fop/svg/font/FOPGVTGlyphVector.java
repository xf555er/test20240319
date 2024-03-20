package org.apache.fop.svg.font;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphJustificationInfo;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.List;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTGlyphMetrics;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.GVTLineMetrics;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.GlyphMapping;
import org.apache.fop.fonts.TextFragment;
import org.apache.fop.traits.MinOptMax;

public class FOPGVTGlyphVector implements GVTGlyphVector {
   protected final TextFragment text;
   protected final FOPGVTFont font;
   private final int fontSize;
   private final FontMetrics fontMetrics;
   private final FontRenderContext frc;
   protected int[] glyphs;
   protected List associations;
   protected int[][] gposAdjustments;
   protected float[] positions;
   protected Rectangle2D[] boundingBoxes;
   protected GeneralPath outline;
   protected AffineTransform[] glyphTransforms;
   protected boolean[] glyphVisibilities;
   protected Rectangle2D logicalBounds;
   private static final int[] PA_ZERO = new int[4];

   FOPGVTGlyphVector(FOPGVTFont font, CharacterIterator iter, FontRenderContext frc) {
      this.text = new SVGTextFragment(iter);
      this.font = font;
      Font f = font.getFont();
      this.fontSize = f.getFontSize();
      this.fontMetrics = f.getFontMetrics();
      this.frc = frc;
   }

   public void performDefaultLayout() {
      Font f = this.font.getFont();
      MinOptMax letterSpaceIPD = MinOptMax.ZERO;
      MinOptMax[] letterSpaceAdjustments = new MinOptMax[this.text.getEndIndex()];
      boolean retainControls = false;
      GlyphMapping mapping = GlyphMapping.doGlyphMapping(this.text, this.text.getBeginIndex(), this.text.getEndIndex(), f, letterSpaceIPD, letterSpaceAdjustments, '\u0000', '\u0000', false, this.text.getBidiLevel(), true, true, retainControls);
      CharacterIterator glyphAsCharIter = mapping.mapping != null ? new StringCharacterIterator(mapping.mapping) : this.text.getIterator();
      this.glyphs = this.buildGlyphs(f, (CharacterIterator)glyphAsCharIter);
      this.associations = mapping.associations;
      this.gposAdjustments = mapping.gposAdjustments;
      if (this.text.getBeginIndex() > 0) {
         int arrlen = this.text.getEndIndex() - this.text.getBeginIndex();
         MinOptMax[] letterSpaceAdjustmentsNew = new MinOptMax[arrlen];
         System.arraycopy(letterSpaceAdjustments, this.text.getBeginIndex(), letterSpaceAdjustmentsNew, 0, arrlen);
         letterSpaceAdjustments = letterSpaceAdjustmentsNew;
      }

      this.positions = this.buildGlyphPositions((CharacterIterator)glyphAsCharIter, mapping.gposAdjustments, letterSpaceAdjustments);
      this.glyphVisibilities = new boolean[this.glyphs.length];
      Arrays.fill(this.glyphVisibilities, true);
      this.glyphTransforms = new AffineTransform[this.glyphs.length];
   }

   private int[] buildGlyphs(Font font, CharacterIterator glyphAsCharIter) {
      int[] glyphs = new int[glyphAsCharIter.getEndIndex() - glyphAsCharIter.getBeginIndex()];
      int index = 0;

      for(char c = glyphAsCharIter.first(); c != '\uffff'; c = glyphAsCharIter.next()) {
         glyphs[index] = font.mapChar(c);
         ++index;
      }

      return glyphs;
   }

   private float[] buildGlyphPositions(CharacterIterator glyphAsCharIter, int[][] dp, MinOptMax[] lsa) {
      int numGlyphs = glyphAsCharIter.getEndIndex() - glyphAsCharIter.getBeginIndex();
      float[] positions = new float[2 * (numGlyphs + 1)];
      float xc = 0.0F;
      float yc = 0.0F;
      int i;
      float xa;
      float ya;
      int k;
      if (dp != null) {
         for(i = 0; i < numGlyphs + 1; ++i) {
            int[] pa = i < dp.length && dp[i] != null ? dp[i] : PA_ZERO;
            float xo = xc + (float)pa[0] / 1000.0F;
            float yo = yc - (float)pa[1] / 1000.0F;
            xa = this.getGlyphWidth(i) + (float)pa[2] / 1000.0F;
            ya = (float)pa[3] / 1000.0F;
            k = 2 * i;
            positions[k + 0] = xo;
            positions[k + 1] = yo;
            xc += xa;
            yc += ya;
         }
      } else if (lsa != null) {
         for(i = 0; i < numGlyphs + 1; ++i) {
            MinOptMax sa = i + 1 < lsa.length && lsa[i + 1] != null ? lsa[i + 1] : MinOptMax.ZERO;
            xa = this.getGlyphWidth(i) + (float)sa.getOpt() / 1000.0F;
            ya = 0.0F;
            k = 2 * i;
            positions[k + 0] = xc;
            positions[k + 1] = yc;
            xc += xa;
            yc += ya;
         }
      }

      return positions;
   }

   private float getGlyphWidth(int index) {
      return index < this.glyphs.length ? (float)this.fontMetrics.getWidth(this.glyphs[index], this.fontSize) / 1000000.0F : 0.0F;
   }

   public GVTFont getFont() {
      return this.font;
   }

   public FontRenderContext getFontRenderContext() {
      return this.frc;
   }

   public void setGlyphCode(int glyphIndex, int glyphCode) {
      this.glyphs[glyphIndex] = glyphCode;
   }

   public int getGlyphCode(int glyphIndex) {
      return this.glyphs[glyphIndex];
   }

   public int[] getGlyphCodes(int beginGlyphIndex, int numEntries, int[] codeReturn) {
      if (codeReturn == null) {
         codeReturn = new int[numEntries];
      }

      System.arraycopy(this.glyphs, beginGlyphIndex, codeReturn, 0, numEntries);
      return codeReturn;
   }

   public GlyphJustificationInfo getGlyphJustificationInfo(int glyphIndex) {
      throw new UnsupportedOperationException();
   }

   public Shape getGlyphLogicalBounds(int glyphIndex) {
      GVTGlyphMetrics metrics = this.getGlyphMetrics(glyphIndex);
      Point2D pos = this.getGlyphPosition(glyphIndex);
      GVTLineMetrics fontMetrics = this.font.getLineMetrics(0);
      Rectangle2D bounds = new Rectangle2D.Float(0.0F, -fontMetrics.getDescent(), metrics.getHorizontalAdvance(), fontMetrics.getAscent() + fontMetrics.getDescent());
      AffineTransform t = AffineTransform.getTranslateInstance(pos.getX(), pos.getY());
      AffineTransform transf = this.getGlyphTransform(glyphIndex);
      if (transf != null) {
         t.concatenate(transf);
      }

      t.scale(1.0, -1.0);
      return t.createTransformedShape(bounds);
   }

   public GVTGlyphMetrics getGlyphMetrics(int glyphIndex) {
      Rectangle2D bbox = this.getBoundingBoxes()[glyphIndex];
      return new GVTGlyphMetrics(this.positions[2 * (glyphIndex + 1)] - this.positions[2 * glyphIndex], (float)(this.fontMetrics.getAscender(this.fontSize) - this.fontMetrics.getDescender(this.fontSize)) / 1000000.0F, bbox, (byte)0);
   }

   public Shape getGlyphOutline(int glyphIndex) {
      Shape glyphBox = this.getBoundingBoxes()[glyphIndex];
      AffineTransform tr = AffineTransform.getTranslateInstance((double)this.positions[glyphIndex * 2], (double)this.positions[glyphIndex * 2 + 1]);
      AffineTransform glyphTransform = this.getGlyphTransform(glyphIndex);
      if (glyphTransform != null) {
         tr.concatenate(glyphTransform);
      }

      return tr.createTransformedShape(glyphBox);
   }

   public Rectangle2D getGlyphCellBounds(int glyphIndex) {
      throw new UnsupportedOperationException();
   }

   public int[][] getGlyphPositionAdjustments() {
      return this.gposAdjustments;
   }

   public Point2D getGlyphPosition(int glyphIndex) {
      int positionIndex = glyphIndex * 2;
      return new Point2D.Float(this.positions[positionIndex], this.positions[positionIndex + 1]);
   }

   public float[] getGlyphPositions(int beginGlyphIndex, int numEntries, float[] positionReturn) {
      if (positionReturn == null) {
         positionReturn = new float[numEntries * 2];
      }

      System.arraycopy(this.positions, beginGlyphIndex * 2, positionReturn, 0, numEntries * 2);
      return positionReturn;
   }

   public AffineTransform getGlyphTransform(int glyphIndex) {
      return this.glyphTransforms[glyphIndex];
   }

   public Shape getGlyphVisualBounds(int glyphIndex) {
      Rectangle2D bbox = this.getBoundingBoxes()[glyphIndex];
      Point2D pos = this.getGlyphPosition(glyphIndex);
      AffineTransform t = AffineTransform.getTranslateInstance(pos.getX(), pos.getY());
      AffineTransform transf = this.getGlyphTransform(glyphIndex);
      if (transf != null) {
         t.concatenate(transf);
      }

      return t.createTransformedShape(bbox);
   }

   public Rectangle2D getLogicalBounds() {
      if (this.logicalBounds == null) {
         GeneralPath logicalBoundsPath = new GeneralPath();

         for(int i = 0; i < this.getNumGlyphs(); ++i) {
            Shape glyphLogicalBounds = this.getGlyphLogicalBounds(i);
            logicalBoundsPath.append(glyphLogicalBounds, false);
         }

         this.logicalBounds = logicalBoundsPath.getBounds2D();
      }

      return this.logicalBounds;
   }

   public int getNumGlyphs() {
      return this.glyphs.length;
   }

   public Shape getOutline() {
      if (this.outline == null) {
         this.outline = new GeneralPath();

         for(int i = 0; i < this.glyphs.length; ++i) {
            this.outline.append(this.getGlyphOutline(i), false);
         }
      }

      return this.outline;
   }

   public Shape getOutline(float x, float y) {
      throw new UnsupportedOperationException();
   }

   public Rectangle2D getGeometricBounds() {
      throw new UnsupportedOperationException();
   }

   public Rectangle2D getBounds2D(AttributedCharacterIterator aci) {
      return this.getOutline().getBounds2D();
   }

   public void setGlyphPosition(int glyphIndex, Point2D newPos) {
      int idx = glyphIndex * 2;
      this.positions[idx] = (float)newPos.getX();
      this.positions[idx + 1] = (float)newPos.getY();
   }

   public void setGlyphTransform(int glyphIndex, AffineTransform newTX) {
      this.glyphTransforms[glyphIndex] = newTX;
   }

   public void setGlyphVisible(int glyphIndex, boolean visible) {
      this.glyphVisibilities[glyphIndex] = visible;
   }

   public boolean isGlyphVisible(int glyphIndex) {
      return this.glyphVisibilities[glyphIndex];
   }

   public int getCharacterCount(int startGlyphIndex, int endGlyphIndex) {
      return endGlyphIndex - startGlyphIndex + 1;
   }

   public boolean isReversed() {
      return false;
   }

   public void maybeReverse(boolean mirror) {
   }

   public void draw(Graphics2D graphics2d, AttributedCharacterIterator aci) {
   }

   private Rectangle2D[] getBoundingBoxes() {
      if (this.boundingBoxes == null) {
         this.buildBoundingBoxes();
      }

      return this.boundingBoxes;
   }

   private void buildBoundingBoxes() {
      this.boundingBoxes = new Rectangle2D[this.glyphs.length];

      for(int i = 0; i < this.glyphs.length; ++i) {
         Rectangle bbox = this.fontMetrics.getBoundingBox(this.glyphs[i], this.fontSize);
         this.boundingBoxes[i] = new Rectangle2D.Float((float)bbox.x / 1000000.0F, (float)(-(bbox.y + bbox.height)) / 1000000.0F, (float)bbox.width / 1000000.0F, (float)bbox.height / 1000000.0F);
      }

   }

   private static class SVGTextFragment implements TextFragment {
      private final CharacterIterator charIter;
      private String script;
      private String language;
      private int level = -1;

      SVGTextFragment(CharacterIterator charIter) {
         this.charIter = charIter;
         if (charIter instanceof AttributedCharacterIterator) {
            AttributedCharacterIterator aci = (AttributedCharacterIterator)charIter;
            aci.first();
            this.script = (String)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.SCRIPT);
            this.language = (String)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.LANGUAGE);
            Integer level = (Integer)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.BIDI_LEVEL);
            if (level != null) {
               this.level = level;
            }
         }

      }

      public CharacterIterator getIterator() {
         return this.charIter;
      }

      public int getBeginIndex() {
         return this.charIter.getBeginIndex();
      }

      public int getEndIndex() {
         return this.charIter.getEndIndex();
      }

      public CharSequence subSequence(int startIndex, int endIndex) {
         StringBuilder sb = new StringBuilder();

         for(char c = this.charIter.first(); c != '\uffff'; c = this.charIter.next()) {
            sb.append(c);
         }

         return sb.toString();
      }

      public String getScript() {
         return this.script != null ? this.script : "auto";
      }

      public String getLanguage() {
         return this.language != null ? this.language : "none";
      }

      public int getBidiLevel() {
         return this.level;
      }

      public char charAt(int index) {
         return this.charIter.setIndex(index - this.charIter.getBeginIndex());
      }
   }
}
