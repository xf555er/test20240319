package org.apache.batik.dom.traversal;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.traversal.TreeWalker;

public class TraversalSupport {
   protected List iterators;

   public static TreeWalker createTreeWalker(AbstractDocument doc, Node root, int whatToShow, NodeFilter filter, boolean entityReferenceExpansion) {
      if (root == null) {
         throw doc.createDOMException((short)9, "null.root", (Object[])null);
      } else {
         return new DOMTreeWalker(root, whatToShow, filter, entityReferenceExpansion);
      }
   }

   public NodeIterator createNodeIterator(AbstractDocument doc, Node root, int whatToShow, NodeFilter filter, boolean entityReferenceExpansion) throws DOMException {
      if (root == null) {
         throw doc.createDOMException((short)9, "null.root", (Object[])null);
      } else {
         NodeIterator result = new DOMNodeIterator(doc, root, whatToShow, filter, entityReferenceExpansion);
         if (this.iterators == null) {
            this.iterators = new LinkedList();
         }

         this.iterators.add(result);
         return result;
      }
   }

   public void nodeToBeRemoved(Node removedNode) {
      if (this.iterators != null) {
         Iterator var2 = this.iterators.iterator();

         while(var2.hasNext()) {
            Object iterator = var2.next();
            ((DOMNodeIterator)iterator).nodeToBeRemoved(removedNode);
         }
      }

   }

   public void detachNodeIterator(NodeIterator it) {
      this.iterators.remove(it);
   }
}
