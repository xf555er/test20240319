package org.apache.batik.bridge;

import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.GaussianBlurRable8Bit;
import org.apache.batik.ext.awt.image.renderable.PadRable;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;

public class SVGFeGaussianBlurElementBridge extends AbstractSVGFilterPrimitiveElementBridge {
   public String getLocalName() {
      return "feGaussianBlur";
   }

   public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Rectangle2D filterRegion, Map filterMap) {
      float[] stdDeviationXY = convertStdDeviation(filterElement, ctx);
      if (!(stdDeviationXY[0] < 0.0F) && !(stdDeviationXY[1] < 0.0F)) {
         Filter in = getIn(filterElement, filteredElement, filteredNode, inputFilter, filterMap, ctx);
         if (in == null) {
            return null;
         } else {
            Rectangle2D defaultRegion = in.getBounds2D();
            Rectangle2D primitiveRegion = SVGUtilities.convertFilterPrimitiveRegion(filterElement, filteredElement, filteredNode, defaultRegion, filterRegion, ctx);
            PadRable pad = new PadRable8Bit(in, primitiveRegion, PadMode.ZERO_PAD);
            Filter blur = new GaussianBlurRable8Bit(pad, (double)stdDeviationXY[0], (double)stdDeviationXY[1]);
            handleColorInterpolationFilters(blur, filterElement);
            PadRable filter = new PadRable8Bit(blur, primitiveRegion, PadMode.ZERO_PAD);
            updateFilterMap(filterElement, filter, filterMap);
            return filter;
         }
      } else {
         throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"stdDeviation", String.valueOf(stdDeviationXY[0]) + stdDeviationXY[1]});
      }
   }

   protected static float[] convertStdDeviation(Element filterElement, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "stdDeviation");
      if (s.length() == 0) {
         return new float[]{0.0F, 0.0F};
      } else {
         float[] stdDevs = new float[2];
         StringTokenizer tokens = new StringTokenizer(s, " ,");

         try {
            stdDevs[0] = SVGUtilities.convertSVGNumber(tokens.nextToken());
            if (tokens.hasMoreTokens()) {
               stdDevs[1] = SVGUtilities.convertSVGNumber(tokens.nextToken());
            } else {
               stdDevs[1] = stdDevs[0];
            }
         } catch (NumberFormatException var6) {
            throw new BridgeException(ctx, filterElement, var6, "attribute.malformed", new Object[]{"stdDeviation", s, var6});
         }

         if (tokens.hasMoreTokens()) {
            throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"stdDeviation", s});
         } else {
            return stdDevs;
         }
      }
   }
}
