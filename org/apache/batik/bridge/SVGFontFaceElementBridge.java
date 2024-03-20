package org.apache.batik.bridge;

import java.util.LinkedList;
import java.util.List;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class SVGFontFaceElementBridge extends AbstractSVGBridge implements ErrorConstants {
   public String getLocalName() {
      return "font-face";
   }

   public SVGFontFace createFontFace(BridgeContext ctx, Element fontFaceElement) {
      String familyNames = fontFaceElement.getAttributeNS((String)null, "font-family");
      String unitsPerEmStr = fontFaceElement.getAttributeNS((String)null, "units-per-em");
      if (unitsPerEmStr.length() == 0) {
         unitsPerEmStr = "1000";
      }

      float unitsPerEm;
      try {
         unitsPerEm = SVGUtilities.convertSVGNumber(unitsPerEmStr);
      } catch (NumberFormatException var40) {
         throw new BridgeException(ctx, fontFaceElement, var40, "attribute.malformed", new Object[]{"units-per-em", unitsPerEmStr});
      }

      String fontWeight = fontFaceElement.getAttributeNS((String)null, "font-weight");
      if (fontWeight.length() == 0) {
         fontWeight = "all";
      }

      String fontStyle = fontFaceElement.getAttributeNS((String)null, "font-style");
      if (fontStyle.length() == 0) {
         fontStyle = "all";
      }

      String fontVariant = fontFaceElement.getAttributeNS((String)null, "font-variant");
      if (fontVariant.length() == 0) {
         fontVariant = "normal";
      }

      String fontStretch = fontFaceElement.getAttributeNS((String)null, "font-stretch");
      if (fontStretch.length() == 0) {
         fontStretch = "normal";
      }

      String slopeStr = fontFaceElement.getAttributeNS((String)null, "slope");
      if (slopeStr.length() == 0) {
         slopeStr = "0";
      }

      float slope;
      try {
         slope = SVGUtilities.convertSVGNumber(slopeStr);
      } catch (NumberFormatException var39) {
         throw new BridgeException(ctx, fontFaceElement, var39, "attribute.malformed", new Object[]{"0", slopeStr});
      }

      String panose1 = fontFaceElement.getAttributeNS((String)null, "panose-1");
      if (panose1.length() == 0) {
         panose1 = "0 0 0 0 0 0 0 0 0 0";
      }

      String ascentStr = fontFaceElement.getAttributeNS((String)null, "ascent");
      if (ascentStr.length() == 0) {
         ascentStr = String.valueOf((double)unitsPerEm * 0.8);
      }

      float ascent;
      try {
         ascent = SVGUtilities.convertSVGNumber(ascentStr);
      } catch (NumberFormatException var38) {
         throw new BridgeException(ctx, fontFaceElement, var38, "attribute.malformed", new Object[]{"0", ascentStr});
      }

      String descentStr = fontFaceElement.getAttributeNS((String)null, "descent");
      if (descentStr.length() == 0) {
         descentStr = String.valueOf((double)unitsPerEm * 0.2);
      }

      float descent;
      try {
         descent = SVGUtilities.convertSVGNumber(descentStr);
      } catch (NumberFormatException var37) {
         throw new BridgeException(ctx, fontFaceElement, var37, "attribute.malformed", new Object[]{"0", descentStr});
      }

      String underlinePosStr = fontFaceElement.getAttributeNS((String)null, "underline-position");
      if (underlinePosStr.length() == 0) {
         underlinePosStr = String.valueOf(-3.0F * unitsPerEm / 40.0F);
      }

      float underlinePos;
      try {
         underlinePos = SVGUtilities.convertSVGNumber(underlinePosStr);
      } catch (NumberFormatException var36) {
         throw new BridgeException(ctx, fontFaceElement, var36, "attribute.malformed", new Object[]{"0", underlinePosStr});
      }

      String underlineThicknessStr = fontFaceElement.getAttributeNS((String)null, "underline-thickness");
      if (underlineThicknessStr.length() == 0) {
         underlineThicknessStr = String.valueOf(unitsPerEm / 20.0F);
      }

      float underlineThickness;
      try {
         underlineThickness = SVGUtilities.convertSVGNumber(underlineThicknessStr);
      } catch (NumberFormatException var35) {
         throw new BridgeException(ctx, fontFaceElement, var35, "attribute.malformed", new Object[]{"0", underlineThicknessStr});
      }

      String strikethroughPosStr = fontFaceElement.getAttributeNS((String)null, "strikethrough-position");
      if (strikethroughPosStr.length() == 0) {
         strikethroughPosStr = String.valueOf(3.0F * ascent / 8.0F);
      }

      float strikethroughPos;
      try {
         strikethroughPos = SVGUtilities.convertSVGNumber(strikethroughPosStr);
      } catch (NumberFormatException var34) {
         throw new BridgeException(ctx, fontFaceElement, var34, "attribute.malformed", new Object[]{"0", strikethroughPosStr});
      }

      String strikethroughThicknessStr = fontFaceElement.getAttributeNS((String)null, "strikethrough-thickness");
      if (strikethroughThicknessStr.length() == 0) {
         strikethroughThicknessStr = String.valueOf(unitsPerEm / 20.0F);
      }

      float strikethroughThickness;
      try {
         strikethroughThickness = SVGUtilities.convertSVGNumber(strikethroughThicknessStr);
      } catch (NumberFormatException var33) {
         throw new BridgeException(ctx, fontFaceElement, var33, "attribute.malformed", new Object[]{"0", strikethroughThicknessStr});
      }

      String overlinePosStr = fontFaceElement.getAttributeNS((String)null, "overline-position");
      if (overlinePosStr.length() == 0) {
         overlinePosStr = String.valueOf(ascent);
      }

      float overlinePos;
      try {
         overlinePos = SVGUtilities.convertSVGNumber(overlinePosStr);
      } catch (NumberFormatException var32) {
         throw new BridgeException(ctx, fontFaceElement, var32, "attribute.malformed", new Object[]{"0", overlinePosStr});
      }

      String overlineThicknessStr = fontFaceElement.getAttributeNS((String)null, "overline-thickness");
      if (overlineThicknessStr.length() == 0) {
         overlineThicknessStr = String.valueOf(unitsPerEm / 20.0F);
      }

      float overlineThickness;
      try {
         overlineThickness = SVGUtilities.convertSVGNumber(overlineThicknessStr);
      } catch (NumberFormatException var31) {
         throw new BridgeException(ctx, fontFaceElement, var31, "attribute.malformed", new Object[]{"0", overlineThicknessStr});
      }

      List srcs = null;
      Element fontElt = SVGUtilities.getParentElement(fontFaceElement);
      if (!fontElt.getNamespaceURI().equals("http://www.w3.org/2000/svg") || !fontElt.getLocalName().equals("font")) {
         srcs = this.getFontFaceSrcs(fontFaceElement);
      }

      return new SVGFontFace(fontFaceElement, srcs, familyNames, unitsPerEm, fontWeight, fontStyle, fontVariant, fontStretch, slope, panose1, ascent, descent, strikethroughPos, strikethroughThickness, underlinePos, underlineThickness, overlinePos, overlineThickness);
   }

   public List getFontFaceSrcs(Element fontFaceElement) {
      Element ffsrc = null;

      for(Node n = fontFaceElement.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1 && n.getNamespaceURI().equals("http://www.w3.org/2000/svg") && n.getLocalName().equals("font-face-src")) {
            ffsrc = (Element)n;
            break;
         }
      }

      if (ffsrc == null) {
         return null;
      } else {
         List ret = new LinkedList();

         for(Node n = ffsrc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == 1 && n.getNamespaceURI().equals("http://www.w3.org/2000/svg")) {
               Element ffname;
               String s;
               if (n.getLocalName().equals("font-face-uri")) {
                  ffname = (Element)n;
                  s = XLinkSupport.getXLinkHref(ffname);
                  String base = AbstractNode.getBaseURI(ffname);
                  ParsedURL purl;
                  if (base != null) {
                     purl = new ParsedURL(base, s);
                  } else {
                     purl = new ParsedURL(s);
                  }

                  ret.add(purl);
               } else if (n.getLocalName().equals("font-face-name")) {
                  ffname = (Element)n;
                  s = ffname.getAttribute("name");
                  if (s.length() != 0) {
                     ret.add(s);
                  }
               }
            }
         }

         return ret;
      }
   }
}
