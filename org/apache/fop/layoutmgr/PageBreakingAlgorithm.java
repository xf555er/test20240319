package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.FObj;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.util.ListUtil;

class PageBreakingAlgorithm extends BreakingAlgorithm {
   private static Log log = LogFactory.getLog(PageBreakingAlgorithm.class);
   private final LayoutManager topLevelLM;
   private final PageProvider pageProvider;
   private final PageBreakingLayoutListener layoutListener;
   private LinkedList pageBreaks;
   private List footnotesList;
   private List lengthList;
   private int totalFootnotesLength;
   private int insertedFootnotesLength;
   private boolean footnotesPending;
   private boolean newFootnotes;
   private int firstNewFootnoteIndex;
   private int footnoteListIndex;
   private int footnoteElementIndex = -1;
   private final int splitFootnoteDemerits = 5000;
   private final int deferredFootnoteDemerits = 10000;
   private MinOptMax footnoteSeparatorLength;
   private int storedPrevBreakIndex = -1;
   private int storedBreakIndex = -1;
   private boolean storedValue;
   private boolean autoHeight;
   private boolean favorSinglePart;
   private int ipdDifference;
   private BreakingAlgorithm.KnuthNode bestNodeForIPDChange;
   public BreakingAlgorithm.KnuthNode bestNodeForLastPage;
   private int currentKeepContext = 9;
   private BreakingAlgorithm.KnuthNode lastBeforeKeepContextSwitch;
   private boolean handlingStartOfFloat;
   private boolean handlingEndOfFloat;
   private int floatHeight;
   private BreakingAlgorithm.KnuthNode bestFloatEdgeNode;
   private AbstractBreaker.FloatPosition floatPosition;
   private int previousFootnoteListIndex = -2;
   private int previousFootnoteElementIndex = -2;
   private boolean relayingFootnotes;

   public PageBreakingAlgorithm(LayoutManager topLevelLM, PageProvider pageProvider, PageBreakingLayoutListener layoutListener, int alignment, int alignmentLast, MinOptMax footnoteSeparatorLength, boolean partOverflowRecovery, boolean autoHeight, boolean favorSinglePart) {
      super(alignment, alignmentLast, true, partOverflowRecovery, 0);
      this.topLevelLM = topLevelLM;
      this.pageProvider = pageProvider;
      this.layoutListener = layoutListener;
      this.best = new BestPageRecords();
      this.footnoteSeparatorLength = footnoteSeparatorLength;
      this.autoHeight = autoHeight;
      this.favorSinglePart = favorSinglePart;
   }

   protected void initialize() {
      super.initialize();
      this.insertedFootnotesLength = 0;
      this.footnoteListIndex = 0;
      this.footnoteElementIndex = -1;
      if (this.topLevelLM instanceof PageSequenceLayoutManager) {
         PageSequenceLayoutManager pslm = (PageSequenceLayoutManager)this.topLevelLM;
         if (pslm.handlingStartOfFloat() || pslm.handlingEndOfFloat()) {
            pslm.retrieveFootnotes(this);
         }

         if (pslm.handlingStartOfFloat()) {
            this.floatHeight = Math.min(pslm.getFloatHeight(), this.lineWidth - pslm.getFloatYOffset());
         }

         if (pslm.handlingEndOfFloat()) {
            this.totalWidth += pslm.getOffsetDueToFloat() + this.insertedFootnotesLength;
         }
      }

   }

   protected BreakingAlgorithm.KnuthNode recoverFromTooLong(BreakingAlgorithm.KnuthNode lastTooLong) {
      if (log.isDebugEnabled()) {
         log.debug("Recovering from too long: " + lastTooLong);
         log.debug("\tlastTooShort = " + this.getLastTooShort());
         log.debug("\tlastBeforeKeepContextSwitch = " + this.lastBeforeKeepContextSwitch);
         log.debug("\tcurrentKeepContext = " + AbstractBreaker.getBreakClassName(this.currentKeepContext));
      }

      if (this.lastBeforeKeepContextSwitch != null && this.currentKeepContext != 9) {
         BreakingAlgorithm.KnuthNode node = this.lastBeforeKeepContextSwitch;

         for(this.lastBeforeKeepContextSwitch = null; !this.pageProvider.endPage(node.line - 1); node = this.createNode(node.position, node.line + 1, 1, 0, 0, 0, 0.0, 0, 0, 0, 0.0, node)) {
            log.trace("Adding node for empty column");
         }

         return node;
      } else {
         return super.recoverFromTooLong(lastTooLong);
      }
   }

