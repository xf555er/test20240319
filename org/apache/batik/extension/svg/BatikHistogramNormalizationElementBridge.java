package org.apache.batik.extension.svg;

import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.apache.batik.bridge.AbstractSVGFilterPrimitiveElementBridge;
import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.SVGUtilities;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;

public class BatikHistogramNormalizationElementBridge extends AbstractSVGFilterPrimitiveElementBridge implements BatikExtConstants {
   public String getNamespaceURI() {
      return "http://xml.apache.org/batik/ext";
   }

   public String getLocalName() {
      return "histogramNormalization";
   }

   public Bridge getInstance() {
      return new BatikHistogramNormalizationElementBridge();
   }

   public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Rectangle2D filterRegion, Map filterMap) {
      Filter in = getIn(filterElement, filteredElement, filteredNode, inputFilter, filterMap, ctx);
      if (in == null) {
         return null;
      } else {
         Filter sourceGraphics = (Filter)filterMap.get("SourceGraphic");
         Rectangle2D defaultRegion;
         if (in == sourceGraphics) {
            defaultRegion = filterRegion;
         } else {
            defaultRegion = in.getBounds2D();
         }

         Rectangle2D primitiveRegion = SVGUtilities.convertFilterPrimitiveRegion(filterElement, filteredElement, filteredNode, defaultRegion, filterRegion, ctx);
         float trim = 1.0F;
         String s = filterElement.getAttributeNS((String)null, "trim");
         if (s.length() != 0) {
            try {
               trim = SVGUtilities.convertSVGNumber(s);
            } catch (NumberFormatException var15) {
               throw new BridgeException(ctx, filterElement, var15, "attribute.malformed", new Object[]{"trim", s});
            }
         }

         if (trim < 0.0F) {
            trim = 0.0F;
         } else if (trim > 100.0F) {
            trim = 100.0F;
         }

         Filter filter = new BatikHistogramNormalizationFilter8Bit(in, trim / 100.0F);
         Filter filter = new PadRable8Bit(filter, primitiveRegion, PadMode.ZERO_PAD);
         updateFilterMap(filterElement, filter, filterMap);
         handleColorInterpolationFilters(filter, filterElement);
         return filter;
      }
   }

   protected static int convertSides(Element filterElement, String attrName, int defaultValue, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, attrName);
      if (s.length() == 0) {
         return defaultValue;
      } else {
         int ret = false;

         int ret;
         try {
            ret = SVGUtilities.convertSVGInteger(s);
         } catch (NumberFormatException var7) {
            throw new BridgeException(ctx, filterElement, var7, "attribute.malformed", new Object[]{attrName, s});
         }

         if (ret < 3) {
            throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{attrName, s});
         } else {
            return ret;
         }
      }
   }
}
