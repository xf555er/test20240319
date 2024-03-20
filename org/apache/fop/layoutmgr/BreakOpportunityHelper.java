package org.apache.fop.layoutmgr;

import org.apache.fop.fo.properties.BreakPropertySet;
import org.apache.fop.util.BreakUtil;

public final class BreakOpportunityHelper {
   private BreakOpportunityHelper() {
   }

   public static int getBreakBefore(AbstractLayoutManager layoutManager) {
      int breakBefore = 9;
      if (layoutManager.getFObj() instanceof BreakPropertySet) {
         breakBefore = ((BreakPropertySet)layoutManager.getFObj()).getBreakBefore();
      }

      LayoutManager childLM = layoutManager.getChildLM();
      if (childLM instanceof BreakOpportunity) {
         BreakOpportunity bo = (BreakOpportunity)childLM;
         breakBefore = BreakUtil.compareBreakClasses(breakBefore, bo.getBreakBefore());
      }

      return breakBefore;
   }
}
