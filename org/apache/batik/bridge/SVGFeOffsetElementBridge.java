package org.apache.batik.bridge;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.AffineRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;

public class SVGFeOffsetElementBridge extends AbstractSVGFilterPrimitiveElementBridge {
   public String getLocalName() {
      return "feOffset";
   }

   public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Rectangle2D filterRegion, Map filterMap) {
      Filter in = getIn(filterElement, filteredElement, filteredNode, inputFilter, filterMap, ctx);
      if (in == null) {
         return null;
      } else {
         Rectangle2D defaultRegion = in.getBounds2D();
         Rectangle2D primitiveRegion = SVGUtilities.convertFilterPrimitiveRegion(filterElement, filteredElement, filteredNode, defaultRegion, filterRegion, ctx);
         float dx = convertNumber(filterElement, "dx", 0.0F, ctx);
         float dy = convertNumber(filterElement, "dy", 0.0F, ctx);
         AffineTransform at = AffineTransform.getTranslateInstance((double)dx, (double)dy);
         PadRable pad = new PadRable8Bit(in, primitiveRegion, PadMode.ZERO_PAD);
         Filter filter = new AffineRable8Bit(pad, at);
         Filter filter = new PadRable8Bit(filter, primitiveRegion, PadMode.ZERO_PAD);
         handleColorInterpolationFilters(filter, filterElement);
         updateFilterMap(filterElement, filter, filterMap);
         return filter;
      }
   }
}
