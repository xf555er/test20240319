package org.apache.batik.bridge;

import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.batik.ext.awt.image.ComponentTransferFunction;
import org.apache.batik.ext.awt.image.ConcreteComponentTransferFunction;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.ComponentTransferRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SVGFeComponentTransferElementBridge extends AbstractSVGFilterPrimitiveElementBridge {
   public String getLocalName() {
      return "feComponentTransfer";
   }

   public Filter createFilter(BridgeContext ctx, Element filterElement, Element filteredElement, GraphicsNode filteredNode, Filter inputFilter, Rectangle2D filterRegion, Map filterMap) {
      Filter in = getIn(filterElement, filteredElement, filteredNode, inputFilter, filterMap, ctx);
      if (in == null) {
         return null;
      } else {
         Rectangle2D defaultRegion = in.getBounds2D();
         Rectangle2D primitiveRegion = SVGUtilities.convertFilterPrimitiveRegion(filterElement, filteredElement, filteredNode, defaultRegion, filterRegion, ctx);
         ComponentTransferFunction funcR = null;
         ComponentTransferFunction funcG = null;
         ComponentTransferFunction funcB = null;
         ComponentTransferFunction funcA = null;

         for(Node n = filterElement.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == 1) {
               Element e = (Element)n;
               Bridge bridge = ctx.getBridge(e);
               if (bridge != null && bridge instanceof SVGFeFuncElementBridge) {
                  SVGFeFuncElementBridge funcBridge = (SVGFeFuncElementBridge)bridge;
                  ComponentTransferFunction func = funcBridge.createComponentTransferFunction(filterElement, e);
                  if (funcBridge instanceof SVGFeFuncRElementBridge) {
                     funcR = func;
                  } else if (funcBridge instanceof SVGFeFuncGElementBridge) {
                     funcG = func;
                  } else if (funcBridge instanceof SVGFeFuncBElementBridge) {
                     funcB = func;
                  } else if (funcBridge instanceof SVGFeFuncAElementBridge) {
                     funcA = func;
                  }
               }
            }
         }

         Filter filter = new ComponentTransferRable8Bit(in, funcA, funcR, funcG, funcB);
         handleColorInterpolationFilters(filter, filterElement);
         Filter filter = new PadRable8Bit(filter, primitiveRegion, PadMode.ZERO_PAD);
         updateFilterMap(filterElement, filter, filterMap);
         return filter;
      }
   }

   protected abstract static class SVGFeFuncElementBridge extends AnimatableGenericSVGBridge {
      public ComponentTransferFunction createComponentTransferFunction(Element filterElement, Element funcElement) {
         int type = convertType(funcElement, this.ctx);
         float amplitude;
         float exponent;
         float[] v;
         switch (type) {
            case 0:
               return ConcreteComponentTransferFunction.getIdentityTransfer();
            case 1:
               v = convertTableValues(funcElement, this.ctx);
               if (v == null) {
                  return ConcreteComponentTransferFunction.getIdentityTransfer();
               }

               return ConcreteComponentTransferFunction.getTableTransfer(v);
            case 2:
               v = convertTableValues(funcElement, this.ctx);
               if (v == null) {
                  return ConcreteComponentTransferFunction.getIdentityTransfer();
               }

               return ConcreteComponentTransferFunction.getDiscreteTransfer(v);
            case 3:
               amplitude = AbstractSVGFilterPrimitiveElementBridge.convertNumber(funcElement, "slope", 1.0F, this.ctx);
               exponent = AbstractSVGFilterPrimitiveElementBridge.convertNumber(funcElement, "intercept", 0.0F, this.ctx);
               return ConcreteComponentTransferFunction.getLinearTransfer(amplitude, exponent);
            case 4:
               amplitude = AbstractSVGFilterPrimitiveElementBridge.convertNumber(funcElement, "amplitude", 1.0F, this.ctx);
               exponent = AbstractSVGFilterPrimitiveElementBridge.convertNumber(funcElement, "exponent", 1.0F, this.ctx);
               float offset = AbstractSVGFilterPrimitiveElementBridge.convertNumber(funcElement, "offset", 0.0F, this.ctx);
               return ConcreteComponentTransferFunction.getGammaTransfer(amplitude, exponent, offset);
            default:
               throw new RuntimeException("invalid convertType:" + type);
         }
      }

      protected static float[] convertTableValues(Element e, BridgeContext ctx) {
         String s = e.getAttributeNS((String)null, "tableValues");
         if (s.length() == 0) {
            return null;
         } else {
            StringTokenizer tokens = new StringTokenizer(s, " ,");
            float[] v = new float[tokens.countTokens()];

            try {
               for(int i = 0; tokens.hasMoreTokens(); ++i) {
                  v[i] = SVGUtilities.convertSVGNumber(tokens.nextToken());
               }

               return v;
            } catch (NumberFormatException var6) {
               throw new BridgeException(ctx, e, var6, "attribute.malformed", new Object[]{"tableValues", s});
            }
         }
      }

      protected static int convertType(Element e, BridgeContext ctx) {
         String s = e.getAttributeNS((String)null, "type");
         if (s.length() == 0) {
            throw new BridgeException(ctx, e, "attribute.missing", new Object[]{"type"});
         } else if ("discrete".equals(s)) {
            return 2;
         } else if ("identity".equals(s)) {
            return 0;
         } else if ("gamma".equals(s)) {
            return 4;
         } else if ("linear".equals(s)) {
            return 3;
         } else if ("table".equals(s)) {
            return 1;
         } else {
            throw new BridgeException(ctx, e, "attribute.malformed", new Object[]{"type", s});
         }
      }
   }

   public static class SVGFeFuncBElementBridge extends SVGFeFuncElementBridge {
      public String getLocalName() {
         return "feFuncB";
      }
   }

   public static class SVGFeFuncGElementBridge extends SVGFeFuncElementBridge {
      public String getLocalName() {
         return "feFuncG";
      }
   }

   public static class SVGFeFuncRElementBridge extends SVGFeFuncElementBridge {
      public String getLocalName() {
         return "feFuncR";
      }
   }

   public static class SVGFeFuncAElementBridge extends SVGFeFuncElementBridge {
      public String getLocalName() {
         return "feFuncA";
      }
   }
}
