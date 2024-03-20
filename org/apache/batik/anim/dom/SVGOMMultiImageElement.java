package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class SVGOMMultiImageElement extends SVGStylableElement {
   protected SVGOMMultiImageElement() {
   }

   public SVGOMMultiImageElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "multiImage";
   }

   protected Node newNode() {
      return new SVGOMMultiImageElement();
   }
}
