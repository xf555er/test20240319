package org.apache.batik.bridge;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.AffineRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SVGFeImageElementBridge extends AbstractSVGFilterPrimitiveElementBridge {
   public String getLocalName() {
      return "feImage";
   }

   public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Rectangle2D filterRegion, Map filterMap) {
      String uriStr = XLinkSupport.getXLinkHref(filterElement);
      if (uriStr.length() == 0) {
         throw new BridgeException(ctx, filterElement, "attribute.missing", new Object[]{"xlink:href"});
      } else {
         Document document = filterElement.getOwnerDocument();
         boolean isUse = uriStr.indexOf(35) != -1;
         Element contentElement = null;
         if (isUse) {
            contentElement = document.createElementNS("http://www.w3.org/2000/svg", "use");
         } else {
            contentElement = document.createElementNS("http://www.w3.org/2000/svg", "image");
         }

         contentElement.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", uriStr);
         Element proxyElement = document.createElementNS("http://www.w3.org/2000/svg", "g");
         proxyElement.appendChild(contentElement);
         Element filterDefElement = (Element)((Element)filterElement.getParentNode());
         Rectangle2D primitiveRegion = SVGUtilities.getBaseFilterPrimitiveRegion(filterElement, filteredElement, filteredNode, filterRegion, ctx);
         contentElement.setAttributeNS((String)null, "x", String.valueOf(primitiveRegion.getX()));
         contentElement.setAttributeNS((String)null, "y", String.valueOf(primitiveRegion.getY()));
         contentElement.setAttributeNS((String)null, "width", String.valueOf(primitiveRegion.getWidth()));
         contentElement.setAttributeNS((String)null, "height", String.valueOf(primitiveRegion.getHeight()));
         GraphicsNode node = ctx.getGVTBuilder().build(ctx, proxyElement);
         Filter filter = node.getGraphicsNodeRable(true);
         String s = SVGUtilities.getChainableAttributeNS(filterDefElement, (String)null, "primitiveUnits", ctx);
         short coordSystemType;
         if (s.length() == 0) {
            coordSystemType = 1;
         } else {
            coordSystemType = SVGUtilities.parseCoordinateSystem(filterDefElement, "primitiveUnits", s, ctx);
         }

         AffineTransform at = new AffineTransform();
         if (coordSystemType == 2) {
            at = SVGUtilities.toObjectBBox(at, filteredNode);
         }

         Filter filter = new AffineRable8Bit(filter, at);
         handleColorInterpolationFilters(filter, filterElement);
         Rectangle2D primitiveRegionUserSpace = SVGUtilities.convertFilterPrimitiveRegion(filterElement, filteredElement, filteredNode, filterRegion, filterRegion, ctx);
         Filter filter = new PadRable8Bit(filter, primitiveRegionUserSpace, PadMode.ZERO_PAD);
         updateFilterMap(filterElement, filter, filterMap);
         return filter;
      }
   }

   protected static Filter createSVGFeImage(BridgeContext ctx, Rectangle2D primitiveRegion, Element refElement, boolean toBBoxNeeded, Element filterElement, GraphicsNode filteredNode) {
      GraphicsNode node = ctx.getGVTBuilder().build(ctx, refElement);
      Filter filter = node.getGraphicsNodeRable(true);
      AffineTransform at = new AffineTransform();
      if (toBBoxNeeded) {
         Element filterDefElement = (Element)((Element)filterElement.getParentNode());
         String s = SVGUtilities.getChainableAttributeNS(filterDefElement, (String)null, "primitiveUnits", ctx);
         short coordSystemType;
         if (s.length() == 0) {
            coordSystemType = 1;
         } else {
            coordSystemType = SVGUtilities.parseCoordinateSystem(filterDefElement, "primitiveUnits", s, ctx);
         }

         if (coordSystemType == 2) {
            at = SVGUtilities.toObjectBBox(at, filteredNode);
         }

         Rectangle2D bounds = filteredNode.getGeometryBounds();
         at.preConcatenate(AffineTransform.getTranslateInstance(primitiveRegion.getX() - bounds.getX(), primitiveRegion.getY() - bounds.getY()));
      } else {
         at.translate(primitiveRegion.getX(), primitiveRegion.getY());
      }

      return new AffineRable8Bit(filter, at);
   }

   protected static Filter createRasterFeImage(BridgeContext ctx, Rectangle2D primitiveRegion, ParsedURL purl) {
      Filter filter = ImageTagRegistry.getRegistry().readURL(purl);
      Rectangle2D bounds = filter.getBounds2D();
      AffineTransform scale = new AffineTransform();
      scale.translate(primitiveRegion.getX(), primitiveRegion.getY());
      scale.scale(primitiveRegion.getWidth() / (bounds.getWidth() - 1.0), primitiveRegion.getHeight() / (bounds.getHeight() - 1.0));
      scale.translate(-bounds.getX(), -bounds.getY());
      return new AffineRable8Bit(filter, scale);
   }
}
