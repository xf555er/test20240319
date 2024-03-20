package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGFontFaceFormatElement;

public class SVGOMFontFaceFormatElement extends SVGOMElement implements SVGFontFaceFormatElement {
   protected SVGOMFontFaceFormatElement() {
   }

   public SVGOMFontFaceFormatElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "font-face-format";
   }

   protected Node newNode() {
      return new SVGOMFontFaceFormatElement();
   }
}
