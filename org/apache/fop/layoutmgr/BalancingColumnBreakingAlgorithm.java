package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.fop.traits.MinOptMax;

public class BalancingColumnBreakingAlgorithm extends PageBreakingAlgorithm {
   private int columnCount;
   private List idealBreaks;

   public BalancingColumnBreakingAlgorithm(LayoutManager topLevelLM, PageProvider pageProvider, PageBreakingAlgorithm.PageBreakingLayoutListener layoutListener, int alignment, int alignmentLast, MinOptMax footnoteSeparatorLength, boolean partOverflowRecovery, int columnCount) {
      super(topLevelLM, pageProvider, layoutListener, alignment, alignmentLast, footnoteSeparatorLength, partOverflowRecovery, false, false);
      this.columnCount = columnCount;
      this.considerTooShort = true;
   }

   protected double computeDemerits(BreakingAlgorithm.KnuthNode activeNode, KnuthElement element, int fitnessClass, double r) {
      double demerits = Double.MAX_VALUE;
      if (this.idealBreaks == null) {
         this.idealBreaks = this.calculateIdealBreaks(activeNode.position);
      }

      LinkedList curPossibility = this.getPossibilityTrail(activeNode);
      boolean notIdeal = false;
      int idealDemerit = this.columnCount + 1 - curPossibility.size();
      if (curPossibility.size() > this.idealBreaks.size()) {
         return demerits;
      } else {
         for(int breakPos = 0; breakPos < curPossibility.size(); ++breakPos) {
            if ((Integer)curPossibility.get(breakPos) != 0 && !((Integer)curPossibility.get(breakPos)).equals(this.idealBreaks.get(breakPos))) {
               notIdeal = true;
               break;
            }
         }

         if (!notIdeal) {
            demerits = (double)idealDemerit;
         }

         return demerits;
      }
   }

   private List calculateIdealBreaks(int startPos) {
      List previousPreviousBreaks = null;
      List previousBreaks = null;
      List breaks = new ArrayList();
      ((List)breaks).add(new ColumnContent(startPos, this.par.size() - 1));

      do {
         previousPreviousBreaks = previousBreaks;
         previousBreaks = breaks;
         breaks = this.getInitialBreaks(startPos, this.getAverageColumnLength((List)breaks));
      } while(!((List)breaks).equals(previousBreaks) && !((List)breaks).equals(previousPreviousBreaks));

      List breaks = this.sortElementsForBreaks((List)breaks);
      return this.getElementIdBreaks(breaks, startPos);
   }

   private int getAverageColumnLength(List columns) {
      int totalLength = 0;

      ColumnContent col;
      for(Iterator var3 = columns.iterator(); var3.hasNext(); totalLength += this.calcContentLength(this.par, col.startIndex, col.endIndex)) {
         col = (ColumnContent)var3.next();
      }

      return totalLength / this.columnCount;
   }

   private List getInitialBreaks(int startIndex, int averageColLength) {
      List initialColumns = new ArrayList();
      int colStartIndex = startIndex;
      int totalLength = 0;
      int idealBreakLength = averageColLength;
      int previousBreakLength = 0;
      int prevBreakIndex = startIndex;
      boolean prevIsBox = false;
      int colNumber = 1;

      for(int i = startIndex; i < this.par.size(); ++i) {
         KnuthElement element = (KnuthElement)this.par.get(i);
         if (this.isLegalBreak(i, prevIsBox)) {
            int breakLength = totalLength + (element instanceof KnuthPenalty ? element.getWidth() : 0);
            if (breakLength > idealBreakLength && colNumber < this.columnCount) {
               int breakIndex;
               if (breakLength - idealBreakLength > idealBreakLength - previousBreakLength) {
                  breakIndex = prevBreakIndex;
                  totalLength = previousBreakLength;
               } else {
                  breakIndex = element instanceof KnuthPenalty ? i : i - 1;
                  totalLength = breakLength;
               }

               initialColumns.add(new ColumnContent(colStartIndex, breakIndex));
               i = this.getNextStartIndex(breakIndex);
               colStartIndex = i--;
               ++colNumber;
               idealBreakLength += averageColLength;
            } else {
               previousBreakLength = breakLength;
               prevBreakIndex = element instanceof KnuthPenalty ? i : i - 1;
               prevIsBox = false;
            }
         } else {
            totalLength += element instanceof KnuthPenalty ? 0 : element.getWidth();
            prevIsBox = element instanceof KnuthBox;
         }
      }

      assert initialColumns.size() == this.columnCount - 1;

      initialColumns.add(new ColumnContent(colStartIndex, this.par.size() - 1));
      return initialColumns;
   }

