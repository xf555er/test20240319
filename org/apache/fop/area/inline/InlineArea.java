package org.apache.fop.area.inline;

import java.io.Serializable;
import java.util.List;
import org.apache.fop.area.Area;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.Trait;
import org.apache.fop.complexscripts.bidi.InlineRun;

public class InlineArea extends Area {
   private static final long serialVersionUID = -8940066479810170980L;
   protected int blockProgressionOffset;
   private Area parentArea;
   private int storedIPDVariation;
   protected InlineAdjustingInfo adjustingInfo;

   public InlineArea() {
      this(0, -1);
   }

   protected InlineArea(int blockProgressionOffset, int bidiLevel) {
      this.blockProgressionOffset = blockProgressionOffset;
      this.setBidiLevel(bidiLevel);
   }

   public InlineAdjustingInfo getAdjustingInfo() {
      return this.adjustingInfo;
   }

   public void setAdjustingInfo(int stretch, int shrink, int adjustment) {
      this.adjustingInfo = new InlineAdjustingInfo(stretch, shrink, adjustment);
   }

   public void setAdjustingInfo(InlineAdjustingInfo adjustingInfo) {
      this.adjustingInfo = adjustingInfo;
   }

   public void setAdjustment(int adjustment) {
      if (this.adjustingInfo != null) {
         this.adjustingInfo.adjustment = adjustment;
      }

   }

   public void increaseIPD(int ipd) {
      this.ipd += ipd;
   }

   public void setBlockProgressionOffset(int blockProgressionOffset) {
      this.blockProgressionOffset = blockProgressionOffset;
   }

   public int getBlockProgressionOffset() {
      return this.blockProgressionOffset;
   }

   public void setParentArea(Area parentArea) {
      this.parentArea = parentArea;
   }

   public Area getParentArea() {
      return this.parentArea;
   }

   public void addChildArea(Area childArea) {
      super.addChildArea(childArea);
      if (childArea instanceof InlineArea) {
         ((InlineArea)childArea).setParentArea(this);
      }

   }

   public boolean hasUnderline() {
      return this.getTraitAsBoolean(Trait.UNDERLINE);
   }

   public boolean hasOverline() {
      return this.getTraitAsBoolean(Trait.OVERLINE);
   }

   public boolean hasLineThrough() {
      return this.getTraitAsBoolean(Trait.LINETHROUGH);
   }

   public boolean isBlinking() {
      return this.getTraitAsBoolean(Trait.BLINK);
   }

   public boolean applyVariationFactor(double variationFactor, int lineStretch, int lineShrink) {
      if (this.adjustingInfo != null) {
         this.setIPD(this.getIPD() + this.adjustingInfo.applyVariationFactor(variationFactor));
      }

      return false;
   }

   public void handleIPDVariation(int ipdVariation) {
      if (log.isTraceEnabled()) {
         log.trace("Handling IPD variation for " + this.getClass().getSimpleName() + ": increase by " + ipdVariation + " mpt.");
      }

      if (ipdVariation != 0) {
         this.increaseIPD(ipdVariation);
         this.notifyIPDVariation(ipdVariation);
      }

   }

   protected void notifyIPDVariation(int ipdVariation) {
      Area parentArea = this.getParentArea();
      if (parentArea instanceof InlineArea) {
         ((InlineArea)parentArea).handleIPDVariation(ipdVariation);
      } else if (parentArea instanceof LineArea) {
         ((LineArea)parentArea).handleIPDVariation(ipdVariation);
      } else if (parentArea == null) {
         this.storedIPDVariation += ipdVariation;
      }

   }

   int getVirtualOffset() {
      return this.getBlockProgressionOffset();
   }

   int getVirtualBPD() {
      return this.getBPD();
   }

   public List collectInlineRuns(List runs) {
      assert runs != null;

      runs.add(new InlineRun(this, new int[]{this.getBidiLevel()}));
      return runs;
   }

   public boolean isAncestorOrSelf(InlineArea ia) {
      return ia == this || this.isAncestor(ia);
   }

   public boolean isAncestor(InlineArea ia) {
      Area p = this.getParentArea();

      while(p != null) {
         if (p == ia) {
            return true;
         }

         if (p instanceof InlineArea) {
            p = ((InlineArea)p).getParentArea();
         } else {
            p = null;
         }
      }

      return false;
   }

   protected class InlineAdjustingInfo implements Serializable {
      private static final long serialVersionUID = -5601387735459712149L;
      protected int availableStretch;
      protected int availableShrink;
      protected int adjustment;

      protected InlineAdjustingInfo(int stretch, int shrink, int adj) {
         this.availableStretch = stretch;
         this.availableShrink = shrink;
         this.adjustment = adj;
      }

      protected int applyVariationFactor(double variationFactor) {
         int oldAdjustment = this.adjustment;
         this.adjustment = (int)((double)this.adjustment * variationFactor);
         return this.adjustment - oldAdjustment;
      }
   }
}
