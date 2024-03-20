package org.apache.batik.svggen;

import java.awt.geom.GeneralPath;

class ClipKey {
   int hashCodeValue = 0;

   public ClipKey(GeneralPath proxiedPath, SVGGeneratorContext gc) {
      String pathData = SVGPath.toSVGPathData(proxiedPath, gc);
      this.hashCodeValue = pathData.hashCode();
   }

   public int hashCode() {
      return this.hashCodeValue;
   }

   public boolean equals(Object clipKey) {
      return clipKey instanceof ClipKey && this.hashCodeValue == ((ClipKey)clipKey).hashCodeValue;
   }
}
