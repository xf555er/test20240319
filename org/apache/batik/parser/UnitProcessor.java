package org.apache.batik.parser;

import org.w3c.dom.Element;

public abstract class UnitProcessor {
   public static final short HORIZONTAL_LENGTH = 2;
   public static final short VERTICAL_LENGTH = 1;
   public static final short OTHER_LENGTH = 0;
   static final double SQRT2 = Math.sqrt(2.0);

   protected UnitProcessor() {
   }

   public static float svgToObjectBoundingBox(String s, String attr, short d, Context ctx) throws ParseException {
      LengthParser lengthParser = new LengthParser();
      UnitResolver ur = new UnitResolver();
      lengthParser.setLengthHandler(ur);
      lengthParser.parse(s);
      return svgToObjectBoundingBox(ur.value, ur.unit, d, ctx);
   }

   public static float svgToObjectBoundingBox(float value, short type, short d, Context ctx) {
      switch (type) {
         case 1:
            return value;
         case 2:
            return value / 100.0F;
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
            return svgToUserSpace(value, type, d, ctx);
         default:
            throw new IllegalArgumentException("Length has unknown type");
      }
   }

   public static float svgToUserSpace(String s, String attr, short d, Context ctx) throws ParseException {
      LengthParser lengthParser = new LengthParser();
      UnitResolver ur = new UnitResolver();
      lengthParser.setLengthHandler(ur);
      lengthParser.parse(s);
      return svgToUserSpace(ur.value, ur.unit, d, ctx);
   }

   public static float svgToUserSpace(float v, short type, short d, Context ctx) {
      switch (type) {
         case 1:
         case 5:
            return v;
         case 2:
            return percentagesToPixels(v, d, ctx);
         case 3:
            return emsToPixels(v, d, ctx);
         case 4:
            return exsToPixels(v, d, ctx);
         case 6:
            return v * 10.0F / ctx.getPixelUnitToMillimeter();
         case 7:
            return v / ctx.getPixelUnitToMillimeter();
         case 8:
            return v * 25.4F / ctx.getPixelUnitToMillimeter();
         case 9:
            return v * 25.4F / (72.0F * ctx.getPixelUnitToMillimeter());
         case 10:
            return v * 25.4F / (6.0F * ctx.getPixelUnitToMillimeter());
         default:
            throw new IllegalArgumentException("Length has unknown type");
      }
   }

   public static float userSpaceToSVG(float v, short type, short d, Context ctx) {
      switch (type) {
         case 1:
         case 5:
            return v;
         case 2:
            return pixelsToPercentages(v, d, ctx);
         case 3:
            return pixelsToEms(v, d, ctx);
         case 4:
            return pixelsToExs(v, d, ctx);
         case 6:
            return v * ctx.getPixelUnitToMillimeter() / 10.0F;
         case 7:
            return v * ctx.getPixelUnitToMillimeter();
         case 8:
            return v * ctx.getPixelUnitToMillimeter() / 25.4F;
         case 9:
            return v * 72.0F * ctx.getPixelUnitToMillimeter() / 25.4F;
         case 10:
            return v * 6.0F * ctx.getPixelUnitToMillimeter() / 25.4F;
         default:
            throw new IllegalArgumentException("Length has unknown type");
      }
   }

   protected static float percentagesToPixels(float v, short d, Context ctx) {
      float h;
      if (d == 2) {
         h = ctx.getViewportWidth();
         return h * v / 100.0F;
      } else if (d == 1) {
         h = ctx.getViewportHeight();
         return h * v / 100.0F;
      } else {
         double w = (double)ctx.getViewportWidth();
         double h = (double)ctx.getViewportHeight();
         double vpp = Math.sqrt(w * w + h * h) / SQRT2;
         return (float)(vpp * (double)v / 100.0);
      }
   }

   protected static float pixelsToPercentages(float v, short d, Context ctx) {
      float h;
      if (d == 2) {
         h = ctx.getViewportWidth();
         return v * 100.0F / h;
      } else if (d == 1) {
         h = ctx.getViewportHeight();
         return v * 100.0F / h;
      } else {
         double w = (double)ctx.getViewportWidth();
         double h = (double)ctx.getViewportHeight();
         double vpp = Math.sqrt(w * w + h * h) / SQRT2;
         return (float)((double)v * 100.0 / vpp);
      }
   }

   protected static float pixelsToEms(float v, short d, Context ctx) {
      return v / ctx.getFontSize();
   }

   protected static float emsToPixels(float v, short d, Context ctx) {
      return v * ctx.getFontSize();
   }

   protected static float pixelsToExs(float v, short d, Context ctx) {
      float xh = ctx.getXHeight();
      return v / xh / ctx.getFontSize();
   }

   protected static float exsToPixels(float v, short d, Context ctx) {
      float xh = ctx.getXHeight();
      return v * xh * ctx.getFontSize();
   }

   public interface Context {
      Element getElement();

      float getPixelUnitToMillimeter();

      float getPixelToMM();

      float getFontSize();

      float getXHeight();

      float getViewportWidth();

      float getViewportHeight();
   }

   public static class UnitResolver implements LengthHandler {
      public float value;
      public short unit = 1;

      public void startLength() throws ParseException {
      }

      public void lengthValue(float v) throws ParseException {
         this.value = v;
      }

      public void em() throws ParseException {
         this.unit = 3;
      }

      public void ex() throws ParseException {
         this.unit = 4;
      }

      public void in() throws ParseException {
         this.unit = 8;
      }

      public void cm() throws ParseException {
         this.unit = 6;
      }

      public void mm() throws ParseException {
         this.unit = 7;
      }

      public void pc() throws ParseException {
         this.unit = 10;
      }

      public void pt() throws ParseException {
         this.unit = 9;
      }

      public void px() throws ParseException {
         this.unit = 5;
      }

      public void percentage() throws ParseException {
         this.unit = 2;
      }

      public void endLength() throws ParseException {
      }
   }
}
