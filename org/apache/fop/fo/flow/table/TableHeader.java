package org.apache.fop.fo.flow.table;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;

public class TableHeader extends TablePart {
   public TableHeader(FONode parent) {
      super(parent);
   }

   public void startOfNode() throws FOPException {
      super.startOfNode();
      this.getFOEventHandler().startHeader(this);
   }

   public void endOfNode() throws FOPException {
      super.endOfNode();
      this.getFOEventHandler().endHeader(this);
   }

   public String getLocalName() {
      return "table-header";
   }

   public int getNameId() {
      return 78;
   }
}
