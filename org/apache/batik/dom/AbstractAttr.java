package org.apache.batik.dom;

import org.apache.batik.dom.util.DOMUtilities;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

public abstract class AbstractAttr extends AbstractParentNode implements Attr {
   protected String nodeName;
   protected boolean unspecified;
   protected boolean isIdAttr;
   protected AbstractElement ownerElement;
   protected TypeInfo typeInfo;

   protected AbstractAttr() {
   }

   protected AbstractAttr(String name, AbstractDocument owner) throws DOMException {
      this.ownerDocument = owner;
      if (owner.getStrictErrorChecking() && !DOMUtilities.isValidName(name)) {
         throw this.createDOMException((short)5, "xml.name", new Object[]{name});
      }
   }

   public void setNodeName(String v) {
      this.nodeName = v;
      this.isIdAttr = this.ownerDocument.isId(this);
   }

   public String getNodeName() {
      return this.nodeName;
   }

   public short getNodeType() {
      return 2;
   }

   public String getNodeValue() throws DOMException {
      Node first = this.getFirstChild();
      if (first == null) {
         return "";
      } else {
         Node n = first.getNextSibling();
         if (n == null) {
            return first.getNodeValue();
         } else {
            StringBuffer result = new StringBuffer(first.getNodeValue());

            do {
               result.append(n.getNodeValue());
               n = n.getNextSibling();
            } while(n != null);

            return result.toString();
         }
      }
   }

   public void setNodeValue(String nodeValue) throws DOMException {
      if (this.isReadonly()) {
         throw this.createDOMException((short)7, "readonly.node", new Object[]{Integer.valueOf(this.getNodeType()), this.getNodeName()});
      } else {
         String s = this.getNodeValue();

         Node n;
         while((n = this.getFirstChild()) != null) {
            this.removeChild(n);
         }

         String val = nodeValue == null ? "" : nodeValue;
         Node n = this.getOwnerDocument().createTextNode(val);
         this.appendChild(n);
         if (this.ownerElement != null) {
            this.ownerElement.fireDOMAttrModifiedEvent(this.nodeName, this, s, val, (short)1);
         }

      }
   }

   public String getName() {
      return this.getNodeName();
   }

   public boolean getSpecified() {
      return !this.unspecified;
   }

   public void setSpecified(boolean v) {
      this.unspecified = !v;
   }

   public String getValue() {
      return this.getNodeValue();
   }

   public void setValue(String value) throws DOMException {
      this.setNodeValue(value);
   }

   public void setOwnerElement(AbstractElement v) {
      this.ownerElement = v;
   }

   public Element getOwnerElement() {
      return this.ownerElement;
   }

   public TypeInfo getSchemaTypeInfo() {
      if (this.typeInfo == null) {
         this.typeInfo = new AttrTypeInfo();
      }

      return this.typeInfo;
   }

   public boolean isId() {
      return this.isIdAttr;
   }

   public void setIsId(boolean isId) {
      this.isIdAttr = isId;
   }

   protected void nodeAdded(Node n) {
      this.setSpecified(true);
   }

   protected void nodeToBeRemoved(Node n) {
      this.setSpecified(true);
   }

   protected Node export(Node n, AbstractDocument d) {
      super.export(n, d);
      AbstractAttr aa = (AbstractAttr)n;
      aa.nodeName = this.nodeName;
      aa.unspecified = false;
      aa.isIdAttr = d.isId(aa);
      return n;
   }

   protected Node deepExport(Node n, AbstractDocument d) {
      super.deepExport(n, d);
      AbstractAttr aa = (AbstractAttr)n;
      aa.nodeName = this.nodeName;
      aa.unspecified = false;
      aa.isIdAttr = d.isId(aa);
      return n;
   }

   protected Node copyInto(Node n) {
      super.copyInto(n);
      AbstractAttr aa = (AbstractAttr)n;
      aa.nodeName = this.nodeName;
      aa.unspecified = this.unspecified;
      aa.isIdAttr = this.isIdAttr;
      return n;
   }

   protected Node deepCopyInto(Node n) {
      super.deepCopyInto(n);
      AbstractAttr aa = (AbstractAttr)n;
      aa.nodeName = this.nodeName;
      aa.unspecified = this.unspecified;
      aa.isIdAttr = this.isIdAttr;
      return n;
   }

   protected void checkChildType(Node n, boolean replace) {
      switch (n.getNodeType()) {
         case 3:
         case 5:
         case 11:
            return;
         default:
            throw this.createDOMException((short)3, "child.type", new Object[]{Integer.valueOf(this.getNodeType()), this.getNodeName(), Integer.valueOf(n.getNodeType()), n.getNodeName()});
      }
   }

   protected void fireDOMSubtreeModifiedEvent() {
      AbstractDocument doc = this.getCurrentDocument();
      if (doc.getEventsEnabled()) {
         super.fireDOMSubtreeModifiedEvent();
         if (this.getOwnerElement() != null) {
            ((AbstractElement)this.getOwnerElement()).fireDOMSubtreeModifiedEvent();
         }
      }

   }

   public static class AttrTypeInfo implements TypeInfo {
      public String getTypeNamespace() {
         return null;
      }

      public String getTypeName() {
         return null;
      }

      public boolean isDerivedFrom(String ns, String name, int method) {
         return false;
      }
   }
}
