package org.apache.fop.layoutmgr.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.table.EffRow;
import org.apache.fop.fo.flow.table.PrimaryGridUnit;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableBody;
import org.apache.fop.fo.flow.table.TablePart;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.FootenoteUtil;
import org.apache.fop.layoutmgr.Keep;
import org.apache.fop.layoutmgr.KnuthBlockBox;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.PageBreaker;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.SpaceResolver;
import org.apache.fop.util.BreakUtil;

public class TableContentLayoutManager implements PercentBaseContext {
   private static final Log LOG = LogFactory.getLog(TableContentLayoutManager.class);
   private TableLayoutManager tableLM;
   private TableRowIterator bodyIter;
   private TableRowIterator headerIter;
   private TableRowIterator footerIter;
   private LinkedList headerList;
   private LinkedList footerList;
   private int headerNetHeight;
   private int footerNetHeight;
   private int startXOffset;
   private int usedBPD;
   private TableStepper stepper;
   private boolean headerIsBeingRepeated;
   private boolean atLeastOnce;

   TableContentLayoutManager(TableLayoutManager parent) {
      this.tableLM = parent;
      Table table = this.getTableLM().getTable();
      this.bodyIter = new TableRowIterator(table, 0);
      if (table.getTableHeader() != null) {
         this.headerIter = new TableRowIterator(table, 1);
      }

      if (table.getTableFooter() != null) {
         this.footerIter = new TableRowIterator(table, 2);
      }

      this.stepper = new TableStepper(this);
   }

   TableLayoutManager getTableLM() {
      return this.tableLM;
   }

   boolean isSeparateBorderModel() {
      return this.getTableLM().getTable().isSeparateBorderModel();
   }

   ColumnSetup getColumns() {
      return this.getTableLM().getColumns();
   }

   protected int getHeaderNetHeight() {
      return this.headerNetHeight;
   }

   protected int getFooterNetHeight() {
      return this.footerNetHeight;
   }

   protected LinkedList getHeaderElements() {
      return this.headerList;
   }

   protected LinkedList getFooterElements() {
      return this.footerList;
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      if (LOG.isDebugEnabled()) {
         LOG.debug("==> Columns: " + this.getTableLM().getColumns());
      }

      KnuthBox headerAsFirst = null;
      KnuthBox headerAsSecondToLast = null;
      KnuthBox footerAsLast = null;
      LinkedList returnList = new LinkedList();
      int headerFootnoteBPD = 0;
      TableHeaderFooterPosition pos;
      List footnoteList;
      if (this.headerIter != null && this.headerList == null) {
         this.headerList = this.getKnuthElementsForRowIterator(this.headerIter, context, alignment, 1);
         this.headerNetHeight = ElementListUtils.calcContentLength(this.headerList);
         if (LOG.isDebugEnabled()) {
            LOG.debug("==> Header: " + this.headerNetHeight + " - " + this.headerList);
         }

         pos = new TableHeaderFooterPosition(this.getTableLM(), true, this.headerList);
         footnoteList = FootenoteUtil.getFootnotes(this.headerList);
         KnuthBox box = !footnoteList.isEmpty() && this.getTableLM().getTable().omitHeaderAtBreak() ? new KnuthBlockBox(this.headerNetHeight, footnoteList, pos, false) : new KnuthBox(this.headerNetHeight, pos, false);
         if (this.getTableLM().getTable().omitHeaderAtBreak()) {
            headerAsFirst = box;
         } else {
            if (!footnoteList.isEmpty()) {
               List footnotes = PageBreaker.getFootnoteKnuthElements(this.getTableLM().getPSLM().getFlowLayoutManager(), context, footnoteList);
               this.getTableLM().setHeaderFootnotes(footnotes);
               headerFootnoteBPD = this.getFootnotesBPD(footnotes);
               returnList.add(new KnuthBlockBox(-headerFootnoteBPD, footnoteList, new Position(this.getTableLM()), true));
               this.headerNetHeight += headerFootnoteBPD;
            }

            headerAsSecondToLast = box;
         }
      }

      if (this.footerIter != null && this.footerList == null) {
         this.footerList = this.getKnuthElementsForRowIterator(this.footerIter, context, alignment, 2);
         this.footerNetHeight = ElementListUtils.calcContentLength(this.footerList);
         if (LOG.isDebugEnabled()) {
            LOG.debug("==> Footer: " + this.footerNetHeight + " - " + this.footerList);
         }

         pos = new TableHeaderFooterPosition(this.getTableLM(), false, this.footerList);
         footnoteList = FootenoteUtil.getFootnotes(this.footerList);
         footerAsLast = footnoteList.isEmpty() ? new KnuthBox(this.footerNetHeight, pos, false) : new KnuthBlockBox(this.footerNetHeight, footnoteList, pos, false);
         if (!this.getTableLM().getTable().omitFooterAtBreak() && !footnoteList.isEmpty()) {
            List footnotes = PageBreaker.getFootnoteKnuthElements(this.getTableLM().getPSLM().getFlowLayoutManager(), context, footnoteList);
            this.getTableLM().setFooterFootnotes(footnotes);
            this.footerNetHeight += this.getFootnotesBPD(footnotes);
         }
      }

      returnList.addAll(this.getKnuthElementsForRowIterator(this.bodyIter, context, alignment, 0));
      int insertionPoint;
      if (headerAsFirst != null) {
         insertionPoint = 0;
         if (returnList.size() > 0 && ((ListElement)returnList.getFirst()).isForcedBreak()) {
            ++insertionPoint;
         }

         returnList.add(insertionPoint, headerAsFirst);
      } else if (headerAsSecondToLast != null) {
         insertionPoint = returnList.size();
         if (returnList.size() > 0 && ((ListElement)returnList.getLast()).isForcedBreak()) {
            --insertionPoint;
         }

         returnList.add(insertionPoint, headerAsSecondToLast);
      }

      if (footerAsLast != null) {
         insertionPoint = returnList.size();
         if (returnList.size() > 0 && ((ListElement)returnList.getLast()).isForcedBreak()) {
            --insertionPoint;
         }

         returnList.add(insertionPoint, footerAsLast);
      }

      if (headerFootnoteBPD != 0) {
         returnList.add(new KnuthBox(headerFootnoteBPD, new Position(this.getTableLM()), true));
      }

      return returnList;
   }

