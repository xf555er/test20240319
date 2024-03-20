package org.apache.fop.layoutmgr;

import java.util.LinkedList;

public final class AreaAdditionUtil {
   private AreaAdditionUtil() {
   }

   public static void addAreas(AbstractLayoutManager parentLM, PositionIterator parentIter, LayoutContext layoutContext) {
      LayoutContext lc = LayoutContext.offspringOf(layoutContext);
      LayoutManager firstLM = null;
      LayoutManager lastLM = null;
      Position firstPos = null;
      Position lastPos = null;
      if (parentLM != null) {
         parentLM.addId();
      }

      LinkedList positionList = new LinkedList();

      while(parentIter.hasNext()) {
         Position pos = parentIter.next();
         if (pos != null) {
            if (pos.getIndex() >= 0) {
               if (firstPos == null) {
                  firstPos = pos;
               }

               lastPos = pos;
            }

            if (pos instanceof NonLeafPosition) {
               positionList.add(pos.getPosition());
               lastLM = pos.getPosition().getLM();
               if (firstLM == null) {
                  firstLM = lastLM;
               }
            } else if (pos instanceof SpaceResolver.SpaceHandlingBreakPosition) {
               positionList.add(pos);
            }
         }
      }

      if (firstPos != null) {
         if (parentLM != null) {
            parentLM.registerMarkers(true, parentLM.isFirst(firstPos), parentLM.isLast(lastPos));
         }

         PositionIterator childPosIter = new PositionIterator(positionList.listIterator());

         LayoutManager childLM;
         while((childLM = childPosIter.getNextChildLM()) != null) {
            lc.setFlags(4, childLM == firstLM);
            lc.setFlags(8, childLM == lastLM);
            lc.setSpaceAdjust(layoutContext.getSpaceAdjust());
            lc.setSpaceBefore(childLM == firstLM ? layoutContext.getSpaceBefore() : 0);
            lc.setSpaceAfter(layoutContext.getSpaceAfter());
            lc.setStackLimitBP(layoutContext.getStackLimitBP());
            childLM.addAreas(childPosIter, lc);
         }

         if (parentLM != null) {
            parentLM.registerMarkers(false, parentLM.isFirst(firstPos), parentLM.isLast(lastPos));
         }

      }
   }
}
