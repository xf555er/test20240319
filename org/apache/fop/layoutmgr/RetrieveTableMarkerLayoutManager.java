package org.apache.fop.layoutmgr;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.RetrieveTableMarker;
import org.apache.fop.layoutmgr.inline.InlineLevelLayoutManager;
import org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager;
import org.apache.fop.layoutmgr.table.TableLayoutManager;

public class RetrieveTableMarkerLayoutManager extends LeafNodeLayoutManager {
   private static Log log = LogFactory.getLog(RetrieveTableMarkerLayoutManager.class);

   public RetrieveTableMarkerLayoutManager(RetrieveTableMarker node) {
      super(node);
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      this.setFinished(true);
      FONode foNode = this.getFObj();
      FONode foNode = this.getTableLayoutManager().resolveRetrieveTableMarker((RetrieveTableMarker)foNode);
      if (foNode != null) {
         InlineLevelLayoutManager illm = (InlineLevelLayoutManager)this.getPSLM().getLayoutManagerMaker().makeLayoutManager(foNode);
         if (illm instanceof RetrieveTableMarkerLayoutManager) {
            return null;
         } else {
            illm.setParent(this.getParent());
            illm.initialize();
            return illm.getNextKnuthElements(context, alignment);
         }
      } else {
         return null;
      }
   }

   public void addAreas(PositionIterator posIter, LayoutContext context) {
   }

   private TableLayoutManager getTableLayoutManager() {
      LayoutManager parentLM;
      for(parentLM = this.getParent(); !(parentLM instanceof TableLayoutManager); parentLM = parentLM.getParent()) {
      }

      TableLayoutManager tlm = (TableLayoutManager)parentLM;
      return tlm;
   }
}
