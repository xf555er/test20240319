package org.apache.fop.util;

import java.awt.Color;
import java.awt.color.ColorSpace;
import org.apache.xmlgraphics.java2d.color.ColorWithAlternatives;

public class ColorWithFallback extends ColorWithAlternatives {
   private static final long serialVersionUID = 7913922854959637136L;
   private final Color fallback;

   public ColorWithFallback(ColorSpace cspace, float[] components, float alpha, Color[] alternativeColors, Color fallback) {
      super(cspace, components, alpha, alternativeColors);
      this.fallback = fallback;
   }

   public ColorWithFallback(Color color, Color fallback) {
      this(color.getColorSpace(), color.getColorComponents((float[])null), getAlphaFloat(color), getAlternativeColors(color), fallback);
   }

   private static float getAlphaFloat(Color color) {
      float[] comps = color.getComponents((float[])null);
      return comps[comps.length - 1];
   }

   private static Color[] getAlternativeColors(Color color) {
      if (color instanceof ColorWithAlternatives) {
         ColorWithAlternatives cwa = (ColorWithAlternatives)color;
         if (cwa.hasAlternativeColors()) {
            return cwa.getAlternativeColors();
         }
      }

      return null;
   }

   public Color getFallbackColor() {
      return this.fallback;
   }

   public int hashCode() {
      return super.hashCode() ^ this.fallback.hashCode();
   }

   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!super.equals(obj)) {
         return false;
      } else if (obj instanceof ColorWithFallback) {
         ColorWithFallback other = (ColorWithFallback)obj;
         return other.fallback.equals(this.fallback);
      } else {
         return false;
      }
   }
}
