package org.apache.batik.bridge;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.color.ICC_Profile;
import java.io.IOException;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.ICCColor;
import org.apache.batik.css.engine.value.svg12.CIELabColor;
import org.apache.batik.css.engine.value.svg12.DeviceColor;
import org.apache.batik.css.engine.value.svg12.ICCNamedColor;
import org.apache.batik.gvt.CompositeShapePainter;
import org.apache.batik.gvt.FillShapePainter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.Marker;
import org.apache.batik.gvt.MarkerShapePainter;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.gvt.ShapePainter;
import org.apache.batik.gvt.StrokeShapePainter;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGConstants;
import org.apache.xmlgraphics.java2d.color.CIELabColorSpace;
import org.apache.xmlgraphics.java2d.color.ColorSpaces;
import org.apache.xmlgraphics.java2d.color.ColorWithAlternatives;
import org.apache.xmlgraphics.java2d.color.DeviceCMYKColorSpace;
import org.apache.xmlgraphics.java2d.color.ICCColorSpaceWithIntent;
import org.apache.xmlgraphics.java2d.color.NamedColorSpace;
import org.apache.xmlgraphics.java2d.color.profile.NamedColorProfile;
import org.apache.xmlgraphics.java2d.color.profile.NamedColorProfileParser;
import org.w3c.dom.Element;

public abstract class PaintServer implements SVGConstants, CSSConstants, ErrorConstants {
   protected PaintServer() {
   }

   public static ShapePainter convertMarkers(Element e, ShapeNode node, BridgeContext ctx) {
      Value v = CSSUtilities.getComputedStyle(e, 36);
      Marker startMarker = convertMarker(e, v, ctx);
      v = CSSUtilities.getComputedStyle(e, 35);
      Marker midMarker = convertMarker(e, v, ctx);
      v = CSSUtilities.getComputedStyle(e, 34);
      Marker endMarker = convertMarker(e, v, ctx);
      if (startMarker == null && midMarker == null && endMarker == null) {
         return null;
      } else {
         MarkerShapePainter p = new MarkerShapePainter(node.getShape());
         p.setStartMarker(startMarker);
         p.setMiddleMarker(midMarker);
         p.setEndMarker(endMarker);
         return p;
      }
   }

   public static Marker convertMarker(Element e, Value v, BridgeContext ctx) {
      if (v.getPrimitiveType() == 21) {
         return null;
      } else {
         String uri = v.getStringValue();
         Element markerElement = ctx.getReferencedElement(e, uri);
         Bridge bridge = ctx.getBridge(markerElement);
         if (bridge != null && bridge instanceof MarkerBridge) {
            return ((MarkerBridge)bridge).createMarker(ctx, markerElement, e);
         } else {
            throw new BridgeException(ctx, e, "css.uri.badTarget", new Object[]{uri});
         }
      }
   }

   public static ShapePainter convertFillAndStroke(Element e, ShapeNode node, BridgeContext ctx) {
      Shape shape = node.getShape();
      if (shape == null) {
         return null;
      } else {
         Paint fillPaint = convertFillPaint(e, node, ctx);
         FillShapePainter fp = new FillShapePainter(shape);
         fp.setPaint(fillPaint);
         Stroke stroke = convertStroke(e);
         if (stroke == null) {
            return fp;
         } else {
            Paint strokePaint = convertStrokePaint(e, node, ctx);
            StrokeShapePainter sp = new StrokeShapePainter(shape);
            sp.setStroke(stroke);
            sp.setPaint(strokePaint);
            CompositeShapePainter cp = new CompositeShapePainter(shape);
            cp.addShapePainter(fp);
            cp.addShapePainter(sp);
            return cp;
         }
      }
   }

   public static ShapePainter convertStrokePainter(Element e, ShapeNode node, BridgeContext ctx) {
      Shape shape = node.getShape();
      if (shape == null) {
         return null;
      } else {
         Stroke stroke = convertStroke(e);
         if (stroke == null) {
            return null;
         } else {
            Paint strokePaint = convertStrokePaint(e, node, ctx);
            StrokeShapePainter sp = new StrokeShapePainter(shape);
            sp.setStroke(stroke);
            sp.setPaint(strokePaint);
            return sp;
         }
      }
   }

