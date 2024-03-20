package org.apache.fop.layoutmgr.table;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.Trait;
import org.apache.fop.fo.flow.table.GridUnit;
import org.apache.fop.fo.flow.table.PrimaryGridUnit;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableCell;
import org.apache.fop.fo.flow.table.TableColumn;
import org.apache.fop.fo.flow.table.TableFooter;
import org.apache.fop.fo.flow.table.TableHeader;
import org.apache.fop.fo.flow.table.TablePart;
import org.apache.fop.fo.flow.table.TableRow;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.layoutmgr.AbstractLayoutManager;
import org.apache.fop.layoutmgr.AreaAdditionUtil;
import org.apache.fop.layoutmgr.BlockStackingLayoutManager;
import org.apache.fop.layoutmgr.ElementListObserver;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.Keep;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LocalBreaker;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.RetrieveTableMarkerLayoutManager;
import org.apache.fop.layoutmgr.SpaceResolver;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.util.ListUtil;

public class TableCellLayoutManager extends BlockStackingLayoutManager {
   private static Log log = LogFactory.getLog(TableCellLayoutManager.class);
   private PrimaryGridUnit primaryGridUnit;
   private Block curBlockArea;
   private int xoffset;
   private int yoffset;
   private int cellIPD;
   private int totalHeight;
   private int usedBPD;
   private boolean emptyCell = true;
   private boolean isDescendantOfTableFooter;
   private boolean isDescendantOfTableHeader;
   private boolean hasRetrieveTableMarker;
   private boolean hasRepeatedHeader;
   private boolean savedAddAreasArguments;
   private PositionIterator savedParentIter;
   private LayoutContext savedLayoutContext;
   private int[] savedSpannedGridRowHeights;
   private int savedStartRow;
   private int savedEndRow;
   private int savedBorderBeforeWhich;
   private int savedBorderAfterWhich;
   private boolean savedFirstOnPage;
   private boolean savedLastOnPage;
   private RowPainter savedPainter;
   private int savedFirstRowHeight;
   private boolean flushArea = true;
   private boolean isLastTrait;

   public TableCellLayoutManager(TableCell node, PrimaryGridUnit pgu) {
      super(node);
      this.setGeneratesBlockArea(true);
      this.primaryGridUnit = pgu;
      this.isDescendantOfTableHeader = node.getParent().getParent() instanceof TableHeader || node.getParent() instanceof TableHeader;
      this.isDescendantOfTableFooter = node.getParent().getParent() instanceof TableFooter || node.getParent() instanceof TableFooter;
      this.hasRetrieveTableMarker = node.hasRetrieveTableMarker();
   }

   public TableCell getTableCell() {
      return (TableCell)this.fobj;
   }

   private boolean isSeparateBorderModel() {
      return this.getTable().isSeparateBorderModel();
   }

   public Table getTable() {
      return this.getTableCell().getTable();
   }

   public void setHasRepeatedHeader(boolean hasRepeatedHeader) {
      this.hasRepeatedHeader = hasRepeatedHeader;
   }

