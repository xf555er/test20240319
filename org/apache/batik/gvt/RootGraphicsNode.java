package org.apache.batik.gvt;

import java.util.LinkedList;
import java.util.List;
import org.apache.batik.gvt.event.GraphicsNodeChangeListener;

public class RootGraphicsNode extends CompositeGraphicsNode {
   List treeGraphicsNodeChangeListeners = null;

   public RootGraphicsNode getRoot() {
      return this;
   }

   public List getTreeGraphicsNodeChangeListeners() {
      if (this.treeGraphicsNodeChangeListeners == null) {
         this.treeGraphicsNodeChangeListeners = new LinkedList();
      }

      return this.treeGraphicsNodeChangeListeners;
   }

   public void addTreeGraphicsNodeChangeListener(GraphicsNodeChangeListener l) {
      this.getTreeGraphicsNodeChangeListeners().add(l);
   }

   public void removeTreeGraphicsNodeChangeListener(GraphicsNodeChangeListener l) {
      this.getTreeGraphicsNodeChangeListeners().remove(l);
   }
}
