package org.apache.batik.bridge;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.batik.ext.awt.image.CompositeRule;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.CompositeRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;

public class SVGFeBlendElementBridge extends AbstractSVGFilterPrimitiveElementBridge {
   public String getLocalName() {
      return "feBlend";
   }

   public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Rectangle2D filterRegion, Map filterMap) {
      CompositeRule rule = convertMode(filterElement, ctx);
      Filter in = getIn(filterElement, filteredElement, filteredNode, inputFilter, filterMap, ctx);
      if (in == null) {
         return null;
      } else {
         Filter in2 = getIn2(filterElement, filteredElement, filteredNode, inputFilter, filterMap, ctx);
         if (in2 == null) {
            return null;
         } else {
            Rectangle2D defaultRegion = (Rectangle2D)in.getBounds2D().clone();
            defaultRegion.add(in2.getBounds2D());
            Rectangle2D primitiveRegion = SVGUtilities.convertFilterPrimitiveRegion(filterElement, filteredElement, filteredNode, defaultRegion, filterRegion, ctx);
            List srcs = new ArrayList(2);
            srcs.add(in2);
            srcs.add(in);
            Filter filter = new CompositeRable8Bit(srcs, rule, true);
            handleColorInterpolationFilters(filter, filterElement);
            Filter filter = new PadRable8Bit(filter, primitiveRegion, PadMode.ZERO_PAD);
            updateFilterMap(filterElement, filter, filterMap);
            return filter;
         }
      }
   }

   protected static CompositeRule convertMode(Element filterElement, BridgeContext ctx) {
      String rule = filterElement.getAttributeNS((String)null, "mode");
      if (rule.length() == 0) {
         return CompositeRule.OVER;
      } else if ("normal".equals(rule)) {
         return CompositeRule.OVER;
      } else if ("multiply".equals(rule)) {
         return CompositeRule.MULTIPLY;
      } else if ("screen".equals(rule)) {
         return CompositeRule.SCREEN;
      } else if ("darken".equals(rule)) {
         return CompositeRule.DARKEN;
      } else if ("lighten".equals(rule)) {
         return CompositeRule.LIGHTEN;
      } else {
         throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"mode", rule});
      }
   }
}