   protected BreakingAlgorithm.KnuthNode compareNodes(BreakingAlgorithm.KnuthNode node1, BreakingAlgorithm.KnuthNode node2) {
      if (node1 != null && node2 != null) {
         if (this.pageProvider != null) {
            if (this.pageProvider.endPage(node1.line - 1) && !this.pageProvider.endPage(node2.line - 1)) {
               return node1;
            }

            if (this.pageProvider.endPage(node2.line - 1) && !this.pageProvider.endPage(node1.line - 1)) {
               return node2;
            }
         }

         return super.compareNodes(node1, node2);
      } else {
         return node1 == null ? node2 : node1;
      }
   }

   protected BreakingAlgorithm.KnuthNode createNode(int position, int line, int fitness, int totalWidth, int totalStretch, int totalShrink, double adjustRatio, int availableShrink, int availableStretch, int difference, double totalDemerits, BreakingAlgorithm.KnuthNode previous) {
      return new KnuthPageNode(position, line, fitness, totalWidth, totalStretch, totalShrink, this.insertedFootnotesLength, this.totalFootnotesLength, this.footnoteListIndex, this.footnoteElementIndex, adjustRatio, availableShrink, availableStretch, difference, totalDemerits, previous);
   }

   protected BreakingAlgorithm.KnuthNode createNode(int position, int line, int fitness, int totalWidth, int totalStretch, int totalShrink) {
      return new KnuthPageNode(position, line, fitness, totalWidth, totalStretch, totalShrink, ((BestPageRecords)this.best).getInsertedFootnotesLength(fitness), ((BestPageRecords)this.best).getTotalFootnotesLength(fitness), ((BestPageRecords)this.best).getFootnoteListIndex(fitness), ((BestPageRecords)this.best).getFootnoteElementIndex(fitness), this.best.getAdjust(fitness), this.best.getAvailableShrink(fitness), this.best.getAvailableStretch(fitness), this.best.getDifference(fitness), this.best.getDemerits(fitness), this.best.getNode(fitness));
   }

   protected void handleBox(KnuthBox box) {
      super.handleBox(box);
      if (box instanceof KnuthBlockBox && ((KnuthBlockBox)box).hasAnchors()) {
         this.handleFootnotes(((KnuthBlockBox)box).getElementLists());
         if (!this.newFootnotes) {
            this.newFootnotes = true;
            this.firstNewFootnoteIndex = this.footnotesList.size() - 1;
         }
      }

      if (box instanceof KnuthBlockBox && ((KnuthBlockBox)box).hasFloatAnchors()) {
         this.handlingStartOfFloat = true;
      }

      if (this.floatHeight != 0 && this.totalWidth >= this.floatHeight) {
         this.handlingEndOfFloat = true;
      }

   }

   protected void handlePenaltyAt(KnuthPenalty penalty, int position, int allowedBreaks) {
      super.handlePenaltyAt(penalty, position, allowedBreaks);
      if (penalty.getPenalty() == 1000) {
         int breakClass = penalty.getBreakClass();
         if (breakClass == 104 || breakClass == 28) {
            this.considerLegalBreak(penalty, position);
         }
      }

   }

   private void handleFootnotes(List elementLists) {
      if (!this.footnotesPending) {
         this.footnotesPending = true;
         this.footnotesList = new ArrayList();
         this.lengthList = new ArrayList();
         this.totalFootnotesLength = 0;
      }

      if (!this.newFootnotes) {
         this.newFootnotes = true;
         this.firstNewFootnoteIndex = this.footnotesList.size();
      }

      int noteLength;
      label49:
      for(Iterator var2 = elementLists.iterator(); var2.hasNext(); this.totalFootnotesLength += noteLength) {
         List noteList = (List)var2.next();
         noteLength = 0;
         this.footnotesList.add(noteList);
         Iterator var5 = noteList.iterator();

         while(true) {
            KnuthElement element;
            do {
               if (!var5.hasNext()) {
                  int prevLength = this.lengthList != null && !this.lengthList.isEmpty() ? (Integer)ListUtil.getLast(this.lengthList) : 0;
                  if (this.lengthList != null) {
                     this.lengthList.add(prevLength + noteLength);
                  }
                  continue label49;
               }

               element = (KnuthElement)var5.next();
            } while(!element.isBox() && !element.isGlue());

            noteLength += element.getWidth();
         }
      }

   }

