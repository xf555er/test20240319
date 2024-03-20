package org.apache.fop.layoutmgr;

import org.apache.fop.area.Area;
import org.apache.fop.fo.FObj;

public class MultiCaseLayoutManager extends BlockStackingLayoutManager {
   public MultiCaseLayoutManager(FObj node) {
      super(node);
   }

   public Keep getKeepTogether() {
      return Keep.KEEP_AUTO;
   }

   public Keep getKeepWithNext() {
      return Keep.KEEP_AUTO;
   }

   public Keep getKeepWithPrevious() {
      return Keep.KEEP_AUTO;
   }

   public Area getParentArea(Area childArea) {
      return this.parentLayoutManager.getParentArea(childArea);
   }

   public void addChildArea(Area childArea) {
      this.parentLayoutManager.addChildArea(childArea);
   }

   public void addAreas(PositionIterator posIter, LayoutContext context) {
      AreaAdditionUtil.addAreas(this, posIter, context);
      this.flush();
   }
}
