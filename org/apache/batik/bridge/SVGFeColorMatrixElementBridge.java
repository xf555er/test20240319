package org.apache.batik.bridge;

import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.ColorMatrixRable;
import org.apache.batik.ext.awt.image.renderable.ColorMatrixRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;

public class SVGFeColorMatrixElementBridge extends AbstractSVGFilterPrimitiveElementBridge {
   public String getLocalName() {
      return "feColorMatrix";
   }

   public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Rectangle2D filterRegion, Map filterMap) {
      Filter in = getIn(filterElement, filteredElement, filteredNode, inputFilter, filterMap, ctx);
      if (in == null) {
         return null;
      } else {
         Rectangle2D defaultRegion = in.getBounds2D();
         Rectangle2D primitiveRegion = SVGUtilities.convertFilterPrimitiveRegion(filterElement, filteredElement, filteredNode, defaultRegion, filterRegion, ctx);
         int type = convertType(filterElement, ctx);
         ColorMatrixRable colorMatrix;
         switch (type) {
            case 0:
               float[][] matrix = convertValuesToMatrix(filterElement, ctx);
               colorMatrix = ColorMatrixRable8Bit.buildMatrix(matrix);
               break;
            case 1:
               float s = convertValuesToSaturate(filterElement, ctx);
               colorMatrix = ColorMatrixRable8Bit.buildSaturate(s);
               break;
            case 2:
               float a = convertValuesToHueRotate(filterElement, ctx);
               colorMatrix = ColorMatrixRable8Bit.buildHueRotate(a);
               break;
            case 3:
               colorMatrix = ColorMatrixRable8Bit.buildLuminanceToAlpha();
               break;
            default:
               throw new RuntimeException("invalid convertType:" + type);
         }

         colorMatrix.setSource(in);
         handleColorInterpolationFilters(colorMatrix, filterElement);
         Filter filter = new PadRable8Bit(colorMatrix, primitiveRegion, PadMode.ZERO_PAD);
         updateFilterMap(filterElement, filter, filterMap);
         return filter;
      }
   }

   protected static float[][] convertValuesToMatrix(Element filterElement, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "values");
      float[][] matrix = new float[4][5];
      if (s.length() == 0) {
         matrix[0][0] = 1.0F;
         matrix[1][1] = 1.0F;
         matrix[2][2] = 1.0F;
         matrix[3][3] = 1.0F;
         return matrix;
      } else {
         StringTokenizer tokens = new StringTokenizer(s, " ,");
         int n = 0;

         try {
            while(n < 20 && tokens.hasMoreTokens()) {
               matrix[n / 5][n % 5] = SVGUtilities.convertSVGNumber(tokens.nextToken());
               ++n;
            }
         } catch (NumberFormatException var7) {
            throw new BridgeException(ctx, filterElement, var7, "attribute.malformed", new Object[]{"values", s, var7});
         }

         if (n == 20 && !tokens.hasMoreTokens()) {
            for(int i = 0; i < 4; ++i) {
               matrix[i][4] *= 255.0F;
            }

            return matrix;
         } else {
            throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"values", s});
         }
      }
   }

   protected static float convertValuesToSaturate(Element filterElement, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "values");
      if (s.length() == 0) {
         return 1.0F;
      } else {
         try {
            return SVGUtilities.convertSVGNumber(s);
         } catch (NumberFormatException var4) {
            throw new BridgeException(ctx, filterElement, var4, "attribute.malformed", new Object[]{"values", s});
         }
      }
   }

   protected static float convertValuesToHueRotate(Element filterElement, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "values");
      if (s.length() == 0) {
         return 0.0F;
      } else {
         try {
            return (float)Math.toRadians((double)SVGUtilities.convertSVGNumber(s));
         } catch (NumberFormatException var4) {
            throw new BridgeException(ctx, filterElement, var4, "attribute.malformed", new Object[]{"values", s});
         }
      }
   }

   protected static int convertType(Element filterElement, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "type");
      if (s.length() == 0) {
         return 0;
      } else if ("hueRotate".equals(s)) {
         return 2;
      } else if ("luminanceToAlpha".equals(s)) {
         return 3;
      } else if ("matrix".equals(s)) {
         return 0;
      } else if ("saturate".equals(s)) {
         return 1;
      } else {
         throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"type", s});
      }
   }
}
