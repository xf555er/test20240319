package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGGlyphElement;

public class SVGOMGlyphElement extends SVGStylableElement implements SVGGlyphElement {
   protected SVGOMGlyphElement() {
   }

   public SVGOMGlyphElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "glyph";
   }

   protected Node newNode() {
      return new SVGOMGlyphElement();
   }
}
