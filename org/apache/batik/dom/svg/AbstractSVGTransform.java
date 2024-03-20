package org.apache.batik.dom.svg;

import java.awt.geom.AffineTransform;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGTransform;

public abstract class AbstractSVGTransform implements SVGTransform {
   protected short type = 0;
   protected AffineTransform affineTransform;
   protected float angle;
   protected float x;
   protected float y;

   protected abstract SVGMatrix createMatrix();

   public void setType(short type) {
      this.type = type;
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public void assign(AbstractSVGTransform t) {
      this.type = t.type;
      this.affineTransform = t.affineTransform;
      this.angle = t.angle;
      this.x = t.x;
      this.y = t.y;
   }

   public short getType() {
      return this.type;
   }

   public SVGMatrix getMatrix() {
      return this.createMatrix();
   }

   public float getAngle() {
      return this.angle;
   }

   public void setMatrix(SVGMatrix matrix) {
      this.type = 1;
      this.affineTransform = new AffineTransform(matrix.getA(), matrix.getB(), matrix.getC(), matrix.getD(), matrix.getE(), matrix.getF());
   }

   public void setTranslate(float tx, float ty) {
      this.type = 2;
      this.affineTransform = AffineTransform.getTranslateInstance((double)tx, (double)ty);
   }

   public void setScale(float sx, float sy) {
      this.type = 3;
      this.affineTransform = AffineTransform.getScaleInstance((double)sx, (double)sy);
   }

   public void setRotate(float angle, float cx, float cy) {
      this.type = 4;
      this.affineTransform = AffineTransform.getRotateInstance(Math.toRadians((double)angle), (double)cx, (double)cy);
      this.angle = angle;
      this.x = cx;
      this.y = cy;
   }

   public void setSkewX(float angle) {
      this.type = 5;
      this.affineTransform = AffineTransform.getShearInstance(Math.tan(Math.toRadians((double)angle)), 0.0);
      this.angle = angle;
   }

   public void setSkewY(float angle) {
      this.type = 6;
      this.affineTransform = AffineTransform.getShearInstance(0.0, Math.tan(Math.toRadians((double)angle)));
      this.angle = angle;
   }
}
