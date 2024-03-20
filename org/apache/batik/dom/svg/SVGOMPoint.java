package org.apache.batik.dom.svg;

import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGPoint;

public class SVGOMPoint implements SVGPoint {
   protected float x;
   protected float y;

   public SVGOMPoint() {
   }

   public SVGOMPoint(float x, float y) {
      this.x = x;
      this.y = y;
   }

   public float getX() {
      return this.x;
   }

   public void setX(float x) throws DOMException {
      this.x = x;
   }

   public float getY() {
      return this.y;
   }

   public void setY(float y) throws DOMException {
      this.y = y;
   }

   public SVGPoint matrixTransform(SVGMatrix matrix) {
      return matrixTransform(this, matrix);
   }

   public static SVGPoint matrixTransform(SVGPoint point, SVGMatrix matrix) {
      float newX = matrix.getA() * point.getX() + matrix.getC() * point.getY() + matrix.getE();
      float newY = matrix.getB() * point.getX() + matrix.getD() * point.getY() + matrix.getF();
      return new SVGOMPoint(newX, newY);
   }
}