   protected int getIPIndents() {
      int[] startEndBorderWidths = this.primaryGridUnit.getStartEndBorderWidths();
      this.startIndent = startEndBorderWidths[0];
      this.endIndent = startEndBorderWidths[1];
      if (this.isSeparateBorderModel()) {
         int borderSep = this.getTable().getBorderSeparation().getLengthPair().getIPD().getLength().getValue(this);
         this.startIndent += borderSep / 2;
         this.endIndent += borderSep / 2;
      } else {
         this.startIndent /= 2;
         this.endIndent /= 2;
      }

      this.startIndent += this.getTableCell().getCommonBorderPaddingBackground().getPaddingStart(false, this);
      this.endIndent += this.getTableCell().getCommonBorderPaddingBackground().getPaddingEnd(false, this);
      return this.startIndent + this.endIndent;
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      MinOptMax stackLimit = context.getStackLimitBP();
      this.referenceIPD = context.getRefIPD();
      this.cellIPD = this.referenceIPD;
      this.cellIPD -= this.getIPIndents();
      List contentList = new LinkedList();
      List returnList = new LinkedList();
      LayoutManager prevLM = null;

      LayoutManager curLM;
      while((curLM = this.getChildLM()) != null) {
         LayoutContext childLC = LayoutContext.newInstance();
         childLC.setStackLimitBP(context.getStackLimitBP().minus(stackLimit));
         childLC.setRefIPD(this.cellIPD);
         List returnedList = curLM.getNextKnuthElements(childLC, alignment);
         if (childLC.isKeepWithNextPending()) {
            log.debug("child LM signals pending keep with next");
         }

         if (contentList.isEmpty() && childLC.isKeepWithPreviousPending()) {
            this.primaryGridUnit.setKeepWithPrevious(childLC.getKeepWithPreviousPending());
            childLC.clearKeepWithPreviousPending();
         }

         if (prevLM != null && !ElementListUtils.endsWithForcedBreak(contentList)) {
            this.addInBetweenBreak(contentList, context, childLC);
         }

         contentList.addAll(returnedList);
         if (!returnedList.isEmpty()) {
            if (childLC.isKeepWithNextPending()) {
               context.updateKeepWithNextPending(childLC.getKeepWithNextPending());
               childLC.clearKeepWithNextPending();
            }

            prevLM = curLM;
         }
      }

      this.primaryGridUnit.setKeepWithNext(context.getKeepWithNextPending());
      new LinkedList();
      if (!contentList.isEmpty()) {
         this.wrapPositionElements(contentList, returnList);
      } else {
         returnList.add(new KnuthBox(0, this.notifyPos(new Position(this)), true));
      }

      SpaceResolver.resolveElementList(returnList);
      if (((KnuthElement)returnList.get(0)).isForcedBreak()) {
         this.primaryGridUnit.setBreakBefore(((KnuthPenalty)returnList.get(0)).getBreakClass());
         returnList.remove(0);

         assert !returnList.isEmpty();
      }

      KnuthElement lastItem = (KnuthElement)ListUtil.getLast(returnList);
      if (lastItem.isForcedBreak()) {
         KnuthPenalty p = (KnuthPenalty)lastItem;
         this.primaryGridUnit.setBreakAfter(p.getBreakClass());
         p.setPenalty(0);
      }

      this.setFinished(true);
      return returnList;
   }

   public void setYOffset(int off) {
      this.yoffset = off;
   }

   public void setXOffset(int off) {
      this.xoffset = off;
   }

   public void setContentHeight(int h) {
      this.usedBPD = h;
   }

   public void setTotalHeight(int h) {
      this.totalHeight = h;
   }

   private void clearRetrieveTableMarkerChildNodes(List childrenLMs) {
      if (childrenLMs != null) {
         int n = childrenLMs.size();
         Iterator var3 = childrenLMs.iterator();

         while(var3.hasNext()) {
            LayoutManager lm = (LayoutManager)var3.next();
            if (lm == null) {
               return;
            }

            if (lm instanceof RetrieveTableMarkerLayoutManager) {
               ((AbstractLayoutManager)lm).getFObj().clearChildNodes();
            } else {
               List lms = lm.getChildLMs();
               this.clearRetrieveTableMarkerChildNodes(lms);
            }
         }

      }
   }

   private boolean isDescendantOfTableHeaderOrFooter() {
      return this.isDescendantOfTableFooter || this.isDescendantOfTableHeader;
   }

   private void saveAddAreasArguments(PositionIterator parentIter, LayoutContext layoutContext, int[] spannedGridRowHeights, int startRow, int endRow, int borderBeforeWhich, int borderAfterWhich, boolean firstOnPage, boolean lastOnPage, RowPainter painter, int firstRowHeight) {
      if (!this.savedAddAreasArguments) {
         if (this.isDescendantOfTableHeader) {
            this.savedAddAreasArguments = true;
            this.savedParentIter = null;
            this.savedLayoutContext = null;
            this.savedSpannedGridRowHeights = spannedGridRowHeights;
            this.savedStartRow = startRow;
            this.savedEndRow = endRow;
            this.savedBorderBeforeWhich = borderBeforeWhich;
            this.savedBorderAfterWhich = borderAfterWhich;
            this.savedFirstOnPage = firstOnPage;
            this.savedLastOnPage = lastOnPage;
            this.savedPainter = painter;
            this.savedFirstRowHeight = firstRowHeight;
            TableLayoutManager parentTableLayoutManager = this.getTableLayoutManager();
            parentTableLayoutManager.saveTableHeaderTableCellLayoutManagers(this);
            this.flushArea = false;
         }

      }
   }

