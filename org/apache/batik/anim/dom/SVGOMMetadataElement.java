package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGMetadataElement;

public class SVGOMMetadataElement extends SVGOMElement implements SVGMetadataElement {
   protected SVGOMMetadataElement() {
   }

   public SVGOMMetadataElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "metadata";
   }

   protected Node newNode() {
      return new SVGOMMetadataElement();
   }
}
