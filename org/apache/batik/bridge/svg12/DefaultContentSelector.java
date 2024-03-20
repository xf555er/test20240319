package org.apache.batik.bridge.svg12;

import java.util.ArrayList;
import org.apache.batik.anim.dom.XBLOMContentElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DefaultContentSelector extends AbstractContentSelector {
   protected SelectedNodes selectedContent;

   public DefaultContentSelector(ContentManager cm, XBLOMContentElement content, Element bound) {
      super(cm, content, bound);
   }

   public NodeList getSelectedContent() {
      if (this.selectedContent == null) {
         this.selectedContent = new SelectedNodes();
      }

      return this.selectedContent;
   }

   boolean update() {
      if (this.selectedContent == null) {
         this.selectedContent = new SelectedNodes();
         return true;
      } else {
         return this.selectedContent.update();
      }
   }

   protected class SelectedNodes implements NodeList {
      protected ArrayList nodes = new ArrayList(10);

      public SelectedNodes() {
         this.update();
      }

      protected boolean update() {
         ArrayList oldNodes = (ArrayList)this.nodes.clone();
         this.nodes.clear();

         for(Node n = DefaultContentSelector.this.boundElement.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (!DefaultContentSelector.this.isSelected(n)) {
               this.nodes.add(n);
            }
         }

         int nodesSize = this.nodes.size();
         if (oldNodes.size() != nodesSize) {
            return true;
         } else {
            for(int i = 0; i < nodesSize; ++i) {
               if (oldNodes.get(i) != this.nodes.get(i)) {
                  return true;
               }
            }

            return false;
         }
      }

      public Node item(int index) {
         return index >= 0 && index < this.nodes.size() ? (Node)this.nodes.get(index) : null;
      }

      public int getLength() {
         return this.nodes.size();
      }
   }
}
