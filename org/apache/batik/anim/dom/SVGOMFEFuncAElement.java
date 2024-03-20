package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGFEFuncAElement;

public class SVGOMFEFuncAElement extends SVGOMComponentTransferFunctionElement implements SVGFEFuncAElement {
   protected SVGOMFEFuncAElement() {
   }

   public SVGOMFEFuncAElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "feFuncA";
   }

   protected Node newNode() {
      return new SVGOMFEFuncAElement();
   }
}
