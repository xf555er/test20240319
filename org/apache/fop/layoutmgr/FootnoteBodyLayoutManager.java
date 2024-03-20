package org.apache.fop.layoutmgr;

import java.util.LinkedList;
import java.util.List;
import org.apache.fop.area.Area;
import org.apache.fop.fo.flow.FootnoteBody;

public class FootnoteBodyLayoutManager extends BlockStackingLayoutManager {
   private List knuthElements;

   public FootnoteBodyLayoutManager(FootnoteBody body) {
      super(body);
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      if (this.knuthElements == null) {
         this.knuthElements = super.getNextKnuthElements(context, alignment);
      }

      return this.knuthElements;
   }

   public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
      LayoutManager lastLM = null;
      LayoutContext lc = LayoutContext.newInstance();
      LinkedList positionList = new LinkedList();

      while(parentIter.hasNext()) {
         Position pos = parentIter.next();
         if (pos instanceof NonLeafPosition) {
            Position innerPosition = pos.getPosition();
            if (innerPosition.getLM() != this) {
               positionList.add(innerPosition);
               lastLM = innerPosition.getLM();
            }
         }
      }

      PositionIterator childPosIter = new PositionIterator(positionList.listIterator());

      LayoutManager childLM;
      while((childLM = childPosIter.getNextChildLM()) != null) {
         lc.setFlags(8, layoutContext.isLastArea() && childLM == lastLM);
         childLM.addAreas(childPosIter, lc);
      }

   }

   public void addChildArea(Area childArea) {
      childArea.setAreaClass(4);
      this.parentLayoutManager.addChildArea(childArea);
   }

   protected FootnoteBody getFootnodeBodyFO() {
      return (FootnoteBody)this.fobj;
   }

   public Keep getKeepTogether() {
      return this.getParentKeepTogether();
   }

   public Keep getKeepWithNext() {
      return Keep.KEEP_AUTO;
   }

   public Keep getKeepWithPrevious() {
      return Keep.KEEP_AUTO;
   }

   public void reset() {
      super.reset();
      this.knuthElements = null;
   }
}
