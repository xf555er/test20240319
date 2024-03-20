package org.apache.batik.bridge;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.constants.XMLConstants;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.image.renderable.ClipRable;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.filter.Mask;
import org.apache.batik.util.CSSConstants;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public abstract class CSSUtilities implements CSSConstants, ErrorConstants, XMLConstants {
   public static final Composite TRANSPARENT = AlphaComposite.getInstance(3, 0.0F);

   protected CSSUtilities() {
   }

   public static CSSEngine getCSSEngine(Element e) {
      return ((SVGOMDocument)e.getOwnerDocument()).getCSSEngine();
   }

   public static Value getComputedStyle(Element e, int property) {
      CSSEngine engine = getCSSEngine(e);
      return engine == null ? null : engine.getComputedStyle((CSSStylableElement)e, (String)null, property);
   }

   public static int convertPointerEvents(Element e) {
      Value v = getComputedStyle(e, 40);
      String s = v.getStringValue();
      switch (s.charAt(0)) {
         case 'a':
            return 7;
         case 'f':
            return 5;
         case 'n':
            return 8;
         case 'p':
            return 4;
         case 's':
            return 6;
         case 'v':
            if (s.length() == 7) {
               return 3;
            } else {
               switch (s.charAt(7)) {
                  case 'f':
                     return 1;
                  case 'p':
                     return 0;
                  case 's':
                     return 2;
                  default:
                     throw new IllegalStateException("unexpected event, must be one of (p,f,s) is:" + s.charAt(7));
               }
            }
         default:
            throw new IllegalStateException("unexpected event, must be one of (v,p,f,s,a,n) is:" + s.charAt(0));
      }
   }

   public static Rectangle2D convertEnableBackground(Element e) {
      Value v = getComputedStyle(e, 14);
      if (v.getCssValueType() != 2) {
         return null;
      } else {
         ListValue lv = (ListValue)v;
         int length = lv.getLength();
         switch (length) {
            case 1:
               return CompositeGraphicsNode.VIEWPORT;
            case 5:
               float x = lv.item(1).getFloatValue();
               float y = lv.item(2).getFloatValue();
               float w = lv.item(3).getFloatValue();
               float h = lv.item(4).getFloatValue();
               return new Rectangle2D.Float(x, y, w, h);
            default:
               throw new IllegalStateException("Unexpected length:" + length);
         }
      }
   }

   public static boolean convertColorInterpolationFilters(Element e) {
      Value v = getComputedStyle(e, 7);
      return "linearrgb" == v.getStringValue();
   }

   public static MultipleGradientPaint.ColorSpaceEnum convertColorInterpolation(Element e) {
      Value v = getComputedStyle(e, 6);
      return "linearrgb" == v.getStringValue() ? MultipleGradientPaint.LINEAR_RGB : MultipleGradientPaint.SRGB;
   }

   public static boolean isAutoCursor(Element e) {
      Value cursorValue = getComputedStyle(e, 10);
      boolean isAuto = false;
      if (cursorValue != null) {
         if (cursorValue.getCssValueType() == 1 && cursorValue.getPrimitiveType() == 21 && cursorValue.getStringValue().charAt(0) == 'a') {
            isAuto = true;
         } else if (cursorValue.getCssValueType() == 2 && cursorValue.getLength() == 1) {
            Value lValue = cursorValue.item(0);
            if (lValue != null && lValue.getCssValueType() == 1 && lValue.getPrimitiveType() == 21 && lValue.getStringValue().charAt(0) == 'a') {
               isAuto = true;
            }
         }
      }

      return isAuto;
   }

   public static Cursor convertCursor(Element e, BridgeContext ctx) {
      return ctx.getCursorManager().convertCursor(e);
   }

   public static RenderingHints convertShapeRendering(Element e, RenderingHints hints) {
      Value v = getComputedStyle(e, 42);
      String s = v.getStringValue();
      int len = s.length();
      if (len == 4 && s.charAt(0) == 'a') {
         return hints;
      } else if (len < 10) {
         return hints;
      } else {
         if (hints == null) {
            hints = new RenderingHints((Map)null);
         }

         switch (s.charAt(0)) {
            case 'c':
               hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
               hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
               break;
            case 'g':
               hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
               hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
               hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
               break;
            case 'o':
               hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
               hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
         }

         return hints;
      }
   }

   public static RenderingHints convertTextRendering(Element e, RenderingHints hints) {
      Value v = getComputedStyle(e, 55);
      String s = v.getStringValue();
      int len = s.length();
      if (len == 4 && s.charAt(0) == 'a') {
         return hints;
      } else if (len < 13) {
         return hints;
      } else {
         if (hints == null) {
            hints = new RenderingHints((Map)null);
         }

         switch (s.charAt(8)) {
            case 'c':
               hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
               hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
               hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
               hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
               hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
               break;
            case 'l':
               hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
               hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
               hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
               break;
            case 's':
               hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
               hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
               hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
         }

         return hints;
      }
   }

   public static RenderingHints convertImageRendering(Element e, RenderingHints hints) {
      Value v = getComputedStyle(e, 30);
      String s = v.getStringValue();
      int len = s.length();
      if (len == 4 && s.charAt(0) == 'a') {
         return hints;
      } else if (len < 13) {
         return hints;
      } else {
         if (hints == null) {
            hints = new RenderingHints((Map)null);
         }

         switch (s.charAt(8)) {
            case 'q':
               hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
               hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
               break;
            case 's':
               hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
               hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
         }

         return hints;
      }
   }

   public static RenderingHints convertColorRendering(Element e, RenderingHints hints) {
      Value v = getComputedStyle(e, 9);
      String s = v.getStringValue();
      int len = s.length();
      if (len == 4 && s.charAt(0) == 'a') {
         return hints;
      } else if (len < 13) {
         return hints;
      } else {
         if (hints == null) {
            hints = new RenderingHints((Map)null);
         }

         switch (s.charAt(8)) {
            case 'q':
               hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
               hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
               break;
            case 's':
               hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
               hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
         }

         return hints;
      }
   }

   public static boolean convertDisplay(Element e) {
      if (!(e instanceof CSSStylableElement)) {
         return true;
      } else {
         Value v = getComputedStyle(e, 12);
         return v.getStringValue().charAt(0) != 'n';
      }
   }

   public static boolean convertVisibility(Element e) {
      Value v = getComputedStyle(e, 57);
      return v.getStringValue().charAt(0) == 'v';
   }

   public static Composite convertOpacity(Element e) {
      Value v = getComputedStyle(e, 38);
      float f = v.getFloatValue();
      if (f <= 0.0F) {
         return TRANSPARENT;
      } else {
         return f >= 1.0F ? AlphaComposite.SrcOver : AlphaComposite.getInstance(3, f);
      }
   }

   public static boolean convertOverflow(Element e) {
      Value v = getComputedStyle(e, 39);
      String s = v.getStringValue();
      return s.charAt(0) == 'h' || s.charAt(0) == 's';
   }

   public static float[] convertClip(Element e) {
      Value v = getComputedStyle(e, 2);
      int primitiveType = v.getPrimitiveType();
      switch (primitiveType) {
         case 21:
            return null;
         case 24:
            float[] off = new float[]{v.getTop().getFloatValue(), v.getRight().getFloatValue(), v.getBottom().getFloatValue(), v.getLeft().getFloatValue()};
            return off;
         default:
            throw new IllegalStateException("Unexpected primitiveType:" + primitiveType);
      }
   }

   public static Filter convertFilter(Element filteredElement, GraphicsNode filteredNode, BridgeContext ctx) {
      Value v = getComputedStyle(filteredElement, 18);
      int primitiveType = v.getPrimitiveType();
      switch (primitiveType) {
         case 20:
            String uri = v.getStringValue();
            Element filter = ctx.getReferencedElement(filteredElement, uri);
            Bridge bridge = ctx.getBridge(filter);
            if (bridge != null && bridge instanceof FilterBridge) {
               return ((FilterBridge)bridge).createFilter(ctx, filter, filteredElement, filteredNode);
            }

            throw new BridgeException(ctx, filteredElement, "css.uri.badTarget", new Object[]{uri});
         case 21:
            return null;
         default:
            throw new IllegalStateException("Unexpected primitive type:" + primitiveType);
      }
   }

   public static ClipRable convertClipPath(Element clippedElement, GraphicsNode clippedNode, BridgeContext ctx) {
      Value v = getComputedStyle(clippedElement, 3);
      int primitiveType = v.getPrimitiveType();
      switch (primitiveType) {
         case 20:
            String uri = v.getStringValue();
            Element cp = ctx.getReferencedElement(clippedElement, uri);
            Bridge bridge = ctx.getBridge(cp);
            if (bridge != null && bridge instanceof ClipBridge) {
               return ((ClipBridge)bridge).createClip(ctx, cp, clippedElement, clippedNode);
            }

            throw new BridgeException(ctx, clippedElement, "css.uri.badTarget", new Object[]{uri});
         case 21:
            return null;
         default:
            throw new IllegalStateException("Unexpected primitive type:" + primitiveType);
      }
   }

   public static int convertClipRule(Element e) {
      Value v = getComputedStyle(e, 4);
      return v.getStringValue().charAt(0) == 'n' ? 1 : 0;
   }

   public static Mask convertMask(Element maskedElement, GraphicsNode maskedNode, BridgeContext ctx) {
      Value v = getComputedStyle(maskedElement, 37);
      int primitiveType = v.getPrimitiveType();
      switch (primitiveType) {
         case 20:
            String uri = v.getStringValue();
            Element m = ctx.getReferencedElement(maskedElement, uri);
            Bridge bridge = ctx.getBridge(m);
            if (bridge != null && bridge instanceof MaskBridge) {
               return ((MaskBridge)bridge).createMask(ctx, m, maskedElement, maskedNode);
            }

            throw new BridgeException(ctx, maskedElement, "css.uri.badTarget", new Object[]{uri});
         case 21:
            return null;
         default:
            throw new IllegalStateException("Unexpected primitive type:" + primitiveType);
      }
   }

   public static int convertFillRule(Element e) {
      Value v = getComputedStyle(e, 17);
      return v.getStringValue().charAt(0) == 'n' ? 1 : 0;
   }

   public static Color convertLightingColor(Element e, BridgeContext ctx) {
      Value v = getComputedStyle(e, 33);
      return v.getCssValueType() == 1 ? PaintServer.convertColor(v, 1.0F) : PaintServer.convertRGBICCColor(e, v.item(0), v.item(1), 1.0F, ctx);
   }

   public static Color convertFloodColor(Element e, BridgeContext ctx) {
      Value v = getComputedStyle(e, 19);
      Value o = getComputedStyle(e, 20);
      float f = PaintServer.convertOpacity(o);
      return v.getCssValueType() == 1 ? PaintServer.convertColor(v, f) : PaintServer.convertRGBICCColor(e, v.item(0), v.item(1), f, ctx);
   }

   public static Color convertStopColor(Element e, float opacity, BridgeContext ctx) {
      Value v = getComputedStyle(e, 43);
      Value o = getComputedStyle(e, 44);
      opacity *= PaintServer.convertOpacity(o);
      return v.getCssValueType() == 1 ? PaintServer.convertColor(v, opacity) : PaintServer.convertRGBICCColor(e, v.item(0), v.item(1), opacity, ctx);
   }

   public static void computeStyleAndURIs(Element refElement, Element localRefElement, String uri) {
      int idx = uri.indexOf(35);
      if (idx != -1) {
         uri = uri.substring(0, idx);
      }

      if (uri.length() != 0) {
         localRefElement.setAttributeNS("http://www.w3.org/XML/1998/namespace", "base", uri);
      }

      CSSEngine engine = getCSSEngine(localRefElement);
      CSSEngine refEngine = getCSSEngine(refElement);
      engine.importCascadedStyleMaps(refElement, refEngine, localRefElement);
   }

   protected static int rule(CSSValue v) {
      return ((CSSPrimitiveValue)v).getStringValue().charAt(0) == 'n' ? 1 : 0;
   }
}
