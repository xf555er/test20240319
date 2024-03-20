package org.apache.batik.bridge;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.batik.ext.awt.image.CompositeRule;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.CompositeRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SVGFeMergeElementBridge extends AbstractSVGFilterPrimitiveElementBridge {
   public String getLocalName() {
      return "feMerge";
   }

   public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Rectangle2D filterRegion, Map filterMap) {
      List srcs = extractFeMergeNode(filterElement, filteredElement, filteredNode, inputFilter, filterMap, ctx);
      if (srcs == null) {
         return null;
      } else if (srcs.size() == 0) {
         return null;
      } else {
         Iterator iter = srcs.iterator();
         Rectangle2D defaultRegion = (Rectangle2D)((Filter)iter.next()).getBounds2D().clone();

         while(iter.hasNext()) {
            defaultRegion.add(((Filter)iter.next()).getBounds2D());
         }

         Rectangle2D primitiveRegion = SVGUtilities.convertFilterPrimitiveRegion(filterElement, filteredElement, filteredNode, defaultRegion, filterRegion, ctx);
         Filter filter = new CompositeRable8Bit(srcs, CompositeRule.OVER, true);
         handleColorInterpolationFilters(filter, filterElement);
         Filter filter = new PadRable8Bit(filter, primitiveRegion, PadMode.ZERO_PAD);
         updateFilterMap(filterElement, filter, filterMap);
         return filter;
      }
   }

   protected static List extractFeMergeNode(Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Map filterMap, BridgeContext ctx) {
      List srcs = null;

      for(Node n = filterElement.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1) {
            Element e = (Element)n;
            Bridge bridge = ctx.getBridge(e);
            if (bridge != null && bridge instanceof SVGFeMergeNodeElementBridge) {
               Filter filter = ((SVGFeMergeNodeElementBridge)bridge).createFilter(ctx, e, filteredElement, filteredNode, inputFilter, filterMap);
               if (filter != null) {
                  if (srcs == null) {
                     srcs = new LinkedList();
                  }

                  srcs.add(filter);
               }
            }
         }
      }

      return srcs;
   }

   public static class SVGFeMergeNodeElementBridge extends AnimatableGenericSVGBridge {
      public String getLocalName() {
         return "feMergeNode";
      }

      public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Map filterMap) {
         return AbstractSVGFilterPrimitiveElementBridge.getIn(filterElement, filteredElement, filteredNode, inputFilter, filterMap, ctx);
      }
   }
}
