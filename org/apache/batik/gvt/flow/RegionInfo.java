package org.apache.batik.gvt.flow;

import java.awt.Shape;

public class RegionInfo {
   private Shape shape;
   private float verticalAlignment;

   public RegionInfo(Shape s, float verticalAlignment) {
      this.shape = s;
      this.verticalAlignment = verticalAlignment;
   }

   public Shape getShape() {
      return this.shape;
   }

   public void setShape(Shape s) {
      this.shape = s;
   }

   public float getVerticalAlignment() {
      return this.verticalAlignment;
   }

   public void setVerticalAlignment(float verticalAlignment) {
      this.verticalAlignment = verticalAlignment;
   }
}