   private TableLayoutManager getTableLayoutManager() {
      LayoutManager parentLM;
      for(parentLM = this.getParent(); !(parentLM instanceof TableLayoutManager); parentLM = parentLM.getParent()) {
      }

      TableLayoutManager tlm = (TableLayoutManager)parentLM;
      return tlm;
   }

   protected void repeatAddAreas() {
      if (this.savedAddAreasArguments) {
         this.addAreas(this.savedParentIter, this.savedLayoutContext, this.savedSpannedGridRowHeights, this.savedStartRow, this.savedEndRow, this.savedBorderBeforeWhich, this.savedBorderAfterWhich, this.savedFirstOnPage, this.savedLastOnPage, this.savedPainter, this.savedFirstRowHeight);
         this.savedAddAreasArguments = false;
      }

   }

   public void addAreas(PositionIterator parentIter, LayoutContext layoutContext, int[] spannedGridRowHeights, int startRow, int endRow, int borderBeforeWhich, int borderAfterWhich, boolean firstOnPage, boolean lastOnPage, RowPainter painter, int firstRowHeight) {
      this.getParentArea((Area)null);
      this.addId();
      int borderBeforeWidth = this.primaryGridUnit.getBeforeBorderWidth(startRow, borderBeforeWhich);
      int borderAfterWidth = this.primaryGridUnit.getAfterBorderWidth(endRow, borderAfterWhich);
      CommonBorderPaddingBackground padding = this.primaryGridUnit.getCell().getCommonBorderPaddingBackground();
      int paddingRectBPD = this.totalHeight - borderBeforeWidth - borderAfterWidth;
      int cellBPD = paddingRectBPD - padding.getPaddingBefore(borderBeforeWhich == 2, this);
      cellBPD -= padding.getPaddingAfter(borderAfterWhich == 2, this);
      this.addBackgroundAreas(painter, firstRowHeight, borderBeforeWidth, paddingRectBPD);
      int displayAlign;
      if (this.isSeparateBorderModel()) {
         if (!this.emptyCell || this.getTableCell().showEmptyCells()) {
            if (borderBeforeWidth > 0) {
               displayAlign = this.getTableCell().getTable().getBorderSeparation().getBPD().getLength().getValue() / 2;
               adjustYOffset(this.curBlockArea, displayAlign);
            }

            TraitSetter.addBorders(this.curBlockArea, this.getTableCell().getCommonBorderPaddingBackground(), borderBeforeWidth == 0, borderAfterWidth == 0, false, false, this);
         }
      } else {
         boolean inFirstColumn = this.primaryGridUnit.getColIndex() == 0;
         boolean inLastColumn = this.primaryGridUnit.getColIndex() + this.getTableCell().getNumberColumnsSpanned() == this.getTable().getNumberOfColumns();
         if (!this.primaryGridUnit.hasSpanning()) {
            adjustYOffset(this.curBlockArea, -borderBeforeWidth);
            boolean[] outer = new boolean[]{firstOnPage, lastOnPage, inFirstColumn, inLastColumn};
            TraitSetter.addCollapsingBorders(this.curBlockArea, this.primaryGridUnit.getBorderBefore(borderBeforeWhich), this.primaryGridUnit.getBorderAfter(borderAfterWhich), this.primaryGridUnit.getBorderStart(), this.primaryGridUnit.getBorderEnd(), outer);
         } else {
            adjustYOffset(this.curBlockArea, borderBeforeWidth);
            Block[][] blocks = new Block[this.getTableCell().getNumberRowsSpanned()][this.getTableCell().getNumberColumnsSpanned()];
            GridUnit[] gridUnits = (GridUnit[])this.primaryGridUnit.getRows().get(startRow);
            int level = this.getTableCell().getBidiLevelRecursive();

            int dy;
            GridUnit gu;
            CommonBorderPaddingBackground.BorderInfo border;
            int dx;
            for(dy = 0; dy < this.getTableCell().getNumberColumnsSpanned(); ++dy) {
               gu = gridUnits[dy];
               border = gu.getBorderBefore(borderBeforeWhich);
               dx = border.getRetainedWidth() / 2;
               if (dx > 0) {
                  this.addBorder(blocks, startRow, dy, Trait.BORDER_BEFORE, border, firstOnPage, level);
                  adjustYOffset(blocks[startRow][dy], -dx);
                  adjustBPD(blocks[startRow][dy], -dx);
               }
            }

            gridUnits = (GridUnit[])this.primaryGridUnit.getRows().get(endRow);

            for(dy = 0; dy < this.getTableCell().getNumberColumnsSpanned(); ++dy) {
               gu = gridUnits[dy];
               border = gu.getBorderAfter(borderAfterWhich);
               dx = border.getRetainedWidth() / 2;
               if (dx > 0) {
                  this.addBorder(blocks, endRow, dy, Trait.BORDER_AFTER, border, lastOnPage, level);
                  adjustBPD(blocks[endRow][dy], -dx);
               }
            }

            int bpd;
            for(dy = startRow; dy <= endRow; ++dy) {
               gridUnits = (GridUnit[])this.primaryGridUnit.getRows().get(dy);
               CommonBorderPaddingBackground.BorderInfo border = gridUnits[0].getBorderStart();
               bpd = border.getRetainedWidth() / 2;
               if (bpd > 0) {
                  if (level == 1) {
                     this.addBorder(blocks, dy, gridUnits.length - 1, Trait.BORDER_START, border, inFirstColumn, level);
                     adjustIPD(blocks[dy][gridUnits.length - 1], -bpd);
                  } else {
                     this.addBorder(blocks, dy, 0, Trait.BORDER_START, border, inFirstColumn, level);
                     adjustXOffset(blocks[dy][0], bpd);
                     adjustIPD(blocks[dy][0], -bpd);
                  }
               }

               border = gridUnits[gridUnits.length - 1].getBorderEnd();
               bpd = border.getRetainedWidth() / 2;
               if (bpd > 0) {
                  if (level == 1) {
                     this.addBorder(blocks, dy, 0, Trait.BORDER_END, border, inLastColumn, level);
                     adjustXOffset(blocks[dy][0], bpd);
                     adjustIPD(blocks[dy][0], -bpd);
                  } else {
                     this.addBorder(blocks, dy, gridUnits.length - 1, Trait.BORDER_END, border, inLastColumn, level);
                     adjustIPD(blocks[dy][gridUnits.length - 1], -bpd);
                  }
               }
            }

            dy = this.yoffset;

            for(int y = startRow; y <= endRow; ++y) {
               bpd = spannedGridRowHeights[y - startRow];
               dx = this.xoffset;

               for(int x = 0; x < gridUnits.length; ++x) {
                  int ipd = this.getTable().getColumn(this.primaryGridUnit.getColIndex() + x).getColumnWidth().getValue(this.getParent());
                  if (blocks[y][x] != null) {
                     Block block = blocks[y][x];
                     adjustYOffset(block, dy);
                     adjustXOffset(block, dx);
                     adjustIPD(block, ipd);
                     adjustBPD(block, bpd);
                     this.parentLayoutManager.addChildArea(block);
                  }

                  dx += ipd;
               }

               dy += bpd;
            }
         }
      }

      TraitSetter.addPadding(this.curBlockArea, padding, borderBeforeWhich == 2, borderAfterWhich == 2, false, false, this);
      if (this.usedBPD < cellBPD) {
         Block space;
         if (this.getTableCell().getDisplayAlign() == 23) {
            space = new Block();
            space.setChangeBarList(this.getChangeBarList());
            space.setBPD((cellBPD - this.usedBPD) / 2);
            space.setBidiLevel(this.getTableCell().getBidiLevelRecursive());
            this.curBlockArea.addBlock(space);
         } else if (this.getTableCell().getDisplayAlign() == 3) {
            space = new Block();
            space.setChangeBarList(this.getChangeBarList());
            space.setBPD(cellBPD - this.usedBPD);
            space.setBidiLevel(this.getTableCell().getBidiLevelRecursive());
            this.curBlockArea.addBlock(space);
         }
      }

      if (this.isDescendantOfTableHeaderOrFooter() && this.hasRetrieveTableMarker) {
         if (this.isDescendantOfTableHeader && !this.savedAddAreasArguments) {
            this.saveAddAreasArguments(parentIter, layoutContext, spannedGridRowHeights, startRow, endRow, borderBeforeWhich, borderAfterWhich, firstOnPage, lastOnPage, painter, firstRowHeight);
         }

         this.recreateChildrenLMs();
         displayAlign = ((TableCell)this.getFObj()).getDisplayAlign();
         TableCellBreaker breaker = new TableCellBreaker(this, this.cellIPD, displayAlign);
         breaker.setDescendantOfTableFooter(this.isDescendantOfTableHeader);
         if (this.isDescendantOfTableHeader) {
            breaker.setRepeatedHeader(this.hasRepeatedHeader);
         } else {
            breaker.setRepeatedFooter(layoutContext.treatAsArtifact());
         }

         breaker.doLayout(this.usedBPD, false);
         this.clearRetrieveTableMarkerChildNodes(this.getChildLMs());
      }

      if (!this.hasRetrieveTableMarker) {
         AreaAdditionUtil.addAreas(this, parentIter, layoutContext);
      }

      this.curBlockArea.setBPD(cellBPD);
      if (!this.isSeparateBorderModel() || !this.emptyCell || this.getTableCell().showEmptyCells()) {
         TraitSetter.addBackground(this.curBlockArea, this.getTableCell().getCommonBorderPaddingBackground(), this);
      }

      if (this.flushArea) {
         this.flush();
      } else {
         this.flushArea = true;
      }

      this.curBlockArea = null;
      this.notifyEndOfLayout();
   }