   protected int restartFrom(BreakingAlgorithm.KnuthNode restartingNode, int currentIndex) {
      int returnValue = super.restartFrom(restartingNode, currentIndex);
      this.newFootnotes = false;
      if (this.footnotesPending) {
         for(int j = currentIndex; j >= restartingNode.position; --j) {
            KnuthElement resetElement = this.getElement(j);
            if (resetElement instanceof KnuthBlockBox && ((KnuthBlockBox)resetElement).hasAnchors()) {
               this.resetFootnotes(((KnuthBlockBox)resetElement).getElementLists());
            }
         }

         assert restartingNode instanceof KnuthPageNode;

         KnuthPageNode restartingPageNode = (KnuthPageNode)restartingNode;
         this.footnoteElementIndex = restartingPageNode.footnoteElementIndex;
         this.footnoteListIndex = restartingPageNode.footnoteListIndex;
         this.totalFootnotesLength = restartingPageNode.totalFootnotes;
         this.insertedFootnotesLength = restartingPageNode.insertedFootnotes;
      }

      return returnValue;
   }

   private void resetFootnotes(List elementLists) {
      for(int i = 0; i < elementLists.size(); ++i) {
         ListUtil.removeLast(this.footnotesList);
         ListUtil.removeLast(this.lengthList);
      }

      if (this.footnotesList.size() == 0) {
         this.footnotesPending = false;
      }

   }

   protected void considerLegalBreak(KnuthElement element, int elementIdx) {
      if (element.isPenalty()) {
         int breakClass = ((KnuthPenalty)element).getBreakClass();
         switch (breakClass) {
            case 9:
               this.currentKeepContext = breakClass;
               break;
            case 28:
               if (this.currentKeepContext != breakClass) {
                  this.lastBeforeKeepContextSwitch = this.getLastTooShort();
               }

               this.currentKeepContext = breakClass;
               break;
            case 104:
               if (this.currentKeepContext != breakClass) {
                  this.lastBeforeKeepContextSwitch = this.getLastTooShort();
               }

               this.currentKeepContext = breakClass;
         }
      }

      super.considerLegalBreak(element, elementIdx);
      this.newFootnotes = false;
   }

   protected boolean elementCanEndLine(KnuthElement element, int line, int difference) {
      if (element.isPenalty() && this.pageProvider != null) {
         KnuthPenalty p = (KnuthPenalty)element;
         if (p.getPenalty() <= 0) {
            return true;
         } else {
            int context = p.getBreakClass();
            switch (context) {
               case 9:
                  log.debug("keep is not auto but context is");
                  return true;
               case 28:
               case 75:
                  return p.getPenalty() < 1000;
               case 104:
                  return p.getPenalty() < 1000 || !this.pageProvider.endPage(line - 1);
               default:
                  if (p.getPenalty() < 1000) {
                     log.debug("Non recognized keep context:" + context);
                     return true;
                  } else {
                     return false;
                  }
            }
         }
      } else {
         return true;
      }
   }

   protected int computeDifference(BreakingAlgorithm.KnuthNode activeNode, KnuthElement element, int elementIndex) {
      KnuthPageNode pageNode = (KnuthPageNode)activeNode;
      int actualWidth = this.totalWidth - pageNode.totalWidth;
      actualWidth += pageNode.totalVariantsWidth;
      if (element instanceof WhitespaceManagementPenalty) {
         actualWidth += this.handleWhitespaceManagementPenalty(pageNode, (WhitespaceManagementPenalty)element, elementIndex);
      } else if (element.isPenalty()) {
         actualWidth += element.getWidth();
      }

      int allFootnotes;
      if (this.footnotesPending) {
         allFootnotes = this.totalFootnotesLength - pageNode.insertedFootnotes;
         if (allFootnotes > 0) {
            actualWidth += this.footnoteSeparatorLength.getOpt();
            if (actualWidth + allFootnotes <= this.getLineWidth(activeNode.line)) {
               actualWidth += allFootnotes;
               this.insertedFootnotesLength = pageNode.insertedFootnotes + allFootnotes;
               this.footnoteListIndex = this.footnotesList.size() - 1;
               this.footnoteElementIndex = this.getFootnoteList(this.footnoteListIndex).size() - 1;
            } else {
               int footnoteSplit;
               boolean canDeferOldFN;
               if (((canDeferOldFN = this.canDeferOldFootnotes(pageNode, elementIndex)) || this.newFootnotes) && (footnoteSplit = this.getFootnoteSplit(pageNode, this.getLineWidth(activeNode.line) - actualWidth, canDeferOldFN)) > 0) {
                  actualWidth += footnoteSplit;
                  this.insertedFootnotesLength = pageNode.insertedFootnotes + footnoteSplit;
               } else {
                  actualWidth += allFootnotes;
                  this.insertedFootnotesLength = pageNode.insertedFootnotes + allFootnotes;
                  this.footnoteListIndex = this.footnotesList.size() - 1;
                  this.footnoteElementIndex = this.getFootnoteList(this.footnoteListIndex).size() - 1;
               }
            }
         }
      }

      allFootnotes = this.getLineWidth(activeNode.line) - actualWidth;
      return this.autoHeight && allFootnotes < 0 ? 0 : allFootnotes;
   }

