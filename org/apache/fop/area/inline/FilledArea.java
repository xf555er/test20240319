package org.apache.fop.area.inline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FilledArea extends InlineParent {
   private static final long serialVersionUID = 8586584705587017474L;
   private int unitWidth;

   public void setUnitWidth(int width) {
      this.unitWidth = width;
   }

   public int getUnitWidth() {
      return this.unitWidth;
   }

   public int getBPD() {
      int bpd = 0;
      Iterator var2 = this.getChildAreas().iterator();

      while(var2.hasNext()) {
         InlineArea area = (InlineArea)var2.next();
         if (bpd < area.getBPD()) {
            bpd = area.getBPD();
         }
      }

      return bpd;
   }

   public List getChildAreas() {
      int units = this.getIPD() / this.unitWidth;
      List newList = new ArrayList();

      for(int count = 0; count < units; ++count) {
         newList.addAll(this.inlines);
      }

      return newList;
   }

   public boolean applyVariationFactor(double variationFactor, int lineStretch, int lineShrink) {
      this.setIPD(this.getIPD() + this.adjustingInfo.applyVariationFactor(variationFactor));
      return false;
   }
}
