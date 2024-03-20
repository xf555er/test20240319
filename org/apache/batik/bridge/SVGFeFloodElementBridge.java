package org.apache.batik.bridge;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.FloodRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;

public class SVGFeFloodElementBridge extends AbstractSVGFilterPrimitiveElementBridge {
   public String getLocalName() {
      return "feFlood";
   }

   public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Rectangle2D filterRegion, Map filterMap) {
      Rectangle2D primitiveRegion = SVGUtilities.convertFilterPrimitiveRegion(filterElement, filteredElement, filteredNode, filterRegion, filterRegion, ctx);
      Color color = CSSUtilities.convertFloodColor(filterElement, ctx);
      Filter filter = new FloodRable8Bit(primitiveRegion, color);
      handleColorInterpolationFilters(filter, filterElement);
      updateFilterMap(filterElement, filter, filterMap);
      return filter;
   }
}
