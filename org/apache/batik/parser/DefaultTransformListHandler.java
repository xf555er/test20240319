package org.apache.batik.parser;

public class DefaultTransformListHandler implements TransformListHandler {
   public static final TransformListHandler INSTANCE = new DefaultTransformListHandler();

   protected DefaultTransformListHandler() {
   }

   public void startTransformList() throws ParseException {
   }

   public void matrix(float a, float b, float c, float d, float e, float f) throws ParseException {
   }

   public void rotate(float theta) throws ParseException {
   }

   public void rotate(float theta, float cx, float cy) throws ParseException {
   }

   public void translate(float tx) throws ParseException {
   }

   public void translate(float tx, float ty) throws ParseException {
   }

   public void scale(float sx) throws ParseException {
   }

   public void scale(float sx, float sy) throws ParseException {
   }

   public void skewX(float skx) throws ParseException {
   }

   public void skewY(float sky) throws ParseException {
   }

   public void endTransformList() throws ParseException {
   }
}
