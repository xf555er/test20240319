package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGTextContentElement;

public class SVGOMFlowDivElement extends SVGOMTextContentElement implements SVGTextContentElement {
   protected SVGOMFlowDivElement() {
   }

   public SVGOMFlowDivElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "flowDiv";
   }

   protected Node newNode() {
      return new SVGOMFlowDivElement();
   }
}
