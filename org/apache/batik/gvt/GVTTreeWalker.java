package org.apache.batik.gvt;

import java.util.List;

public class GVTTreeWalker {
   protected GraphicsNode gvtRoot;
   protected GraphicsNode treeRoot;
   protected GraphicsNode currentNode;

   public GVTTreeWalker(GraphicsNode treeRoot) {
      this.gvtRoot = treeRoot.getRoot();
      this.treeRoot = treeRoot;
      this.currentNode = treeRoot;
   }

   public GraphicsNode getRoot() {
      return this.treeRoot;
   }

   public GraphicsNode getGVTRoot() {
      return this.gvtRoot;
   }

   public void setCurrentGraphicsNode(GraphicsNode node) {
      if (node.getRoot() != this.gvtRoot) {
         throw new IllegalArgumentException("The node " + node + " is not part of the document " + this.gvtRoot);
      } else {
         this.currentNode = node;
      }
   }

   public GraphicsNode getCurrentGraphicsNode() {
      return this.currentNode;
   }

   public GraphicsNode previousGraphicsNode() {
      GraphicsNode result = this.getPreviousGraphicsNode(this.currentNode);
      if (result != null) {
         this.currentNode = result;
      }

      return result;
   }

   public GraphicsNode nextGraphicsNode() {
      GraphicsNode result = this.getNextGraphicsNode(this.currentNode);
      if (result != null) {
         this.currentNode = result;
      }

      return result;
   }

   public GraphicsNode parentGraphicsNode() {
      if (this.currentNode == this.treeRoot) {
         return null;
      } else {
         GraphicsNode result = this.currentNode.getParent();
         if (result != null) {
            this.currentNode = result;
         }

         return result;
      }
   }

   public GraphicsNode getNextSibling() {
      GraphicsNode result = getNextSibling(this.currentNode);
      if (result != null) {
         this.currentNode = result;
      }

      return result;
   }

   public GraphicsNode getPreviousSibling() {
      GraphicsNode result = getPreviousSibling(this.currentNode);
      if (result != null) {
         this.currentNode = result;
      }

      return result;
   }

   public GraphicsNode firstChild() {
      GraphicsNode result = getFirstChild(this.currentNode);
      if (result != null) {
         this.currentNode = result;
      }

      return result;
   }

   public GraphicsNode lastChild() {
      GraphicsNode result = getLastChild(this.currentNode);
      if (result != null) {
         this.currentNode = result;
      }

      return result;
   }

   protected GraphicsNode getNextGraphicsNode(GraphicsNode node) {
      if (node == null) {
         return null;
      } else {
         GraphicsNode n = getFirstChild(node);
         if (n != null) {
            return n;
         } else {
            n = getNextSibling(node);
            if (n != null) {
               return n;
            } else {
               GraphicsNode n = node;

               while((n = ((GraphicsNode)n).getParent()) != null && n != this.treeRoot) {
                  GraphicsNode t = getNextSibling((GraphicsNode)n);
                  if (t != null) {
                     return t;
                  }
               }

               return null;
            }
         }
      }
   }

   protected GraphicsNode getPreviousGraphicsNode(GraphicsNode node) {
      if (node == null) {
         return null;
      } else if (node == this.treeRoot) {
         return null;
      } else {
         GraphicsNode n = getPreviousSibling(node);
         if (n == null) {
            return node.getParent();
         } else {
            GraphicsNode t;
            while((t = getLastChild(n)) != null) {
               n = t;
            }

            return n;
         }
      }
   }

   protected static GraphicsNode getLastChild(GraphicsNode node) {
      if (!(node instanceof CompositeGraphicsNode)) {
         return null;
      } else {
         CompositeGraphicsNode parent = (CompositeGraphicsNode)node;
         List children = parent.getChildren();
         if (children == null) {
            return null;
         } else {
            return children.size() >= 1 ? (GraphicsNode)children.get(children.size() - 1) : null;
         }
      }
   }

   protected static GraphicsNode getPreviousSibling(GraphicsNode node) {
      CompositeGraphicsNode parent = node.getParent();
      if (parent == null) {
         return null;
      } else {
         List children = parent.getChildren();
         if (children == null) {
            return null;
         } else {
            int index = children.indexOf(node);
            return index - 1 >= 0 ? (GraphicsNode)children.get(index - 1) : null;
         }
      }
   }

   protected static GraphicsNode getFirstChild(GraphicsNode node) {
      if (!(node instanceof CompositeGraphicsNode)) {
         return null;
      } else {
         CompositeGraphicsNode parent = (CompositeGraphicsNode)node;
         List children = parent.getChildren();
         if (children == null) {
            return null;
         } else {
            return children.size() >= 1 ? (GraphicsNode)children.get(0) : null;
         }
      }
   }

   protected static GraphicsNode getNextSibling(GraphicsNode node) {
      CompositeGraphicsNode parent = node.getParent();
      if (parent == null) {
         return null;
      } else {
         List children = parent.getChildren();
         if (children == null) {
            return null;
         } else {
            int index = children.indexOf(node);
            return index + 1 < children.size() ? (GraphicsNode)children.get(index + 1) : null;
         }
      }
   }
}
