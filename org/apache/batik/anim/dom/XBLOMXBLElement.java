package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class XBLOMXBLElement extends XBLOMElement {
   protected XBLOMXBLElement() {
   }

   public XBLOMXBLElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "xbl";
   }

   protected Node newNode() {
      return new XBLOMXBLElement();
   }
}
