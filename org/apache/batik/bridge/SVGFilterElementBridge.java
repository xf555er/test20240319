package org.apache.batik.bridge;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.FilterChainRable;
import org.apache.batik.ext.awt.image.renderable.FilterChainRable8Bit;
import org.apache.batik.ext.awt.image.renderable.FloodRable8Bit;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SVGFilterElementBridge extends AnimatableGenericSVGBridge implements FilterBridge, ErrorConstants {
   protected static final Color TRANSPARENT_BLACK = new Color(0, true);

   public String getLocalName() {
      return "filter";
   }

   public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode) {
      Rectangle2D filterRegion = SVGUtilities.convertFilterChainRegion(filterElement, filteredElement, filteredNode, ctx);
      if (filterRegion == null) {
         return null;
      } else {
         Filter sourceGraphic = filteredNode.getGraphicsNodeRable(true);
         Filter sourceGraphic = new PadRable8Bit(sourceGraphic, filterRegion, PadMode.ZERO_PAD);
         FilterChainRable filterChain = new FilterChainRable8Bit(sourceGraphic, filterRegion);
         float[] filterRes = SVGUtilities.convertFilterRes(filterElement, ctx);
         filterChain.setFilterResolutionX((int)filterRes[0]);
         filterChain.setFilterResolutionY((int)filterRes[1]);
         Map filterNodeMap = new HashMap(11);
         filterNodeMap.put("SourceGraphic", sourceGraphic);
         Filter in = buildFilterPrimitives(filterElement, filterRegion, filteredElement, filteredNode, sourceGraphic, filterNodeMap, ctx);
         if (in == null) {
            return null;
         } else {
            if (in == sourceGraphic) {
               in = createEmptyFilter(filterElement, filterRegion, filteredElement, filteredNode, ctx);
            }

            filterChain.setSource(in);
            return filterChain;
         }
      }
   }

   protected static Filter createEmptyFilter(Element filterElement, Rectangle2D filterRegion, Element filteredElement, GraphicsNode filteredNode, BridgeContext ctx) {
      Rectangle2D primitiveRegion = SVGUtilities.convertFilterPrimitiveRegion((Element)null, filterElement, filteredElement, filteredNode, filterRegion, filterRegion, ctx);
      return new FloodRable8Bit(primitiveRegion, TRANSPARENT_BLACK);
   }

   protected static Filter buildFilterPrimitives(Element filterElement, Rectangle2D filterRegion, Element filteredElement, GraphicsNode filteredNode, Filter in, Map filterNodeMap, BridgeContext ctx) {
      List refs = new LinkedList();

      while(true) {
         Filter newIn = buildLocalFilterPrimitives(filterElement, filterRegion, filteredElement, filteredNode, in, filterNodeMap, ctx);
         if (newIn != in) {
            return newIn;
         }

         String uri = XLinkSupport.getXLinkHref(filterElement);
         if (uri.length() == 0) {
            return in;
         }

         SVGOMDocument doc = (SVGOMDocument)filterElement.getOwnerDocument();
         ParsedURL url = new ParsedURL(doc.getURLObject(), uri);
         if (refs.contains(url)) {
            throw new BridgeException(ctx, filterElement, "xlink.href.circularDependencies", new Object[]{uri});
         }

         refs.add(url);
         filterElement = ctx.getReferencedElement(filterElement, uri);
      }
   }

   protected static Filter buildLocalFilterPrimitives(Element filterElement, Rectangle2D filterRegion, Element filteredElement, GraphicsNode filteredNode, Filter in, Map filterNodeMap, BridgeContext ctx) {
      for(Node n = filterElement.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1) {
            Element e = (Element)n;
            Bridge bridge = ctx.getBridge(e);
            if (bridge != null && bridge instanceof FilterPrimitiveBridge) {
               FilterPrimitiveBridge filterBridge = (FilterPrimitiveBridge)bridge;
               Filter filterNode = filterBridge.createFilter(ctx, e, filteredElement, filteredNode, in, filterRegion, filterNodeMap);
               if (filterNode == null) {
                  return null;
               }

               in = filterNode;
            }
         }
      }

      return in;
   }
}
