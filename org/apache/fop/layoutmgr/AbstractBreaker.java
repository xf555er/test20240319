package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.util.ListUtil;

public abstract class AbstractBreaker {
   protected static final Log log = LogFactory.getLog(AbstractBreaker.class);
   protected LayoutManager originalRestartAtLM;
   protected Position positionAtBreak;
   protected List firstElementsForRestart;
   protected PageSequenceLayoutManager pslm;
   protected List blockLists;
   private boolean empty = true;
   protected int blockListIndex;
   protected int alignment;
   private int alignmentLast;
   protected MinOptMax footnoteSeparatorLength;

   public AbstractBreaker() {
      this.footnoteSeparatorLength = MinOptMax.ZERO;
   }

   static String getBreakClassName(int breakClassId) {
      switch (breakClassId) {
         case 5:
            return "ALL";
         case 8:
            return "ANY";
         case 9:
            return "AUTO";
         case 28:
            return "COLUMN";
         case 44:
            return "EVEN PAGE";
         case 75:
            return "LINE";
         case 95:
            return "NONE";
         case 100:
            return "ODD PAGE";
         case 104:
            return "PAGE";
         default:
            return "??? (" + String.valueOf(breakClassId) + ")";
      }
   }

   protected abstract int getCurrentDisplayAlign();

   protected abstract boolean hasMoreContent();

   protected abstract void addAreas(PositionIterator var1, LayoutContext var2);

   protected abstract LayoutManager getTopLevelLM();

   protected abstract LayoutManager getCurrentChildLM();

   protected boolean isPartOverflowRecoveryActivated() {
      return true;
   }

   protected boolean isSinglePartFavored() {
      return false;
   }

   protected PageProvider getPageProvider() {
      return null;
   }

   protected PageBreakingAlgorithm.PageBreakingLayoutListener createLayoutListener() {
      return null;
   }

   protected abstract List getNextKnuthElements(LayoutContext var1, int var2);

   protected List getNextKnuthElements(LayoutContext context, int alignment, Position positionAtIPDChange, LayoutManager restartAtLM) {
      throw new UnsupportedOperationException("TODO: implement acceptable fallback");
   }

   public boolean isEmpty() {
      return this.empty;
   }

   protected void startPart(BlockSequence list, int breakClass, boolean emptyContent) {
   }

   protected void handleEmptyContent() {
   }

   protected abstract void finishPart(PageBreakingAlgorithm var1, PageBreakPosition var2);

   protected LayoutContext createLayoutContext() {
      return LayoutContext.newInstance();
   }

   protected void updateLayoutContext(LayoutContext context) {
   }

   protected void observeElementList(List elementList) {
      ElementListObserver.observe(elementList, "breaker", (String)null);
   }