   private int handleWhitespaceManagementPenalty(KnuthPageNode activeNode, WhitespaceManagementPenalty penalty, int elementIndex) {
      Iterator var4 = penalty.getVariants().iterator();

      WhitespaceManagementPenalty.Variant var;
      double r;
      do {
         if (!var4.hasNext()) {
            return 0;
         }

         var = (WhitespaceManagementPenalty.Variant)var4.next();
         int difference = this.computeDifference(activeNode, var.getPenalty(), elementIndex);
         r = this.computeAdjustmentRatio(activeNode, difference);
      } while(!(r >= -1.0));

      activeNode.addVariant(var);
      return var.width;
   }

   private boolean canDeferOldFootnotes(KnuthPageNode node, int contentElementIndex) {
      return this.noBreakBetween(node.position, contentElementIndex) && this.deferredFootnotes(node.footnoteListIndex, node.footnoteElementIndex, node.insertedFootnotes);
   }

   private boolean noBreakBetween(int prevBreakIndex, int breakIndex) {
      if (this.storedPrevBreakIndex == -1 || (prevBreakIndex < this.storedPrevBreakIndex || breakIndex != this.storedBreakIndex || !this.storedValue) && (prevBreakIndex > this.storedPrevBreakIndex || breakIndex < this.storedBreakIndex || this.storedValue)) {
         int index;
         for(index = prevBreakIndex + 1; !this.par.getElement(index).isBox(); ++index) {
         }

         while(index < breakIndex && (!this.par.getElement(index).isGlue() || !this.par.getElement(index - 1).isBox()) && (!this.par.getElement(index).isPenalty() || ((KnuthElement)this.par.getElement(index)).getPenalty() >= 1000)) {
            ++index;
         }

         this.storedPrevBreakIndex = prevBreakIndex;
         this.storedBreakIndex = breakIndex;
         this.storedValue = index == breakIndex;
      }

      return this.storedValue;
   }

   private boolean deferredFootnotes(int listIndex, int elementIndex, int length) {
      return this.newFootnotes && this.firstNewFootnoteIndex != 0 && (listIndex < this.firstNewFootnoteIndex - 1 || elementIndex < this.getFootnoteList(listIndex).size() - 1) || length < this.totalFootnotesLength;
   }

   private int getFootnoteSplit(KnuthPageNode activeNode, int availableLength, boolean canDeferOldFootnotes) {
      return this.getFootnoteSplit(activeNode.footnoteListIndex, activeNode.footnoteElementIndex, activeNode.insertedFootnotes, availableLength, canDeferOldFootnotes);
   }

   private int getFootnoteSplit(int prevListIndex, int prevElementIndex, int prevLength, int availableLength, boolean canDeferOldFootnotes) {
      if (availableLength <= 0) {
         return 0;
      } else {
         int splitLength = 0;
         boolean somethingAdded = false;
         int listIndex = prevListIndex;
         int elementIndex;
         if (prevElementIndex == this.getFootnoteList(prevListIndex).size() - 1) {
            listIndex = prevListIndex + 1;
            elementIndex = 0;
         } else {
            elementIndex = prevElementIndex + 1;
         }

         if (this.footnotesList.size() - 1 > listIndex) {
            if (!canDeferOldFootnotes && this.newFootnotes && this.firstNewFootnoteIndex > 0) {
               splitLength = (Integer)this.lengthList.get(this.firstNewFootnoteIndex - 1) - prevLength;
               listIndex = this.firstNewFootnoteIndex;
               elementIndex = 0;
            }

            while((Integer)this.lengthList.get(listIndex) - prevLength <= availableLength) {
               splitLength = (Integer)this.lengthList.get(listIndex) - prevLength;
               somethingAdded = true;
               ++listIndex;
               elementIndex = 0;
            }
         }

         ListIterator noteListIterator = this.getFootnoteList(listIndex).listIterator(elementIndex);
         int prevSplitLength = 0;
         int prevIndex = -1;
         int index = -1;

         while(true) {
            while(splitLength <= availableLength) {
               if (somethingAdded) {
                  prevSplitLength = splitLength;
                  prevIndex = index;
               }

               boolean boxPreceding = false;

               while(noteListIterator.hasNext()) {
                  KnuthElement element = (KnuthElement)noteListIterator.next();
                  if (element.isBox()) {
                     splitLength += element.getWidth();
                     boxPreceding = true;
                     if (splitLength > prevSplitLength) {
                        somethingAdded = true;
                     }
                  } else if (element.isGlue()) {
                     if (boxPreceding) {
                        index = noteListIterator.previousIndex();
                        break;
                     }

                     boxPreceding = false;
                     splitLength += element.getWidth();
                  } else {
                     if (element.getPenalty() < 1000) {
                        index = noteListIterator.previousIndex();
                        break;
                     }

                     boxPreceding = false;
                  }
               }
            }

            if (!somethingAdded) {
               prevSplitLength = 0;
            } else if (prevSplitLength > 0) {
               this.footnoteListIndex = prevIndex != -1 ? listIndex : listIndex - 1;
               this.footnoteElementIndex = prevIndex != -1 ? prevIndex : this.getFootnoteList(this.footnoteListIndex).size() - 1;
            }

            return prevSplitLength;
         }
      }
   }

