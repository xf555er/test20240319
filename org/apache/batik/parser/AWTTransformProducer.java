package org.apache.batik.parser;

import java.awt.geom.AffineTransform;
import java.io.Reader;

public class AWTTransformProducer implements TransformListHandler {
   protected AffineTransform affineTransform;

   public static AffineTransform createAffineTransform(Reader r) throws ParseException {
      TransformListParser p = new TransformListParser();
      AWTTransformProducer th = new AWTTransformProducer();
      p.setTransformListHandler(th);
      p.parse(r);
      return th.getAffineTransform();
   }

   public static AffineTransform createAffineTransform(String s) throws ParseException {
      TransformListParser p = new TransformListParser();
      AWTTransformProducer th = new AWTTransformProducer();
      p.setTransformListHandler(th);
      p.parse(s);
      return th.getAffineTransform();
   }

   public AffineTransform getAffineTransform() {
      return this.affineTransform;
   }

   public void startTransformList() throws ParseException {
      this.affineTransform = new AffineTransform();
   }

   public void matrix(float a, float b, float c, float d, float e, float f) throws ParseException {
      this.affineTransform.concatenate(new AffineTransform(a, b, c, d, e, f));
   }

   public void rotate(float theta) throws ParseException {
      this.affineTransform.concatenate(AffineTransform.getRotateInstance(Math.toRadians((double)theta)));
   }

   public void rotate(float theta, float cx, float cy) throws ParseException {
      AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians((double)theta), (double)cx, (double)cy);
      this.affineTransform.concatenate(at);
   }

   public void translate(float tx) throws ParseException {
      AffineTransform at = AffineTransform.getTranslateInstance((double)tx, 0.0);
      this.affineTransform.concatenate(at);
   }

   public void translate(float tx, float ty) throws ParseException {
      AffineTransform at = AffineTransform.getTranslateInstance((double)tx, (double)ty);
      this.affineTransform.concatenate(at);
   }

   public void scale(float sx) throws ParseException {
      this.affineTransform.concatenate(AffineTransform.getScaleInstance((double)sx, (double)sx));
   }

   public void scale(float sx, float sy) throws ParseException {
      this.affineTransform.concatenate(AffineTransform.getScaleInstance((double)sx, (double)sy));
   }

   public void skewX(float skx) throws ParseException {
      this.affineTransform.concatenate(AffineTransform.getShearInstance(Math.tan(Math.toRadians((double)skx)), 0.0));
   }

   public void skewY(float sky) throws ParseException {
      this.affineTransform.concatenate(AffineTransform.getShearInstance(0.0, Math.tan(Math.toRadians((double)sky))));
   }

   public void endTransformList() throws ParseException {
   }
}
