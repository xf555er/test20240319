package org.apache.batik.gvt.text;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import org.apache.batik.ext.awt.geom.PathLength;

public class TextPath {
   private PathLength pathLength;
   private float startOffset;

   public TextPath(GeneralPath path) {
      this.pathLength = new PathLength(path);
      this.startOffset = 0.0F;
   }

   public void setStartOffset(float startOffset) {
      this.startOffset = startOffset;
   }

   public float getStartOffset() {
      return this.startOffset;
   }

   public float lengthOfPath() {
      return this.pathLength.lengthOfPath();
   }

   public float angleAtLength(float length) {
      return this.pathLength.angleAtLength(length);
   }

   public Point2D pointAtLength(float length) {
      return this.pathLength.pointAtLength(length);
   }
}