   protected double computeAdjustmentRatio(BreakingAlgorithm.KnuthNode activeNode, int difference) {
      int maxAdjustment;
      if (difference > 0) {
         maxAdjustment = this.totalStretch - activeNode.totalStretch;
         if (((KnuthPageNode)activeNode).insertedFootnotes < this.totalFootnotesLength) {
            maxAdjustment += this.footnoteSeparatorLength.getStretch();
         }

         return maxAdjustment > 0 ? (double)difference / (double)maxAdjustment : 1000.0;
      } else if (difference < 0) {
         maxAdjustment = this.totalShrink - activeNode.totalShrink;
         if (((KnuthPageNode)activeNode).insertedFootnotes < this.totalFootnotesLength) {
            maxAdjustment += this.footnoteSeparatorLength.getShrink();
         }

         return maxAdjustment > 0 ? (double)difference / (double)maxAdjustment : -1000.0;
      } else {
         return 0.0;
      }
   }

   protected double computeDemerits(BreakingAlgorithm.KnuthNode activeNode, KnuthElement element, int fitnessClass, double r) {
      double demerits = 0.0;
      double f = Math.abs(r);
      f = 1.0 + 100.0 * f * f * f;
      if (element.isPenalty()) {
         double penalty = (double)element.getPenalty();
         if (penalty >= 0.0) {
            f += penalty;
            demerits = f * f;
         } else if (!element.isForcedBreak()) {
            demerits = f * f - penalty * penalty;
         } else {
            demerits = f * f;
         }
      } else {
         demerits = f * f;
      }

      if (element.isPenalty() && ((KnuthPenalty)element).isPenaltyFlagged() && this.getElement(activeNode.position).isPenalty() && ((KnuthPenalty)this.getElement(activeNode.position)).isPenaltyFlagged()) {
         demerits += (double)this.repeatedFlaggedDemerit;
      }

      if (Math.abs(fitnessClass - activeNode.fitness) > 1) {
         demerits += (double)this.incompatibleFitnessDemerit;
      }

      if (this.footnotesPending) {
         if (this.footnoteListIndex < this.footnotesList.size() - 1) {
            demerits += (double)((this.footnotesList.size() - 1 - this.footnoteListIndex) * 10000);
         }

         if (this.footnoteListIndex < this.footnotesList.size() && this.footnoteElementIndex < this.getFootnoteList(this.footnoteListIndex).size() - 1) {
            demerits += 5000.0;
         }
      }

      demerits += activeNode.totalDemerits;
      return demerits;
   }

   protected void finish() {
      for(int i = this.startLine; i < this.endLine; ++i) {
         for(KnuthPageNode node = (KnuthPageNode)this.getNode(i); node != null; node = (KnuthPageNode)node.next) {
            if (node.insertedFootnotes < this.totalFootnotesLength) {
               this.createFootnotePages(node);
            }
         }
      }

   }

   private void createFootnotePages(KnuthPageNode lastNode) {
      this.insertedFootnotesLength = lastNode.insertedFootnotes;
      this.footnoteListIndex = lastNode.footnoteListIndex;
      this.footnoteElementIndex = lastNode.footnoteElementIndex;
      int availableBPD = this.getLineWidth(lastNode.line);
      int split = false;
      KnuthPageNode prevNode = lastNode;

      KnuthPageNode node;
      while(this.insertedFootnotesLength < this.totalFootnotesLength) {
         if (this.totalFootnotesLength - this.insertedFootnotesLength <= availableBPD) {
            this.insertedFootnotesLength = this.totalFootnotesLength;
            this.footnoteListIndex = this.lengthList.size() - 1;
            this.footnoteElementIndex = this.getFootnoteList(this.footnoteListIndex).size() - 1;
         } else {
            int split;
            if ((split = this.getFootnoteSplit(this.footnoteListIndex, this.footnoteElementIndex, this.insertedFootnotesLength, availableBPD, true)) > 0) {
               availableBPD -= split;
               this.insertedFootnotesLength += split;
            } else {
               node = (KnuthPageNode)this.createNode(lastNode.position, prevNode.line + 1, 1, this.insertedFootnotesLength - prevNode.insertedFootnotes, 0, 0, 0.0, 0, 0, 0, 0.0, prevNode);
               this.addNode(node.line, node);
               this.removeNode(prevNode.line, prevNode);
               prevNode = node;
               availableBPD = this.getLineWidth(node.line);
            }
         }
      }

      node = (KnuthPageNode)this.createNode(lastNode.position, prevNode.line + 1, 1, this.totalFootnotesLength - prevNode.insertedFootnotes, 0, 0, 0.0, 0, 0, 0, 0.0, prevNode);
      this.addNode(node.line, node);
      this.removeNode(prevNode.line, prevNode);
   }

