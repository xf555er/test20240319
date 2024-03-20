package org.apache.batik.bridge;

import java.awt.Color;
import java.util.StringTokenizer;
import org.apache.batik.ext.awt.image.DistantLight;
import org.apache.batik.ext.awt.image.Light;
import org.apache.batik.ext.awt.image.PointLight;
import org.apache.batik.ext.awt.image.SpotLight;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class AbstractSVGLightingElementBridge extends AbstractSVGFilterPrimitiveElementBridge {
   protected AbstractSVGLightingElementBridge() {
   }

   protected static Light extractLight(Element filterElement, BridgeContext ctx) {
      Color color = CSSUtilities.convertLightingColor(filterElement, ctx);

      for(Node n = filterElement.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1) {
            Element e = (Element)n;
            Bridge bridge = ctx.getBridge(e);
            if (bridge != null && bridge instanceof AbstractSVGLightElementBridge) {
               return ((AbstractSVGLightElementBridge)bridge).createLight(ctx, filterElement, e, color);
            }
         }
      }

      return null;
   }

   protected static double[] convertKernelUnitLength(Element filterElement, BridgeContext ctx) {
      String s = filterElement.getAttributeNS((String)null, "kernelUnitLength");
      if (s.length() == 0) {
         return null;
      } else {
         double[] units = new double[2];
         StringTokenizer tokens = new StringTokenizer(s, " ,");

         try {
            units[0] = (double)SVGUtilities.convertSVGNumber(tokens.nextToken());
            if (tokens.hasMoreTokens()) {
               units[1] = (double)SVGUtilities.convertSVGNumber(tokens.nextToken());
            } else {
               units[1] = units[0];
            }
         } catch (NumberFormatException var6) {
            throw new BridgeException(ctx, filterElement, var6, "attribute.malformed", new Object[]{"kernelUnitLength", s});
         }

         if (!tokens.hasMoreTokens() && !(units[0] <= 0.0) && !(units[1] <= 0.0)) {
            return units;
         } else {
            throw new BridgeException(ctx, filterElement, "attribute.malformed", new Object[]{"kernelUnitLength", s});
         }
      }
   }

   public static class SVGFePointLightElementBridge extends AbstractSVGLightElementBridge {
      public String getLocalName() {
         return "fePointLight";
      }

      public Light createLight(BridgeContext ctx, Element filterElement, Element lightElement, Color color) {
         double x = (double)AbstractSVGFilterPrimitiveElementBridge.convertNumber(lightElement, "x", 0.0F, ctx);
         double y = (double)AbstractSVGFilterPrimitiveElementBridge.convertNumber(lightElement, "y", 0.0F, ctx);
         double z = (double)AbstractSVGFilterPrimitiveElementBridge.convertNumber(lightElement, "z", 0.0F, ctx);
         return new PointLight(x, y, z, color);
      }
   }

   public static class SVGFeDistantLightElementBridge extends AbstractSVGLightElementBridge {
      public String getLocalName() {
         return "feDistantLight";
      }

      public Light createLight(BridgeContext ctx, Element filterElement, Element lightElement, Color color) {
         double azimuth = (double)AbstractSVGFilterPrimitiveElementBridge.convertNumber(lightElement, "azimuth", 0.0F, ctx);
         double elevation = (double)AbstractSVGFilterPrimitiveElementBridge.convertNumber(lightElement, "elevation", 0.0F, ctx);
         return new DistantLight(azimuth, elevation, color);
      }
   }

   public static class SVGFeSpotLightElementBridge extends AbstractSVGLightElementBridge {
      public String getLocalName() {
         return "feSpotLight";
      }

      public Light createLight(BridgeContext ctx, Element filterElement, Element lightElement, Color color) {
         double x = (double)AbstractSVGFilterPrimitiveElementBridge.convertNumber(lightElement, "x", 0.0F, ctx);
         double y = (double)AbstractSVGFilterPrimitiveElementBridge.convertNumber(lightElement, "y", 0.0F, ctx);
         double z = (double)AbstractSVGFilterPrimitiveElementBridge.convertNumber(lightElement, "z", 0.0F, ctx);
         double px = (double)AbstractSVGFilterPrimitiveElementBridge.convertNumber(lightElement, "pointsAtX", 0.0F, ctx);
         double py = (double)AbstractSVGFilterPrimitiveElementBridge.convertNumber(lightElement, "pointsAtY", 0.0F, ctx);
         double pz = (double)AbstractSVGFilterPrimitiveElementBridge.convertNumber(lightElement, "pointsAtZ", 0.0F, ctx);
         double specularExponent = (double)AbstractSVGFilterPrimitiveElementBridge.convertNumber(lightElement, "specularExponent", 1.0F, ctx);
         double limitingConeAngle = (double)AbstractSVGFilterPrimitiveElementBridge.convertNumber(lightElement, "limitingConeAngle", 90.0F, ctx);
         return new SpotLight(x, y, z, px, py, pz, specularExponent, limitingConeAngle, color);
      }
   }

   protected abstract static class AbstractSVGLightElementBridge extends AnimatableGenericSVGBridge {
      public abstract Light createLight(BridgeContext var1, Element var2, Element var3, Color var4);
   }
}