   private int getFootnotesBPD(List footnotes) {
      int bpd = 0;

      List footnote;
      for(Iterator var3 = footnotes.iterator(); var3.hasNext(); bpd += ElementListUtils.calcContentLength(footnote)) {
         footnote = (List)var3.next();
      }

      return bpd;
   }

   private LinkedList getKnuthElementsForRowIterator(TableRowIterator iter, LayoutContext context, int alignment, int bodyType) {
      LinkedList returnList = new LinkedList();
      EffRow[] rowGroup = iter.getNextRowGroup();
      context.clearKeepsPending();
      context.setBreakBefore(9);
      context.setBreakAfter(9);
      Keep keepWithPrevious = Keep.KEEP_AUTO;
      int breakBefore = 9;
      if (rowGroup != null) {
         RowGroupLayoutManager rowGroupLM = new RowGroupLayoutManager(this.getTableLM(), rowGroup, this.stepper);
         List nextRowGroupElems = rowGroupLM.getNextKnuthElements(context, alignment, bodyType);
         keepWithPrevious = keepWithPrevious.compare(context.getKeepWithPreviousPending());
         breakBefore = context.getBreakBefore();
         int breakBetween = context.getBreakAfter();
         returnList.addAll(nextRowGroupElems);

         while((rowGroup = iter.getNextRowGroup()) != null) {
            rowGroupLM = new RowGroupLayoutManager(this.getTableLM(), rowGroup, this.stepper);
            Keep keepWithNextPending = context.getKeepWithNextPending();
            context.clearKeepWithNextPending();
            nextRowGroupElems = rowGroupLM.getNextKnuthElements(context, alignment, bodyType);
            Keep keep = keepWithNextPending.compare(context.getKeepWithPreviousPending());
            context.clearKeepWithPreviousPending();
            keep = keep.compare(this.getTableLM().getKeepTogether());
            int penaltyValue = keep.getPenalty();
            int breakClass = keep.getContext();
            breakBetween = BreakUtil.compareBreakClasses(breakBetween, context.getBreakBefore());
            if (breakBetween != 9) {
               penaltyValue = -1000;
               breakClass = breakBetween;
            }

            ListIterator elemIter = returnList.listIterator(returnList.size());
            ListElement elem = (ListElement)elemIter.previous();
            BreakElement breakElement;
            if (elem instanceof KnuthGlue) {
               breakElement = (BreakElement)elemIter.previous();
            } else {
               breakElement = (BreakElement)elem;
            }

            breakElement.setPenaltyValue(penaltyValue);
            breakElement.setBreakClass(breakClass);
            returnList.addAll(nextRowGroupElems);
            breakBetween = context.getBreakAfter();
         }
      }

      if (!returnList.isEmpty()) {
         ListIterator elemIter = returnList.listIterator(returnList.size());
         ListElement elem = (ListElement)elemIter.previous();
         if (elem instanceof KnuthGlue) {
            BreakElement breakElement = (BreakElement)elemIter.previous();
            breakElement.setPenaltyValue(1000);
         } else {
            elemIter.remove();
         }
      }

      context.updateKeepWithPreviousPending(keepWithPrevious);
      context.setBreakBefore(breakBefore);
      int widowContentLimit = this.getTableLM().getTable().getWidowContentLimit().getValue();
      if (widowContentLimit != 0 && bodyType == 0) {
         ElementListUtils.removeLegalBreaks(returnList, widowContentLimit);
      }

      int orphanContentLimit = this.getTableLM().getTable().getOrphanContentLimit().getValue();
      if (orphanContentLimit != 0 && bodyType == 0) {
         ElementListUtils.removeLegalBreaksFromEnd(returnList, orphanContentLimit);
      }

      return returnList;
   }

