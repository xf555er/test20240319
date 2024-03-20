package org.apache.batik.dom.traversal;

import org.apache.batik.dom.AbstractNode;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

public class DOMTreeWalker implements TreeWalker {
   protected Node root;
   protected int whatToShow;
   protected NodeFilter filter;
   protected boolean expandEntityReferences;
   protected Node currentNode;

   public DOMTreeWalker(Node n, int what, NodeFilter nf, boolean exp) {
      this.root = n;
      this.whatToShow = what;
      this.filter = nf;
      this.expandEntityReferences = exp;
      this.currentNode = this.root;
   }

   public Node getRoot() {
      return this.root;
   }

   public int getWhatToShow() {
      return this.whatToShow;
   }

   public NodeFilter getFilter() {
      return this.filter;
   }

   public boolean getExpandEntityReferences() {
      return this.expandEntityReferences;
   }

   public Node getCurrentNode() {
      return this.currentNode;
   }

   public void setCurrentNode(Node n) {
      if (n == null) {
         throw ((AbstractNode)this.root).createDOMException((short)9, "null.current.node", (Object[])null);
      } else {
         this.currentNode = n;
      }
   }

   public Node parentNode() {
      Node result = this.parentNode(this.currentNode);
      if (result != null) {
         this.currentNode = result;
      }

      return result;
   }

   public Node firstChild() {
      Node result = this.firstChild(this.currentNode);
      if (result != null) {
         this.currentNode = result;
      }

      return result;
   }

   public Node lastChild() {
      Node result = this.lastChild(this.currentNode);
      if (result != null) {
         this.currentNode = result;
      }

      return result;
   }

   public Node previousSibling() {
      Node result = this.previousSibling(this.currentNode, this.root);
      if (result != null) {
         this.currentNode = result;
      }

      return result;
   }

   public Node nextSibling() {
      Node result = this.nextSibling(this.currentNode, this.root);
      if (result != null) {
         this.currentNode = result;
      }

      return result;
   }

   public Node previousNode() {
      Node result = this.previousSibling(this.currentNode, this.root);
      if (result == null) {
         result = this.parentNode(this.currentNode);
         if (result != null) {
            this.currentNode = result;
         }

         return result;
      } else {
         Node n = this.lastChild(result);

         Node last;
         for(last = n; n != null; n = this.lastChild(n)) {
            last = n;
         }

         return this.currentNode = last != null ? last : result;
      }
   }

   public Node nextNode() {
      Node result;
      if ((result = this.firstChild(this.currentNode)) != null) {
         return this.currentNode = result;
      } else if ((result = this.nextSibling(this.currentNode, this.root)) != null) {
         return this.currentNode = result;
      } else {
         Node parent = this.currentNode;

         do {
            parent = this.parentNode(parent);
            if (parent == null) {
               return null;
            }
         } while((result = this.nextSibling(parent, this.root)) == null);

         return this.currentNode = result;
      }
   }

   protected Node parentNode(Node n) {
      if (n == this.root) {
         return null;
      } else {
         Node result = n;

         do {
            do {
               result = result.getParentNode();
               if (result == null) {
                  return null;
               }
            } while((this.whatToShow & 1 << result.getNodeType() - 1) == 0);
         } while(this.filter != null && this.filter.acceptNode(result) != 1);

         return result;
      }
   }

   protected Node firstChild(Node n) {
      if (n.getNodeType() == 5 && !this.expandEntityReferences) {
         return null;
      } else {
         Node result = n.getFirstChild();
         if (result == null) {
            return null;
         } else {
            switch (this.acceptNode(result)) {
               case 1:
                  return result;
               case 3:
                  Node t = this.firstChild(result);
                  if (t != null) {
                     return t;
                  }
               default:
                  return this.nextSibling(result, n);
            }
         }
      }
   }

   protected Node lastChild(Node n) {
      if (n.getNodeType() == 5 && !this.expandEntityReferences) {
         return null;
      } else {
         Node result = n.getLastChild();
         if (result == null) {
            return null;
         } else {
            switch (this.acceptNode(result)) {
               case 1:
                  return result;
               case 3:
                  Node t = this.lastChild(result);
                  if (t != null) {
                     return t;
                  }
               default:
                  return this.previousSibling(result, n);
            }
         }
      }
   }

   protected Node previousSibling(Node n, Node root) {
      while(n != root) {
         Node result = n.getPreviousSibling();
         if (result == null) {
            result = n.getParentNode();
            if (result != null && result != root) {
               if (this.acceptNode(result) == 3) {
                  n = result;
                  continue;
               }

               return null;
            }

            return null;
         } else {
            switch (this.acceptNode(result)) {
               case 1:
                  return result;
               case 3:
                  Node t = this.lastChild(result);
                  if (t != null) {
                     return t;
                  }
               default:
                  n = result;
            }
         }
      }

      return null;
   }

   protected Node nextSibling(Node n, Node root) {
      while(n != root) {
         Node result = n.getNextSibling();
         if (result == null) {
            result = n.getParentNode();
            if (result != null && result != root) {
               if (this.acceptNode(result) == 3) {
                  n = result;
                  continue;
               }

               return null;
            }

            return null;
         } else {
            switch (this.acceptNode(result)) {
               case 1:
                  return result;
               case 3:
                  Node t = this.firstChild(result);
                  if (t != null) {
                     return t;
                  }
               default:
                  n = result;
            }
         }
      }

      return null;
   }

   protected short acceptNode(Node n) {
      if ((this.whatToShow & 1 << n.getNodeType() - 1) != 0) {
         return this.filter == null ? 1 : this.filter.acceptNode(n);
      } else {
         return 3;
      }
   }
}
