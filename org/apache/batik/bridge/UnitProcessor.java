package org.apache.batik.bridge;

import org.apache.batik.parser.ParseException;
import org.w3c.dom.Element;

public abstract class UnitProcessor extends org.apache.batik.parser.UnitProcessor {
   public static org.apache.batik.parser.UnitProcessor.Context createContext(BridgeContext ctx, Element e) {
      return new DefaultContext(ctx, e);
   }

   public static float svgHorizontalCoordinateToObjectBoundingBox(String s, String attr, org.apache.batik.parser.UnitProcessor.Context ctx) {
      return svgToObjectBoundingBox(s, attr, (short)2, ctx);
   }

   public static float svgVerticalCoordinateToObjectBoundingBox(String s, String attr, org.apache.batik.parser.UnitProcessor.Context ctx) {
      return svgToObjectBoundingBox(s, attr, (short)1, ctx);
   }

   public static float svgOtherCoordinateToObjectBoundingBox(String s, String attr, org.apache.batik.parser.UnitProcessor.Context ctx) {
      return svgToObjectBoundingBox(s, attr, (short)0, ctx);
   }

   public static float svgHorizontalLengthToObjectBoundingBox(String s, String attr, org.apache.batik.parser.UnitProcessor.Context ctx) {
      return svgLengthToObjectBoundingBox(s, attr, (short)2, ctx);
   }

   public static float svgVerticalLengthToObjectBoundingBox(String s, String attr, org.apache.batik.parser.UnitProcessor.Context ctx) {
      return svgLengthToObjectBoundingBox(s, attr, (short)1, ctx);
   }

   public static float svgOtherLengthToObjectBoundingBox(String s, String attr, org.apache.batik.parser.UnitProcessor.Context ctx) {
      return svgLengthToObjectBoundingBox(s, attr, (short)0, ctx);
   }

   public static float svgLengthToObjectBoundingBox(String s, String attr, short d, org.apache.batik.parser.UnitProcessor.Context ctx) {
      float v = svgToObjectBoundingBox(s, attr, d, ctx);
      if (v < 0.0F) {
         throw new BridgeException(getBridgeContext(ctx), ctx.getElement(), "length.negative", new Object[]{attr, s});
      } else {
         return v;
      }
   }

   public static float svgToObjectBoundingBox(String s, String attr, short d, org.apache.batik.parser.UnitProcessor.Context ctx) {
      try {
         return org.apache.batik.parser.UnitProcessor.svgToObjectBoundingBox(s, attr, d, ctx);
      } catch (ParseException var5) {
         throw new BridgeException(getBridgeContext(ctx), ctx.getElement(), var5, "attribute.malformed", new Object[]{attr, s, var5});
      }
   }

   public static float svgHorizontalLengthToUserSpace(String s, String attr, org.apache.batik.parser.UnitProcessor.Context ctx) {
      return svgLengthToUserSpace(s, attr, (short)2, ctx);
   }

   public static float svgVerticalLengthToUserSpace(String s, String attr, org.apache.batik.parser.UnitProcessor.Context ctx) {
      return svgLengthToUserSpace(s, attr, (short)1, ctx);
   }

   public static float svgOtherLengthToUserSpace(String s, String attr, org.apache.batik.parser.UnitProcessor.Context ctx) {
      return svgLengthToUserSpace(s, attr, (short)0, ctx);
   }

   public static float svgHorizontalCoordinateToUserSpace(String s, String attr, org.apache.batik.parser.UnitProcessor.Context ctx) {
      return svgToUserSpace(s, attr, (short)2, ctx);
   }

   public static float svgVerticalCoordinateToUserSpace(String s, String attr, org.apache.batik.parser.UnitProcessor.Context ctx) {
      return svgToUserSpace(s, attr, (short)1, ctx);
   }

   public static float svgOtherCoordinateToUserSpace(String s, String attr, org.apache.batik.parser.UnitProcessor.Context ctx) {
      return svgToUserSpace(s, attr, (short)0, ctx);
   }

   public static float svgLengthToUserSpace(String s, String attr, short d, org.apache.batik.parser.UnitProcessor.Context ctx) {
      float v = svgToUserSpace(s, attr, d, ctx);
      if (v < 0.0F) {
         throw new BridgeException(getBridgeContext(ctx), ctx.getElement(), "length.negative", new Object[]{attr, s});
      } else {
         return v;
      }
   }

   public static float svgToUserSpace(String s, String attr, short d, org.apache.batik.parser.UnitProcessor.Context ctx) {
      try {
         return org.apache.batik.parser.UnitProcessor.svgToUserSpace(s, attr, d, ctx);
      } catch (ParseException var5) {
         throw new BridgeException(getBridgeContext(ctx), ctx.getElement(), var5, "attribute.malformed", new Object[]{attr, s, var5});
      }
   }

   protected static BridgeContext getBridgeContext(org.apache.batik.parser.UnitProcessor.Context ctx) {
      return ctx instanceof DefaultContext ? ((DefaultContext)ctx).ctx : null;
   }

   public static class DefaultContext implements org.apache.batik.parser.UnitProcessor.Context {
      protected Element e;
      protected BridgeContext ctx;

      public DefaultContext(BridgeContext ctx, Element e) {
         this.ctx = ctx;
         this.e = e;
      }

      public Element getElement() {
         return this.e;
      }

      public float getPixelUnitToMillimeter() {
         return this.ctx.getUserAgent().getPixelUnitToMillimeter();
      }

      public float getPixelToMM() {
         return this.getPixelUnitToMillimeter();
      }

      public float getFontSize() {
         return CSSUtilities.getComputedStyle(this.e, 22).getFloatValue();
      }

      public float getXHeight() {
         return 0.5F;
      }

      public float getViewportWidth() {
         return this.ctx.getViewport(this.e).getWidth();
      }

      public float getViewportHeight() {
         return this.ctx.getViewport(this.e).getHeight();
      }
   }
}
