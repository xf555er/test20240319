package org.apache.batik.bridge;

import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.apache.batik.ext.awt.image.Light;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.SpecularLightingRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;

public class SVGFeSpecularLightingElementBridge extends AbstractSVGLightingElementBridge {
   public String getLocalName() {
      return "feSpecularLighting";
   }

   public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Rectangle2D filterRegion, Map filterMap) {
      float surfaceScale = convertNumber(filterElement, "surfaceScale", 1.0F, ctx);
      float specularConstant = convertNumber(filterElement, "specularConstant", 1.0F, ctx);
      float specularExponent = convertSpecularExponent(filterElement, ctx);
      Light light = extractLight(filterElement, ctx);
      double[] kernelUnitLength = convertKernelUnitLength(filterElement, ctx);
      Filter in = getIn(filterElement, filteredElement, filteredNode, inputFilter, filterMap, ctx);
      if (in == null) {
         return null;
      } else {
         Rectangle2D defaultRegion = in.getBounds2D();
         Rectangle2D primitiveRegion = SVGUtilities.convertFilterPrimitiveRegion(filterElement, filteredElement, filteredNode, defaultRegion, filterRegion, ctx);
         Filter filter = new SpecularLightingRable8Bit(in, primitiveRegion, light, (double)specularConstant, (double)specularExponent, (double)surfaceScale, kernelUnitLength);
         handleColorInterpolationFilters(filter, filterElement);
         updateFilterMap(filterElement, filter, filterMap);
         return filter;
      }
   }

   protected static float convertSpecularExponent(Element filterElement, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "specularExponent");
      if (s.length() == 0) {
         return 1.0F;
      } else {
         try {
            float v = SVGUtilities.convertSVGNumber(s);
            if (!(v < 1.0F) && !(v > 128.0F)) {
               return v;
            } else {
               throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"specularConstant", s});
            }
         } catch (NumberFormatException var4) {
            throw new BridgeException(ctx, filterElement, var4, "attribute.malformed", new Object[]{"specularConstant", s, var4});
         }
      }
   }
}
