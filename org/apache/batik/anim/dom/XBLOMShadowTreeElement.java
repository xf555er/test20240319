package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.IdContainer;
import org.apache.batik.dom.xbl.XBLShadowTreeElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XBLOMShadowTreeElement extends XBLOMElement implements XBLShadowTreeElement, IdContainer {
   protected XBLOMShadowTreeElement() {
   }

   public XBLOMShadowTreeElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "shadowTree";
   }

   protected Node newNode() {
      return new XBLOMShadowTreeElement();
   }

   public Element getElementById(String elementId) {
      return this.getElementById(elementId, this);
   }

   protected Element getElementById(String elementId, Node n) {
      if (n.getNodeType() == 1) {
         Element e = (Element)n;
         if (e.getAttributeNS((String)null, "id").equals(elementId)) {
            return (Element)n;
         }
      }

      for(Node m = n.getFirstChild(); m != null; m = m.getNextSibling()) {
         Element result = this.getElementById(elementId, m);
         if (result != null) {
            return result;
         }
      }

      return null;
   }

   public Node getCSSParentNode() {
      return this.ownerDocument.getXBLManager().getXblBoundElement(this);
   }
}
