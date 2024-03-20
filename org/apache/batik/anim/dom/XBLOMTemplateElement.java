package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class XBLOMTemplateElement extends XBLOMElement {
   protected XBLOMTemplateElement() {
   }

   public XBLOMTemplateElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "template";
   }

   protected Node newNode() {
      return new XBLOMTemplateElement();
   }
}
