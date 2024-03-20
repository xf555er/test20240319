package org.apache.batik.bridge;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.batik.ext.awt.image.ARGBChannel;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.DisplacementMapRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;

public class SVGFeDisplacementMapElementBridge extends AbstractSVGFilterPrimitiveElementBridge {
   public String getLocalName() {
      return "feDisplacementMap";
   }

   public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Rectangle2D filterRegion, Map filterMap) {
      float scale = convertNumber(filterElement, "scale", 0.0F, ctx);
      ARGBChannel xChannelSelector = convertChannelSelector(filterElement, "xChannelSelector", ARGBChannel.A, ctx);
      ARGBChannel yChannelSelector = convertChannelSelector(filterElement, "yChannelSelector", ARGBChannel.A, ctx);
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
            PadRable pad = new PadRable8Bit(in, primitiveRegion, PadMode.ZERO_PAD);
            List srcs = new ArrayList(2);
            srcs.add(pad);
            srcs.add(in2);
            Filter displacementMap = new DisplacementMapRable8Bit(srcs, (double)scale, xChannelSelector, yChannelSelector);
            handleColorInterpolationFilters(displacementMap, filterElement);
            PadRable filter = new PadRable8Bit(displacementMap, primitiveRegion, PadMode.ZERO_PAD);
            updateFilterMap(filterElement, filter, filterMap);
            return filter;
         }
      }
   }

   protected static ARGBChannel convertChannelSelector(Element filterElement, String attrName, ARGBChannel defaultChannel, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, attrName);
      if (s.length() == 0) {
         return defaultChannel;
      } else if ("A".equals(s)) {
         return ARGBChannel.A;
      } else if ("R".equals(s)) {
         return ARGBChannel.R;
      } else if ("G".equals(s)) {
         return ARGBChannel.G;
      } else if ("B".equals(s)) {
         return ARGBChannel.B;
      } else {
         throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{attrName, s});
      }
   }
}
