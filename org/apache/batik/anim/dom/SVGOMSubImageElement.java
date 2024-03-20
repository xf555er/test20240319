package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class SVGOMSubImageElement extends SVGStylableElement {
   protected SVGOMSubImageElement() {
   }

   public SVGOMSubImageElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "subImage";
   }

   protected Node newNode() {
      return new SVGOMSubImageElement();
   }
}
