package org.apache.batik.bridge;

import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.TurbulenceRable;
import org.apache.batik.ext.awt.image.renderable.TurbulenceRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;

public class SVGFeTurbulenceElementBridge extends AbstractSVGFilterPrimitiveElementBridge {
   public String getLocalName() {
      return "feTurbulence";
   }

   public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Rectangle2D filterRegion, Map filterMap) {
      Filter in = getIn(filterElement, filteredElement, filteredNode, inputFilter, filterMap, ctx);
      if (in == null) {
         return null;
      } else {
         Rectangle2D primitiveRegion = SVGUtilities.convertFilterPrimitiveRegion(filterElement, filteredElement, filteredNode, filterRegion, filterRegion, ctx);
         float[] baseFrequency = convertBaseFrenquency(filterElement, ctx);
         int numOctaves = convertInteger(filterElement, "numOctaves", 1, ctx);
         int seed = convertInteger(filterElement, "seed", 0, ctx);
         boolean stitchTiles = convertStitchTiles(filterElement, ctx);
         boolean isFractalNoise = convertType(filterElement, ctx);
         TurbulenceRable turbulenceRable = new TurbulenceRable8Bit(primitiveRegion);
         turbulenceRable.setBaseFrequencyX((double)baseFrequency[0]);
         turbulenceRable.setBaseFrequencyY((double)baseFrequency[1]);
         turbulenceRable.setNumOctaves(numOctaves);
         turbulenceRable.setSeed(seed);
         turbulenceRable.setStitched(stitchTiles);
         turbulenceRable.setFractalNoise(isFractalNoise);
         handleColorInterpolationFilters(turbulenceRable, filterElement);
         updateFilterMap(filterElement, turbulenceRable, filterMap);
         return turbulenceRable;
      }
   }

   protected static float[] convertBaseFrenquency(Element e, BridgeContext ctx) {
      String s = e.getAttributeNS((String)null, "baseFrequency");
      if (s.length() == 0) {
         return new float[]{0.001F, 0.001F};
      } else {
         float[] v = new float[2];
         StringTokenizer tokens = new StringTokenizer(s, " ,");

         try {
            v[0] = SVGUtilities.convertSVGNumber(tokens.nextToken());
            if (tokens.hasMoreTokens()) {
               v[1] = SVGUtilities.convertSVGNumber(tokens.nextToken());
            } else {
               v[1] = v[0];
            }

            if (tokens.hasMoreTokens()) {
               throw new BridgeException(ctx, e, "attribute.malformed", new Object[]{"baseFrequency", s});
            }
         } catch (NumberFormatException var6) {
            throw new BridgeException(ctx, e, var6, "attribute.malformed", new Object[]{"baseFrequency", s});
         }

         if (!(v[0] < 0.0F) && !(v[1] < 0.0F)) {
            return v;
         } else {
            throw new BridgeException(ctx, e, "attribute.malformed", new Object[]{"baseFrequency", s});
         }
      }
   }

   protected static boolean convertStitchTiles(Element e, BridgeContext ctx) {
      String s = e.getAttributeNS((String)null, "stitchTiles");
      if (s.length() == 0) {
         return false;
      } else if ("stitch".equals(s)) {
         return true;
      } else if ("noStitch".equals(s)) {
         return false;
      } else {
         throw new BridgeException(ctx, e, "attribute.malformed", new Object[]{"stitchTiles", s});
      }
   }

   protected static boolean convertType(Element e, BridgeContext ctx) {
      String s = e.getAttributeNS((String)null, "type");
      if (s.length() == 0) {
         return false;
      } else if ("fractalNoise".equals(s)) {
         return true;
      } else if ("turbulence".equals(s)) {
         return false;
      } else {
         throw new BridgeException(ctx, e, "attribute.malformed", new Object[]{"type", s});
      }
   }
}