   public boolean doLayout(int flowBPD, boolean autoHeight) {
      LayoutContext childLC = this.createLayoutContext();
      childLC.setStackLimitBP(MinOptMax.getInstance(flowBPD));
      this.alignment = 135;
      this.alignmentLast = 135;
      childLC.setBPAlignment(this.alignment);
      this.blockLists = new ArrayList();
      log.debug("PLM> flow BPD =" + flowBPD);
      int nextSequenceStartsOn = 8;

      while(this.hasMoreContent()) {
         this.blockLists.clear();
         nextSequenceStartsOn = this.getNextBlockList(childLC, nextSequenceStartsOn);
         this.empty = this.empty && this.blockLists.size() == 0;
         log.debug("PLM> blockLists.size() = " + this.blockLists.size());

         for(this.blockListIndex = 0; this.blockListIndex < this.blockLists.size(); ++this.blockListIndex) {
            BlockSequence blockList = (BlockSequence)this.blockLists.get(this.blockListIndex);
            if (log.isDebugEnabled()) {
               log.debug("  blockListIndex = " + this.blockListIndex);
               log.debug("  sequence starts on " + getBreakClassName(blockList.startOn));
            }

            this.observeElementList(blockList);
            log.debug("PLM> start of algorithm (" + this.getClass().getName() + "), flow BPD =" + flowBPD);
            PageBreakingAlgorithm alg = new PageBreakingAlgorithm(this.getTopLevelLM(), this.getPageProvider(), this.createLayoutListener(), this.alignment, this.alignmentLast, this.footnoteSeparatorLength, this.isPartOverflowRecoveryActivated(), autoHeight, this.isSinglePartFavored());
            alg.setConstantLineWidth(flowBPD);
            int optimalPageCount = alg.findBreakingPoints(blockList, 1.0, true, 0);
            boolean ipdChangesOnNextPage = alg.getIPDdifference() != 0;
            boolean onLastPageAndIPDChanges = false;
            if (!ipdChangesOnNextPage) {
               onLastPageAndIPDChanges = this.lastPageHasIPDChange(optimalPageCount) && !this.thereIsANonRestartableLM(alg) && (this.shouldRedoLayout() || this.wasLayoutRedone() && optimalPageCount > 1);
            }

            if ((ipdChangesOnNextPage || this.hasMoreContent() || optimalPageCount > 1) && this.pslm != null && this.pslm.getCurrentPage().isPagePositionOnly) {
               return false;
            }

            if (alg.handlingFloat()) {
               nextSequenceStartsOn = this.handleFloatLayout(alg, optimalPageCount, blockList, childLC);
            } else if (!ipdChangesOnNextPage && !onLastPageAndIPDChanges) {
               log.debug("PLM> optimalPageCount= " + optimalPageCount + " pageBreaks.size()= " + alg.getPageBreaks().size());
               this.doPhase3(alg, optimalPageCount, blockList, blockList);
            } else {
               boolean visitedBefore = false;
               if (onLastPageAndIPDChanges) {
                  visitedBefore = this.wasLayoutRedone();
                  this.prepareToRedoLayout(alg, optimalPageCount, blockList, blockList);
               }

               this.firstElementsForRestart = null;
               RestartAtLM restartAtLMClass = new RestartAtLM();
               LayoutManager restartAtLM = restartAtLMClass.getRestartAtLM(this, alg, ipdChangesOnNextPage, onLastPageAndIPDChanges, visitedBefore, blockList, 1);
               if (restartAtLMClass.invalidPosition) {
                  return false;
               }

               if (restartAtLM == null || restartAtLM.getChildLMs().isEmpty()) {
                  this.firstElementsForRestart = null;
                  LayoutManager restartAtLM2 = (new RestartAtLM()).getRestartAtLM(this, alg, ipdChangesOnNextPage, onLastPageAndIPDChanges, visitedBefore, blockList, 0);
                  if (restartAtLM2 != null) {
                     restartAtLM = restartAtLM2;
                  }
               }

               if (ipdChangesOnNextPage) {
                  this.addAreas(alg, optimalPageCount, blockList, blockList);
               }

               this.blockLists.clear();
               this.blockListIndex = -1;
               nextSequenceStartsOn = this.getNextBlockList(childLC, 28, this.positionAtBreak, restartAtLM, this.firstElementsForRestart);
            }
         }
      }

      this.blockLists = null;
      return true;
   }

   protected boolean containsNonRestartableLM(Position position) {
      LayoutManager lm = position.getLM();
      if (lm != null && !lm.isRestartable()) {
         return true;
      } else {
         Position subPosition = position.getPosition();
         return subPosition != null && this.containsNonRestartableLM(subPosition);
      }
   }

   protected abstract void doPhase3(PageBreakingAlgorithm var1, int var2, BlockSequence var3, BlockSequence var4);

   protected void addAreas(PageBreakingAlgorithm alg, int partCount, BlockSequence originalList, BlockSequence effectiveList) {
      this.addAreas(alg, 0, partCount, originalList, effectiveList);
   }

   protected void addAreas(PageBreakingAlgorithm alg, int startPart, int partCount, BlockSequence originalList, BlockSequence effectiveList) {
      this.addAreas(alg, startPart, partCount, originalList, effectiveList, LayoutContext.newInstance());
   }

