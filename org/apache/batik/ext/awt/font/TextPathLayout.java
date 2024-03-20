package org.apache.batik.ext.awt.font;

import java.awt.Shape;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import org.apache.batik.ext.awt.geom.PathLength;

public class TextPathLayout {
   public static final int ALIGN_START = 0;
   public static final int ALIGN_MIDDLE = 1;
   public static final int ALIGN_END = 2;
   public static final int ADJUST_SPACING = 0;
   public static final int ADJUST_GLYPHS = 1;

   public static Shape layoutGlyphVector(GlyphVector glyphs, Shape path, int align, float startOffset, float textLength, int lengthAdjustMode) {
      GeneralPath newPath = new GeneralPath();
      PathLength pl = new PathLength(path);
      float pathLength = pl.lengthOfPath();
      if (glyphs == null) {
         return newPath;
      } else {
         float glyphsLength = (float)glyphs.getVisualBounds().getWidth();
         if (path != null && glyphs.getNumGlyphs() != 0 && pl.lengthOfPath() != 0.0F && glyphsLength != 0.0F) {
            float lengthRatio = textLength / glyphsLength;
            float currentPosition = startOffset;
            if (align == 2) {
               currentPosition = startOffset + (pathLength - textLength);
            } else if (align == 1) {
               currentPosition = startOffset + (pathLength - textLength) / 2.0F;
            }

            for(int i = 0; i < glyphs.getNumGlyphs(); ++i) {
               GlyphMetrics gm = glyphs.getGlyphMetrics(i);
               float charAdvance = gm.getAdvance();
               Shape glyph = glyphs.getGlyphOutline(i);
               if (lengthAdjustMode == 1) {
                  AffineTransform scale = AffineTransform.getScaleInstance((double)lengthRatio, 1.0);
                  glyph = scale.createTransformedShape(glyph);
                  charAdvance *= lengthRatio;
               }

               float glyphWidth = (float)glyph.getBounds2D().getWidth();
               float charMidPos = currentPosition + glyphWidth / 2.0F;
               Point2D charMidPoint = pl.pointAtLength(charMidPos);
               if (charMidPoint != null) {
                  float angle = pl.angleAtLength(charMidPos);
                  AffineTransform glyphTrans = new AffineTransform();
                  glyphTrans.translate(charMidPoint.getX(), charMidPoint.getY());
                  glyphTrans.rotate((double)angle);
                  glyphTrans.translate((double)(charAdvance / -2.0F), 0.0);
                  glyph = glyphTrans.createTransformedShape(glyph);
                  newPath.append(glyph, false);
               }

               if (lengthAdjustMode == 0) {
                  currentPosition += charAdvance * lengthRatio;
               } else {
                  currentPosition += charAdvance;
               }
            }

            return newPath;
         } else {
            return newPath;
         }
      }
   }

   public static Shape layoutGlyphVector(GlyphVector glyphs, Shape path, int align) {
      return layoutGlyphVector(glyphs, path, align, 0.0F, (float)glyphs.getVisualBounds().getWidth(), 0);
   }

   public static Shape layoutGlyphVector(GlyphVector glyphs, Shape path) {
      return layoutGlyphVector(glyphs, path, 0);
   }
}
