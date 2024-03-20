package org.apache.batik.bridge;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.FilterAlphaRable;
import org.apache.batik.ext.awt.image.renderable.FilterColorInterpolation;
import org.apache.batik.ext.awt.image.renderable.FloodRable8Bit;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.filter.BackgroundRable8Bit;
import org.w3c.dom.Element;

public abstract class AbstractSVGFilterPrimitiveElementBridge extends AnimatableGenericSVGBridge implements FilterPrimitiveBridge, ErrorConstants {
   static final Rectangle2D INFINITE_FILTER_REGION = new Rectangle2D.Float(-1.7014117E38F, -1.7014117E38F, Float.MAX_VALUE, Float.MAX_VALUE);

   protected AbstractSVGFilterPrimitiveElementBridge() {
   }

   protected static Filter getIn(Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Map filterMap, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "in");
      return s.length() == 0 ? inputFilter : getFilterSource(filterElement, s, filteredElement, filteredNode, filterMap, ctx);
   }

   protected static Filter getIn2(Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Map filterMap, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "in2");
      if (s.length() == 0) {
         throw new BridgeException(ctx, filterElement, "attribute.missing", new Object[]{"in2"});
      } else {
         return getFilterSource(filterElement, s, filteredElement, filteredNode, filterMap, ctx);
      }
   }

   protected static void updateFilterMap(Element filterElement, Filter filter, Map filterMap) {
      String s = filterElement.getAttributeNS((String)null, "result");
      if (s.length() != 0 && s.trim().length() != 0) {
         filterMap.put(s, filter);
      }

   }

   protected static void handleColorInterpolationFilters(Filter filter, Element filterElement) {
      if (filter instanceof FilterColorInterpolation) {
         boolean isLinear = CSSUtilities.convertColorInterpolationFilters(filterElement);
         ((FilterColorInterpolation)filter).setColorSpaceLinear(isLinear);
      }

   }

   static Filter getFilterSource(Element filterElement, String s, Element filteredElement, GraphicsNode filteredNode, Map filterMap, BridgeContext ctx) {
      Filter srcG = (Filter)filterMap.get("SourceGraphic");
      Rectangle2D filterRegion = srcG.getBounds2D();
      int length = s.length();
      Filter source = null;
      switch (length) {
         case 9:
            if ("FillPaint".equals(s)) {
               Paint paint = PaintServer.convertFillPaint(filteredElement, filteredNode, ctx);
               if (paint == null) {
                  paint = new Color(0, 0, 0, 0);
               }

               source = new FloodRable8Bit(filterRegion, (Paint)paint);
            }
         case 10:
         case 12:
         case 14:
         default:
            break;
         case 11:
            if (s.charAt(1) == "SourceAlpha".charAt(1)) {
               if ("SourceAlpha".equals(s)) {
                  source = new FilterAlphaRable(srcG);
               }
            } else if ("StrokePaint".equals(s)) {
               Paint paint = PaintServer.convertStrokePaint(filteredElement, filteredNode, ctx);
               source = new FloodRable8Bit(filterRegion, paint);
            }
            break;
         case 13:
            if ("SourceGraphic".equals(s)) {
               source = srcG;
            }
            break;
         case 15:
            BackgroundRable8Bit source;
            if (s.charAt(10) == "BackgroundImage".charAt(10)) {
               if ("BackgroundImage".equals(s)) {
                  source = new BackgroundRable8Bit(filteredNode);
                  source = new PadRable8Bit(source, filterRegion, PadMode.ZERO_PAD);
               }
            } else if ("BackgroundAlpha".equals(s)) {
               source = new BackgroundRable8Bit(filteredNode);
               Filter source = new FilterAlphaRable(source);
               source = new PadRable8Bit(source, filterRegion, PadMode.ZERO_PAD);
            }
      }

      if (source == null) {
         source = (Filter)filterMap.get(s);
      }

      return (Filter)source;
   }

   protected static int convertInteger(Element filterElement, String attrName, int defaultValue, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, attrName);
      if (s.length() == 0) {
         return defaultValue;
      } else {
         try {
            return SVGUtilities.convertSVGInteger(s);
         } catch (NumberFormatException var6) {
            throw new BridgeException(ctx, filterElement, var6, "attribute.malformed", new Object[]{attrName, s});
         }
      }
   }

   protected static float convertNumber(Element filterElement, String attrName, float defaultValue, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, attrName);
      if (s.length() == 0) {
         return defaultValue;
      } else {
         try {
            return SVGUtilities.convertSVGNumber(s);
         } catch (NumberFormatException var6) {
            throw new BridgeException(ctx, filterElement, var6, "attribute.malformed", new Object[]{attrName, s, var6});
         }
      }
   }
}