   protected int getXOffsetOfGridUnit(PrimaryGridUnit gu) {
      return this.getXOffsetOfGridUnit(gu.getColIndex(), gu.getCell().getNumberColumnsSpanned());
   }

   protected int getXOffsetOfGridUnit(int colIndex, int nrColSpan) {
      return this.startXOffset + this.getTableLM().getColumns().getXOffset(colIndex + 1, nrColSpan, this.getTableLM());
   }

   void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
      this.usedBPD = 0;
      RowPainter painter = new RowPainter(this, layoutContext);
      List tablePositions = new ArrayList();
      List headerElements = null;
      List footerElements = null;
      Position firstPos = null;
      Position lastPos = null;
      Position lastCheckPos = null;

      while(parentIter.hasNext()) {
         Position pos = parentIter.next();
         if (pos instanceof SpaceResolver.SpaceHandlingBreakPosition) {
            pos = ((SpaceResolver.SpaceHandlingBreakPosition)pos).getOriginalBreakPosition();
         }

         if (pos != null) {
            if (firstPos == null) {
               firstPos = pos;
            }

            lastPos = pos;
            if (pos.getIndex() >= 0) {
               lastCheckPos = pos;
            }

            if (pos instanceof TableHeaderFooterPosition) {
               TableHeaderFooterPosition thfpos = (TableHeaderFooterPosition)pos;
               if (thfpos.header) {
                  headerElements = thfpos.nestedElements;
               } else {
                  footerElements = thfpos.nestedElements;
               }
            } else if (!(pos instanceof TableHFPenaltyPosition)) {
               if (pos instanceof TableContentPosition) {
                  tablePositions.add(pos);
               } else if (LOG.isDebugEnabled()) {
                  LOG.debug("Ignoring position: " + pos);
               }
            }
         }
      }

      boolean treatFooterAsArtifact = layoutContext.treatAsArtifact();
      if (lastPos instanceof TableHFPenaltyPosition) {
         TableHFPenaltyPosition penaltyPos = (TableHFPenaltyPosition)lastPos;
         LOG.debug("Break at penalty!");
         if (penaltyPos.headerElements != null) {
            headerElements = penaltyPos.headerElements;
         }

         if (penaltyPos.footerElements != null) {
            footerElements = penaltyPos.footerElements;
            treatFooterAsArtifact = true;
         }
      }

      this.tableLM.clearTableFragmentMarkers();
      Map markers = this.getTableLM().getTable().getMarkers();
      if (markers != null) {
         this.getTableLM().getCurrentPV().registerMarkers(markers, true, this.getTableLM().isFirst(firstPos), this.getTableLM().isLast(lastCheckPos));
      }

      boolean ancestorTreatAsArtifact;
      if (headerElements != null) {
         ancestorTreatAsArtifact = layoutContext.treatAsArtifact();
         if (this.headerIsBeingRepeated) {
            layoutContext.setTreatAsArtifact(true);
            if (!this.getTableLM().getHeaderFootnotes().isEmpty()) {
               this.getTableLM().getPSLM().addTableHeaderFootnotes(this.getTableLM().getHeaderFootnotes());
            }
         }

         this.addHeaderFooterAreas(headerElements, this.tableLM.getTable().getTableHeader(), painter, false);
         if (!ancestorTreatAsArtifact) {
            this.headerIsBeingRepeated = true;
         }

         layoutContext.setTreatAsArtifact(ancestorTreatAsArtifact);
      }