   public static Paint convertStrokePaint(Element strokedElement, GraphicsNode strokedNode, BridgeContext ctx) {
      Value v = CSSUtilities.getComputedStyle(strokedElement, 51);
      float opacity = convertOpacity(v);
      v = CSSUtilities.getComputedStyle(strokedElement, 45);
      return convertPaint(strokedElement, strokedNode, v, opacity, ctx);
   }

   public static Paint convertFillPaint(Element filledElement, GraphicsNode filledNode, BridgeContext ctx) {
      Value v = CSSUtilities.getComputedStyle(filledElement, 16);
      float opacity = convertOpacity(v);
      v = CSSUtilities.getComputedStyle(filledElement, 15);
      return convertPaint(filledElement, filledNode, v, opacity, ctx);
   }

   public static Paint convertPaint(Element paintedElement, GraphicsNode paintedNode, Value paintDef, float opacity, BridgeContext ctx) {
      if (paintDef.getCssValueType() == 1) {
         switch (paintDef.getPrimitiveType()) {
            case 20:
               return convertURIPaint(paintedElement, paintedNode, paintDef, opacity, ctx);
            case 21:
               return null;
            case 25:
               return convertColor(paintDef, opacity);
            default:
               throw new IllegalArgumentException("Paint argument is not an appropriate CSS value");
         }
      } else {
         Value v = paintDef.item(0);
         switch (v.getPrimitiveType()) {
            case 20:
               Paint result = silentConvertURIPaint(paintedElement, paintedNode, v, opacity, ctx);
               if (result != null) {
                  return result;
               } else {
                  v = paintDef.item(1);
                  switch (v.getPrimitiveType()) {
                     case 21:
                        return null;
                     case 25:
                        if (paintDef.getLength() == 2) {
                           return convertColor(v, opacity);
                        }

                        return convertRGBICCColor(paintedElement, v, paintDef.item(2), opacity, ctx);
                     default:
                        throw new IllegalArgumentException("Paint argument is not an appropriate CSS value");
                  }
               }
            case 25:
               return convertRGBICCColor(paintedElement, v, paintDef.item(1), opacity, ctx);
            default:
               throw new IllegalArgumentException("Paint argument is not an appropriate CSS value");
         }
      }
   }

   public static Paint silentConvertURIPaint(Element paintedElement, GraphicsNode paintedNode, Value paintDef, float opacity, BridgeContext ctx) {
      Paint paint = null;

      try {
         paint = convertURIPaint(paintedElement, paintedNode, paintDef, opacity, ctx);
      } catch (BridgeException var7) {
      }

      return paint;
   }

   public static Paint convertURIPaint(Element paintedElement, GraphicsNode paintedNode, Value paintDef, float opacity, BridgeContext ctx) {
      String uri = paintDef.getStringValue();
      Element paintElement = ctx.getReferencedElement(paintedElement, uri);
      Bridge bridge = ctx.getBridge(paintElement);
      if (bridge != null && bridge instanceof PaintBridge) {
         return ((PaintBridge)bridge).createPaint(ctx, paintElement, paintedElement, paintedNode, opacity);
      } else {
         throw new BridgeException(ctx, paintedElement, "css.uri.badTarget", new Object[]{uri});
      }
   }

   public static Color convertRGBICCColor(Element paintedElement, Value colorDef, Value iccColor, float opacity, BridgeContext ctx) {
      Color color = null;
      if (iccColor != null) {
         if (iccColor instanceof ICCColor) {
            color = convertICCColor(paintedElement, (ICCColor)iccColor, opacity, ctx);
         } else if (iccColor instanceof ICCNamedColor) {
            color = convertICCNamedColor(paintedElement, (ICCNamedColor)iccColor, opacity, ctx);
         } else if (iccColor instanceof CIELabColor) {
            color = convertCIELabColor(paintedElement, (CIELabColor)iccColor, opacity, ctx);
         } else if (iccColor instanceof DeviceColor) {
            color = convertDeviceColor(paintedElement, colorDef, (DeviceColor)iccColor, opacity, ctx);
         }
      }

      if (color == null) {
         color = convertColor(colorDef, opacity);
      }

      return color;
   }

