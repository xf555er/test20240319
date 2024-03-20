package org.apache.batik.extension.svg;

import java.awt.geom.Rectangle2D;

public class RegionInfo extends Rectangle2D.Float {
   private float verticalAlignment = 0.0F;

   public RegionInfo(float x, float y, float w, float h, float verticalAlignment) {
      super(x, y, w, h);
      this.verticalAlignment = verticalAlignment;
   }

   public float getVerticalAlignment() {
      return this.verticalAlignment;
   }

   public void setVerticalAlignment(float verticalAlignment) {
      this.verticalAlignment = verticalAlignment;
   }
}
