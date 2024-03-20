package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGFontFaceNameElement;

public class SVGOMFontFaceNameElement extends SVGOMElement implements SVGFontFaceNameElement {
   protected SVGOMFontFaceNameElement() {
   }

   public SVGOMFontFaceNameElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "font-face-name";
   }

   protected Node newNode() {
      return new SVGOMFontFaceNameElement();
   }
}
