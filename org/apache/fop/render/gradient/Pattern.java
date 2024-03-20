package org.apache.fop.render.gradient;

import java.util.List;

public class Pattern {
   private final int patternType;
   private final Shading shading;
   private final List matrix;

   public Pattern(int patternType, Shading shading, List matrix) {
      this.patternType = patternType;
      this.shading = shading;
      this.matrix = matrix;
   }

   public int getPatternType() {
      return this.patternType;
   }

   public Shading getShading() {
      return this.shading;
   }

   public List getMatrix() {
      return this.matrix;
   }
}