      if (tablePositions.isEmpty()) {
         LOG.error("tablePositions empty. Please send your FO file to fop-users@xmlgraphics.apache.org");
      } else {
         this.addBodyAreas(tablePositions.iterator(), painter, footerElements == null);
      }

      this.tableLM.setRepeateHeader(this.atLeastOnce);
      this.tableLM.repeatAddAreasForSavedTableHeaderTableCellLayoutManagers();
      this.atLeastOnce = true;
      if (footerElements != null && !footerElements.isEmpty()) {
         ancestorTreatAsArtifact = layoutContext.treatAsArtifact();
         layoutContext.setTreatAsArtifact(treatFooterAsArtifact);
         this.addHeaderFooterAreas(footerElements, this.tableLM.getTable().getTableFooter(), painter, true);
         if (lastPos instanceof TableHFPenaltyPosition && !this.tableLM.getFooterFootnotes().isEmpty()) {
            this.tableLM.getPSLM().addTableFooterFootnotes(this.getTableLM().getFooterFootnotes());
         }

         layoutContext.setTreatAsArtifact(ancestorTreatAsArtifact);
      }

      this.usedBPD += painter.getAccumulatedBPD();
      if (markers != null) {
         this.getTableLM().getCurrentPV().registerMarkers(markers, false, this.getTableLM().isFirst(firstPos), this.getTableLM().isLast(lastCheckPos));
      }

   }

   private void addHeaderFooterAreas(List elements, TablePart part, RowPainter painter, boolean lastOnPage) {
      List lst = new ArrayList(elements.size());
      Iterator iter = new KnuthPossPosIter(elements);

      while(iter.hasNext()) {
         Position pos = (Position)iter.next();
         if (pos instanceof TableContentPosition) {
            lst.add((TableContentPosition)pos);
         }
      }

      this.addTablePartAreas(lst, painter, part, true, true, true, lastOnPage);
   }

   private void addBodyAreas(Iterator iterator, RowPainter painter, boolean lastOnPage) {
      painter.startBody();
      List lst = new ArrayList();
      TableContentPosition pos = (TableContentPosition)iterator.next();
      boolean isFirstPos = pos.getFlag(1) && pos.getRow().getFlag(0);
      TablePart part = pos.getTablePart();
      lst.add(pos);

      for(; iterator.hasNext(); lst.add(pos)) {
         pos = (TableContentPosition)iterator.next();
         if (pos.getTablePart() != part) {
            this.addTablePartAreas(lst, painter, part, isFirstPos, true, false, false);
            isFirstPos = true;
            lst.clear();
            part = pos.getTablePart();
         }
      }

      boolean isLastPos = pos.getFlag(2) && pos.getRow().getFlag(1);
      this.addTablePartAreas(lst, painter, part, isFirstPos, isLastPos, true, lastOnPage);
      painter.endBody();
   }

   private void addTablePartAreas(List positions, RowPainter painter, TablePart body, boolean isFirstPos, boolean isLastPos, boolean lastInBody, boolean lastOnPage) {
      this.getTableLM().getCurrentPV().registerMarkers(body.getMarkers(), true, isFirstPos, isLastPos);
      if (body instanceof TableBody) {
         this.getTableLM().registerMarkers(body.getMarkers(), true, isFirstPos, isLastPos);
      }

      painter.startTablePart(body);
      Iterator var8 = positions.iterator();

      while(var8.hasNext()) {
         Object position = var8.next();
         painter.handleTableContentPosition((TableContentPosition)position);
      }

      this.getTableLM().getCurrentPV().registerMarkers(body.getMarkers(), false, isFirstPos, isLastPos);
      if (body instanceof TableBody) {
         this.getTableLM().registerMarkers(body.getMarkers(), false, isFirstPos, isLastPos);
      }

      painter.endTablePart(lastInBody, lastOnPage);
   }

   void setStartXOffset(int startXOffset) {
      this.startXOffset = startXOffset;
   }

   int getUsedBPD() {
      return this.usedBPD;
   }

   public int getBaseLength(int lengthBase, FObj fobj) {
      return this.tableLM.getBaseLength(lengthBase, fobj);
   }
}
