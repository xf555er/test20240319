package org.apache.batik.dom;

import org.apache.batik.dom.util.DOMUtilities;
import org.w3c.dom.DOMException;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;

public abstract class AbstractEntityReference extends AbstractParentChildNode implements EntityReference {
   protected String nodeName;

   protected AbstractEntityReference() {
   }

   protected AbstractEntityReference(String name, AbstractDocument owner) throws DOMException {
      this.ownerDocument = owner;
      if (owner.getStrictErrorChecking() && !DOMUtilities.isValidName(name)) {
         throw this.createDOMException((short)5, "xml.name", new Object[]{name});
      } else {
         this.nodeName = name;
      }
   }

   public short getNodeType() {
      return 5;
   }

   public void setNodeName(String v) {
      this.nodeName = v;
   }

   public String getNodeName() {
      return this.nodeName;
   }

   protected Node export(Node n, AbstractDocument d) {
      super.export(n, d);
      AbstractEntityReference ae = (AbstractEntityReference)n;
      ae.nodeName = this.nodeName;
      return n;
   }

   protected Node deepExport(Node n, AbstractDocument d) {
      super.deepExport(n, d);
      AbstractEntityReference ae = (AbstractEntityReference)n;
      ae.nodeName = this.nodeName;
      return n;
   }

   protected Node copyInto(Node n) {
      super.copyInto(n);
      AbstractEntityReference ae = (AbstractEntityReference)n;
      ae.nodeName = this.nodeName;
      return n;
   }

   protected Node deepCopyInto(Node n) {
      super.deepCopyInto(n);
      AbstractEntityReference ae = (AbstractEntityReference)n;
      ae.nodeName = this.nodeName;
      return n;
   }

   protected void checkChildType(Node n, boolean replace) {
      switch (n.getNodeType()) {
         case 1:
         case 3:
         case 4:
         case 5:
         case 7:
         case 8:
         case 11:
            return;
         case 2:
         case 6:
         case 9:
         case 10:
         default:
            throw this.createDOMException((short)3, "child.type", new Object[]{Integer.valueOf(this.getNodeType()), this.getNodeName(), Integer.valueOf(n.getNodeType()), n.getNodeName()});
      }
   }
}
