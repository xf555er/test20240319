package org.apache.batik.dom.svg;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGMatrix;

public abstract class AbstractSVGMatrix implements SVGMatrix {
   protected static final AffineTransform FLIP_X_TRANSFORM = new AffineTransform(-1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F);
   protected static final AffineTransform FLIP_Y_TRANSFORM = new AffineTransform(1.0F, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F);

   protected abstract AffineTransform getAffineTransform();

   public float getA() {
      return (float)this.getAffineTransform().getScaleX();
   }

   public void setA(float a) throws DOMException {
      AffineTransform at = this.getAffineTransform();
      at.setTransform((double)a, at.getShearY(), at.getShearX(), at.getScaleY(), at.getTranslateX(), at.getTranslateY());
   }

   public float getB() {
      return (float)this.getAffineTransform().getShearY();
   }

   public void setB(float b) throws DOMException {
      AffineTransform at = this.getAffineTransform();
      at.setTransform(at.getScaleX(), (double)b, at.getShearX(), at.getScaleY(), at.getTranslateX(), at.getTranslateY());
   }

   public float getC() {
      return (float)this.getAffineTransform().getShearX();
   }

   public void setC(float c) throws DOMException {
      AffineTransform at = this.getAffineTransform();
      at.setTransform(at.getScaleX(), at.getShearY(), (double)c, at.getScaleY(), at.getTranslateX(), at.getTranslateY());
   }

   public float getD() {
      return (float)this.getAffineTransform().getScaleY();
   }

   public void setD(float d) throws DOMException {
      AffineTransform at = this.getAffineTransform();
      at.setTransform(at.getScaleX(), at.getShearY(), at.getShearX(), (double)d, at.getTranslateX(), at.getTranslateY());
   }

   public float getE() {
      return (float)this.getAffineTransform().getTranslateX();
   }

   public void setE(float e) throws DOMException {
      AffineTransform at = this.getAffineTransform();
      at.setTransform(at.getScaleX(), at.getShearY(), at.getShearX(), at.getScaleY(), (double)e, at.getTranslateY());
   }

   public float getF() {
      return (float)this.getAffineTransform().getTranslateY();
   }

   public void setF(float f) throws DOMException {
      AffineTransform at = this.getAffineTransform();
      at.setTransform(at.getScaleX(), at.getShearY(), at.getShearX(), at.getScaleY(), at.getTranslateX(), (double)f);
   }

   public SVGMatrix multiply(SVGMatrix secondMatrix) {
      AffineTransform at = new AffineTransform(secondMatrix.getA(), secondMatrix.getB(), secondMatrix.getC(), secondMatrix.getD(), secondMatrix.getE(), secondMatrix.getF());
      AffineTransform tr = (AffineTransform)this.getAffineTransform().clone();
      tr.concatenate(at);
      return new SVGOMMatrix(tr);
   }

   public SVGMatrix inverse() throws SVGException {
      try {
         return new SVGOMMatrix(this.getAffineTransform().createInverse());
      } catch (NoninvertibleTransformException var2) {
         throw new SVGOMException((short)2, var2.getMessage());
      }
   }

   public SVGMatrix translate(float x, float y) {
      AffineTransform tr = (AffineTransform)this.getAffineTransform().clone();
      tr.translate((double)x, (double)y);
      return new SVGOMMatrix(tr);
   }

   public SVGMatrix scale(float scaleFactor) {
      AffineTransform tr = (AffineTransform)this.getAffineTransform().clone();
      tr.scale((double)scaleFactor, (double)scaleFactor);
      return new SVGOMMatrix(tr);
   }

   public SVGMatrix scaleNonUniform(float scaleFactorX, float scaleFactorY) {
      AffineTransform tr = (AffineTransform)this.getAffineTransform().clone();
      tr.scale((double)scaleFactorX, (double)scaleFactorY);
      return new SVGOMMatrix(tr);
   }

   public SVGMatrix rotate(float angle) {
      AffineTransform tr = (AffineTransform)this.getAffineTransform().clone();
      tr.rotate(Math.toRadians((double)angle));
      return new SVGOMMatrix(tr);
   }

   public SVGMatrix rotateFromVector(float x, float y) throws SVGException {
      if (x != 0.0F && y != 0.0F) {
         AffineTransform tr = (AffineTransform)this.getAffineTransform().clone();
         tr.rotate(Math.atan2((double)y, (double)x));
         return new SVGOMMatrix(tr);
      } else {
         throw new SVGOMException((short)1, "");
      }
   }

   public SVGMatrix flipX() {
      AffineTransform tr = (AffineTransform)this.getAffineTransform().clone();
      tr.concatenate(FLIP_X_TRANSFORM);
      return new SVGOMMatrix(tr);
   }

   public SVGMatrix flipY() {
      AffineTransform tr = (AffineTransform)this.getAffineTransform().clone();
      tr.concatenate(FLIP_Y_TRANSFORM);
      return new SVGOMMatrix(tr);
   }

   public SVGMatrix skewX(float angleDeg) {
      AffineTransform tr = (AffineTransform)this.getAffineTransform().clone();
      tr.concatenate(AffineTransform.getShearInstance(Math.tan(Math.toRadians((double)angleDeg)), 0.0));
      return new SVGOMMatrix(tr);
   }

   public SVGMatrix skewY(float angleDeg) {
      AffineTransform tr = (AffineTransform)this.getAffineTransform().clone();
      tr.concatenate(AffineTransform.getShearInstance(0.0, Math.tan(Math.toRadians((double)angleDeg))));
      return new SVGOMMatrix(tr);
   }
}
