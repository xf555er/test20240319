package org.apache.batik.bridge;

import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.MorphologyRable8Bit;
import org.apache.batik.ext.awt.image.renderable.PadRable;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;

public class SVGFeMorphologyElementBridge extends AbstractSVGFilterPrimitiveElementBridge {
   public String getLocalName() {
      return "feMorphology";
   }

   public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Rectangle2D filterRegion, Map filterMap) {
      float[] radii = convertRadius(filterElement, ctx);
      if (radii[0] != 0.0F && radii[1] != 0.0F) {
         boolean isDilate = convertOperator(filterElement, ctx);
         Filter in = getIn(filterElement, filteredElement, filteredNode, inputFilter, filterMap, ctx);
         if (in == null) {
            return null;
         } else {
            Rectangle2D defaultRegion = in.getBounds2D();
            Rectangle2D primitiveRegion = SVGUtilities.convertFilterPrimitiveRegion(filterElement, filteredElement, filteredNode, defaultRegion, filterRegion, ctx);
            PadRable pad = new PadRable8Bit(in, primitiveRegion, PadMode.ZERO_PAD);
            Filter morphology = new MorphologyRable8Bit(pad, (double)radii[0], (double)radii[1], isDilate);
            handleColorInterpolationFilters(morphology, filterElement);
            PadRable filter = new PadRable8Bit(morphology, primitiveRegion, PadMode.ZERO_PAD);
            updateFilterMap(filterElement, filter, filterMap);
            return filter;
         }
      } else {
         return null;
      }
   }

   protected static float[] convertRadius(Element filterElement, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "radius");
      if (s.length() == 0) {
         return new float[]{0.0F, 0.0F};
      } else {
         float[] radii = new float[2];
         StringTokenizer tokens = new StringTokenizer(s, " ,");

         try {
            radii[0] = SVGUtilities.convertSVGNumber(tokens.nextToken());
            if (tokens.hasMoreTokens()) {
               radii[1] = SVGUtilities.convertSVGNumber(tokens.nextToken());
            } else {
               radii[1] = radii[0];
            }
         } catch (NumberFormatException var6) {
            throw new BridgeException(ctx, filterElement, var6, "attribute.malformed", new Object[]{"radius", s, var6});
         }

         if (!tokens.hasMoreTokens() && !(radii[0] < 0.0F) && !(radii[1] < 0.0F)) {
            return radii;
         } else {
            throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"radius", s});
         }
      }
   }

   protected static boolean convertOperator(Element filterElement, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "operator");
      if (s.length() == 0) {
         return false;
      } else if ("erode".equals(s)) {
         return false;
      } else if ("dilate".equals(s)) {
         return true;
      } else {
         throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"operator", s});
      }
   }
}