   protected void addAreas(PageBreakingAlgorithm alg, int startPart, int partCount, BlockSequence originalList, BlockSequence effectiveList, LayoutContext childLC) {
      int startElementIndex = 0;
      int endElementIndex = 0;
      int lastBreak = -1;

      for(int p = startPart; p < startPart + partCount; ++p) {
         PageBreakPosition pbp = (PageBreakPosition)alg.getPageBreaks().get(p);
         int lastBreakClass;
         if (p == 0) {
            lastBreakClass = effectiveList.getStartOn();
         } else {
            ListElement lastBreakElement = effectiveList.getElement(endElementIndex);
            if (lastBreakElement.isPenalty()) {
               KnuthPenalty pen = (KnuthPenalty)lastBreakElement;
               if (pen.getPenalty() == 1000) {
                  lastBreakClass = 28;
               } else {
                  lastBreakClass = pen.getBreakClass();
               }
            } else {
               lastBreakClass = 28;
            }
         }

         endElementIndex = pbp.getLeafPos();
         startElementIndex += startElementIndex == 0 ? effectiveList.ignoreAtStart : 0;
         log.debug("PLM> part: " + (p + 1) + ", start at pos " + startElementIndex + ", break at pos " + endElementIndex + ", break class = " + getBreakClassName(lastBreakClass));
         this.startPart(effectiveList, lastBreakClass, startElementIndex > endElementIndex);
         int displayAlign = this.getCurrentDisplayAlign();
         int notificationEndElementIndex = endElementIndex;
         endElementIndex -= endElementIndex == originalList.size() - 1 ? effectiveList.ignoreAtEnd : 0;
         if (((KnuthElement)effectiveList.get(endElementIndex)).isGlue()) {
            --endElementIndex;
         }

         startElementIndex = alg.par.getFirstBoxIndex(startElementIndex);
         if (startElementIndex <= endElementIndex) {
            if (log.isDebugEnabled()) {
               log.debug("     addAreas from " + startElementIndex + " to " + endElementIndex);
            }

            childLC.setSpaceAdjust(pbp.bpdAdjust);
            if (pbp.difference != 0 && displayAlign == 23) {
               childLC.setSpaceBefore(pbp.difference / 2);
            } else if (pbp.difference != 0 && displayAlign == 3) {
               childLC.setSpaceBefore(pbp.difference);
            }

            SpaceResolver.performConditionalsNotification(effectiveList, startElementIndex, notificationEndElementIndex, lastBreak);
            this.addAreas(new KnuthPossPosIter(effectiveList, startElementIndex, endElementIndex + 1), childLC);
         } else {
            this.handleEmptyContent();
         }

         this.finishPart(alg, pbp);
         lastBreak = endElementIndex;
         startElementIndex = pbp.getLeafPos() + 1;
      }

      if (alg.handlingFloat()) {
         this.addAreasForFloats(alg, startPart, partCount, originalList, effectiveList, childLC, lastBreak, startElementIndex, endElementIndex);
      }

   }

   protected int handleSpanChange(LayoutContext childLC, int nextSequenceStartsOn) {
      return nextSequenceStartsOn;
   }

   protected int getNextBlockList(LayoutContext childLC, int nextSequenceStartsOn) {
      return this.getNextBlockList(childLC, nextSequenceStartsOn, (Position)null, (LayoutManager)null, (List)null);
   }

   protected int getNextBlockList(LayoutContext childLC, int nextSequenceStartsOn, Position positionAtIPDChange, LayoutManager restartAtLM, List firstElements) {
      this.updateLayoutContext(childLC);
      childLC.signalSpanChange(0);
      List returnedList;
      if (firstElements == null) {
         returnedList = this.getNextKnuthElements(childLC, this.alignment);
      } else if (positionAtIPDChange == null) {
         returnedList = firstElements;
         if (firstElements.size() > 2) {
            ListIterator iter = firstElements.listIterator(firstElements.size());

            for(int i = 0; i < 3; ++i) {
               iter.previous();
               iter.remove();
            }
         }
      } else {
         returnedList = this.getNextKnuthElements(childLC, this.alignment, positionAtIPDChange, restartAtLM);
         returnedList.addAll(0, firstElements);
      }

      if (returnedList != null) {
         if (returnedList.isEmpty()) {
            nextSequenceStartsOn = this.handleSpanChange(childLC, nextSequenceStartsOn);
            return nextSequenceStartsOn;
         }

         BlockSequence blockList = new BlockSequence(nextSequenceStartsOn, this.getCurrentDisplayAlign());
         nextSequenceStartsOn = this.handleSpanChange(childLC, nextSequenceStartsOn);
         Position breakPosition = null;
         if (ElementListUtils.endsWithForcedBreak(returnedList)) {
            KnuthPenalty breakPenalty = (KnuthPenalty)ListUtil.removeLast(returnedList);
            breakPosition = breakPenalty.getPosition();
            log.debug("PLM> break - " + getBreakClassName(breakPenalty.getBreakClass()));
            switch (breakPenalty.getBreakClass()) {
               case 28:
                  nextSequenceStartsOn = 28;
                  break;
               case 44:
                  nextSequenceStartsOn = 44;
                  break;
               case 100:
                  nextSequenceStartsOn = 100;
                  break;
               case 104:
                  nextSequenceStartsOn = 8;
                  break;
               default:
                  throw new IllegalStateException("Invalid break class: " + breakPenalty.getBreakClass());
            }

            if (ElementListUtils.isEmptyBox(returnedList)) {
               ListUtil.removeLast(returnedList);
            }
         }

         blockList.addAll(returnedList);
         BlockSequence seq = blockList.endBlockSequence(breakPosition);
         if (seq != null) {
            this.blockLists.add(seq);
         }
      }

      return nextSequenceStartsOn;
   }

