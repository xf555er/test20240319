package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class XBLOMImportElement extends XBLOMElement {
   protected XBLOMImportElement() {
   }

   public XBLOMImportElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "import";
   }

   protected Node newNode() {
      return new XBLOMImportElement();
   }
}