   public LinkedList getPageBreaks() {
      return this.pageBreaks;
   }

   public void insertPageBreakAsFirst(AbstractBreaker.PageBreakPosition pageBreak) {
      if (this.pageBreaks == null) {
         this.pageBreaks = new LinkedList();
      }

      this.pageBreaks.addFirst(pageBreak);
   }

   public void removeAllPageBreaks() {
      if (this.pageBreaks != null && !this.pageBreaks.isEmpty()) {
         this.pageBreaks.subList(0, this.pageBreaks.size() - 1).clear();
      }
   }

   public void updateData1(int total, double demerits) {
   }

   public void updateData2(BreakingAlgorithm.KnuthNode bestActiveNode, KnuthSequence sequence, int total) {
      KnuthPageNode pageNode = (KnuthPageNode)bestActiveNode;
      KnuthPageNode previousPageNode = (KnuthPageNode)pageNode.previous;
      Iterator var6 = previousPageNode.pendingVariants.iterator();

      while(var6.hasNext()) {
         WhitespaceManagementPenalty.Variant var = (WhitespaceManagementPenalty.Variant)var6.next();
         WhitespaceManagementPenalty penalty = var.getWhitespaceManagementPenalty();
         if (!penalty.hasActiveVariant()) {
            penalty.setActiveVariant(var);
         }
      }

      int difference = bestActiveNode.difference;
      if (difference + bestActiveNode.availableShrink < 0 && !this.autoHeight && this.layoutListener != null) {
         this.layoutListener.notifyOverflow(bestActiveNode.line - 1, -difference, this.getFObj());
      }

      boolean isNonLastPage = bestActiveNode.line < total;
      int blockAlignment = isNonLastPage ? this.alignment : this.alignmentLast;
      double ratio = bestActiveNode.adjustRatio;
      if (ratio < 0.0) {
         difference = 0;
      } else if (ratio <= 1.0 && isNonLastPage) {
         difference = 0;
      } else if (ratio > 1.0) {
         ratio = 1.0;
         difference -= bestActiveNode.availableStretch;
      } else if (blockAlignment != 70) {
         ratio = 0.0;
      } else {
         difference = 0;
      }

      if (log.isDebugEnabled()) {
         log.debug("BBA> difference=" + difference + " ratio=" + ratio + " position=" + bestActiveNode.position);
      }

      if (this.handlingFloat() && this.floatPosition == null) {
         this.floatPosition = new AbstractBreaker.FloatPosition(this.topLevelLM, bestActiveNode.position, ratio, difference);
      } else {
         boolean useRelayedFootnotes = this.relayingFootnotes && bestActiveNode.previous.position == 0;
         int firstListIndex = useRelayedFootnotes ? this.previousFootnoteListIndex : ((KnuthPageNode)bestActiveNode.previous).footnoteListIndex;
         int firstElementIndex = useRelayedFootnotes ? this.previousFootnoteElementIndex : ((KnuthPageNode)bestActiveNode.previous).footnoteElementIndex;
         if (useRelayedFootnotes) {
            this.previousFootnoteListIndex = -2;
            this.previousFootnoteElementIndex = -2;
            this.relayingFootnotes = false;
         }

         if (this.footnotesList != null && firstElementIndex == this.getFootnoteList(firstListIndex).size() - 1) {
            ++firstListIndex;
            firstElementIndex = 0;
         } else {
            ++firstElementIndex;
         }

         this.insertPageBreakAsFirst(new AbstractBreaker.PageBreakPosition(this.topLevelLM, bestActiveNode.position, firstListIndex, firstElementIndex, ((KnuthPageNode)bestActiveNode).footnoteListIndex, ((KnuthPageNode)bestActiveNode).footnoteElementIndex, ratio, difference));
      }

   }

