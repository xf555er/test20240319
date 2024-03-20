package org.apache.batik.dom;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

public abstract class AbstractDocumentFragment extends AbstractParentNode implements DocumentFragment {
   public String getNodeName() {
      return "#document-fragment";
   }

   public short getNodeType() {
      return 11;
   }

   protected void checkChildType(Node n, boolean replace) {
      switch (n.getNodeType()) {
         case 1:
         case 3:
         case 4:
         case 5:
         case 7:
         case 8:
         case 11:
            return;
         case 2:
         case 6:
         case 9:
         case 10:
         default:
            throw this.createDOMException((short)3, "child.type", new Object[]{Integer.valueOf(this.getNodeType()), this.getNodeName(), Integer.valueOf(n.getNodeType()), n.getNodeName()});
      }
   }
}
