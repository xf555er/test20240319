package org.apache.fop.layoutmgr.table;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.fo.flow.table.TableCaption;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.Keep;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.PositionIterator;

public class TableCaptionLayoutManager extends BlockStackingLayoutManager {
   private Block curBlockArea;

   public TableCaptionLayoutManager(TableCaption node) {
      super(node);
   }

   public TableCaption getTableCaptionFO() {
      return (TableCaption)this.fobj;
   }

   public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
      this.getParentArea((Area)null);
      this.addId();
      this.flush();
      this.curBlockArea = null;
   }

   public Area getParentArea(Area childArea) {
      if (this.curBlockArea == null) {
         this.curBlockArea = new Block();
         this.curBlockArea.setChangeBarList(this.getChangeBarList());
         Area parentArea = this.parentLayoutManager.getParentArea(this.curBlockArea);
         int referenceIPD = parentArea.getIPD();
         this.curBlockArea.setIPD(referenceIPD);
         this.curBlockArea.setBidiLevel(this.getTableCaptionFO().getBidiLevel());
         this.setCurrentArea(this.curBlockArea);
      }

      return this.curBlockArea;
   }

   public void addChildArea(Area childArea) {
      if (this.curBlockArea != null) {
         this.curBlockArea.addBlock((Block)childArea);
      }

   }

   public Keep getKeepWithNext() {
      return Keep.KEEP_AUTO;
   }

   public Keep getKeepWithPrevious() {
      return Keep.KEEP_AUTO;
   }
}
