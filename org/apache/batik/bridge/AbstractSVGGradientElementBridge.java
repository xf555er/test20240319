package org.apache.batik.bridge;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class AbstractSVGGradientElementBridge extends AnimatableGenericSVGBridge implements PaintBridge, ErrorConstants {
   protected AbstractSVGGradientElementBridge() {
   }

   public Paint createPaint(BridgeContext ctx, Element paintElement, Element paintedElement, GraphicsNode paintedNode, float opacity) {
      List stops = extractStop(paintElement, opacity, ctx);
      if (stops == null) {
         return null;
      } else {
         int stopLength = stops.size();
         if (stopLength == 1) {
            return ((Stop)stops.get(0)).color;
         } else {
            float[] offsets = new float[stopLength];
            Color[] colors = new Color[stopLength];
            Iterator iter = stops.iterator();

            for(int i = 0; iter.hasNext(); ++i) {
               Stop stop = (Stop)iter.next();
               offsets[i] = stop.offset;
               colors[i] = stop.color;
            }

            MultipleGradientPaint.CycleMethodEnum spreadMethod = MultipleGradientPaint.NO_CYCLE;
            String s = SVGUtilities.getChainableAttributeNS(paintElement, (String)null, "spreadMethod", ctx);
            if (s.length() != 0) {
               spreadMethod = convertSpreadMethod(paintElement, s, ctx);
            }

            MultipleGradientPaint.ColorSpaceEnum colorSpace = CSSUtilities.convertColorInterpolation(paintElement);
            s = SVGUtilities.getChainableAttributeNS(paintElement, (String)null, "gradientTransform", ctx);
            AffineTransform transform;
            if (s.length() != 0) {
               transform = SVGUtilities.convertTransform(paintElement, "gradientTransform", s, ctx);
            } else {
               transform = new AffineTransform();
            }

            Paint paint = this.buildGradient(paintElement, paintedElement, paintedNode, spreadMethod, colorSpace, transform, colors, offsets, ctx);
            return paint;
         }
      }
   }

   protected abstract Paint buildGradient(Element var1, Element var2, GraphicsNode var3, MultipleGradientPaint.CycleMethodEnum var4, MultipleGradientPaint.ColorSpaceEnum var5, AffineTransform var6, Color[] var7, float[] var8, BridgeContext var9);

   protected static MultipleGradientPaint.CycleMethodEnum convertSpreadMethod(Element paintElement, String s, BridgeContext ctx) {
      if ("repeat".equals(s)) {
         return MultipleGradientPaint.REPEAT;
      } else if ("reflect".equals(s)) {
         return MultipleGradientPaint.REFLECT;
      } else if ("pad".equals(s)) {
         return MultipleGradientPaint.NO_CYCLE;
      } else {
         throw new BridgeException(ctx, paintElement, "attribute.malformed", new Object[]{"spreadMethod", s});
      }
   }

   protected static List extractStop(Element paintElement, float opacity, BridgeContext ctx) {
      List refs = new LinkedList();

      while(true) {
         List stops = extractLocalStop(paintElement, opacity, ctx);
         if (stops != null) {
            return stops;
         }

         String uri = XLinkSupport.getXLinkHref(paintElement);
         if (uri.length() == 0) {
            return null;
         }

         String baseURI = paintElement.getBaseURI();
         ParsedURL purl = new ParsedURL(baseURI, uri);
         if (contains(refs, purl)) {
            throw new BridgeException(ctx, paintElement, "xlink.href.circularDependencies", new Object[]{uri});
         }

         refs.add(purl);
         paintElement = ctx.getReferencedElement(paintElement, uri);
      }
   }

   protected static List extractLocalStop(Element gradientElement, float opacity, BridgeContext ctx) {
      LinkedList stops = null;
      Stop previous = null;

      for(Node n = gradientElement.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1) {
            Element e = (Element)n;
            Bridge bridge = ctx.getBridge(e);
            if (bridge != null && bridge instanceof SVGStopElementBridge) {
               Stop stop = ((SVGStopElementBridge)bridge).createStop(ctx, gradientElement, e, opacity);
               if (stops == null) {
                  stops = new LinkedList();
               }

               if (previous != null && stop.offset < previous.offset) {
                  stop.offset = previous.offset;
               }

               stops.add(stop);
               previous = stop;
            }
         }
      }

      return stops;
   }

   private static boolean contains(List urls, ParsedURL key) {
      Iterator var2 = urls.iterator();

      Object url;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         url = var2.next();
      } while(!key.equals(url));

      return true;
   }

   public static class SVGStopElementBridge extends AnimatableGenericSVGBridge implements Bridge {
      public String getLocalName() {
         return "stop";
      }

      public Stop createStop(BridgeContext ctx, Element gradientElement, Element stopElement, float opacity) {
         String s = stopElement.getAttributeNS((String)null, "offset");
         if (s.length() == 0) {
            throw new BridgeException(ctx, stopElement, "attribute.missing", new Object[]{"offset"});
         } else {
            float offset;
            try {
               offset = SVGUtilities.convertRatio(s);
            } catch (NumberFormatException var8) {
               throw new BridgeException(ctx, stopElement, var8, "attribute.malformed", new Object[]{"offset", s, var8});
            }

            Color color = CSSUtilities.convertStopColor(stopElement, opacity, ctx);
            return new Stop(color, offset);
         }
      }
   }

   public static class Stop {
      public Color color;
      public float offset;

      public Stop(Color color, float offset) {
         this.color = color;
         this.offset = offset;
      }
   }
}
