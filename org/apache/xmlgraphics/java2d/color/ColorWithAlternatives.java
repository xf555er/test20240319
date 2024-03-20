package org.apache.xmlgraphics.java2d.color;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.util.Arrays;

public class ColorWithAlternatives extends Color {
   private static final long serialVersionUID = -6125884937776779150L;
   private Color[] alternativeColors;

   public ColorWithAlternatives(float r, float g, float b, float a, Color[] alternativeColors) {
      super(r, g, b, a);
      this.initAlternativeColors(alternativeColors);
   }

   public ColorWithAlternatives(float r, float g, float b, Color[] alternativeColors) {
      super(r, g, b);
      this.initAlternativeColors(alternativeColors);
   }

   public ColorWithAlternatives(int rgba, boolean hasalpha, Color[] alternativeColors) {
      super(rgba, hasalpha);
      this.initAlternativeColors(alternativeColors);
   }

   public ColorWithAlternatives(int r, int g, int b, int a, Color[] alternativeColors) {
      super(r, g, b, a);
      this.initAlternativeColors(alternativeColors);
   }

   public ColorWithAlternatives(int r, int g, int b, Color[] alternativeColors) {
      super(r, g, b);
      this.initAlternativeColors(alternativeColors);
   }

   public ColorWithAlternatives(int rgb, Color[] alternativeColors) {
      super(rgb);
      this.initAlternativeColors(alternativeColors);
   }

   public ColorWithAlternatives(ColorSpace cspace, float[] components, float alpha, Color[] alternativeColors) {
      super(cspace, components, alpha);
      this.initAlternativeColors(alternativeColors);
   }

   private void initAlternativeColors(Color[] colors) {
      if (colors != null) {
         this.alternativeColors = new Color[colors.length];
         System.arraycopy(colors, 0, this.alternativeColors, 0, colors.length);
      }

   }

   public Color[] getAlternativeColors() {
      if (this.alternativeColors != null) {
         Color[] cols = new Color[this.alternativeColors.length];
         System.arraycopy(this.alternativeColors, 0, cols, 0, this.alternativeColors.length);
         return cols;
      } else {
         return new Color[0];
      }
   }

   public boolean hasAlternativeColors() {
      return this.alternativeColors != null && this.alternativeColors.length > 0;
   }

   public boolean hasSameAlternativeColors(ColorWithAlternatives col) {
      if (!this.hasAlternativeColors()) {
         return !col.hasAlternativeColors();
      } else if (!col.hasAlternativeColors()) {
         return false;
      } else {
         Color[] alt1 = this.getAlternativeColors();
         Color[] alt2 = col.getAlternativeColors();
         if (alt1.length != alt2.length) {
            return false;
         } else {
            int i = 0;

            for(int c = alt1.length; i < c; ++i) {
               Color c1 = alt1[i];
               Color c2 = alt2[i];
               if (!ColorUtil.isSameColor(c1, c2)) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public Color getFirstAlternativeOfType(int colorSpaceType) {
      if (this.hasAlternativeColors()) {
         Color[] var2 = this.alternativeColors;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Color alternativeColor = var2[var4];
            if (alternativeColor.getColorSpace().getType() == colorSpaceType) {
               return alternativeColor;
            }
         }
      }

      return null;
   }

   public int hashCode() {
      int hash = super.hashCode();
      if (this.alternativeColors != null) {
         hash = 37 * hash + Arrays.hashCode(this.alternativeColors);
      }

      return hash;
   }
}
