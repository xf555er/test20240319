package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.DOMUtilities;
import org.w3c.dom.Node;

public class XBLOMDefinitionElement extends XBLOMElement {
   protected XBLOMDefinitionElement() {
   }

   public XBLOMDefinitionElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "definition";
   }

   protected Node newNode() {
      return new XBLOMDefinitionElement();
   }

   public String getElementNamespaceURI() {
      String qname = this.getAttributeNS((String)null, "element");
      String prefix = DOMUtilities.getPrefix(qname);
      String ns = this.lookupNamespaceURI(prefix);
      if (ns == null) {
         throw this.createDOMException((short)14, "prefix", new Object[]{Integer.valueOf(this.getNodeType()), this.getNodeName(), prefix});
      } else {
         return ns;
      }
   }

   public String getElementLocalName() {
      String qname = this.getAttributeNS((String)null, "element");
      return DOMUtilities.getLocalName(qname);
   }
}
