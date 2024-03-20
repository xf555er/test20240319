package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.List;

public class WhitespaceManagementPenalty extends KnuthPenalty {
   private final MultiSwitchLayoutManager.WhitespaceManagementPosition whitespaceManagementPosition;
   private final List variantList;

   public WhitespaceManagementPenalty(MultiSwitchLayoutManager.WhitespaceManagementPosition pos) {
      super(0, 0, false, pos, false);
      this.whitespaceManagementPosition = pos;
      this.variantList = new ArrayList();
   }

   public void addVariant(Variant variant) {
      this.variantList.add(variant);
   }

   public void setActiveVariant(Variant bestVariant) {
      this.whitespaceManagementPosition.setKnuthList(bestVariant.knuthList);
   }

   public boolean hasActiveVariant() {
      return this.whitespaceManagementPosition.getKnuthList() != null;
   }

   public List getVariants() {
      return this.variantList;
   }

   public String toString() {
      String str = super.toString();
      StringBuffer buffer = new StringBuffer(64);
      buffer.append(" number of variants = " + this.variantList.size());
      return str + buffer;
   }

   public class Variant {
      public final List knuthList;
      public final int width;
      private final KnuthPenalty penalty;

      public Variant(List knuthList, int width) {
         this.knuthList = knuthList;
         this.width = width;
         this.penalty = new KnuthPenalty(width, 0, false, (Position)null, false);
      }

      public KnuthElement getPenalty() {
         return this.penalty;
      }

      public WhitespaceManagementPenalty getWhitespaceManagementPenalty() {
         return WhitespaceManagementPenalty.this;
      }
   }
}