   private int getNextStartIndex(int breakIndex) {
      int startIndex = breakIndex;

      for(Iterator iter = this.par.listIterator(breakIndex); iter.hasNext() && !(iter.next() instanceof KnuthBox); ++startIndex) {
      }

      return startIndex;
   }

   private List sortElementsForBreaks(List breaks) {
      int fFactor = 4000;

      boolean changes;
      do {
         changes = false;
         ColumnContent curColumn = (ColumnContent)breaks.get(breaks.size() - 1);
         int curColLength = this.calcContentLength(this.par, curColumn.startIndex, curColumn.endIndex);

         for(int colIndex = breaks.size() - 1; colIndex > 0; --colIndex) {
            ColumnContent prevColumn = (ColumnContent)breaks.get(colIndex - 1);
            int prevColLength = this.calcContentLength(this.par, prevColumn.startIndex, prevColumn.endIndex);
            if (prevColLength < curColLength) {
               int newBreakIndex = curColumn.startIndex;

               boolean prevIsBox;
               for(prevIsBox = true; newBreakIndex <= curColumn.endIndex && !this.isLegalBreak(newBreakIndex, prevIsBox); prevIsBox = this.par.get(newBreakIndex) instanceof KnuthBox) {
                  ++newBreakIndex;
               }

               if (newBreakIndex < curColumn.endIndex) {
                  if (prevIsBox) {
                     --newBreakIndex;
                  }

                  int newStartIndex = this.getNextStartIndex(newBreakIndex);
                  int newPrevColLength = this.calcContentLength(this.par, prevColumn.startIndex, newBreakIndex);
                  if (newPrevColLength <= fFactor + curColLength) {
                     prevColumn = new ColumnContent(prevColumn.startIndex, newBreakIndex);
                     breaks.set(colIndex - 1, prevColumn);
                     breaks.set(colIndex, new ColumnContent(newStartIndex, curColumn.endIndex));
                     prevColLength = this.calcContentLength(this.par, prevColumn.startIndex, newBreakIndex);
                     changes = true;
                  }
               }
            }

            curColLength = prevColLength;
            curColumn = prevColumn;
         }
      } while(changes);

      return breaks;
   }

   private boolean isLegalBreak(int index, boolean prevIsBox) {
      KnuthElement element = (KnuthElement)this.par.get(index);
      return element instanceof KnuthPenalty && element.getPenalty() < 1000 || prevIsBox && element instanceof KnuthGlue;
   }

   private int calcContentLength(KnuthSequence par, int startIndex, int endIndex) {
      return ElementListUtils.calcContentLength(par, startIndex, endIndex) + this.getPenaltyWidth(endIndex);
   }

   private int getPenaltyWidth(int index) {
      KnuthElement element = (KnuthElement)this.par.get(index);
      return element instanceof KnuthPenalty ? element.getWidth() : 0;
   }

   private List getElementIdBreaks(List breaks, int startPos) {
      List elementIdBreaks = new ArrayList();
      elementIdBreaks.add(startPos);
      Iterator var4 = breaks.iterator();

      while(var4.hasNext()) {
         ColumnContent column = (ColumnContent)var4.next();
         if (!((ColumnContent)breaks.get(breaks.size() - 1)).equals(column)) {
            elementIdBreaks.add(column.endIndex);
         }
      }

      return elementIdBreaks;
   }

   private LinkedList getPossibilityTrail(BreakingAlgorithm.KnuthNode activeNode) {
      LinkedList trail = new LinkedList();
      BreakingAlgorithm.KnuthNode previous = activeNode;

      do {
         trail.addFirst(previous.position);
         previous = previous.previous;
      } while(previous != null);

      return trail;
   }

   private static final class ColumnContent {
      public final int startIndex;
      public final int endIndex;

      ColumnContent(int startIndex, int endIndex) {
         this.startIndex = startIndex;
         this.endIndex = endIndex;
      }

      public int hashCode() {
         return this.startIndex << 16 | this.endIndex;
      }

      public boolean equals(Object obj) {
         if (!(obj instanceof ColumnContent)) {
            return false;
         } else {
            ColumnContent other = (ColumnContent)obj;
            return other.startIndex == this.startIndex && other.endIndex == this.endIndex;
         }
      }

      public String toString() {
         return this.startIndex + "-" + this.endIndex;
      }
   }
}