   private void addBackgroundAreas(RowPainter painter, int firstRowHeight, int borderBeforeWidth, int paddingRectBPD) {
      TableColumn column = this.getTable().getColumn(this.primaryGridUnit.getColIndex());
      if (column.getCommonBorderPaddingBackground().hasBackground()) {
         Block colBackgroundArea = this.getBackgroundArea(paddingRectBPD, borderBeforeWidth);
         ((TableLayoutManager)this.parentLayoutManager).registerColumnBackgroundArea(column, colBackgroundArea, -this.startIndent);
      }

      TablePart body = this.primaryGridUnit.getTablePart();
      if (body.getCommonBorderPaddingBackground().hasBackground()) {
         painter.registerPartBackgroundArea(this.getBackgroundArea(paddingRectBPD, borderBeforeWidth));
      }

      TableRow row = this.primaryGridUnit.getRow();
      if (row != null && row.getCommonBorderPaddingBackground().hasBackground()) {
         Block rowBackgroundArea = this.getBackgroundArea(paddingRectBPD, borderBeforeWidth);
         ((TableLayoutManager)this.parentLayoutManager).addBackgroundArea(rowBackgroundArea);
         TraitSetter.addBackground(rowBackgroundArea, row.getCommonBorderPaddingBackground(), this.parentLayoutManager, -this.xoffset - this.startIndent, -borderBeforeWidth, this.parentLayoutManager.getContentAreaIPD(), firstRowHeight);
      }

   }

