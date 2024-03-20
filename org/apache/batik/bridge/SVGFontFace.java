package org.apache.batik.bridge;

import java.util.List;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.w3c.dom.Element;

public class SVGFontFace extends FontFace {
   Element fontFaceElement;
   GVTFontFamily fontFamily = null;

   public SVGFontFace(Element fontFaceElement, List srcs, String familyName, float unitsPerEm, String fontWeight, String fontStyle, String fontVariant, String fontStretch, float slope, String panose1, float ascent, float descent, float strikethroughPosition, float strikethroughThickness, float underlinePosition, float underlineThickness, float overlinePosition, float overlineThickness) {
      super(srcs, familyName, unitsPerEm, fontWeight, fontStyle, fontVariant, fontStretch, slope, panose1, ascent, descent, strikethroughPosition, strikethroughThickness, underlinePosition, underlineThickness, overlinePosition, overlineThickness);
      this.fontFaceElement = fontFaceElement;
   }

   public GVTFontFamily getFontFamily(BridgeContext ctx) {
      if (this.fontFamily != null) {
         return this.fontFamily;
      } else {
         Element fontElt = SVGUtilities.getParentElement(this.fontFaceElement);
         if (fontElt.getNamespaceURI().equals("http://www.w3.org/2000/svg") && fontElt.getLocalName().equals("font")) {
            return new SVGFontFamily(this, fontElt, ctx);
         } else {
            this.fontFamily = super.getFontFamily(ctx);
            return this.fontFamily;
         }
      }
   }

   public Element getFontFaceElement() {
      return this.fontFaceElement;
   }

   protected Element getBaseElement(BridgeContext ctx) {
      return this.fontFaceElement != null ? this.fontFaceElement : super.getBaseElement(ctx);
   }
}
