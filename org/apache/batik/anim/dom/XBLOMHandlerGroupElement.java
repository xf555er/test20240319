package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class XBLOMHandlerGroupElement extends XBLOMElement {
   protected XBLOMHandlerGroupElement() {
   }

   public XBLOMHandlerGroupElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "handlerGroup";
   }

   protected Node newNode() {
      return new XBLOMHandlerGroupElement();
   }
}