   public static Color convertICCColor(Element e, ICCColor c, float opacity, BridgeContext ctx) {
      String iccProfileName = c.getColorProfile();
      if (iccProfileName == null) {
         return null;
      } else {
         SVGColorProfileElementBridge profileBridge = (SVGColorProfileElementBridge)ctx.getBridge("http://www.w3.org/2000/svg", "color-profile");
         if (profileBridge == null) {
            return null;
         } else {
            ICCColorSpaceWithIntent profileCS = profileBridge.createICCColorSpaceWithIntent(ctx, e, iccProfileName);
            if (profileCS == null) {
               return null;
            } else {
               int n = c.getNumberOfColors();
               float[] colorValue = new float[n];
               if (n == 0) {
                  return null;
               } else {
                  for(int i = 0; i < n; ++i) {
                     colorValue[i] = c.getColor(i);
                  }

                  float[] rgb = profileCS.intendedToRGB(colorValue);
                  return new Color(rgb[0], rgb[1], rgb[2], opacity);
               }
            }
         }
      }
   }

   public static Color convertICCNamedColor(Element e, ICCNamedColor c, float opacity, BridgeContext ctx) {
      String iccProfileName = c.getColorProfile();
      if (iccProfileName == null) {
         return null;
      } else {
         SVGColorProfileElementBridge profileBridge = (SVGColorProfileElementBridge)ctx.getBridge("http://www.w3.org/2000/svg", "color-profile");
         if (profileBridge == null) {
            return null;
         } else {
            ICCColorSpaceWithIntent profileCS = profileBridge.createICCColorSpaceWithIntent(ctx, e, iccProfileName);
            if (profileCS == null) {
               return null;
            } else {
               ICC_Profile iccProfile = profileCS.getProfile();
               String iccProfileSrc = null;
               if (NamedColorProfileParser.isNamedColorProfile(iccProfile)) {
                  NamedColorProfileParser parser = new NamedColorProfileParser();

                  NamedColorProfile ncp;
                  try {
                     ncp = parser.parseProfile(iccProfile, iccProfileName, (String)iccProfileSrc);
                  } catch (IOException var13) {
                     return null;
                  }

                  NamedColorSpace ncs = ncp.getNamedColor(c.getColorName());
                  if (ncs != null) {
                     Color specColor = new ColorWithAlternatives(ncs, new float[]{1.0F}, opacity, (Color[])null);
                     return specColor;
                  }
               }

               return null;
            }
         }
      }
   }

   public static Color convertCIELabColor(Element e, CIELabColor c, float opacity, BridgeContext ctx) {
      CIELabColorSpace cs = new CIELabColorSpace(c.getWhitePoint());
      float[] lab = c.getColorValues();
      Color specColor = cs.toColor(lab[0], lab[1], lab[2], opacity);
      return specColor;
   }

   public static Color convertDeviceColor(Element e, Value srgb, DeviceColor c, float opacity, BridgeContext ctx) {
      int r = resolveColorComponent(srgb.getRed());
      int g = resolveColorComponent(srgb.getGreen());
      int b = resolveColorComponent(srgb.getBlue());
      if (c.isNChannel()) {
         return convertColor(srgb, opacity);
      } else if (c.getNumberOfColors() != 4) {
         return convertColor(srgb, opacity);
      } else {
         DeviceCMYKColorSpace cmykCs = ColorSpaces.getDeviceCMYKColorSpace();
         float[] comps = new float[4];

         for(int i = 0; i < 4; ++i) {
            comps[i] = c.getColor(i);
         }

         Color cmyk = new ColorWithAlternatives(cmykCs, comps, opacity, (Color[])null);
         Color specColor = new ColorWithAlternatives(r, g, b, Math.round(opacity * 255.0F), new Color[]{cmyk});
         return specColor;
      }
   }

