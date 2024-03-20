package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGFEFuncRElement;

public class SVGOMFEFuncRElement extends SVGOMComponentTransferFunctionElement implements SVGFEFuncRElement {
   protected SVGOMFEFuncRElement() {
   }

   public SVGOMFEFuncRElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "feFuncR";
   }

   protected Node newNode() {
      return new SVGOMFEFuncRElement();
   }
}