   private void addBorder(Block[][] blocks, int i, int j, Integer side, CommonBorderPaddingBackground.BorderInfo border, boolean outer, int level) {
      if (blocks[i][j] == null) {
         blocks[i][j] = new Block();
         blocks[i][j].setChangeBarList(this.getChangeBarList());
         blocks[i][j].addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
         blocks[i][j].setPositioning(2);
         blocks[i][j].setBidiLevel(level);
      }

      blocks[i][j].addTrait(side, BorderProps.makeRectangular(border.getStyle(), border.getRetainedWidth(), border.getColor(), outer ? BorderProps.Mode.COLLAPSE_OUTER : BorderProps.Mode.COLLAPSE_INNER));
   }

   private static void adjustXOffset(Block block, int amount) {
      block.setXOffset(block.getXOffset() + amount);
   }

   private static void adjustYOffset(Block block, int amount) {
      block.setYOffset(block.getYOffset() + amount);
   }

   private static void adjustIPD(Block block, int amount) {
      block.setIPD(block.getIPD() + amount);
   }

   private static void adjustBPD(Block block, int amount) {
      block.setBPD(block.getBPD() + amount);
   }

   private Block getBackgroundArea(int bpd, int borderBeforeWidth) {
      CommonBorderPaddingBackground padding = this.getTableCell().getCommonBorderPaddingBackground();
      int paddingStart = padding.getPaddingStart(false, this);
      int paddingEnd = padding.getPaddingEnd(false, this);
      Block block = new Block();
      block.setChangeBarList(this.getChangeBarList());
      TraitSetter.setProducerID(block, this.getTable().getId());
      block.setPositioning(2);
      block.setIPD(this.cellIPD + paddingStart + paddingEnd);
      block.setBPD(bpd);
      block.setXOffset(this.xoffset + this.startIndent - paddingStart);
      block.setYOffset(this.yoffset + borderBeforeWidth);
      block.setBidiLevel(this.getTableCell().getBidiLevelRecursive());
      return block;
   }

