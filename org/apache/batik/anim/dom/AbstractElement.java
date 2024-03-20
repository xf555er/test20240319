package org.apache.batik.anim.dom;

import org.apache.batik.css.engine.CSSNavigableNode;
import org.apache.batik.dom.AbstractAttr;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.svg.LiveAttributeValue;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class AbstractElement extends org.apache.batik.dom.AbstractElement implements NodeEventTarget, CSSNavigableNode, SVGConstants {
   protected transient DoublyIndexedTable liveAttributeValues = new DoublyIndexedTable();

   protected AbstractElement() {
   }

   protected AbstractElement(String prefix, AbstractDocument owner) {
      this.ownerDocument = owner;
      this.setPrefix(prefix);
      this.initializeAttributes();
   }

   public Node getCSSParentNode() {
      return this.getXblParentNode();
   }

   public Node getCSSPreviousSibling() {
      return this.getXblPreviousSibling();
   }

   public Node getCSSNextSibling() {
      return this.getXblNextSibling();
   }

   public Node getCSSFirstChild() {
      return this.getXblFirstChild();
   }

   public Node getCSSLastChild() {
      return this.getXblLastChild();
   }

   public boolean isHiddenFromSelectors() {
      return false;
   }

   public void fireDOMAttrModifiedEvent(String name, Attr node, String oldv, String newv, short change) {
      super.fireDOMAttrModifiedEvent(name, node, oldv, newv, change);
      if (((SVGOMDocument)this.ownerDocument).isSVG12 && (change == 2 || change == 1)) {
         Attr a;
         if (node.getNamespaceURI() == null && node.getNodeName().equals("id")) {
            a = this.getAttributeNodeNS("http://www.w3.org/XML/1998/namespace", "id");
            if (a == null) {
               this.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:id", newv);
            } else if (!a.getNodeValue().equals(newv)) {
               a.setNodeValue(newv);
            }
         } else if (node.getNodeName().equals("xml:id")) {
            a = this.getAttributeNodeNS((String)null, "id");
            if (a == null) {
               this.setAttributeNS((String)null, "id", newv);
            } else if (!a.getNodeValue().equals(newv)) {
               a.setNodeValue(newv);
            }
         }
      }

   }

   public LiveAttributeValue getLiveAttributeValue(String ns, String ln) {
      return (LiveAttributeValue)this.liveAttributeValues.get(ns, ln);
   }

   public void putLiveAttributeValue(String ns, String ln, LiveAttributeValue val) {
      this.liveAttributeValues.put(ns, ln, val);
   }

   protected AttributeInitializer getAttributeInitializer() {
      return null;
   }

   protected void initializeAttributes() {
      AttributeInitializer ai = this.getAttributeInitializer();
      if (ai != null) {
         ai.initializeAttributes(this);
      }

   }

   protected boolean resetAttribute(String ns, String prefix, String ln) {
      AttributeInitializer ai = this.getAttributeInitializer();
      return ai == null ? false : ai.resetAttribute(this, ns, prefix, ln);
   }

   protected NamedNodeMap createAttributes() {
      return new ExtendedNamedNodeHashMap();
   }

   public void setUnspecifiedAttribute(String nsURI, String name, String value) {
      if (this.attributes == null) {
         this.attributes = this.createAttributes();
      }

      ((ExtendedNamedNodeHashMap)this.attributes).setUnspecifiedAttribute(nsURI, name, value);
   }

   protected void attrAdded(Attr node, String newv) {
      LiveAttributeValue lav = this.getLiveAttributeValue(node);
      if (lav != null) {
         lav.attrAdded(node, newv);
      }

   }

   protected void attrModified(Attr node, String oldv, String newv) {
      LiveAttributeValue lav = this.getLiveAttributeValue(node);
      if (lav != null) {
         lav.attrModified(node, oldv, newv);
      }

   }

   protected void attrRemoved(Attr node, String oldv) {
      LiveAttributeValue lav = this.getLiveAttributeValue(node);
      if (lav != null) {
         lav.attrRemoved(node, oldv);
      }

   }

   private LiveAttributeValue getLiveAttributeValue(Attr node) {
      String ns = node.getNamespaceURI();
      return this.getLiveAttributeValue(ns, ns == null ? node.getNodeName() : node.getLocalName());
   }

   protected Node export(Node n, AbstractDocument d) {
      super.export(n, d);
      ((AbstractElement)n).initializeAttributes();
      super.export(n, d);
      return n;
   }

   protected Node deepExport(Node n, AbstractDocument d) {
      super.export(n, d);
      ((AbstractElement)n).initializeAttributes();
      super.deepExport(n, d);
      return n;
   }

   protected class ExtendedNamedNodeHashMap extends org.apache.batik.dom.AbstractElement.NamedNodeHashMap {
      public ExtendedNamedNodeHashMap() {
         super();
      }

      public void setUnspecifiedAttribute(String nsURI, String name, String value) {
         Attr attr = AbstractElement.this.getOwnerDocument().createAttributeNS(nsURI, name);
         attr.setValue(value);
         ((AbstractAttr)attr).setSpecified(false);
         this.setNamedItemNS(attr);
      }

      public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
         if (AbstractElement.this.isReadonly()) {
            throw AbstractElement.this.createDOMException((short)7, "readonly.node.map", new Object[0]);
         } else if (localName == null) {
            throw AbstractElement.this.createDOMException((short)8, "attribute.missing", new Object[]{""});
         } else {
            AbstractAttr n = (AbstractAttr)this.remove(namespaceURI, localName);
            if (n == null) {
               throw AbstractElement.this.createDOMException((short)8, "attribute.missing", new Object[]{localName});
            } else {
               n.setOwnerElement((org.apache.batik.dom.AbstractElement)null);
               String prefix = n.getPrefix();
               if (!AbstractElement.this.resetAttribute(namespaceURI, prefix, localName)) {
                  AbstractElement.this.fireDOMAttrModifiedEvent(n.getNodeName(), n, n.getNodeValue(), "", (short)3);
               }

               return n;
            }
         }
      }
   }
}
