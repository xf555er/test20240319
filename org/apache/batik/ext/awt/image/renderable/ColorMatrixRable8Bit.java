package org.apache.batik.ext.awt.image.renderable;

import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.util.Map;
import org.apache.batik.ext.awt.image.rendered.ColorMatrixRed;

public final class ColorMatrixRable8Bit extends AbstractColorInterpolationRable implements ColorMatrixRable {
   private static float[][] MATRIX_LUMINANCE_TO_ALPHA = new float[][]{{0.0F, 0.0F, 0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F, 0.0F, 0.0F}, {0.2125F, 0.7154F, 0.0721F, 0.0F, 0.0F}};
   private int type;
   private float[][] matrix;

   public void setSource(Filter src) {
      this.init(src, (Map)null);
   }

   public Filter getSource() {
      return (Filter)this.getSources().get(0);
   }

   public int getType() {
      return this.type;
   }

   public float[][] getMatrix() {
      return this.matrix;
   }

   private ColorMatrixRable8Bit() {
   }

   public static ColorMatrixRable buildMatrix(float[][] matrix) {
      if (matrix == null) {
         throw new IllegalArgumentException();
      } else if (matrix.length != 4) {
         throw new IllegalArgumentException();
      } else {
         float[][] newMatrix = new float[4][];

         for(int i = 0; i < 4; ++i) {
            float[] m = matrix[i];
            if (m == null) {
               throw new IllegalArgumentException();
            }

            if (m.length != 5) {
               throw new IllegalArgumentException();
            }

            newMatrix[i] = new float[5];

            for(int j = 0; j < 5; ++j) {
               newMatrix[i][j] = m[j];
            }
         }

         ColorMatrixRable8Bit filter = new ColorMatrixRable8Bit();
         filter.type = 0;
         filter.matrix = newMatrix;
         return filter;
      }
   }

   public static ColorMatrixRable buildSaturate(float s) {
      ColorMatrixRable8Bit filter = new ColorMatrixRable8Bit();
      filter.type = 1;
      filter.matrix = new float[][]{{0.213F + 0.787F * s, 0.715F - 0.715F * s, 0.072F - 0.072F * s, 0.0F, 0.0F}, {0.213F - 0.213F * s, 0.715F + 0.285F * s, 0.072F - 0.072F * s, 0.0F, 0.0F}, {0.213F - 0.213F * s, 0.715F - 0.715F * s, 0.072F + 0.928F * s, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F, 1.0F, 0.0F}};
      return filter;
   }

   public static ColorMatrixRable buildHueRotate(float a) {
      ColorMatrixRable8Bit filter = new ColorMatrixRable8Bit();
      filter.type = 2;
      float cos = (float)Math.cos((double)a);
      float sin = (float)Math.sin((double)a);
      float a00 = 0.213F + cos * 0.787F - sin * 0.213F;
      float a10 = 0.213F - cos * 0.212F + sin * 0.143F;
      float a20 = 0.213F - cos * 0.213F - sin * 0.787F;
      float a01 = 0.715F - cos * 0.715F - sin * 0.715F;
      float a11 = 0.715F + cos * 0.285F + sin * 0.14F;
      float a21 = 0.715F - cos * 0.715F + sin * 0.715F;
      float a02 = 0.072F - cos * 0.072F + sin * 0.928F;
      float a12 = 0.072F - cos * 0.072F - sin * 0.283F;
      float a22 = 0.072F + cos * 0.928F + sin * 0.072F;
      filter.matrix = new float[][]{{a00, a01, a02, 0.0F, 0.0F}, {a10, a11, a12, 0.0F, 0.0F}, {a20, a21, a22, 0.0F, 0.0F}, {0.0F, 0.0F, 0.0F, 1.0F, 0.0F}};
      return filter;
   }

   public static ColorMatrixRable buildLuminanceToAlpha() {
      ColorMatrixRable8Bit filter = new ColorMatrixRable8Bit();
      filter.type = 3;
      filter.matrix = MATRIX_LUMINANCE_TO_ALPHA;
      return filter;
   }

   public RenderedImage createRendering(RenderContext rc) {
      RenderedImage srcRI = this.getSource().createRendering(rc);
      return srcRI == null ? null : new ColorMatrixRed(this.convertSourceCS(srcRI), this.matrix);
   }
}
