package org.apache.batik.bridge;

import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.batik.gvt.text.ArabicTextHandler;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SVGFontElementBridge extends AbstractSVGBridge {
   public String getLocalName() {
      return "font";
   }

   public SVGGVTFont createFont(BridgeContext ctx, Element fontElement, Element textElement, float size, GVTFontFace fontFace) {
      NodeList glyphElements = fontElement.getElementsByTagNameNS("http://www.w3.org/2000/svg", "glyph");
      int numGlyphs = glyphElements.getLength();
      String[] glyphCodes = new String[numGlyphs];
      String[] glyphNames = new String[numGlyphs];
      String[] glyphLangs = new String[numGlyphs];
      String[] glyphOrientations = new String[numGlyphs];
      String[] glyphForms = new String[numGlyphs];
      Element[] glyphElementArray = new Element[numGlyphs];

      Element missingGlyphElement;
      for(int i = 0; i < numGlyphs; ++i) {
         missingGlyphElement = (Element)glyphElements.item(i);
         glyphCodes[i] = missingGlyphElement.getAttributeNS((String)null, "unicode");
         if (glyphCodes[i].length() > 1 && ArabicTextHandler.arabicChar(glyphCodes[i].charAt(0))) {
            glyphCodes[i] = (new StringBuffer(glyphCodes[i])).reverse().toString();
         }

         glyphNames[i] = missingGlyphElement.getAttributeNS((String)null, "glyph-name");
         glyphLangs[i] = missingGlyphElement.getAttributeNS((String)null, "lang");
         glyphOrientations[i] = missingGlyphElement.getAttributeNS((String)null, "orientation");
         glyphForms[i] = missingGlyphElement.getAttributeNS((String)null, "arabic-form");
         glyphElementArray[i] = missingGlyphElement;
      }

      NodeList missingGlyphElements = fontElement.getElementsByTagNameNS("http://www.w3.org/2000/svg", "missing-glyph");
      missingGlyphElement = null;
      if (missingGlyphElements.getLength() > 0) {
         missingGlyphElement = (Element)missingGlyphElements.item(0);
      }

      NodeList hkernElements = fontElement.getElementsByTagNameNS("http://www.w3.org/2000/svg", "hkern");
      Element[] hkernElementArray = new Element[hkernElements.getLength()];

      for(int i = 0; i < hkernElementArray.length; ++i) {
         Element hkernElement = (Element)hkernElements.item(i);
         hkernElementArray[i] = hkernElement;
      }

      NodeList vkernElements = fontElement.getElementsByTagNameNS("http://www.w3.org/2000/svg", "vkern");
      Element[] vkernElementArray = new Element[vkernElements.getLength()];

      for(int i = 0; i < vkernElementArray.length; ++i) {
         Element vkernElement = (Element)vkernElements.item(i);
         vkernElementArray[i] = vkernElement;
      }

      return new SVGGVTFont(size, fontFace, glyphCodes, glyphNames, glyphLangs, glyphOrientations, glyphForms, ctx, glyphElementArray, missingGlyphElement, hkernElementArray, vkernElementArray, textElement);
   }
}
