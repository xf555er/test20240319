package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDefinitionSrcElement;

public class SVGOMDefinitionSrcElement extends SVGOMElement implements SVGDefinitionSrcElement {
   protected SVGOMDefinitionSrcElement() {
   }

   public SVGOMDefinitionSrcElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "definition-src";
   }

   protected Node newNode() {
      return new SVGOMDefinitionSrcElement();
   }
}
