package org.apache.batik.bridge;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.Character.UnicodeBlock;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.HashSet;
import java.util.Set;
import org.apache.batik.gvt.font.AWTGVTFont;
import org.apache.batik.gvt.font.AltGlyphHandler;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTGlyphMetrics;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.GVTLineMetrics;
import org.apache.batik.gvt.text.ArabicTextHandler;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPath;

public class GlyphLayout implements TextSpanLayout {
   protected GVTGlyphVector gv;
   private GVTFont font;
   private GVTLineMetrics metrics;
   private AttributedCharacterIterator aci;
   protected Point2D advance;
   private Point2D offset;
   private float xScale = 1.0F;
   private float yScale = 1.0F;
   private TextPath textPath;
   private Point2D textPathAdvance;
   private int[] charMap;
   private boolean vertical;
   private boolean adjSpacing = true;
   private float[] glyphAdvances;
   private boolean isAltGlyph;
   protected boolean layoutApplied = false;
   private boolean spacingApplied = false;
   private boolean pathApplied = false;
   public static final AttributedCharacterIterator.Attribute FLOW_LINE_BREAK;
   public static final AttributedCharacterIterator.Attribute FLOW_PARAGRAPH;
   public static final AttributedCharacterIterator.Attribute FLOW_EMPTY_PARAGRAPH;
   public static final AttributedCharacterIterator.Attribute LINE_HEIGHT;
   public static final AttributedCharacterIterator.Attribute VERTICAL_ORIENTATION;
   public static final AttributedCharacterIterator.Attribute VERTICAL_ORIENTATION_ANGLE;
   public static final AttributedCharacterIterator.Attribute HORIZONTAL_ORIENTATION_ANGLE;
   private static final AttributedCharacterIterator.Attribute X;
   private static final AttributedCharacterIterator.Attribute Y;
   private static final AttributedCharacterIterator.Attribute DX;
   private static final AttributedCharacterIterator.Attribute DY;
   private static final AttributedCharacterIterator.Attribute ROTATION;
   private static final AttributedCharacterIterator.Attribute BASELINE_SHIFT;
   private static final AttributedCharacterIterator.Attribute WRITING_MODE;
   private static final Integer WRITING_MODE_TTB;
   private static final Integer ORIENTATION_AUTO;
   public static final AttributedCharacterIterator.Attribute GVT_FONT;
   protected static Set runAtts;
   protected static Set szAtts;
   public static final double eps = 1.0E-5;

