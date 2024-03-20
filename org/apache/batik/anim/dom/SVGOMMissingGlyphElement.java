package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGMissingGlyphElement;

public class SVGOMMissingGlyphElement extends SVGStylableElement implements SVGMissingGlyphElement {
   protected SVGOMMissingGlyphElement() {
   }

   public SVGOMMissingGlyphElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "missing-glyph";
   }

   protected Node newNode() {
      return new SVGOMMissingGlyphElement();
   }
}
