package org.apache.batik.dom.xbl;

import org.apache.batik.dom.AbstractNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GenericXBLManager implements XBLManager {
   protected boolean isProcessing;

   public void startProcessing() {
      this.isProcessing = true;
   }

   public void stopProcessing() {
      this.isProcessing = false;
   }

   public boolean isProcessing() {
      return this.isProcessing;
   }

   public Node getXblParentNode(Node n) {
      return n.getParentNode();
   }

   public NodeList getXblChildNodes(Node n) {
      return n.getChildNodes();
   }

   public NodeList getXblScopedChildNodes(Node n) {
      return n.getChildNodes();
   }

   public Node getXblFirstChild(Node n) {
      return n.getFirstChild();
   }

   public Node getXblLastChild(Node n) {
      return n.getLastChild();
   }

   public Node getXblPreviousSibling(Node n) {
      return n.getPreviousSibling();
   }

   public Node getXblNextSibling(Node n) {
      return n.getNextSibling();
   }

   public Element getXblFirstElementChild(Node n) {
      Node m;
      for(m = n.getFirstChild(); m != null && m.getNodeType() != 1; m = m.getNextSibling()) {
      }

      return (Element)m;
   }

   public Element getXblLastElementChild(Node n) {
      Node m;
      for(m = n.getLastChild(); m != null && m.getNodeType() != 1; m = m.getPreviousSibling()) {
      }

      return (Element)m;
   }

   public Element getXblPreviousElementSibling(Node n) {
      Node m = n;

      do {
         m = m.getPreviousSibling();
      } while(m != null && m.getNodeType() != 1);

      return (Element)m;
   }

   public Element getXblNextElementSibling(Node n) {
      Node m = n;

      do {
         m = m.getNextSibling();
      } while(m != null && m.getNodeType() != 1);

      return (Element)m;
   }

   public Element getXblBoundElement(Node n) {
      return null;
   }

   public Element getXblShadowTree(Node n) {
      return null;
   }

   public NodeList getXblDefinitions(Node n) {
      return AbstractNode.EMPTY_NODE_LIST;
   }
}
