package org.apache.fop.layoutmgr.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.flow.table.EffRow;
import org.apache.fop.fo.flow.table.GridUnit;
import org.apache.fop.fo.flow.table.PrimaryGridUnit;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.Keep;
import org.apache.fop.layoutmgr.KnuthBlockBox;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.util.BreakUtil;

public class TableStepper {
   private static Log log = LogFactory.getLog(TableStepper.class);
   private TableContentLayoutManager tclm;
   private EffRow[] rowGroup;
   private int columnCount;
   private int totalHeight;
   private int previousRowsLength;
   private int activeRowIndex;
   private boolean rowFinished;
   private List activeCells = new LinkedList();
   private List nextActiveCells = new LinkedList();
   private boolean delayingNextRow;
   private int rowFirstStep;
   private boolean rowHeightSmallerThanFirstStep;
   private int nextBreakClass;

   public TableStepper(TableContentLayoutManager tclm) {
      this.tclm = tclm;
      this.columnCount = tclm.getTableLM().getTable().getNumberOfColumns();
   }

   private void setup(EffRow[] rows) {
      this.rowGroup = rows;
      this.previousRowsLength = 0;
      this.activeRowIndex = 0;
      this.activeCells.clear();
      this.nextActiveCells.clear();
      this.delayingNextRow = false;
      this.rowFirstStep = 0;
      this.rowHeightSmallerThanFirstStep = false;
   }

   private void calcTotalHeight() {
      this.totalHeight = 0;
      EffRow[] var1 = this.rowGroup;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         EffRow aRowGroup = var1[var3];
         this.totalHeight += aRowGroup.getHeight().getOpt();
      }

