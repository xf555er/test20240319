package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;

public class ListItemBody extends AbstractListItemPart {
   public ListItemBody(FONode parent) {
      super(parent);
   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      this.getFOEventHandler().startListBody(this);
   }

   public void endOfNode() throws FOPException {
      super.endOfNode();
      this.getFOEventHandler().endListBody(this);
   }

   public String getLocalName() {
      return "list-item-body";
   }

   public int getNameId() {
      return 42;
   }
}
