package org.apache.batik.dom;

import org.w3c.dom.Node;

public abstract class AbstractParentChildNode extends AbstractParentNode {
   protected Node parentNode;
   protected Node previousSibling;
   protected Node nextSibling;

   public Node getParentNode() {
      return this.parentNode;
   }

   public void setParentNode(Node v) {
      this.parentNode = v;
   }

   public void setPreviousSibling(Node v) {
      this.previousSibling = v;
   }

   public Node getPreviousSibling() {
      return this.previousSibling;
   }

   public void setNextSibling(Node v) {
      this.nextSibling = v;
   }

   public Node getNextSibling() {
      return this.nextSibling;
   }
}