      if (log.isDebugEnabled()) {
         log.debug("totalHeight=" + this.totalHeight);
      }

   }

   private int getMaxRemainingHeight() {
      int maxW = 0;

      int remain;
      for(Iterator var2 = this.activeCells.iterator(); var2.hasNext(); maxW = Math.max(maxW, remain)) {
         Object activeCell1 = var2.next();
         ActiveCell activeCell = (ActiveCell)activeCell1;
         remain = activeCell.getRemainingLength();
         PrimaryGridUnit pgu = activeCell.getPrimaryGridUnit();

         for(int i = this.activeRowIndex + 1; i < pgu.getRowIndex() - this.rowGroup[0].getIndex() + pgu.getCell().getNumberRowsSpanned(); ++i) {
            remain -= this.rowGroup[i].getHeight().getOpt();
         }
      }

      for(int i = this.activeRowIndex + 1; i < this.rowGroup.length; ++i) {
         maxW += this.rowGroup[i].getHeight().getOpt();
      }

      return maxW;
   }

   private void activateCells(List activeCellList, int rowIndex) {
      EffRow row = this.rowGroup[rowIndex];

      for(int i = 0; i < this.columnCount; ++i) {
         GridUnit gu = row.getGridUnit(i);
         if (!gu.isEmpty() && gu.isPrimary()) {
            assert gu instanceof PrimaryGridUnit;

            activeCellList.add(new ActiveCell((PrimaryGridUnit)gu, row, rowIndex, this.previousRowsLength, this.getTableLM()));
         }
      }

   }

   public LinkedList getCombinedKnuthElementsForRowGroup(LayoutContext context, EffRow[] rows, int bodyType) {
      this.setup(rows);
      this.activateCells(this.activeCells, 0);
      this.calcTotalHeight();
      int cumulateLength = 0;
      TableContentPosition lastTCPos = null;
      LinkedList returnList = new LinkedList();
      int laststep = 0;
      int step = this.getFirstStep();

      TableContentPosition tcpos;
      do {
         int maxRemainingHeight = this.getMaxRemainingHeight();
         int penaltyOrGlueLen = step + maxRemainingHeight - this.totalHeight;
         int boxLen = step - cumulateLength - Math.max(0, penaltyOrGlueLen);
         cumulateLength += boxLen + Math.max(0, -penaltyOrGlueLen);
         if (log.isDebugEnabled()) {
            log.debug("Next step: " + step + " (+" + (step - laststep) + ")");
            log.debug("           max remaining height: " + maxRemainingHeight);
            if (penaltyOrGlueLen >= 0) {
               log.debug("           box = " + boxLen + " penalty = " + penaltyOrGlueLen);
            } else {
               log.debug("           box = " + boxLen + " glue = " + -penaltyOrGlueLen);
            }
         }

         LinkedList footnoteList = new LinkedList();
         List cellParts = new ArrayList(this.activeCells.size());
         Iterator var14 = this.activeCells.iterator();

         while(var14.hasNext()) {
            Object activeCell2 = var14.next();
            ActiveCell activeCell = (ActiveCell)activeCell2;
            CellPart part = activeCell.createCellPart();
            cellParts.add(part);
            activeCell.addFootnotes(footnoteList);
         }

         tcpos = new TableContentPosition(this.getTableLM(), cellParts, this.rowGroup[this.activeRowIndex]);
         if (this.delayingNextRow) {
            tcpos.setNewPageRow(this.rowGroup[this.activeRowIndex + 1]);
         }

         if (returnList.size() == 0) {
            tcpos.setFlag(1, true);
         }

         if (footnoteList.isEmpty()) {
            returnList.add(new KnuthBox(boxLen, tcpos, false));
         } else {
            returnList.add(new KnuthBlockBox(boxLen, footnoteList, tcpos, false));
         }

         int effPenaltyLen = Math.max(0, penaltyOrGlueLen);
         TableHFPenaltyPosition penaltyPos = new TableHFPenaltyPosition(this.getTableLM());
         if (bodyType == 0) {
            if (!this.getTableLM().getTable().omitHeaderAtBreak()) {
               effPenaltyLen += this.tclm.getHeaderNetHeight();
               penaltyPos.headerElements = this.tclm.getHeaderElements();
            }

            if (!this.getTableLM().getTable().omitFooterAtBreak()) {
               effPenaltyLen += this.tclm.getFooterNetHeight();
               penaltyPos.footerElements = this.tclm.getFooterElements();
            }
         }

         Keep keep = this.getTableLM().getKeepTogether();
         int stepPenalty = 0;

         ActiveCell activeCell;
         for(Iterator var19 = this.activeCells.iterator(); var19.hasNext(); stepPenalty = Math.max(stepPenalty, activeCell.getPenaltyValue())) {
            Object activeCell1 = var19.next();
            activeCell = (ActiveCell)activeCell1;
            keep = keep.compare(activeCell.getKeepWithNext());
         }

         if (!this.rowFinished) {
            keep = keep.compare(this.rowGroup[this.activeRowIndex].getKeepTogether());
         } else if (this.activeRowIndex < this.rowGroup.length - 1) {
            keep = keep.compare(this.rowGroup[this.activeRowIndex].getKeepWithNext());
            keep = keep.compare(this.rowGroup[this.activeRowIndex + 1].getKeepWithPrevious());
            this.nextBreakClass = BreakUtil.compareBreakClasses(this.nextBreakClass, this.rowGroup[this.activeRowIndex].getBreakAfter());
            this.nextBreakClass = BreakUtil.compareBreakClasses(this.nextBreakClass, this.rowGroup[this.activeRowIndex + 1].getBreakBefore());
         }

         int p = keep.getPenalty();
         if (this.rowHeightSmallerThanFirstStep) {
            this.rowHeightSmallerThanFirstStep = false;
            p = 1000;
         }

         p = Math.max(p, stepPenalty);
         int breakClass = keep.getContext();
         if (this.nextBreakClass != 9) {
            log.trace("Forced break encountered");
            p = -1000;
            breakClass = this.nextBreakClass;
         }

         returnList.add(new BreakElement(penaltyPos, effPenaltyLen, p, breakClass, context));
         laststep = step;
         step = this.getNextStep();
         if (penaltyOrGlueLen < 0) {
            int shrink = 0;
            int stretch = 0;
            int width = -penaltyOrGlueLen;
            LayoutManager bslm = this.getTableLM().getParent();
            if (bslm instanceof BlockStackingLayoutManager && ((BlockStackingLayoutManager)bslm).isRestartAtLM() && keep.getPenalty() == 1000) {
               width = 0;
            }

            returnList.add(new KnuthGlue(width, stretch, shrink, new Position((LayoutManager)null), true));
         }
      } while(step >= 0);

      assert !returnList.isEmpty();

      tcpos.setFlag(2, true);
      return returnList;
   }

   private int getFirstStep() {
      this.computeRowFirstStep(this.activeCells);
      this.signalRowFirstStep();
      int minStep = this.considerRowLastStep(this.rowFirstStep);
      this.signalNextStep(minStep);
      return minStep;
   }

   private int getNextStep() {
      if (this.rowFinished) {
         if (this.activeRowIndex == this.rowGroup.length - 1) {
            return -1;
         }

         this.rowFinished = false;
         this.removeCellsEndingOnCurrentRow();
         log.trace("Delaying next row");
         this.delayingNextRow = true;
      }

      int minStep;
      if (!this.delayingNextRow) {
         minStep = this.computeMinStep();
         minStep = this.considerRowLastStep(minStep);
         this.signalNextStep(minStep);
         return minStep;
      } else {
         minStep = this.computeMinStep();
         if (minStep < 0 || minStep >= this.rowFirstStep || minStep > this.rowGroup[this.activeRowIndex].getExplicitHeight().getMax()) {
            if (log.isTraceEnabled()) {
               log.trace("Step = " + minStep);
            }

            this.delayingNextRow = false;
            minStep = this.rowFirstStep;
            this.switchToNextRow();
            this.signalRowFirstStep();
            minStep = this.considerRowLastStep(minStep);
         }

         this.signalNextStep(minStep);
         return minStep;
      }
   }

   private void computeRowFirstStep(List cells) {
      ActiveCell activeCell;
      for(Iterator var2 = cells.iterator(); var2.hasNext(); this.rowFirstStep = Math.max(this.rowFirstStep, activeCell.getFirstStep())) {
         Object cell = var2.next();
         activeCell = (ActiveCell)cell;
      }

   }

   private int computeMinStep() {
      int minStep = Integer.MAX_VALUE;
      boolean stepFound = false;
      Iterator var3 = this.activeCells.iterator();

      while(var3.hasNext()) {
         Object activeCell1 = var3.next();
         ActiveCell activeCell = (ActiveCell)activeCell1;
         int nextStep = activeCell.getNextStep();
         if (nextStep >= 0) {
            stepFound = true;
            minStep = Math.min(minStep, nextStep);
         }
      }

      if (stepFound) {
         return minStep;
      } else {
         return -1;
      }
   }

   private void signalRowFirstStep() {
      Iterator var1 = this.activeCells.iterator();

      while(var1.hasNext()) {
         Object activeCell1 = var1.next();
         ActiveCell activeCell = (ActiveCell)activeCell1;
         activeCell.signalRowFirstStep(this.rowFirstStep);
      }

   }

   private void signalNextStep(int step) {
      this.nextBreakClass = 9;

      ActiveCell activeCell;
      for(Iterator var2 = this.activeCells.iterator(); var2.hasNext(); this.nextBreakClass = BreakUtil.compareBreakClasses(this.nextBreakClass, activeCell.signalNextStep(step))) {
         Object activeCell1 = var2.next();
         activeCell = (ActiveCell)activeCell1;
      }

   }

   private int considerRowLastStep(int step) {
      this.rowFinished = true;
      Iterator var2 = this.activeCells.iterator();

      while(var2.hasNext()) {
         Object activeCell3 = var2.next();
         ActiveCell activeCell = (ActiveCell)activeCell3;
         if (activeCell.endsOnRow(this.activeRowIndex) && !activeCell.finishes(step)) {
            this.rowFinished = false;
         }
      }

      if (this.rowFinished) {
         if (log.isTraceEnabled()) {
            log.trace("Step = " + step);
            log.trace("Row finished, computing last step");
         }

         int maxStep = 0;
         Iterator var7 = this.activeCells.iterator();

         ActiveCell activeCell;
         Object activeCell1;
         while(var7.hasNext()) {
            activeCell1 = var7.next();
            activeCell = (ActiveCell)activeCell1;
            if (activeCell.endsOnRow(this.activeRowIndex)) {
               maxStep = Math.max(maxStep, activeCell.getLastStep());
            }
         }

         if (log.isTraceEnabled()) {
            log.trace("Max step: " + maxStep);
         }

         var7 = this.activeCells.iterator();

         while(var7.hasNext()) {
            activeCell1 = var7.next();
            activeCell = (ActiveCell)activeCell1;
            activeCell.endRow(this.activeRowIndex);
            if (!activeCell.endsOnRow(this.activeRowIndex)) {
               activeCell.signalRowLastStep(maxStep);
            }
         }

         if (maxStep < step) {
            log.trace("Row height smaller than first step, produced penalty will be infinite");
            this.rowHeightSmallerThanFirstStep = true;
         }

         step = maxStep;
         this.prepareNextRow();
      }

      return step;
   }

   private void prepareNextRow() {
      if (this.activeRowIndex < this.rowGroup.length - 1) {
         this.previousRowsLength += this.rowGroup[this.activeRowIndex].getHeight().getOpt();
         this.activateCells(this.nextActiveCells, this.activeRowIndex + 1);
         if (log.isTraceEnabled()) {
            log.trace("Computing first step for row " + (this.activeRowIndex + 2));
         }

         this.computeRowFirstStep(this.nextActiveCells);
         if (log.isTraceEnabled()) {
            log.trace("Next first step = " + this.rowFirstStep);
         }
      }

   }

   private void removeCellsEndingOnCurrentRow() {
      Iterator iter = this.activeCells.iterator();

      while(iter.hasNext()) {
         ActiveCell activeCell = (ActiveCell)iter.next();
         if (activeCell.endsOnRow(this.activeRowIndex)) {
            iter.remove();
         }
      }

   }

   private void switchToNextRow() {
      ++this.activeRowIndex;
      if (log.isTraceEnabled()) {
         log.trace("Switching to row " + (this.activeRowIndex + 1));
      }

      Iterator var1 = this.activeCells.iterator();

      while(var1.hasNext()) {
         Object activeCell1 = var1.next();
         ActiveCell activeCell = (ActiveCell)activeCell1;
         activeCell.nextRowStarts();
      }

      this.activeCells.addAll(this.nextActiveCells);
      this.nextActiveCells.clear();
   }

   private TableLayoutManager getTableLM() {
      return this.tclm.getTableLM();
   }
}
