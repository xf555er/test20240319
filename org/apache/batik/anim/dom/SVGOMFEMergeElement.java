package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGFEMergeElement;

public class SVGOMFEMergeElement extends SVGOMFilterPrimitiveStandardAttributes implements SVGFEMergeElement {
   protected SVGOMFEMergeElement() {
   }

   public SVGOMFEMergeElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "feMerge";
   }

   protected Node newNode() {
      return new SVGOMFEMergeElement();
   }
}