   public Area getParentArea(Area childArea) {
      if (this.curBlockArea == null) {
         this.curBlockArea = new Block();
         this.curBlockArea.setChangeBarList(this.getChangeBarList());
         this.curBlockArea.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
         TraitSetter.setProducerID(this.curBlockArea, this.getTableCell().getId());
         this.curBlockArea.setPositioning(2);
         this.curBlockArea.setXOffset(this.xoffset + this.startIndent);
         this.curBlockArea.setYOffset(this.yoffset);
         this.curBlockArea.setIPD(this.cellIPD);
         this.curBlockArea.setBidiLevel(this.getTableCell().getBidiLevelRecursive());
         this.parentLayoutManager.getParentArea(this.curBlockArea);
         this.setCurrentArea(this.curBlockArea);
      }

      return this.curBlockArea;
   }

   public void addChildArea(Area childArea) {
      if (this.curBlockArea != null) {
         this.curBlockArea.addBlock((Block)childArea);
      }

   }

   public int negotiateBPDAdjustment(int adj, KnuthElement lastElement) {
      return 0;
   }

   public void discardSpace(KnuthGlue spaceGlue) {
   }

   public Keep getKeepTogether() {
      return Keep.KEEP_AUTO;
   }

   public Keep getKeepWithNext() {
      return Keep.KEEP_AUTO;
   }

   public Keep getKeepWithPrevious() {
      return Keep.KEEP_AUTO;
   }

   public int getContentAreaIPD() {
      return this.cellIPD;
   }

   public int getContentAreaBPD() {
      if (this.curBlockArea != null) {
         return this.curBlockArea.getBPD();
      } else {
         log.error("getContentAreaBPD called on unknown BPD");
         return -1;
      }
   }

   public boolean getGeneratesReferenceArea() {
      return true;
   }

   public boolean getGeneratesBlockArea() {
      return true;
   }

   protected void registerMarkers(boolean isStarting, boolean isFirst, boolean isLast) {
      Map markers = this.getTableCell().getMarkers();
      if (markers != null) {
         this.getCurrentPV().registerMarkers(markers, isStarting, isFirst, isLast && this.isLastTrait);
         if (!this.isDescendantOfTableHeaderOrFooter()) {
            this.getTableLayoutManager().registerMarkers(markers, isStarting, isFirst, isLast && this.isLastTrait);
         }
      }

   }

   void setLastTrait(boolean isLast) {
      this.isLastTrait = isLast;
   }

   public void setParent(LayoutManager lm) {
      this.parentLayoutManager = lm;
      if (this.hasRetrieveTableMarker) {
         this.getTableLayoutManager().flagAsHavingRetrieveTableMarker();
      }

   }

   private static class TableCellBreaker extends LocalBreaker {
      public TableCellBreaker(TableCellLayoutManager lm, int ipd, int displayAlign) {
         super(lm, ipd, displayAlign);
      }

      protected void observeElementList(List elementList) {
         String elementListID = this.lm.getParent().getFObj().getId() + "-" + this.lm.getFObj().getId();
         ElementListObserver.observe(elementList, "table-cell", elementListID);
      }
   }
}