   protected int filterActiveNodes() {
      BreakingAlgorithm.KnuthNode bestActiveNode = null;

      for(int i = this.startLine; i < this.endLine; ++i) {
         for(BreakingAlgorithm.KnuthNode node = this.getNode(i); node != null; node = node.next) {
            if (!this.favorSinglePart || node.line <= 1 || bestActiveNode == null || Math.abs(bestActiveNode.difference) >= bestActiveNode.availableShrink) {
               bestActiveNode = this.compareNodes(bestActiveNode, node);
            }

            if (node != bestActiveNode) {
               this.removeNode(i, node);
            }
         }
      }

      assert bestActiveNode != null;

      return bestActiveNode.line;
   }

   protected final List getFootnoteList(int index) {
      return (List)this.footnotesList.get(index);
   }

   public FObj getFObj() {
      return this.topLevelLM.getFObj();
   }

   protected int getLineWidth(int line) {
      int bpd;
      if (this.pageProvider != null) {
         bpd = this.pageProvider.getAvailableBPD(line);
      } else {
         bpd = super.getLineWidth(line);
      }

      if (log.isTraceEnabled()) {
         log.trace("getLineWidth(" + line + ") -> " + bpd);
      }

      return bpd;
   }

   protected BreakingAlgorithm.KnuthNode recoverFromOverflow() {
      return this.compareIPDs(this.getLastTooLong().line - 1) != 0 ? this.getLastTooLong() : super.recoverFromOverflow();
   }

   protected int getIPDdifference() {
      return this.ipdDifference;
   }

   protected int handleIpdChange() {
      log.trace("Best node for ipd change:" + this.bestNodeForIPDChange);
      this.calculateBreakPoints(this.bestNodeForIPDChange, this.par, this.bestNodeForIPDChange.line + 1);
      this.activeLines = null;
      return this.bestNodeForIPDChange.line;
   }

   protected void addNode(int line, BreakingAlgorithm.KnuthNode node) {
      if (node.position < this.par.size() - 1 && line > 0 && (this.ipdDifference = this.compareIPDs(line - 1)) != 0) {
         log.trace("IPD changes at page " + line);
         if (this.bestNodeForIPDChange == null || node.totalDemerits < this.bestNodeForIPDChange.totalDemerits) {
            this.bestNodeForIPDChange = node;
         }
      } else {
         if (node.position == this.par.size() - 1) {
            this.ipdDifference = 0;
         } else if (line > 0) {
            this.bestNodeForLastPage = node;
         }

         super.addNode(line, node);
      }

   }

   BreakingAlgorithm.KnuthNode getBestNodeBeforeIPDChange() {
      return this.bestNodeForIPDChange;
   }

   private int compareIPDs(int line) {
      return this.pageProvider == null ? 0 : this.pageProvider.compareIPDs(line);
   }

   BreakingAlgorithm.KnuthNode getBestNodeForLastPage() {
      return this.bestNodeForLastPage;
   }

   protected boolean handlingFloat() {
      return this.handlingStartOfFloat || this.handlingEndOfFloat;
   }

   protected void createForcedNodes(BreakingAlgorithm.KnuthNode node, int line, int elementIdx, int difference, double r, double demerits, int fitnessClass, int availableShrink, int availableStretch, int newWidth, int newStretch, int newShrink) {
      if (this.handlingFloat()) {
         if (this.bestFloatEdgeNode == null || demerits <= this.bestFloatEdgeNode.totalDemerits) {
            this.bestFloatEdgeNode = this.createNode(elementIdx, line + 1, fitnessClass, newWidth, newStretch, newShrink, r, availableShrink, availableStretch, difference, demerits, node);
         }
      } else {
         super.createForcedNodes(node, line, elementIdx, difference, r, demerits, fitnessClass, availableShrink, availableStretch, newWidth, newStretch, newShrink);
      }

   }

   protected int handleFloat() {
      this.calculateBreakPoints(this.bestFloatEdgeNode, this.par, this.bestFloatEdgeNode.line);
      this.activeLines = null;
      return this.bestFloatEdgeNode.line - 1;
   }

   protected BreakingAlgorithm.KnuthNode getBestFloatEdgeNode() {
      return this.bestFloatEdgeNode;
   }

   protected AbstractBreaker.FloatPosition getFloatPosition() {
      return this.floatPosition;
   }

   protected int getFloatHeight() {
      return this.floatHeight;
   }

   protected boolean handlingStartOfFloat() {
      return this.handlingStartOfFloat;
   }

   protected boolean handlingEndOfFloat() {
      return this.handlingEndOfFloat;
   }

   protected void deactivateNode(BreakingAlgorithm.KnuthNode node, int line) {
      super.deactivateNode(node, line);
      if (this.handlingEndOfFloat) {
         this.floatHeight = this.totalWidth;
      }

   }