   public static Color convertColor(Value c, float opacity) {
      int r = resolveColorComponent(c.getRed());
      int g = resolveColorComponent(c.getGreen());
      int b = resolveColorComponent(c.getBlue());
      return new Color(r, g, b, Math.round(opacity * 255.0F));
   }

   public static Stroke convertStroke(Element e) {
      Value v = CSSUtilities.getComputedStyle(e, 52);
      float width = v.getFloatValue();
      if (width == 0.0F) {
         return null;
      } else {
         v = CSSUtilities.getComputedStyle(e, 48);
         int linecap = convertStrokeLinecap(v);
         v = CSSUtilities.getComputedStyle(e, 49);
         int linejoin = convertStrokeLinejoin(v);
         v = CSSUtilities.getComputedStyle(e, 50);
         float miterlimit = convertStrokeMiterlimit(v);
         v = CSSUtilities.getComputedStyle(e, 46);
         float[] dasharray = convertStrokeDasharray(v);
         float dashoffset = 0.0F;
         if (dasharray != null) {
            v = CSSUtilities.getComputedStyle(e, 47);
            dashoffset = v.getFloatValue();
            if (dashoffset < 0.0F) {
               float dashpatternlength = 0.0F;
               float[] var9 = dasharray;
               int var10 = dasharray.length;

               for(int var11 = 0; var11 < var10; ++var11) {
                  float aDasharray = var9[var11];
                  dashpatternlength += aDasharray;
               }

               if (dasharray.length % 2 != 0) {
                  dashpatternlength *= 2.0F;
               }

               if (dashpatternlength == 0.0F) {
                  dashoffset = 0.0F;
               } else {
                  while(dashoffset < 0.0F) {
                     dashoffset += dashpatternlength;
                  }
               }
            }
         }

         return new BasicStroke(width, linecap, linejoin, miterlimit, dasharray, dashoffset);
      }
   }

   public static float[] convertStrokeDasharray(Value v) {
      float[] dasharray = null;
      if (v.getCssValueType() == 2) {
         int length = v.getLength();
         dasharray = new float[length];
         float sum = 0.0F;

         for(int i = 0; i < dasharray.length; ++i) {
            dasharray[i] = v.item(i).getFloatValue();
            sum += dasharray[i];
         }

         if (sum == 0.0F) {
            dasharray = null;
         }
      }

      return dasharray;
   }

   public static float convertStrokeMiterlimit(Value v) {
      float miterlimit = v.getFloatValue();
      return miterlimit < 1.0F ? 1.0F : miterlimit;
   }

   public static int convertStrokeLinecap(Value v) {
      String s = v.getStringValue();
      switch (s.charAt(0)) {
         case 'b':
            return 0;
         case 'r':
            return 1;
         case 's':
            return 2;
         default:
            throw new IllegalArgumentException("Linecap argument is not an appropriate CSS value");
      }
   }

   public static int convertStrokeLinejoin(Value v) {
      String s = v.getStringValue();
      switch (s.charAt(0)) {
         case 'b':
            return 2;
         case 'm':
            return 0;
         case 'r':
            return 1;
         default:
            throw new IllegalArgumentException("Linejoin argument is not an appropriate CSS value");
      }
   }

   public static int resolveColorComponent(Value v) {
      float f;
      switch (v.getPrimitiveType()) {
         case 1:
            f = v.getFloatValue();
            f = f > 255.0F ? 255.0F : (f < 0.0F ? 0.0F : f);
            return Math.round(f);
         case 2:
            f = v.getFloatValue();
            f = f > 100.0F ? 100.0F : (f < 0.0F ? 0.0F : f);
            return Math.round(255.0F * f / 100.0F);
         default:
            throw new IllegalArgumentException("Color component argument is not an appropriate CSS value");
      }
   }

   public static float convertOpacity(Value v) {
      float r = v.getFloatValue();
      return r < 0.0F ? 0.0F : (r > 1.0F ? 1.0F : r);
   }
}