   public GlyphLayout(AttributedCharacterIterator aci, int[] charMap, Point2D offset, FontRenderContext frc) {
      this.aci = aci;
      this.offset = offset;
      this.font = this.getFont();
      this.charMap = charMap;
      this.metrics = this.font.getLineMetrics((CharacterIterator)aci, aci.getBeginIndex(), aci.getEndIndex(), frc);
      this.gv = null;
      this.aci.first();
      this.vertical = aci.getAttribute(WRITING_MODE) == WRITING_MODE_TTB;
      this.textPath = (TextPath)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.TEXTPATH);
      AltGlyphHandler altGlyphHandler = (AltGlyphHandler)this.aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.ALT_GLYPH_HANDLER);
      if (altGlyphHandler != null) {
         this.gv = altGlyphHandler.createGlyphVector(frc, this.font.getSize(), this.aci);
         if (this.gv != null) {
            this.isAltGlyph = true;
         }
      }

      if (this.gv == null) {
         this.gv = this.font.createGlyphVector(frc, (CharacterIterator)this.aci);
      }

   }

   public GVTGlyphVector getGlyphVector() {
      return this.gv;
   }

   public Point2D getOffset() {
      return this.offset;
   }

   public void setScale(float xScale, float yScale, boolean adjSpacing) {
      if (this.vertical) {
         xScale = 1.0F;
      } else {
         yScale = 1.0F;
      }

      if (xScale != this.xScale || yScale != this.yScale || adjSpacing != this.adjSpacing) {
         this.xScale = xScale;
         this.yScale = yScale;
         this.adjSpacing = adjSpacing;
         this.spacingApplied = false;
         this.glyphAdvances = null;
         this.pathApplied = false;
      }

   }

   public void setOffset(Point2D offset) {
      if (offset.getX() != this.offset.getX() || offset.getY() != this.offset.getY()) {
         if (this.layoutApplied || this.spacingApplied) {
            float dx = (float)(offset.getX() - this.offset.getX());
            float dy = (float)(offset.getY() - this.offset.getY());
            int numGlyphs = this.gv.getNumGlyphs();
            float[] gp = this.gv.getGlyphPositions(0, numGlyphs + 1, (float[])null);
            Point2D.Float pos = new Point2D.Float();

            for(int i = 0; i <= numGlyphs; ++i) {
               pos.x = gp[2 * i] + dx;
               pos.y = gp[2 * i + 1] + dy;
               this.gv.setGlyphPosition(i, pos);
            }
         }

         this.offset = offset;
         this.pathApplied = false;
      }

   }

   public GVTGlyphMetrics getGlyphMetrics(int glyphIndex) {
      return this.gv.getGlyphMetrics(glyphIndex);
   }

   public GVTLineMetrics getLineMetrics() {
      return this.metrics;
   }

   public boolean isVertical() {
      return this.vertical;
   }

   public boolean isOnATextPath() {
      return this.textPath != null;
   }

   public int getGlyphCount() {
      return this.gv.getNumGlyphs();
   }

   public int getCharacterCount(int startGlyphIndex, int endGlyphIndex) {
      return this.gv.getCharacterCount(startGlyphIndex, endGlyphIndex);
   }

   public boolean isLeftToRight() {
      this.aci.first();
      int bidiLevel = (Integer)this.aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.BIDI_LEVEL);
      return (bidiLevel & 1) == 0;
   }

   private final void syncLayout() {
      if (!this.pathApplied) {
         this.doPathLayout();
      }

   }

   public void draw(Graphics2D g2d) {
      this.syncLayout();
      this.gv.draw(g2d, this.aci);
   }

   public Point2D getAdvance2D() {
      this.adjustTextSpacing();
      return this.advance;
   }

   public Shape getOutline() {
      this.syncLayout();
      return this.gv.getOutline();
   }

   public float[] getGlyphAdvances() {
      if (this.glyphAdvances != null) {
         return this.glyphAdvances;
      } else {
         if (!this.spacingApplied) {
            this.adjustTextSpacing();
         }

         int numGlyphs = this.gv.getNumGlyphs();
         float[] glyphPos = this.gv.getGlyphPositions(0, numGlyphs + 1, (float[])null);
         this.glyphAdvances = new float[numGlyphs + 1];
         int off = 0;
         if (this.isVertical()) {
            off = 1;
         }

         float start = glyphPos[off];

         for(int i = 0; i < numGlyphs + 1; ++i) {
            this.glyphAdvances[i] = glyphPos[2 * i + off] - start;
         }

         return this.glyphAdvances;
      }
   }

   public Shape getDecorationOutline(int decorationType) {
      this.syncLayout();
      Shape g = new GeneralPath();
      if ((decorationType & 1) != 0) {
         ((GeneralPath)g).append(this.getUnderlineShape(), false);
      }

      if ((decorationType & 2) != 0) {
         ((GeneralPath)g).append(this.getStrikethroughShape(), false);
      }

      if ((decorationType & 4) != 0) {
         ((GeneralPath)g).append(this.getOverlineShape(), false);
      }

      return g;
   }

   public Rectangle2D getBounds2D() {
      this.syncLayout();
      return this.gv.getBounds2D(this.aci);
   }

   public Rectangle2D getGeometricBounds() {
      this.syncLayout();
      Rectangle2D gvB = this.gv.getGeometricBounds();
      Rectangle2D decB = this.getDecorationOutline(7).getBounds2D();
      return gvB.createUnion(decB);
   }

   public Point2D getTextPathAdvance() {
      this.syncLayout();
      return this.textPath != null ? this.textPathAdvance : this.getAdvance2D();
   }

   public int getGlyphIndex(int charIndex) {
      int numGlyphs = this.getGlyphCount();
      int j = 0;

      for(int i = 0; i < numGlyphs; ++i) {
         int count = this.getCharacterCount(i, i);

         for(int n = 0; n < count; ++n) {
            int glyphCharIndex = this.charMap[j++];
            if (charIndex == glyphCharIndex) {
               return i;
            }

            if (j >= this.charMap.length) {
               return -1;
            }
         }
      }

      return -1;
   }

   public int getLastGlyphIndex(int charIndex) {
      int numGlyphs = this.getGlyphCount();
      int j = this.charMap.length - 1;

      for(int i = numGlyphs - 1; i >= 0; --i) {
         int count = this.getCharacterCount(i, i);

         for(int n = 0; n < count; ++n) {
            int glyphCharIndex = this.charMap[j--];
            if (charIndex == glyphCharIndex) {
               return i;
            }

            if (j < 0) {
               return -1;
            }
         }
      }

      return -1;
   }

   public double getComputedOrientationAngle(int index) {
      if (this.isGlyphOrientationAuto()) {
         if (this.isVertical()) {
            char ch = this.aci.setIndex(index);
            return this.isLatinChar(ch) ? 90.0 : 0.0;
         } else {
            return 0.0;
         }
      } else {
         return (double)this.getGlyphOrientationAngle();
      }
   }

   public Shape getHighlightShape(int beginCharIndex, int endCharIndex) {
      this.syncLayout();
      if (beginCharIndex > endCharIndex) {
         int temp = beginCharIndex;
         beginCharIndex = endCharIndex;
         endCharIndex = temp;
      }

      GeneralPath shape = null;
      int numGlyphs = this.getGlyphCount();
      Point2D.Float[] topPts = new Point2D.Float[2 * numGlyphs];
      Point2D.Float[] botPts = new Point2D.Float[2 * numGlyphs];
      int ptIdx = 0;
      int currentChar = 0;

      for(int i = 0; i < numGlyphs; ++i) {
         int glyphCharIndex = this.charMap[currentChar];
         if (glyphCharIndex >= beginCharIndex && glyphCharIndex <= endCharIndex && this.gv.isGlyphVisible(i)) {
            Shape gbounds = this.gv.getGlyphLogicalBounds(i);
            if (gbounds != null) {
               if (shape == null) {
                  shape = new GeneralPath();
               }

               float[] pts = new float[6];
               int count = 0;
               int type = true;
               PathIterator pi = gbounds.getPathIterator((AffineTransform)null);
               Point2D.Float firstPt = null;

               while(!pi.isDone()) {
                  int type = pi.currentSegment(pts);
                  if (type != 0 && type != 1) {
                     if (type != 4 || count < 4 || count > 5) {
                        break;
                     }
                  } else {
                     if (count > 4) {
                        break;
                     }

                     if (count == 4) {
                        if (firstPt == null || firstPt.x != pts[0] || firstPt.y != pts[1]) {
                           break;
                        }
                     } else {
                        Point2D.Float pt = new Point2D.Float(pts[0], pts[1]);
                        if (count == 0) {
                           firstPt = pt;
                        }

                        switch (count) {
                           case 0:
                              botPts[ptIdx] = pt;
                              break;
                           case 1:
                              topPts[ptIdx] = pt;
                              break;
                           case 2:
                              topPts[ptIdx + 1] = pt;
                              break;
                           case 3:
                              botPts[ptIdx + 1] = pt;
                        }
                     }
                  }

                  ++count;
                  pi.next();
               }

               if (pi.isDone()) {
                  if (botPts[ptIdx] != null && (topPts[ptIdx].x != topPts[ptIdx + 1].x || topPts[ptIdx].y != topPts[ptIdx + 1].y)) {
                     ptIdx += 2;
                  }
               } else {
                  addPtsToPath(shape, topPts, botPts, ptIdx);
                  ptIdx = 0;
                  shape.append(gbounds, false);
               }
            }
         }

         currentChar += this.getCharacterCount(i, i);
         if (currentChar >= this.charMap.length) {
            currentChar = this.charMap.length - 1;
         }
      }

      addPtsToPath(shape, topPts, botPts, ptIdx);
      return shape;
   }

   public static boolean epsEQ(double a, double b) {
      return a + 1.0E-5 > b && a - 1.0E-5 < b;
   }

   public static int makeConvexHull(Point2D.Float[] pts, int numPts) {
      for(int i = 1; i < numPts; ++i) {
         if (pts[i].x < pts[i - 1].x || pts[i].x == pts[i - 1].x && pts[i].y < pts[i - 1].y) {
            Point2D.Float tmp = pts[i];
            pts[i] = pts[i - 1];
            pts[i - 1] = tmp;
            i = 0;
         }
      }

      Point2D.Float pt0 = pts[0];
      Point2D.Float pt1 = pts[numPts - 1];
      Point2D.Float dxdy = new Point2D.Float(pt1.x - pt0.x, pt1.y - pt0.y);
      float c = dxdy.y * pt0.x - dxdy.x * pt0.y;
      Point2D.Float[] topList = new Point2D.Float[numPts];
      Point2D.Float[] botList = new Point2D.Float[numPts];
      botList[0] = topList[0] = pts[0];
      int nTopPts = 1;
      int nBotPts = 1;

      float soln;
      float dy;
      float c0;
      for(int i = 1; i < numPts - 1; ++i) {
         Point2D.Float pt = pts[i];
         soln = dxdy.x * pt.y - dxdy.y * pt.x + c;
         float c0;
         if (soln < 0.0F) {
            while(nBotPts >= 2) {
               pt0 = botList[nBotPts - 2];
               pt1 = botList[nBotPts - 1];
               dy = pt1.x - pt0.x;
               c0 = pt1.y - pt0.y;
               c0 = c0 * pt0.x - dy * pt0.y;
               soln = dy * pt.y - c0 * pt.x + c0;
               if ((double)soln > 1.0E-5) {
                  break;
               }

               if ((double)soln > -1.0E-5) {
                  if (pt1.y < pt.y) {
                     pt = pt1;
                  }

                  --nBotPts;
                  break;
               }

               --nBotPts;
            }

            botList[nBotPts++] = pt;
         } else {
            while(nTopPts >= 2) {
               pt0 = topList[nTopPts - 2];
               pt1 = topList[nTopPts - 1];
               dy = pt1.x - pt0.x;
               c0 = pt1.y - pt0.y;
               c0 = c0 * pt0.x - dy * pt0.y;
               soln = dy * pt.y - c0 * pt.x + c0;
               if ((double)soln < -1.0E-5) {
                  break;
               }

               if ((double)soln < 1.0E-5) {
                  if (pt1.y > pt.y) {
                     pt = pt1;
                  }

                  --nTopPts;
                  break;
               }

               --nTopPts;
            }

            topList[nTopPts++] = pt;
         }
      }

      Point2D.Float pt;
      float dx;
      for(pt = pts[numPts - 1]; nBotPts >= 2; --nBotPts) {
         pt0 = botList[nBotPts - 2];
         pt1 = botList[nBotPts - 1];
         dx = pt1.x - pt0.x;
         dy = pt1.y - pt0.y;
         c0 = dy * pt0.x - dx * pt0.y;
         soln = dx * pt.y - dy * pt.x + c0;
         if ((double)soln > 1.0E-5) {
            break;
         }

         if ((double)soln > -1.0E-5) {
            if (pt1.y >= pt.y) {
               --nBotPts;
            }
            break;
         }
      }

      while(nTopPts >= 2) {
         pt0 = topList[nTopPts - 2];
         pt1 = topList[nTopPts - 1];
         dx = pt1.x - pt0.x;
         dy = pt1.y - pt0.y;
         c0 = dy * pt0.x - dx * pt0.y;
         soln = dx * pt.y - dy * pt.x + c0;
         if ((double)soln < -1.0E-5) {
            break;
         }

         if ((double)soln < 1.0E-5) {
            if (pt1.y <= pt.y) {
               --nTopPts;
            }
            break;
         }

         --nTopPts;
      }

      System.arraycopy(topList, 0, pts, 0, nTopPts);
      int i = nTopPts + 1;
      pts[nTopPts] = pts[numPts - 1];

      for(int n = nBotPts - 1; n > 0; ++i) {
         pts[i] = botList[n];
         --n;
      }

      return i;
   }

   public static void addPtsToPath(GeneralPath shape, Point2D.Float[] topPts, Point2D.Float[] botPts, int numPts) {
      if (numPts >= 2) {
         if (numPts == 2) {
            shape.moveTo(topPts[0].x, topPts[0].y);
            shape.lineTo(topPts[1].x, topPts[1].y);
            shape.lineTo(botPts[1].x, botPts[1].y);
            shape.lineTo(botPts[0].x, botPts[0].y);
            shape.lineTo(topPts[0].x, topPts[0].y);
         } else {
            Point2D.Float[] boxes = new Point2D.Float[8];
            Point2D.Float[] chull = new Point2D.Float[8];
            boxes[4] = topPts[0];
            boxes[5] = topPts[1];
            boxes[6] = botPts[1];
            boxes[7] = botPts[0];
            Area[] areas = new Area[numPts / 2];
            int nAreas = 0;

            for(int i = 2; i < numPts; i += 2) {
               boxes[0] = boxes[4];
               boxes[1] = boxes[5];
               boxes[2] = boxes[6];
               boxes[3] = boxes[7];
               boxes[4] = topPts[i];
               boxes[5] = topPts[i + 1];
               boxes[6] = botPts[i + 1];
               boxes[7] = botPts[i];
               float delta = boxes[2].x - boxes[0].x;
               float dist = delta * delta;
               delta = boxes[2].y - boxes[0].y;
               dist += delta * delta;
               float sz = (float)Math.sqrt((double)dist);
               delta = boxes[6].x - boxes[4].x;
               dist = delta * delta;
               delta = boxes[6].y - boxes[4].y;
               dist += delta * delta;
               sz += (float)Math.sqrt((double)dist);
               delta = (boxes[0].x + boxes[1].x + boxes[2].x + boxes[3].x - (boxes[4].x + boxes[5].x + boxes[6].x + boxes[7].x)) / 4.0F;
               dist = delta * delta;
               delta = (boxes[0].y + boxes[1].y + boxes[2].y + boxes[3].y - (boxes[4].y + boxes[5].y + boxes[6].y + boxes[7].y)) / 4.0F;
               dist += delta * delta;
               dist = (float)Math.sqrt((double)dist);
               GeneralPath gp = new GeneralPath();
               if (!(dist < sz)) {
                  mergeAreas(shape, areas, nAreas);
                  nAreas = 0;
                  if (i == 2) {
                     gp.moveTo(boxes[0].x, boxes[0].y);
                     gp.lineTo(boxes[1].x, boxes[1].y);
                     gp.lineTo(boxes[2].x, boxes[2].y);
                     gp.lineTo(boxes[3].x, boxes[3].y);
                     gp.closePath();
                     shape.append(gp, false);
                     gp.reset();
                  }

                  gp.moveTo(boxes[4].x, boxes[4].y);
                  gp.lineTo(boxes[5].x, boxes[5].y);
                  gp.lineTo(boxes[6].x, boxes[6].y);
                  gp.lineTo(boxes[7].x, boxes[7].y);
                  gp.closePath();
               } else {
                  System.arraycopy(boxes, 0, chull, 0, 8);
                  int npts = makeConvexHull(chull, 8);
                  gp.moveTo(chull[0].x, chull[0].y);

                  for(int n = 1; n < npts; ++n) {
                     gp.lineTo(chull[n].x, chull[n].y);
                  }

                  gp.closePath();
               }

               areas[nAreas++] = new Area(gp);
            }

            mergeAreas(shape, areas, nAreas);
         }
      }
   }

   public static void mergeAreas(GeneralPath shape, Area[] shapes, int nShapes) {
      for(; nShapes > 1; nShapes /= 2) {
         int n = 0;

         for(int i = 1; i < nShapes; i += 2) {
            shapes[i - 1].add(shapes[i]);
            shapes[n++] = shapes[i - 1];
            shapes[i] = null;
         }

         if ((nShapes & 1) == 1) {
            shapes[n - 1].add(shapes[nShapes - 1]);
         }
      }

      if (nShapes == 1) {
         shape.append(shapes[0], false);
      }

   }

   public TextHit hitTestChar(float x, float y) {
      this.syncLayout();
      TextHit textHit = null;
      int currentChar = 0;

      for(int i = 0; i < this.gv.getNumGlyphs(); ++i) {
         Shape gbounds = this.gv.getGlyphLogicalBounds(i);
         if (gbounds != null) {
            Rectangle2D gbounds2d = gbounds.getBounds2D();
            if (gbounds.contains((double)x, (double)y)) {
               boolean isRightHalf = (double)x > gbounds2d.getX() + gbounds2d.getWidth() / 2.0;
               boolean isLeadingEdge = !isRightHalf;
               int charIndex = this.charMap[currentChar];
               textHit = new TextHit(charIndex, isLeadingEdge);
               return textHit;
            }
         }

         currentChar += this.getCharacterCount(i, i);
         if (currentChar >= this.charMap.length) {
            currentChar = this.charMap.length - 1;
         }
      }

      return textHit;
   }

   protected GVTFont getFont() {
      this.aci.first();
      GVTFont gvtFont = (GVTFont)this.aci.getAttribute(GVT_FONT);
      return (GVTFont)(gvtFont != null ? gvtFont : new AWTGVTFont(this.aci.getAttributes()));
   }

   protected Shape getOverlineShape() {
      double y = (double)this.metrics.getOverlineOffset();
      float overlineThickness = this.metrics.getOverlineThickness();
      y += (double)overlineThickness;
      this.aci.first();
      Float dy = (Float)this.aci.getAttribute(DY);
      if (dy != null) {
         y += (double)dy;
      }

      Stroke overlineStroke = new BasicStroke(overlineThickness);
      Rectangle2D logicalBounds = this.gv.getLogicalBounds();
      return overlineStroke.createStrokedShape(new Line2D.Double(logicalBounds.getMinX() + (double)overlineThickness / 2.0, this.offset.getY() + y, logicalBounds.getMaxX() - (double)overlineThickness / 2.0, this.offset.getY() + y));
   }

   protected Shape getUnderlineShape() {
      double y = (double)this.metrics.getUnderlineOffset();
      float underlineThickness = this.metrics.getUnderlineThickness();
      y += (double)underlineThickness * 1.5;
      BasicStroke underlineStroke = new BasicStroke(underlineThickness);
      this.aci.first();
      Float dy = (Float)this.aci.getAttribute(DY);
      if (dy != null) {
         y += (double)dy;
      }

      Rectangle2D logicalBounds = this.gv.getLogicalBounds();
      return underlineStroke.createStrokedShape(new Line2D.Double(logicalBounds.getMinX() + (double)underlineThickness / 2.0, this.offset.getY() + y, logicalBounds.getMaxX() - (double)underlineThickness / 2.0, this.offset.getY() + y));
   }

   protected Shape getStrikethroughShape() {
      double y = (double)this.metrics.getStrikethroughOffset();
      float strikethroughThickness = this.metrics.getStrikethroughThickness();
      Stroke strikethroughStroke = new BasicStroke(strikethroughThickness);
      this.aci.first();
      Float dy = (Float)this.aci.getAttribute(DY);
      if (dy != null) {
         y += (double)dy;
      }

      Rectangle2D logicalBounds = this.gv.getLogicalBounds();
      return strikethroughStroke.createStrokedShape(new Line2D.Double(logicalBounds.getMinX() + (double)strikethroughThickness / 2.0, this.offset.getY() + y, logicalBounds.getMaxX() - (double)strikethroughThickness / 2.0, this.offset.getY() + y));
   }

   protected void doExplicitGlyphLayout() {
      this.gv.performDefaultLayout();
      float baselineAscent = this.vertical ? (float)this.gv.getLogicalBounds().getWidth() : this.metrics.getAscent() + Math.abs(this.metrics.getDescent());
      int numGlyphs = this.gv.getNumGlyphs();
      float[] gp = this.gv.getGlyphPositions(0, numGlyphs + 1, (float[])null);
      float verticalFirstOffset = 0.0F;
      float horizontalFirstOffset = 0.0F;
      boolean glyphOrientationAuto = this.isGlyphOrientationAuto();
      int glyphOrientationAngle = 0;
      if (!glyphOrientationAuto) {
         glyphOrientationAngle = this.getGlyphOrientationAngle();
      }

      int i = 0;
      int aciStart = this.aci.getBeginIndex();
      int aciIndex = 0;
      char ch = this.aci.first();
      int runLimit = aciIndex + aciStart;
      Float x = null;
      Float y = null;
      Float dx = null;
      Float dy = null;
      Float rotation = null;
      Object baseline = null;
      float shift_x_pos = 0.0F;
      float shift_y_pos = 0.0F;
      float curr_x_pos = (float)this.offset.getX();
      float curr_y_pos = (float)this.offset.getY();
      Point2D.Float pos = new Point2D.Float();

      boolean hasArabicTransparent;
      float baselineAdjust;
      for(hasArabicTransparent = false; i < numGlyphs; ++i) {
         if (aciIndex + aciStart >= runLimit) {
            runLimit = this.aci.getRunLimit(runAtts);
            x = (Float)this.aci.getAttribute(X);
            y = (Float)this.aci.getAttribute(Y);
            dx = (Float)this.aci.getAttribute(DX);
            dy = (Float)this.aci.getAttribute(DY);
            rotation = (Float)this.aci.getAttribute(ROTATION);
            baseline = this.aci.getAttribute(BASELINE_SHIFT);
         }

         GVTGlyphMetrics gm = this.gv.getGlyphMetrics(i);
         float ox;
         float oy;
         float glyphOrientationRotation;
         if (i == 0) {
            if (this.isVertical()) {
               if (glyphOrientationAuto) {
                  if (this.isLatinChar(ch)) {
                     verticalFirstOffset = 0.0F;
                  } else {
                     ox = gm.getVerticalAdvance();
                     oy = this.metrics.getAscent();
                     glyphOrientationRotation = this.metrics.getDescent();
                     verticalFirstOffset = oy + (ox - (oy + glyphOrientationRotation)) / 2.0F;
                  }
               } else if (glyphOrientationAngle == 0) {
                  ox = gm.getVerticalAdvance();
                  oy = this.metrics.getAscent();
                  glyphOrientationRotation = this.metrics.getDescent();
                  verticalFirstOffset = oy + (ox - (oy + glyphOrientationRotation)) / 2.0F;
               } else {
                  verticalFirstOffset = 0.0F;
               }
            } else if (glyphOrientationAngle == 270) {
               horizontalFirstOffset = (float)gm.getBounds2D().getHeight();
            } else {
               horizontalFirstOffset = 0.0F;
            }
         } else if (glyphOrientationAuto && verticalFirstOffset == 0.0F && !this.isLatinChar(ch)) {
            ox = gm.getVerticalAdvance();
            oy = this.metrics.getAscent();
            glyphOrientationRotation = this.metrics.getDescent();
            verticalFirstOffset = oy + (ox - (oy + glyphOrientationRotation)) / 2.0F;
         }

         ox = 0.0F;
         oy = 0.0F;
         glyphOrientationRotation = 0.0F;
         float glyphRotation = 0.0F;
         if (ch != '\uffff') {
            if (this.vertical) {
               if (glyphOrientationAuto) {
                  if (this.isLatinChar(ch)) {
                     glyphOrientationRotation = 1.5707964F;
                  } else {
                     glyphOrientationRotation = 0.0F;
                  }
               } else {
                  glyphOrientationRotation = (float)Math.toRadians((double)glyphOrientationAngle);
               }

               if (this.textPath != null) {
                  x = null;
               }
            } else {
               glyphOrientationRotation = (float)Math.toRadians((double)glyphOrientationAngle);
               if (this.textPath != null) {
                  y = null;
               }
            }

            if (rotation != null && !rotation.isNaN()) {
               glyphRotation = rotation + glyphOrientationRotation;
            } else {
               glyphRotation = glyphOrientationRotation;
            }

            if (x != null && !x.isNaN()) {
               if (i == 0) {
                  shift_x_pos = (float)((double)x - this.offset.getX());
               }

               curr_x_pos = x - shift_x_pos;
            }

            if (dx != null && !dx.isNaN()) {
               curr_x_pos += dx;
            }

            if (y != null && !y.isNaN()) {
               if (i == 0) {
                  shift_y_pos = (float)((double)y - this.offset.getY());
               }

               curr_y_pos = y - shift_y_pos;
            }

            if (dy != null && !dy.isNaN()) {
               curr_y_pos += dy;
            } else if (i > 0) {
               curr_y_pos += gp[i * 2 + 1] - gp[i * 2 - 1];
            }

            baselineAdjust = 0.0F;
            if (baseline != null) {
               if (baseline instanceof Integer) {
                  if (baseline == TextAttribute.SUPERSCRIPT_SUPER) {
                     baselineAdjust = baselineAscent * 0.5F;
                  } else if (baseline == TextAttribute.SUPERSCRIPT_SUB) {
                     baselineAdjust = -baselineAscent * 0.5F;
                  }
               } else if (baseline instanceof Float) {
                  baselineAdjust = (Float)baseline;
               }

               if (this.vertical) {
                  ox = baselineAdjust;
               } else {
                  oy = -baselineAdjust;
               }
            }

            if (this.vertical) {
               oy += verticalFirstOffset;
               Rectangle2D glyphBounds;
               if (glyphOrientationAuto) {
                  if (this.isLatinChar(ch)) {
                     ox += this.metrics.getStrikethroughOffset();
                  } else {
                     glyphBounds = this.gv.getGlyphVisualBounds(i).getBounds2D();
                     ox -= (float)(glyphBounds.getMaxX() - (double)gp[2 * i] - glyphBounds.getWidth() / 2.0);
                  }
               } else {
                  glyphBounds = this.gv.getGlyphVisualBounds(i).getBounds2D();
                  if (glyphOrientationAngle == 0) {
                     ox -= (float)(glyphBounds.getMaxX() - (double)gp[2 * i] - glyphBounds.getWidth() / 2.0);
                  } else if (glyphOrientationAngle == 180) {
                     ox += (float)(glyphBounds.getMaxX() - (double)gp[2 * i] - glyphBounds.getWidth() / 2.0);
                  } else if (glyphOrientationAngle == 90) {
                     ox += this.metrics.getStrikethroughOffset();
                  } else {
                     ox -= this.metrics.getStrikethroughOffset();
                  }
               }
            } else {
               ox += horizontalFirstOffset;
               if (glyphOrientationAngle == 90) {
                  oy -= gm.getHorizontalAdvance();
               } else if (glyphOrientationAngle == 180) {
                  oy -= this.metrics.getAscent();
               }
            }
         }

         pos.x = curr_x_pos + ox;
         pos.y = curr_y_pos + oy;
         this.gv.setGlyphPosition(i, pos);
         if (ArabicTextHandler.arabicCharTransparent(ch)) {
            hasArabicTransparent = true;
         } else if (!this.vertical) {
            baselineAdjust = 0.0F;
            if (glyphOrientationAngle == 0) {
               baselineAdjust = gm.getHorizontalAdvance();
            } else if (glyphOrientationAngle == 180) {
               baselineAdjust = gm.getHorizontalAdvance();
               this.gv.setGlyphTransform(i, AffineTransform.getTranslateInstance((double)baselineAdjust, 0.0));
            } else {
               baselineAdjust = gm.getVerticalAdvance();
            }

            curr_x_pos += baselineAdjust;
         } else {
            baselineAdjust = 0.0F;
            if (glyphOrientationAuto) {
               if (this.isLatinChar(ch)) {
                  baselineAdjust = gm.getHorizontalAdvance();
               } else {
                  baselineAdjust = gm.getVerticalAdvance();
               }
            } else if (glyphOrientationAngle != 0 && glyphOrientationAngle != 180) {
               if (glyphOrientationAngle == 90) {
                  baselineAdjust = gm.getHorizontalAdvance();
               } else {
                  baselineAdjust = gm.getHorizontalAdvance();
                  this.gv.setGlyphTransform(i, AffineTransform.getTranslateInstance(0.0, (double)baselineAdjust));
               }
            } else {
               baselineAdjust = gm.getVerticalAdvance();
            }

            curr_y_pos += baselineAdjust;
         }

         if (!epsEQ((double)glyphRotation, 0.0)) {
            AffineTransform glyphTransform = this.gv.getGlyphTransform(i);
            if (glyphTransform == null) {
               glyphTransform = new AffineTransform();
            }

            AffineTransform rotAt;
            if (epsEQ((double)glyphRotation, 1.5707963267948966)) {
               rotAt = new AffineTransform(0.0F, 1.0F, -1.0F, 0.0F, 0.0F, 0.0F);
            } else if (epsEQ((double)glyphRotation, Math.PI)) {
               rotAt = new AffineTransform(-1.0F, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F);
            } else if (epsEQ((double)glyphRotation, 4.71238898038469)) {
               rotAt = new AffineTransform(0.0F, -1.0F, 1.0F, 0.0F, 0.0F, 0.0F);
            } else {
               rotAt = AffineTransform.getRotateInstance((double)glyphRotation);
            }

            glyphTransform.concatenate(rotAt);
            this.gv.setGlyphTransform(i, glyphTransform);
         }

         aciIndex += this.gv.getCharacterCount(i, i);
         if (aciIndex >= this.charMap.length) {
            aciIndex = this.charMap.length - 1;
         }

         ch = this.aci.setIndex(aciIndex + aciStart);
      }

      pos.x = curr_x_pos;
      pos.y = curr_y_pos;
      this.gv.setGlyphPosition(i, pos);
      this.advance = new Point2D.Float((float)((double)curr_x_pos - this.offset.getX()), (float)((double)curr_y_pos - this.offset.getY()));
      if (hasArabicTransparent) {
         ch = this.aci.first();
         aciIndex = 0;
         i = 0;

         for(int transparentStart = -1; i < numGlyphs; ++i) {
            if (ArabicTextHandler.arabicCharTransparent(ch)) {
               if (transparentStart == -1) {
                  transparentStart = i;
               }
            } else if (transparentStart != -1) {
               Point2D loc = this.gv.getGlyphPosition(i);
               GVTGlyphMetrics gm = this.gv.getGlyphMetrics(i);
               int tyS = false;
               int txS = false;
               baselineAdjust = 0.0F;
               float advY = 0.0F;
               if (this.vertical) {
                  if (!glyphOrientationAuto && glyphOrientationAngle != 90) {
                     if (glyphOrientationAngle == 270) {
                        advY = 0.0F;
                     } else if (glyphOrientationAngle == 0) {
                        baselineAdjust = gm.getHorizontalAdvance();
                     } else {
                        baselineAdjust = -gm.getHorizontalAdvance();
                     }
                  } else {
                     advY = gm.getHorizontalAdvance();
                  }
               } else if (glyphOrientationAngle == 0) {
                  baselineAdjust = gm.getHorizontalAdvance();
               } else if (glyphOrientationAngle == 90) {
                  advY = gm.getHorizontalAdvance();
               } else if (glyphOrientationAngle == 180) {
                  baselineAdjust = 0.0F;
               } else {
                  advY = -gm.getHorizontalAdvance();
               }

               float baseX = (float)(loc.getX() + (double)baselineAdjust);
               float baseY = (float)(loc.getY() + (double)advY);
               int j = transparentStart;

               while(true) {
                  if (j >= i) {
                     transparentStart = -1;
                     break;
                  }

                  Point2D locT = this.gv.getGlyphPosition(j);
                  GVTGlyphMetrics gmT = this.gv.getGlyphMetrics(j);
                  float locX = (float)locT.getX();
                  float locY = (float)locT.getY();
                  float tx = 0.0F;
                  float ty = 0.0F;
                  float advT = gmT.getHorizontalAdvance();
                  if (this.vertical) {
                     if (!glyphOrientationAuto && glyphOrientationAngle != 90) {
                        if (glyphOrientationAngle == 270) {
                           locY = baseY + advT;
                        } else if (glyphOrientationAngle == 0) {
                           locX = baseX - advT;
                        } else {
                           locX = baseX + advT;
                        }
                     } else {
                        locY = baseY - advT;
                     }
                  } else if (glyphOrientationAngle == 0) {
                     locX = baseX - advT;
                  } else if (glyphOrientationAngle == 90) {
                     locY = baseY - advT;
                  } else if (glyphOrientationAngle == 180) {
                     locX = baseX + advT;
                  } else {
                     locY = baseY + advT;
                  }

                  Point2D locT = new Point2D.Double((double)locX, (double)locY);
                  this.gv.setGlyphPosition(j, locT);
                  if (txS || tyS) {
                     AffineTransform at = AffineTransform.getTranslateInstance((double)tx, (double)ty);
                     at.concatenate(this.gv.getGlyphTransform(i));
                     this.gv.setGlyphTransform(i, at);
                  }

                  ++j;
               }
            }

            aciIndex += this.gv.getCharacterCount(i, i);
            if (aciIndex >= this.charMap.length) {
               aciIndex = this.charMap.length - 1;
            }

            ch = this.aci.setIndex(aciIndex + aciStart);
         }
      }

      this.layoutApplied = true;
      this.spacingApplied = false;
      this.glyphAdvances = null;
      this.pathApplied = false;
   }

   protected void adjustTextSpacing() {
      if (!this.spacingApplied) {
         if (!this.layoutApplied) {
            this.doExplicitGlyphLayout();
         }

         this.aci.first();
         Boolean customSpacing = (Boolean)this.aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.CUSTOM_SPACING);
         if (customSpacing != null && customSpacing) {
            this.advance = this.doSpacing((Float)this.aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.KERNING), (Float)this.aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.LETTER_SPACING), (Float)this.aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.WORD_SPACING));
            this.layoutApplied = false;
         }

         this.applyStretchTransform(!this.adjSpacing);
         this.spacingApplied = true;
         this.pathApplied = false;
      }
   }

   protected Point2D doSpacing(Float kern, Float letterSpacing, Float wordSpacing) {
      boolean autoKern = true;
      boolean doWordSpacing = false;
      boolean doLetterSpacing = false;
      float kernVal = 0.0F;
      float letterSpacingVal = 0.0F;
      if (kern != null && !kern.isNaN()) {
         kernVal = kern;
         autoKern = false;
      }

      if (letterSpacing != null && !letterSpacing.isNaN()) {
         letterSpacingVal = letterSpacing;
         doLetterSpacing = true;
      }

      if (wordSpacing != null && !wordSpacing.isNaN()) {
         doWordSpacing = true;
      }

      int numGlyphs = this.gv.getNumGlyphs();
      float dx = 0.0F;
      float dy = 0.0F;
      Point2D[] newPositions = new Point2D[numGlyphs + 1];
      Point2D prevPos = this.gv.getGlyphPosition(0);
      int prevCode = this.gv.getGlyphCode(0);
      float x = (float)prevPos.getX();
      float y = (float)prevPos.getY();
      Point2D lastCharAdvance = new Point2D.Double(this.advance.getX() - (this.gv.getGlyphPosition(numGlyphs - 1).getX() - (double)x), this.advance.getY() - (this.gv.getGlyphPosition(numGlyphs - 1).getY() - (double)y));

      try {
         GVTFont font = this.gv.getFont();
         int i;
         Point2D gpos;
         if (numGlyphs > 1 && (doLetterSpacing || !autoKern)) {
            for(i = 1; i <= numGlyphs; ++i) {
               gpos = this.gv.getGlyphPosition(i);
               int currCode = i == numGlyphs ? -1 : this.gv.getGlyphCode(i);
               dx = (float)gpos.getX() - (float)prevPos.getX();
               dy = (float)gpos.getY() - (float)prevPos.getY();
               if (autoKern) {
                  if (this.vertical) {
                     dy += letterSpacingVal;
                  } else {
                     dx += letterSpacingVal;
                  }
               } else {
                  float vKern;
                  if (this.vertical) {
                     vKern = 0.0F;
                     if (currCode != -1) {
                        vKern = font.getVKern(prevCode, currCode);
                     }

                     dy += kernVal - vKern + letterSpacingVal;
                  } else {
                     vKern = 0.0F;
                     if (currCode != -1) {
                        vKern = font.getHKern(prevCode, currCode);
                     }

                     dx += kernVal - vKern + letterSpacingVal;
                  }
               }

               x += dx;
               y += dy;
               newPositions[i] = new Point2D.Float(x, y);
               prevPos = gpos;
               prevCode = currCode;
            }

            for(i = 1; i <= numGlyphs; ++i) {
               if (newPositions[i] != null) {
                  this.gv.setGlyphPosition(i, newPositions[i]);
               }
            }
         }

         if (this.vertical) {
            lastCharAdvance.setLocation(lastCharAdvance.getX(), lastCharAdvance.getY() + (double)kernVal + (double)letterSpacingVal);
         } else {
            lastCharAdvance.setLocation(lastCharAdvance.getX() + (double)kernVal + (double)letterSpacingVal, lastCharAdvance.getY());
         }

         dx = 0.0F;
         dy = 0.0F;
         prevPos = this.gv.getGlyphPosition(0);
         x = (float)prevPos.getX();
         y = (float)prevPos.getY();
         if (numGlyphs > 1 && doWordSpacing) {
            for(i = 1; i < numGlyphs; ++i) {
               gpos = this.gv.getGlyphPosition(i);
               dx = (float)gpos.getX() - (float)prevPos.getX();
               dy = (float)gpos.getY() - (float)prevPos.getY();
               boolean inWS = false;
               int endWS = i;

               for(GVTGlyphMetrics gm = this.gv.getGlyphMetrics(i); gm.getBounds2D().getWidth() < 0.01 || gm.isWhitespace(); gm = this.gv.getGlyphMetrics(i)) {
                  if (!inWS) {
                     inWS = true;
                  }

                  if (i == numGlyphs - 1) {
                     break;
                  }

                  ++i;
                  ++endWS;
                  gpos = this.gv.getGlyphPosition(i);
               }

               if (inWS) {
                  int nWS = endWS - i;
                  float px = (float)prevPos.getX();
                  float py = (float)prevPos.getY();
                  dx = (float)(gpos.getX() - (double)px) / (float)(nWS + 1);
                  dy = (float)(gpos.getY() - (double)py) / (float)(nWS + 1);
                  if (this.vertical) {
                     dy += wordSpacing / (float)(nWS + 1);
                  } else {
                     dx += wordSpacing / (float)(nWS + 1);
                  }

                  for(int j = i; j <= endWS; ++j) {
                     x += dx;
                     y += dy;
                     newPositions[j] = new Point2D.Float(x, y);
                  }
               } else {
                  dx = (float)(gpos.getX() - prevPos.getX());
                  dy = (float)(gpos.getY() - prevPos.getY());
                  x += dx;
                  y += dy;
                  newPositions[i] = new Point2D.Float(x, y);
               }

               prevPos = gpos;
            }

            Point2D gPos = this.gv.getGlyphPosition(numGlyphs);
            x += (float)(gPos.getX() - prevPos.getX());
            y += (float)(gPos.getY() - prevPos.getY());
            newPositions[numGlyphs] = new Point2D.Float(x, y);

            for(int i = 1; i <= numGlyphs; ++i) {
               if (newPositions[i] != null) {
                  this.gv.setGlyphPosition(i, newPositions[i]);
               }
            }
         }
      } catch (Exception var29) {
         var29.printStackTrace();
      }

      double advX = this.gv.getGlyphPosition(numGlyphs - 1).getX() - this.gv.getGlyphPosition(0).getX();
      double advY = this.gv.getGlyphPosition(numGlyphs - 1).getY() - this.gv.getGlyphPosition(0).getY();
      Point2D newAdvance = new Point2D.Double(advX + lastCharAdvance.getX(), advY + lastCharAdvance.getY());
      return newAdvance;
   }

   protected void applyStretchTransform(boolean stretchGlyphs) {
      if (this.xScale != 1.0F || this.yScale != 1.0F) {
         AffineTransform scaleAT = AffineTransform.getScaleInstance((double)this.xScale, (double)this.yScale);
         int numGlyphs = this.gv.getNumGlyphs();
         float[] gp = this.gv.getGlyphPositions(0, numGlyphs + 1, (float[])null);
         float initX = gp[0];
         float initY = gp[1];
         Point2D.Float pos = new Point2D.Float();

         for(int i = 0; i <= numGlyphs; ++i) {
            float dx = gp[2 * i] - initX;
            float dy = gp[2 * i + 1] - initY;
            pos.x = initX + dx * this.xScale;
            pos.y = initY + dy * this.yScale;
            this.gv.setGlyphPosition(i, pos);
            if (stretchGlyphs && i != numGlyphs) {
               AffineTransform glyphTransform = this.gv.getGlyphTransform(i);
               if (glyphTransform != null) {
                  glyphTransform.preConcatenate(scaleAT);
                  this.gv.setGlyphTransform(i, glyphTransform);
               } else {
                  this.gv.setGlyphTransform(i, scaleAT);
               }
            }
         }

         this.advance = new Point2D.Float((float)(this.advance.getX() * (double)this.xScale), (float)(this.advance.getY() * (double)this.yScale));
         this.layoutApplied = false;
      }
   }

   protected void doPathLayout() {
      if (!this.pathApplied) {
         if (!this.spacingApplied) {
            this.adjustTextSpacing();
         }

         this.getGlyphAdvances();
         if (this.textPath == null) {
            this.pathApplied = true;
         } else {
            boolean horizontal = !this.isVertical();
            boolean glyphOrientationAuto = this.isGlyphOrientationAuto();
            int glyphOrientationAngle = false;
            if (!glyphOrientationAuto) {
               int glyphOrientationAngle = this.getGlyphOrientationAngle();
            }

            float pathLength = this.textPath.lengthOfPath();
            float startOffset = this.textPath.getStartOffset();
            int numGlyphs = this.gv.getNumGlyphs();

            for(int i = 0; i < numGlyphs; ++i) {
               this.gv.setGlyphVisible(i, true);
            }

            float glyphsLength;
            if (horizontal) {
               glyphsLength = (float)this.gv.getLogicalBounds().getWidth();
            } else {
               glyphsLength = (float)this.gv.getLogicalBounds().getHeight();
            }

            if (pathLength != 0.0F && glyphsLength != 0.0F) {
               Point2D firstGlyphPosition = this.gv.getGlyphPosition(0);
               float glyphOffset = 0.0F;
               float currentPosition;
               if (horizontal) {
                  glyphOffset = (float)firstGlyphPosition.getY();
                  currentPosition = (float)(firstGlyphPosition.getX() + (double)startOffset);
               } else {
                  glyphOffset = (float)firstGlyphPosition.getX();
                  currentPosition = (float)(firstGlyphPosition.getY() + (double)startOffset);
               }

               char ch = this.aci.first();
               int start = this.aci.getBeginIndex();
               int currentChar = 0;
               int lastGlyphDrawn = -1;
               float lastGlyphAdvance = 0.0F;

               for(int i = 0; i < numGlyphs; ++i) {
                  Point2D currentGlyphPos = this.gv.getGlyphPosition(i);
                  float glyphAdvance = 0.0F;
                  float nextGlyphOffset = 0.0F;
                  Point2D nextGlyphPosition = this.gv.getGlyphPosition(i + 1);
                  if (horizontal) {
                     glyphAdvance = (float)(nextGlyphPosition.getX() - currentGlyphPos.getX());
                     nextGlyphOffset = (float)(nextGlyphPosition.getY() - currentGlyphPos.getY());
                  } else {
                     glyphAdvance = (float)(nextGlyphPosition.getY() - currentGlyphPos.getY());
                     nextGlyphOffset = (float)(nextGlyphPosition.getX() - currentGlyphPos.getX());
                  }

                  Rectangle2D glyphBounds = this.gv.getGlyphOutline(i).getBounds2D();
                  float glyphWidth = (float)glyphBounds.getWidth();
                  float glyphHeight = (float)glyphBounds.getHeight();
                  float glyphMidX = 0.0F;
                  if (glyphWidth > 0.0F) {
                     glyphMidX = (float)(glyphBounds.getX() + (double)(glyphWidth / 2.0F));
                     glyphMidX -= (float)currentGlyphPos.getX();
                  }

                  float glyphMidY = 0.0F;
                  if (glyphHeight > 0.0F) {
                     glyphMidY = (float)(glyphBounds.getY() + (double)(glyphHeight / 2.0F));
                     glyphMidY -= (float)currentGlyphPos.getY();
                  }

                  float charMidPos;
                  if (horizontal) {
                     charMidPos = currentPosition + glyphMidX;
                  } else {
                     charMidPos = currentPosition + glyphMidY;
                  }

                  Point2D charMidPoint = this.textPath.pointAtLength(charMidPos);
                  if (charMidPoint != null) {
                     float angle = this.textPath.angleAtLength(charMidPos);
                     AffineTransform glyphPathTransform = new AffineTransform();
                     if (horizontal) {
                        glyphPathTransform.rotate((double)angle);
                     } else {
                        glyphPathTransform.rotate((double)angle - 1.5707963267948966);
                     }

                     if (horizontal) {
                        glyphPathTransform.translate(0.0, (double)glyphOffset);
                     } else {
                        glyphPathTransform.translate((double)glyphOffset, 0.0);
                     }

                     if (horizontal) {
                        glyphPathTransform.translate((double)(-glyphMidX), 0.0);
                     } else {
                        glyphPathTransform.translate(0.0, (double)(-glyphMidY));
                     }

                     AffineTransform glyphTransform = this.gv.getGlyphTransform(i);
                     if (glyphTransform != null) {
                        glyphPathTransform.concatenate(glyphTransform);
                     }

                     this.gv.setGlyphTransform(i, glyphPathTransform);
                     this.gv.setGlyphPosition(i, charMidPoint);
                     lastGlyphDrawn = i;
                     lastGlyphAdvance = glyphAdvance;
                  } else {
                     this.gv.setGlyphVisible(i, false);
                  }

                  currentPosition += glyphAdvance;
                  glyphOffset += nextGlyphOffset;
                  currentChar += this.gv.getCharacterCount(i, i);
                  if (currentChar >= this.charMap.length) {
                     currentChar = this.charMap.length - 1;
                  }

                  this.aci.setIndex(currentChar + start);
               }

               if (lastGlyphDrawn > -1) {
                  Point2D lastGlyphPos = this.gv.getGlyphPosition(lastGlyphDrawn);
                  if (horizontal) {
                     this.textPathAdvance = new Point2D.Double(lastGlyphPos.getX() + (double)lastGlyphAdvance, lastGlyphPos.getY());
                  } else {
                     this.textPathAdvance = new Point2D.Double(lastGlyphPos.getX(), lastGlyphPos.getY() + (double)lastGlyphAdvance);
                  }
               } else {
                  this.textPathAdvance = new Point2D.Double(0.0, 0.0);
               }

               this.layoutApplied = false;
               this.spacingApplied = false;
               this.pathApplied = true;
            } else {
               this.pathApplied = true;
               this.textPathAdvance = this.advance;
            }
         }
      }
   }

   protected boolean isLatinChar(char c) {
      if (c < 255 && Character.isLetterOrDigit(c)) {
         return true;
      } else {
         Character.UnicodeBlock block = UnicodeBlock.of(c);
         return block == UnicodeBlock.BASIC_LATIN || block == UnicodeBlock.LATIN_1_SUPPLEMENT || block == UnicodeBlock.LATIN_EXTENDED_ADDITIONAL || block == UnicodeBlock.LATIN_EXTENDED_A || block == UnicodeBlock.LATIN_EXTENDED_B || block == UnicodeBlock.ARABIC || block == UnicodeBlock.ARABIC_PRESENTATION_FORMS_A || block == UnicodeBlock.ARABIC_PRESENTATION_FORMS_B;
      }
   }

   protected boolean isGlyphOrientationAuto() {
      if (!this.isVertical()) {
         return false;
      } else {
         this.aci.first();
         Integer vOrient = (Integer)this.aci.getAttribute(VERTICAL_ORIENTATION);
         if (vOrient != null) {
            return vOrient == ORIENTATION_AUTO;
         } else {
            return true;
         }
      }
   }

   protected int getGlyphOrientationAngle() {
      int glyphOrientationAngle = 0;
      this.aci.first();
      Float angle;
      if (this.isVertical()) {
         angle = (Float)this.aci.getAttribute(VERTICAL_ORIENTATION_ANGLE);
      } else {
         angle = (Float)this.aci.getAttribute(HORIZONTAL_ORIENTATION_ANGLE);
      }

      if (angle != null) {
         glyphOrientationAngle = (int)angle;
      }

      if (glyphOrientationAngle != 0 || glyphOrientationAngle != 90 || glyphOrientationAngle != 180 || glyphOrientationAngle != 270) {
         while(glyphOrientationAngle < 0) {
            glyphOrientationAngle += 360;
         }

         while(glyphOrientationAngle >= 360) {
            glyphOrientationAngle -= 360;
         }

         if (glyphOrientationAngle > 45 && glyphOrientationAngle <= 315) {
            if (glyphOrientationAngle > 45 && glyphOrientationAngle <= 135) {
               glyphOrientationAngle = 90;
            } else if (glyphOrientationAngle > 135 && glyphOrientationAngle <= 225) {
               glyphOrientationAngle = 180;
            } else {
               glyphOrientationAngle = 270;
            }
         } else {
            glyphOrientationAngle = 0;
         }
      }

      return glyphOrientationAngle;
   }

   public boolean hasCharacterIndex(int index) {
      int[] var2 = this.charMap;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         int aCharMap = var2[var4];
         if (index == aCharMap) {
            return true;
         }
      }

      return false;
   }

   public boolean isAltGlyph() {
      return this.isAltGlyph;
   }

   public boolean isReversed() {
      return this.gv.isReversed();
   }

   public void maybeReverse(boolean mirror) {
      this.gv.maybeReverse(mirror);
   }

   static {
      FLOW_LINE_BREAK = GVTAttributedCharacterIterator.TextAttribute.FLOW_LINE_BREAK;
      FLOW_PARAGRAPH = GVTAttributedCharacterIterator.TextAttribute.FLOW_PARAGRAPH;
      FLOW_EMPTY_PARAGRAPH = GVTAttributedCharacterIterator.TextAttribute.FLOW_EMPTY_PARAGRAPH;
      LINE_HEIGHT = GVTAttributedCharacterIterator.TextAttribute.LINE_HEIGHT;
      VERTICAL_ORIENTATION = GVTAttributedCharacterIterator.TextAttribute.VERTICAL_ORIENTATION;
      VERTICAL_ORIENTATION_ANGLE = GVTAttributedCharacterIterator.TextAttribute.VERTICAL_ORIENTATION_ANGLE;
      HORIZONTAL_ORIENTATION_ANGLE = GVTAttributedCharacterIterator.TextAttribute.HORIZONTAL_ORIENTATION_ANGLE;
      X = GVTAttributedCharacterIterator.TextAttribute.X;
      Y = GVTAttributedCharacterIterator.TextAttribute.Y;
      DX = GVTAttributedCharacterIterator.TextAttribute.DX;
      DY = GVTAttributedCharacterIterator.TextAttribute.DY;
      ROTATION = GVTAttributedCharacterIterator.TextAttribute.ROTATION;
      BASELINE_SHIFT = GVTAttributedCharacterIterator.TextAttribute.BASELINE_SHIFT;
      WRITING_MODE = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE;
      WRITING_MODE_TTB = GVTAttributedCharacterIterator.TextAttribute.WRITING_MODE_TTB;
      ORIENTATION_AUTO = GVTAttributedCharacterIterator.TextAttribute.ORIENTATION_AUTO;
      GVT_FONT = GVTAttributedCharacterIterator.TextAttribute.GVT_FONT;
      runAtts = new HashSet();
      runAtts.add(X);
      runAtts.add(Y);
      runAtts.add(DX);
      runAtts.add(DY);
      runAtts.add(ROTATION);
      runAtts.add(BASELINE_SHIFT);
      szAtts = new HashSet();
      szAtts.add(TextAttribute.SIZE);
      szAtts.add(GVT_FONT);
      szAtts.add(LINE_HEIGHT);
   }
}