   protected void disableFloatHandling() {
      this.handlingEndOfFloat = false;
      this.handlingStartOfFloat = false;
   }

   public void loadFootnotes(List fl, List ll, int tfl, int ifl, boolean fp, boolean nf, int fnfi, int fli, int fei, MinOptMax fsl, int pfli, int pfei) {
      this.footnotesList = fl;
      this.lengthList = ll;
      this.totalFootnotesLength = tfl;
      this.insertedFootnotesLength = ifl;
      this.footnotesPending = fp;
      this.newFootnotes = nf;
      this.firstNewFootnoteIndex = fnfi;
      this.footnoteListIndex = fli;
      this.footnoteElementIndex = fei;
      this.footnoteSeparatorLength = fsl;
      this.previousFootnoteListIndex = pfli;
      this.previousFootnoteElementIndex = pfei;
      this.relayingFootnotes = this.previousFootnoteListIndex != -2 || this.previousFootnoteElementIndex != -2;
   }

   public void relayFootnotes(PageSequenceLayoutManager pslm) {
      if (!this.relayingFootnotes) {
         this.previousFootnoteListIndex = ((KnuthPageNode)this.bestFloatEdgeNode.previous).footnoteListIndex;
         this.previousFootnoteElementIndex = ((KnuthPageNode)this.bestFloatEdgeNode.previous).footnoteElementIndex;
      }

      pslm.holdFootnotes(this.footnotesList, this.lengthList, this.totalFootnotesLength, this.insertedFootnotesLength, this.footnotesPending, this.newFootnotes, this.firstNewFootnoteIndex, this.footnoteListIndex, this.footnoteElementIndex, this.footnoteSeparatorLength, this.previousFootnoteListIndex, this.previousFootnoteElementIndex);
   }

   public interface PageBreakingLayoutListener {
      void notifyOverflow(int var1, int var2, FObj var3);
   }

   protected class BestPageRecords extends BreakingAlgorithm.BestRecords {
      private final int[] bestInsertedFootnotesLength = new int[4];
      private final int[] bestTotalFootnotesLength = new int[4];
      private final int[] bestFootnoteListIndex = new int[4];
      private final int[] bestFootnoteElementIndex = new int[4];

      protected BestPageRecords() {
         super();
      }

      public void addRecord(double demerits, BreakingAlgorithm.KnuthNode node, double adjust, int availableShrink, int availableStretch, int difference, int fitness) {
         super.addRecord(demerits, node, adjust, availableShrink, availableStretch, difference, fitness);
         this.bestInsertedFootnotesLength[fitness] = PageBreakingAlgorithm.this.insertedFootnotesLength;
         this.bestTotalFootnotesLength[fitness] = PageBreakingAlgorithm.this.totalFootnotesLength;
         this.bestFootnoteListIndex[fitness] = PageBreakingAlgorithm.this.footnoteListIndex;
         this.bestFootnoteElementIndex[fitness] = PageBreakingAlgorithm.this.footnoteElementIndex;
      }

      public int getInsertedFootnotesLength(int fitness) {
         return this.bestInsertedFootnotesLength[fitness];
      }

      public int getTotalFootnotesLength(int fitness) {
         return this.bestTotalFootnotesLength[fitness];
      }

      public int getFootnoteListIndex(int fitness) {
         return this.bestFootnoteListIndex[fitness];
      }

      public int getFootnoteElementIndex(int fitness) {
         return this.bestFootnoteElementIndex[fitness];
      }
   }

   protected class KnuthPageNode extends BreakingAlgorithm.KnuthNode {
      public int insertedFootnotes;
      public int totalFootnotes;
      public int footnoteListIndex;
      public int footnoteElementIndex;
      private final List pendingVariants = new ArrayList();
      private int totalVariantsWidth;

      public KnuthPageNode(int position, int line, int fitness, int totalWidth, int totalStretch, int totalShrink, int insertedFootnotes, int totalFootnotes, int footnoteListIndex, int footnoteElementIndex, double adjustRatio, int availableShrink, int availableStretch, int difference, double totalDemerits, BreakingAlgorithm.KnuthNode previous) {
         super(position, line, fitness, totalWidth, totalStretch, totalShrink, adjustRatio, availableShrink, availableStretch, difference, totalDemerits, previous);
         this.totalFootnotes = totalFootnotes;
         this.insertedFootnotes = insertedFootnotes;
         this.footnoteListIndex = footnoteListIndex;
         this.footnoteElementIndex = footnoteElementIndex;
      }

      public void addVariant(WhitespaceManagementPenalty.Variant variant) {
         this.pendingVariants.add(variant);
         this.totalVariantsWidth += variant.width;
      }
   }
}
