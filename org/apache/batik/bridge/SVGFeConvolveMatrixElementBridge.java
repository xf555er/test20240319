package org.apache.batik.bridge;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.Kernel;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.ConvolveMatrixRable;
import org.apache.batik.ext.awt.image.renderable.ConvolveMatrixRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;

public class SVGFeConvolveMatrixElementBridge extends AbstractSVGFilterPrimitiveElementBridge {
   public String getLocalName() {
      return "feConvolveMatrix";
   }

   public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Rectangle2D filterRegion, Map filterMap) {
      int[] orderXY = convertOrder(filterElement, ctx);
      float[] kernelMatrix = convertKernelMatrix(filterElement, orderXY, ctx);
      float divisor = convertDivisor(filterElement, kernelMatrix, ctx);
      float bias = convertNumber(filterElement, "bias", 0.0F, ctx);
      int[] targetXY = convertTarget(filterElement, orderXY, ctx);
      PadMode padMode = convertEdgeMode(filterElement, ctx);
      double[] kernelUnitLength = convertKernelUnitLength(filterElement, ctx);
      boolean preserveAlpha = convertPreserveAlpha(filterElement, ctx);
      Filter in = getIn(filterElement, filteredElement, filteredNode, inputFilter, filterMap, ctx);
      if (in == null) {
         return null;
      } else {
         Rectangle2D defaultRegion = in.getBounds2D();
         Rectangle2D primitiveRegion = SVGUtilities.convertFilterPrimitiveRegion(filterElement, filteredElement, filteredNode, defaultRegion, filterRegion, ctx);
         PadRable pad = new PadRable8Bit(in, primitiveRegion, PadMode.ZERO_PAD);
         ConvolveMatrixRable convolve = new ConvolveMatrixRable8Bit(pad);

         for(int i = 0; i < kernelMatrix.length; ++i) {
            kernelMatrix[i] /= divisor;
         }

         convolve.setKernel(new Kernel(orderXY[0], orderXY[1], kernelMatrix));
         convolve.setTarget(new Point(targetXY[0], targetXY[1]));
         convolve.setBias((double)bias);
         convolve.setEdgeMode(padMode);
         convolve.setKernelUnitLength(kernelUnitLength);
         convolve.setPreserveAlpha(preserveAlpha);
         handleColorInterpolationFilters(convolve, filterElement);
         PadRable filter = new PadRable8Bit(convolve, primitiveRegion, PadMode.ZERO_PAD);
         updateFilterMap(filterElement, filter, filterMap);
         return filter;
      }
   }

   protected static int[] convertOrder(Element filterElement, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "order");
      if (s.length() == 0) {
         return new int[]{3, 3};
      } else {
         int[] orderXY = new int[2];
         StringTokenizer tokens = new StringTokenizer(s, " ,");

         try {
            orderXY[0] = SVGUtilities.convertSVGInteger(tokens.nextToken());
            if (tokens.hasMoreTokens()) {
               orderXY[1] = SVGUtilities.convertSVGInteger(tokens.nextToken());
            } else {
               orderXY[1] = orderXY[0];
            }
         } catch (NumberFormatException var6) {
            throw new BridgeException(ctx, filterElement, var6, "attribute.malformed", new Object[]{"order", s, var6});
         }

         if (!tokens.hasMoreTokens() && orderXY[0] > 0 && orderXY[1] > 0) {
            return orderXY;
         } else {
            throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"order", s});
         }
      }
   }

   protected static float[] convertKernelMatrix(Element filterElement, int[] orderXY, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "kernelMatrix");
      if (s.length() == 0) {
         throw new BridgeException(ctx, filterElement, "attribute.missing", new Object[]{"kernelMatrix"});
      } else {
         int size = orderXY[0] * orderXY[1];
         float[] kernelMatrix = new float[size];
         StringTokenizer tokens = new StringTokenizer(s, " ,");
         int i = 0;

         try {
            while(tokens.hasMoreTokens() && i < size) {
               kernelMatrix[i++] = SVGUtilities.convertSVGNumber(tokens.nextToken());
            }
         } catch (NumberFormatException var9) {
            throw new BridgeException(ctx, filterElement, var9, "attribute.malformed", new Object[]{"kernelMatrix", s, var9});
         }

         if (i != size) {
            throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"kernelMatrix", s});
         } else {
            return kernelMatrix;
         }
      }
   }

   protected static float convertDivisor(Element filterElement, float[] kernelMatrix, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "divisor");
      if (s.length() != 0) {
         try {
            return SVGUtilities.convertSVGNumber(s);
         } catch (NumberFormatException var9) {
            throw new BridgeException(ctx, filterElement, var9, "attribute.malformed", new Object[]{"divisor", s, var9});
         }
      } else {
         float sum = 0.0F;
         float[] var5 = kernelMatrix;
         int var6 = kernelMatrix.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            float aKernelMatrix = var5[var7];
            sum += aKernelMatrix;
         }

         return sum == 0.0F ? 1.0F : sum;
      }
   }

   protected static int[] convertTarget(Element filterElement, int[] orderXY, BridgeContext ctx) {
      int[] targetXY = new int[2];
      String s = filterElement.getAttributeNS((String)null, "targetX");
      int v;
      if (s.length() == 0) {
         targetXY[0] = orderXY[0] / 2;
      } else {
         try {
            v = SVGUtilities.convertSVGInteger(s);
            if (v < 0 || v >= orderXY[0]) {
               throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"targetX", s});
            }

            targetXY[0] = v;
         } catch (NumberFormatException var7) {
            throw new BridgeException(ctx, filterElement, var7, "attribute.malformed", new Object[]{"targetX", s, var7});
         }
      }

      s = filterElement.getAttributeNS((String)null, "targetY");
      if (s.length() == 0) {
         targetXY[1] = orderXY[1] / 2;
      } else {
         try {
            v = SVGUtilities.convertSVGInteger(s);
            if (v < 0 || v >= orderXY[1]) {
               throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"targetY", s});
            }

            targetXY[1] = v;
         } catch (NumberFormatException var6) {
            throw new BridgeException(ctx, filterElement, var6, "attribute.malformed", new Object[]{"targetY", s, var6});
         }
      }

      return targetXY;
   }

   protected static double[] convertKernelUnitLength(Element filterElement, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "kernelUnitLength");
      if (s.length() == 0) {
         return null;
      } else {
         double[] units = new double[2];
         StringTokenizer tokens = new StringTokenizer(s, " ,");

         try {
            units[0] = (double)SVGUtilities.convertSVGNumber(tokens.nextToken());
            if (tokens.hasMoreTokens()) {
               units[1] = (double)SVGUtilities.convertSVGNumber(tokens.nextToken());
            } else {
               units[1] = units[0];
            }
         } catch (NumberFormatException var6) {
            throw new BridgeException(ctx, filterElement, var6, "attribute.malformed", new Object[]{"kernelUnitLength", s});
         }

         if (!tokens.hasMoreTokens() && !(units[0] <= 0.0) && !(units[1] <= 0.0)) {
            return units;
         } else {
            throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"kernelUnitLength", s});
         }
      }
   }

   protected static PadMode convertEdgeMode(Element filterElement, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "edgeMode");
      if (s.length() == 0) {
         return PadMode.REPLICATE;
      } else if ("duplicate".equals(s)) {
         return PadMode.REPLICATE;
      } else if ("wrap".equals(s)) {
         return PadMode.WRAP;
      } else if ("none".equals(s)) {
         return PadMode.ZERO_PAD;
      } else {
         throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"edgeMode", s});
      }
   }

   protected static boolean convertPreserveAlpha(Element filterElement, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "preserveAlpha");
      if (s.length() == 0) {
         return false;
      } else if ("true".equals(s)) {
         return true;
      } else if ("false".equals(s)) {
         return false;
      } else {
         throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"preserveAlpha", s});
      }
   }
}
