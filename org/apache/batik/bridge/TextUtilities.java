package org.apache.batik.bridge;

import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.util.CSSConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class TextUtilities implements CSSConstants, ErrorConstants {
   public static String getElementContent(Element e) {
      StringBuffer result = new StringBuffer();

      for(Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
         switch (n.getNodeType()) {
            case 1:
               result.append(getElementContent((Element)n));
            case 2:
            default:
               break;
            case 3:
            case 4:
               result.append(n.getNodeValue());
         }
      }

      return result.toString();
   }

   public static ArrayList svgHorizontalCoordinateArrayToUserSpace(Element element, String attrName, String valueStr, BridgeContext ctx) {
      org.apache.batik.parser.UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, element);
      ArrayList values = new ArrayList();
      StringTokenizer st = new StringTokenizer(valueStr, ", ", false);

      while(st.hasMoreTokens()) {
         values.add(UnitProcessor.svgHorizontalCoordinateToUserSpace(st.nextToken(), attrName, uctx));
      }

      return values;
   }

   public static ArrayList svgVerticalCoordinateArrayToUserSpace(Element element, String attrName, String valueStr, BridgeContext ctx) {
      org.apache.batik.parser.UnitProcessor.Context uctx = UnitProcessor.createContext(ctx, element);
      ArrayList values = new ArrayList();
      StringTokenizer st = new StringTokenizer(valueStr, ", ", false);

      while(st.hasMoreTokens()) {
         values.add(UnitProcessor.svgVerticalCoordinateToUserSpace(st.nextToken(), attrName, uctx));
      }

      return values;
   }

   public static ArrayList svgRotateArrayToFloats(Element element, String attrName, String valueStr, BridgeContext ctx) {
      StringTokenizer st = new StringTokenizer(valueStr, ", ", false);
      ArrayList values = new ArrayList();

      while(st.hasMoreTokens()) {
         try {
            String s = st.nextToken();
            values.add((float)Math.toRadians((double)SVGUtilities.convertSVGNumber(s)));
         } catch (NumberFormatException var8) {
            throw new BridgeException(ctx, element, var8, "attribute.malformed", new Object[]{attrName, valueStr});
         }
      }

      return values;
   }

   public static Float convertFontSize(Element e) {
      Value v = CSSUtilities.getComputedStyle(e, 22);
      return v.getFloatValue();
   }

   public static Float convertFontStyle(Element e) {
      Value v = CSSUtilities.getComputedStyle(e, 25);
      switch (v.getStringValue().charAt(0)) {
         case 'n':
            return TextAttribute.POSTURE_REGULAR;
         default:
            return TextAttribute.POSTURE_OBLIQUE;
      }
   }

   public static Float convertFontStretch(Element e) {
      Value v = CSSUtilities.getComputedStyle(e, 24);
      String s = v.getStringValue();
      switch (s.charAt(0)) {
         case 'e':
            if (s.charAt(6) == 'c') {
               return TextAttribute.WIDTH_CONDENSED;
            } else {
               if (s.length() == 8) {
                  return TextAttribute.WIDTH_SEMI_EXTENDED;
               }

               return TextAttribute.WIDTH_EXTENDED;
            }
         case 's':
            if (s.charAt(6) == 'c') {
               return TextAttribute.WIDTH_SEMI_CONDENSED;
            }

            return TextAttribute.WIDTH_SEMI_EXTENDED;
         case 'u':
            if (s.charAt(6) == 'c') {
               return TextAttribute.WIDTH_CONDENSED;
            }

            return TextAttribute.WIDTH_EXTENDED;
         default:
            return TextAttribute.WIDTH_REGULAR;
      }
   }

   public static Float convertFontWeight(Element e) {
      Value v = CSSUtilities.getComputedStyle(e, 27);
      int weight = (int)v.getFloatValue();
      switch (weight) {
         case 100:
            return TextAttribute.WEIGHT_EXTRA_LIGHT;
         case 200:
            return TextAttribute.WEIGHT_LIGHT;
         case 300:
            return TextAttribute.WEIGHT_DEMILIGHT;
         case 400:
            return TextAttribute.WEIGHT_REGULAR;
         case 500:
            return TextAttribute.WEIGHT_SEMIBOLD;
         default:
            String javaVersionString = System.getProperty("java.specification.version");
            float javaVersion = javaVersionString != null ? Float.parseFloat(javaVersionString) : 1.5F;
            if ((double)javaVersion < 1.5) {
               return TextAttribute.WEIGHT_BOLD;
            } else {
               switch (weight) {
                  case 600:
                     return TextAttribute.WEIGHT_MEDIUM;
                  case 700:
                     return TextAttribute.WEIGHT_BOLD;
                  case 800:
                     return TextAttribute.WEIGHT_HEAVY;
                  case 900:
                     return TextAttribute.WEIGHT_ULTRABOLD;
                  default:
                     return TextAttribute.WEIGHT_REGULAR;
               }
            }
      }
   }

   public static TextNode.Anchor convertTextAnchor(Element e) {
      Value v = CSSUtilities.getComputedStyle(e, 53);
      switch (v.getStringValue().charAt(0)) {
         case 'm':
            return TextNode.Anchor.MIDDLE;
         case 's':
            return TextNode.Anchor.START;
         default:
            return TextNode.Anchor.END;
      }
   }

   public static Object convertBaselineShift(Element e) {
      Value v = CSSUtilities.getComputedStyle(e, 1);
      if (v.getPrimitiveType() == 21) {
         String s = v.getStringValue();
         switch (s.charAt(2)) {
            case 'b':
               return TextAttribute.SUPERSCRIPT_SUB;
            case 'p':
               return TextAttribute.SUPERSCRIPT_SUPER;
            default:
               return null;
         }
      } else {
         return v.getFloatValue();
      }
   }

   public static Float convertKerning(Element e) {
      Value v = CSSUtilities.getComputedStyle(e, 31);
      return v.getPrimitiveType() == 21 ? null : v.getFloatValue();
   }

   public static Float convertLetterSpacing(Element e) {
      Value v = CSSUtilities.getComputedStyle(e, 32);
      return v.getPrimitiveType() == 21 ? null : v.getFloatValue();
   }

   public static Float convertWordSpacing(Element e) {
      Value v = CSSUtilities.getComputedStyle(e, 58);
      return v.getPrimitiveType() == 21 ? null : v.getFloatValue();
   }
}
