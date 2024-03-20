package org.apache.fop.layoutmgr;

import java.util.LinkedList;
import java.util.List;
import org.apache.fop.fo.FObj;
import org.apache.fop.layoutmgr.inline.TextLayoutManager;

public abstract class LocalBreaker extends AbstractBreaker {
   protected BlockStackingLayoutManager lm;
   private int displayAlign;
   private int ipd;
   private int overflow;
   private boolean repeatedHeader;
   private boolean isDescendantOfTableFooter;
   private boolean repeatedFooter;

   public void setRepeatedFooter(boolean repeatedFooter) {
      this.repeatedFooter = repeatedFooter;
   }

   public void setDescendantOfTableFooter(boolean isDescendantOfTableFooter) {
      this.isDescendantOfTableFooter = isDescendantOfTableFooter;
   }

   public LocalBreaker(BlockStackingLayoutManager lm, int ipd, int displayAlign) {
      this.lm = lm;
      this.ipd = ipd;
      this.displayAlign = displayAlign;
   }

   public void setRepeatedHeader(boolean repeatedHeader) {
      this.repeatedHeader = repeatedHeader;
   }

   protected boolean isPartOverflowRecoveryActivated() {
      return false;
   }

   public boolean isOverflow() {
      return this.overflow != 0;
   }

   public int getOverflowAmount() {
      return this.overflow;
   }

   protected PageBreakingAlgorithm.PageBreakingLayoutListener createLayoutListener() {
      return new PageBreakingAlgorithm.PageBreakingLayoutListener() {
         public void notifyOverflow(int part, int amount, FObj obj) {
            if (LocalBreaker.this.overflow == 0) {
               LocalBreaker.this.overflow = amount;
            }

         }
      };
   }

   protected LayoutManager getTopLevelLM() {
      return this.lm;
   }

   protected LayoutContext createLayoutContext() {
      LayoutContext lc = super.createLayoutContext();
      lc.setRefIPD(this.ipd);
      return lc;
   }

   protected List getNextKnuthElements(LayoutContext context, int alignment) {
      List returnList = new LinkedList();

      LayoutManager curLM;
      while((curLM = this.lm.getChildLM()) != null) {
         LayoutContext childLC = LayoutContext.newInstance();
         childLC.setStackLimitBP(context.getStackLimitBP());
         childLC.setRefIPD(context.getRefIPD());
         childLC.setWritingMode(context.getWritingMode());
         List returnedList = null;
         boolean ignore = curLM instanceof TextLayoutManager;
         if (!curLM.isFinished()) {
            returnedList = curLM.getNextKnuthElements(childLC, alignment);
         }

         if (returnedList != null && !ignore) {
            this.lm.wrapPositionElements(returnedList, returnList);
         }
      }

      SpaceResolver.resolveElementList(returnList);
      this.lm.setFinished(true);
      return returnList;
   }

   protected int getCurrentDisplayAlign() {
      return this.displayAlign;
   }

   protected boolean hasMoreContent() {
      return !this.lm.isFinished();
   }

   protected void addAreas(PositionIterator posIter, LayoutContext context) {
      if (this.isDescendantOfTableFooter) {
         if (this.repeatedHeader) {
            context.setTreatAsArtifact(true);
         }
      } else if (this.repeatedFooter) {
         context.setTreatAsArtifact(true);
      }

      AreaAdditionUtil.addAreas(this.lm, posIter, context);
   }

   protected void doPhase3(PageBreakingAlgorithm alg, int partCount, AbstractBreaker.BlockSequence originalList, AbstractBreaker.BlockSequence effectiveList) {
      if (partCount > 1) {
         AbstractBreaker.PageBreakPosition pos = (AbstractBreaker.PageBreakPosition)alg.getPageBreaks().getFirst();
         int firstPartLength = ElementListUtils.calcContentLength(effectiveList, effectiveList.ignoreAtStart, pos.getLeafPos());
         this.overflow += alg.totalWidth - firstPartLength;
      }

      alg.removeAllPageBreaks();
      this.addAreas(alg, 1, originalList, effectiveList);
   }

   protected void finishPart(PageBreakingAlgorithm alg, AbstractBreaker.PageBreakPosition pbp) {
   }

   protected LayoutManager getCurrentChildLM() {
      return null;
   }
}