   protected boolean shouldRedoLayout() {
      return false;
   }

   protected void prepareToRedoLayout(PageBreakingAlgorithm alg, int partCount, BlockSequence originalList, BlockSequence effectiveList) {
   }

   protected boolean wasLayoutRedone() {
      return false;
   }

   private boolean thereIsANonRestartableLM(PageBreakingAlgorithm alg) {
      BreakingAlgorithm.KnuthNode optimalBreak = alg.getBestNodeForLastPage();
      if (optimalBreak != null) {
         int positionIndex = optimalBreak.position;
         KnuthElement elementAtBreak = alg.getElement(positionIndex);
         Position positionAtBreak = elementAtBreak.getPosition();
         if (!(positionAtBreak instanceof SpaceResolver.SpaceHandlingBreakPosition)) {
            return false;
         }

         positionAtBreak = positionAtBreak.getPosition();
         if (positionAtBreak != null && this.containsNonRestartableLM(positionAtBreak)) {
            return true;
         }
      }

      return false;
   }

   protected boolean lastPageHasIPDChange(int optimalPageCount) {
      return false;
   }

   protected int handleFloatLayout(PageBreakingAlgorithm alg, int optimalPageCount, BlockSequence blockList, LayoutContext childLC) {
      throw new IllegalStateException();
   }

   protected void addAreasForFloats(PageBreakingAlgorithm alg, int startPart, int partCount, BlockSequence originalList, BlockSequence effectiveList, LayoutContext childLC, int lastBreak, int startElementIndex, int endElementIndex) {
      throw new IllegalStateException();
   }

   public static class BlockSequence extends BlockKnuthSequence {
      private static final long serialVersionUID = -5348831120146774118L;
      int ignoreAtStart;
      int ignoreAtEnd;
      private final int startOn;
      private final int displayAlign;

      public BlockSequence(int startOn, int displayAlign) {
         this.startOn = startOn;
         this.displayAlign = displayAlign;
      }

      public int getStartOn() {
         return this.startOn;
      }

      public int getDisplayAlign() {
         return this.displayAlign;
      }

      public KnuthSequence endSequence() {
         return this.endSequence((Position)null);
      }

      public KnuthSequence endSequence(Position breakPosition) {
         while(this.size() > this.ignoreAtStart && !((KnuthElement)ListUtil.getLast(this)).isBox()) {
            ListUtil.removeLast(this);
         }

         if (this.size() > this.ignoreAtStart) {
            this.add(new KnuthPenalty(0, 1000, false, (Position)null, false));
            this.add(new KnuthGlue(0, 10000000, 0, (Position)null, false));
            this.add(new KnuthPenalty(0, -1000, false, breakPosition, false));
            this.ignoreAtEnd = 3;
            return this;
         } else {
            this.clear();
            return null;
         }
      }

      public BlockSequence endBlockSequence(Position breakPosition) {
         KnuthSequence temp = this.endSequence(breakPosition);
         if (temp != null) {
            BlockSequence returnSequence = new BlockSequence(this.startOn, this.displayAlign);
            returnSequence.addAll(temp);
            returnSequence.ignoreAtEnd = this.ignoreAtEnd;
            return returnSequence;
         } else {
            return null;
         }
      }
   }

   public static class FloatPosition extends LeafPosition {
      double bpdAdjust;
      int difference;

      FloatPosition(LayoutManager lm, int breakIndex, double bpdA, int diff) {
         super(lm, breakIndex);
         this.bpdAdjust = bpdA;
         this.difference = diff;
      }
   }

   public static class PageBreakPosition extends LeafPosition {
      double bpdAdjust;
      int difference;
      int footnoteFirstListIndex;
      int footnoteFirstElementIndex;
      int footnoteLastListIndex;
      int footnoteLastElementIndex;

      PageBreakPosition(LayoutManager lm, int breakIndex, int ffli, int ffei, int flli, int flei, double bpdA, int diff) {
         super(lm, breakIndex);
         this.bpdAdjust = bpdA;
         this.difference = diff;
         this.footnoteFirstListIndex = ffli;
         this.footnoteFirstElementIndex = ffei;
         this.footnoteLastListIndex = flli;
         this.footnoteLastElementIndex = flei;
      }
   }
}
