package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class BindableElement extends SVGGraphicsElement {
   protected String namespaceURI;
   protected String localName;
   protected XBLOMShadowTreeElement xblShadowTree;

   protected BindableElement() {
   }

   public BindableElement(String prefix, AbstractDocument owner, String ns, String ln) {
      super(prefix, owner);
      this.namespaceURI = ns;
      this.localName = ln;
   }

   public String getNamespaceURI() {
      return this.namespaceURI;
   }

   public String getLocalName() {
      return this.localName;
   }

   protected AttributeInitializer getAttributeInitializer() {
      return null;
   }

   protected Node newNode() {
      return new BindableElement((String)null, (AbstractDocument)null, this.namespaceURI, this.localName);
   }

   public void setShadowTree(XBLOMShadowTreeElement s) {
      this.xblShadowTree = s;
   }

   public XBLOMShadowTreeElement getShadowTree() {
      return this.xblShadowTree;
   }

   public Node getCSSFirstChild() {
      return this.xblShadowTree != null ? this.xblShadowTree.getFirstChild() : null;
   }

   public Node getCSSLastChild() {
      return this.getCSSFirstChild();
   }
}
