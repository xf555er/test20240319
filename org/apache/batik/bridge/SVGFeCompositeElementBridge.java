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

public class SVGFeCompositeElementBridge extends AbstractSVGFilterPrimitiveElementBridge {
   public String getLocalName() {
      return "feComposite";
   }

   public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Rectangle2D filterRegion, Map filterMap) {
      CompositeRule rule = convertOperator(filterElement, ctx);
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

   protected static CompositeRule convertOperator(Element filterElement, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "operator");
      if (s.length() == 0) {
         return CompositeRule.OVER;
      } else if ("atop".equals(s)) {
         return CompositeRule.ATOP;
      } else if ("in".equals(s)) {
         return CompositeRule.IN;
      } else if ("over".equals(s)) {
         return CompositeRule.OVER;
      } else if ("out".equals(s)) {
         return CompositeRule.OUT;
      } else if ("xor".equals(s)) {
         return CompositeRule.XOR;
      } else if ("arithmetic".equals(s)) {
         float k1 = convertNumber(filterElement, "k1", 0.0F, ctx);
         float k2 = convertNumber(filterElement, "k2", 0.0F, ctx);
         float k3 = convertNumber(filterElement, "k3", 0.0F, ctx);
         float k4 = convertNumber(filterElement, "k4", 0.0F, ctx);
         return CompositeRule.ARITHMETIC(k1, k2, k3, k4);
      } else {
         throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"operator", s});
      }
   }
}
