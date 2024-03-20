package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.util.XBLConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public abstract class XBLOMElement extends SVGOMElement implements XBLConstants {
   protected String prefix;

   protected XBLOMElement() {
   }

   protected XBLOMElement(String prefix, AbstractDocument owner) {
      this.ownerDocument = owner;
      this.setPrefix(prefix);
   }

   public String getNodeName() {
      return this.prefix != null && !this.prefix.equals("") ? this.prefix + ':' + this.getLocalName() : this.getLocalName();
   }

   public String getNamespaceURI() {
      return "http://www.w3.org/2004/xbl";
   }

   public void setPrefix(String prefix) throws DOMException {
      if (this.isReadonly()) {
         throw this.createDOMException((short)7, "readonly.node", new Object[]{Integer.valueOf(this.getNodeType()), this.getNodeName()});
      } else if (prefix != null && !prefix.equals("") && !DOMUtilities.isValidName(prefix)) {
         throw this.createDOMException((short)5, "prefix", new Object[]{Integer.valueOf(this.getNodeType()), this.getNodeName(), prefix});
      } else {
         this.prefix = prefix;
      }
   }

   protected Node export(Node n, AbstractDocument d) {
      super.export(n, d);
      XBLOMElement e = (XBLOMElement)n;
      e.prefix = this.prefix;
      return n;
   }

   protected Node deepExport(Node n, AbstractDocument d) {
      super.deepExport(n, d);
      XBLOMElement e = (XBLOMElement)n;
      e.prefix = this.prefix;
      return n;
   }

   protected Node copyInto(Node n) {
      super.copyInto(n);
      XBLOMElement e = (XBLOMElement)n;
      e.prefix = this.prefix;
      return n;
   }

   protected Node deepCopyInto(Node n) {
      super.deepCopyInto(n);
      XBLOMElement e = (XBLOMElement)n;
      e.prefix = this.prefix;
      return n;
   }
}
