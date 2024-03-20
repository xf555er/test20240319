package org.apache.fop.layoutmgr.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.flow.Markers;
import org.apache.fop.fo.flow.RetrieveTableMarker;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableColumn;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.BreakOpportunity;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.SpacedBorderedPaddedBlockLayoutManager;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.util.BreakUtil;

public class TableLayoutManager extends SpacedBorderedPaddedBlockLayoutManager implements BreakOpportunity {
   private static Log log = LogFactory.getLog(TableLayoutManager.class);
   private TableContentLayoutManager contentLM;
   private ColumnSetup columns;
   private Block curBlockArea;
   private double tableUnit;
   private double oldTableUnit;
   private boolean autoLayout = true;
   private int halfBorderSeparationBPD;
   private int halfBorderSeparationIPD;
   private List columnBackgroundAreas;
   private Position auxiliaryPosition;
   private List savedTCLMs;
   private boolean areAllTCLMsSaved;
   private Markers tableMarkers;
   private Markers tableFragmentMarkers;
   private boolean hasRetrieveTableMarker;
   private boolean repeatedHeader;
   private List headerFootnotes = Collections.emptyList();
   private List footerFootnotes = Collections.emptyList();

   public TableLayoutManager(Table node) {
      super(node);
      this.columns = new ColumnSetup(node);
   }

   protected CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
      return this.getTable().getCommonBorderPaddingBackground();
   }

   public Table getTable() {
      return (Table)this.fobj;
   }

   public ColumnSetup getColumns() {
      return this.columns;
   }

   public void initialize() {
      this.foSpaceBefore = (new SpaceVal(this.getTable().getCommonMarginBlock().spaceBefore, this)).getSpace();
      this.foSpaceAfter = (new SpaceVal(this.getTable().getCommonMarginBlock().spaceAfter, this)).getSpace();
      this.startIndent = this.getTable().getCommonMarginBlock().startIndent.getValue(this);
      this.endIndent = this.getTable().getCommonMarginBlock().endIndent.getValue(this);
      if (this.getTable().isSeparateBorderModel()) {
         this.halfBorderSeparationBPD = this.getTable().getBorderSeparation().getBPD().getLength().getValue(this) / 2;
         this.halfBorderSeparationIPD = this.getTable().getBorderSeparation().getIPD().getLength().getValue(this) / 2;
      } else {
         this.halfBorderSeparationBPD = 0;
         this.halfBorderSeparationIPD = 0;
      }

      if (!this.getTable().isAutoLayout() && this.getTable().getInlineProgressionDimension().getOptimum(this).getEnum() != 9) {
         this.autoLayout = false;
      }

   }

   private void resetSpaces() {
      this.discardBorderBefore = false;
      this.discardBorderAfter = false;
      this.discardPaddingBefore = false;
      this.discardPaddingAfter = false;
      this.effSpaceBefore = null;
      this.effSpaceAfter = null;
   }

   public int getHalfBorderSeparationBPD() {
      return this.halfBorderSeparationBPD;
   }

   public int getHalfBorderSeparationIPD() {
      return this.halfBorderSeparationIPD;
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      List returnList = new LinkedList();
      this.referenceIPD = context.getRefIPD();
      int sumOfColumns;
      if (this.getTable().getInlineProgressionDimension().getOptimum(this).getEnum() != 9) {
         sumOfColumns = this.getTable().getInlineProgressionDimension().getOptimum(this).getLength().getValue(this);
         this.updateContentAreaIPDwithOverconstrainedAdjust(sumOfColumns);
      } else {
         if (!this.getTable().isAutoLayout()) {
            BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(this.getTable().getUserAgent().getEventBroadcaster());
            eventProducer.tableFixedAutoWidthNotSupported(this, this.getTable().getLocator());
         }

         this.updateContentAreaIPDwithOverconstrainedAdjust();
      }

      sumOfColumns = this.columns.getSumOfColumnWidths(this);
      if (!this.autoLayout && sumOfColumns > this.getContentAreaIPD()) {
         log.debug(FONode.decorateWithContextInfo("The sum of all column widths is larger than the specified table width.", this.getTable()));
         this.updateContentAreaIPDwithOverconstrainedAdjust(sumOfColumns);
      }

      int availableIPD = this.referenceIPD - this.getIPIndents();
      if (this.getContentAreaIPD() > availableIPD) {
         BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(this.getTable().getUserAgent().getEventBroadcaster());
         eventProducer.objectTooWide(this, this.getTable().getName(), this.getContentAreaIPD(), context.getRefIPD(), this.getTable().getLocator());
      }

      if (this.tableUnit == 0.0) {
         this.tableUnit = this.columns.computeTableUnit(this);
         this.tableUnit = Math.max(this.tableUnit, this.oldTableUnit);
      }

      if (!this.firstVisibleMarkServed) {
         this.addKnuthElementsForSpaceBefore(returnList, alignment);
      }

      if (this.getTable().isSeparateBorderModel()) {
         this.addKnuthElementsForBorderPaddingBefore(returnList, !this.firstVisibleMarkServed);
         this.firstVisibleMarkServed = true;
         this.addPendingMarks(context);
      }

      this.contentLM = new TableContentLayoutManager(this);
      LayoutContext childLC = LayoutContext.newInstance();
      childLC.setRefIPD(context.getRefIPD());
      childLC.copyPendingMarksFrom(context);
      List contentKnuthElements = this.contentLM.getNextKnuthElements(childLC, alignment);
      Iterator var8 = contentKnuthElements.iterator();

      while(var8.hasNext()) {
         Object contentKnuthElement = var8.next();
         ListElement el = (ListElement)contentKnuthElement;
         this.notifyPos(el.getPosition());
      }

      log.debug(contentKnuthElements);
      this.wrapPositionElements(contentKnuthElements, returnList);
      context.updateKeepWithPreviousPending(this.getKeepWithPrevious());
      context.updateKeepWithPreviousPending(childLC.getKeepWithPreviousPending());
      context.updateKeepWithNextPending(this.getKeepWithNext());
      context.updateKeepWithNextPending(childLC.getKeepWithNextPending());
      if (this.getTable().isSeparateBorderModel()) {
         this.addKnuthElementsForBorderPaddingAfter(returnList, true);
      }

      this.addKnuthElementsForSpaceAfter(returnList, alignment);
      int breakAfter;
      if (!context.suppressBreakBefore()) {
         breakAfter = BreakUtil.compareBreakClasses(this.getTable().getBreakBefore(), childLC.getBreakBefore());
         if (breakAfter != 9) {
            returnList.add(0, new BreakElement(new LeafPosition(this.getParent(), 0), 0, -1000, breakAfter, context));
         }
      }

      breakAfter = BreakUtil.compareBreakClasses(this.getTable().getBreakAfter(), childLC.getBreakAfter());
      if (breakAfter != 9) {
         returnList.add(new BreakElement(new LeafPosition(this.getParent(), 0), 0, -1000, breakAfter, context));
      }

      this.setFinished(true);
      this.resetSpaces();
      return returnList;
   }

   public Position getAuxiliaryPosition() {
      if (this.auxiliaryPosition == null) {
         this.auxiliaryPosition = new LeafPosition(this, 0);
      }

      return this.auxiliaryPosition;
   }

   void registerColumnBackgroundArea(TableColumn column, Block backgroundArea, int xShift) {
      this.addBackgroundArea(backgroundArea);
      if (this.columnBackgroundAreas == null) {
         this.columnBackgroundAreas = new ArrayList();
      }

      this.columnBackgroundAreas.add(new ColumnBackgroundInfo(column, backgroundArea, xShift));
   }

   public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
      this.getParentArea((Area)null);
      this.addId();
      if (layoutContext.getSpaceBefore() != 0) {
         this.addBlockSpacing(0.0, MinOptMax.getInstance(layoutContext.getSpaceBefore()));
      }

      int startXOffset = this.getTable().getCommonMarginBlock().startIndent.getValue(this);
      int tableHeight = 0;
      LayoutContext lc = LayoutContext.offspringOf(layoutContext);
      lc.setRefIPD(this.getContentAreaIPD());
      this.contentLM.setStartXOffset(startXOffset);
      this.contentLM.addAreas(parentIter, lc);
      if (this.fobj.getUserAgent().isTableBorderOverpaint()) {
         new OverPaintBorders(this.curBlockArea);
      }

      tableHeight += this.contentLM.getUsedBPD();
      this.curBlockArea.setBPD(tableHeight);
      if (this.columnBackgroundAreas != null) {
         Iterator var6 = this.columnBackgroundAreas.iterator();

         while(var6.hasNext()) {
            Object columnBackgroundArea = var6.next();
            ColumnBackgroundInfo b = (ColumnBackgroundInfo)columnBackgroundArea;
            TraitSetter.addBackground(b.backgroundArea, b.column.getCommonBorderPaddingBackground(), this, b.xShift, -b.backgroundArea.getYOffset(), b.column.getColumnWidth().getValue(this), tableHeight);
         }

         this.columnBackgroundAreas.clear();
      }

      if (this.getTable().isSeparateBorderModel()) {
         TraitSetter.addBorders(this.curBlockArea, this.getTable().getCommonBorderPaddingBackground(), this.discardBorderBefore, this.discardBorderAfter, false, false, this);
         TraitSetter.addPadding(this.curBlockArea, this.getTable().getCommonBorderPaddingBackground(), this.discardPaddingBefore, this.discardPaddingAfter, false, false, this);
      }

      TraitSetter.addBackground(this.curBlockArea, this.getTable().getCommonBorderPaddingBackground(), this);
      TraitSetter.addMargins(this.curBlockArea, this.getTable().getCommonBorderPaddingBackground(), this.startIndent, this.endIndent, this);
      TraitSetter.addBreaks(this.curBlockArea, this.getTable().getBreakBefore(), this.getTable().getBreakAfter());
      TraitSetter.addSpaceBeforeAfter(this.curBlockArea, layoutContext.getSpaceAdjust(), this.effSpaceBefore, this.effSpaceAfter);
      this.flush();
      this.resetSpaces();
      this.curBlockArea = null;
      this.notifyEndOfLayout();
   }

   public Area getParentArea(Area childArea) {
      if (this.curBlockArea == null) {
         this.curBlockArea = new Block();
         this.curBlockArea.setChangeBarList(this.getChangeBarList());
         this.parentLayoutManager.getParentArea(this.curBlockArea);
         TraitSetter.setProducerID(this.curBlockArea, this.getTable().getId());
         this.curBlockArea.setIPD(this.getContentAreaIPD());
         this.setCurrentArea(this.curBlockArea);
      }

      return this.curBlockArea;
   }

   public void addChildArea(Area childArea) {
      if (this.curBlockArea != null) {
         this.curBlockArea.addBlock((Block)childArea);
      }

   }

   void addBackgroundArea(Block background) {
      this.curBlockArea.addChildArea(background);
   }

   public int negotiateBPDAdjustment(int adj, KnuthElement lastElement) {
      return 0;
   }

   public void discardSpace(KnuthGlue spaceGlue) {
   }

   public KeepProperty getKeepTogetherProperty() {
      return this.getTable().getKeepTogether();
   }

   public KeepProperty getKeepWithPreviousProperty() {
      return this.getTable().getKeepWithPrevious();
   }

   public KeepProperty getKeepWithNextProperty() {
      return this.getTable().getKeepWithNext();
   }

   public int getBaseLength(int lengthBase, FObj fobj) {
      if (fobj instanceof TableColumn && fobj.getParent() == this.getFObj()) {
         switch (lengthBase) {
            case 5:
               return this.getContentAreaIPD();
            case 11:
               return (int)this.tableUnit;
            default:
               log.error("Unknown base type for LengthBase.");
               return 0;
         }
      } else {
         switch (lengthBase) {
            case 11:
               return (int)this.tableUnit;
            default:
               return super.getBaseLength(lengthBase, fobj);
         }
      }
   }

   public void reset() {
      super.reset();
      this.curBlockArea = null;
      this.oldTableUnit = this.tableUnit;
      this.tableUnit = 0.0;
   }

   protected void saveTableHeaderTableCellLayoutManagers(TableCellLayoutManager tclm) {
      if (this.savedTCLMs == null) {
         this.savedTCLMs = new ArrayList();
      }

      if (!this.areAllTCLMsSaved) {
         this.savedTCLMs.add(tclm);
      }

   }

   protected void repeatAddAreasForSavedTableHeaderTableCellLayoutManagers() {
      if (this.savedTCLMs != null) {
         this.areAllTCLMsSaved = true;

         TableCellLayoutManager tclm;
         for(Iterator var1 = this.savedTCLMs.iterator(); var1.hasNext(); tclm.repeatAddAreas()) {
            tclm = (TableCellLayoutManager)var1.next();
            if (this.repeatedHeader) {
               tclm.setHasRepeatedHeader(true);
            }
         }

      }
   }

   public RetrieveTableMarker resolveRetrieveTableMarker(RetrieveTableMarker rtm) {
      String name = rtm.getRetrieveClassName();
      int originalPosition = rtm.getPosition();
      boolean changedPosition = false;
      Marker mark = null;
      mark = this.tableFragmentMarkers == null ? null : this.tableFragmentMarkers.resolve(rtm);
      if (mark == null && rtm.getBoundary() != 193) {
         rtm.changePositionTo(191);
         changedPosition = true;
         mark = this.getCurrentPV().resolveMarker(rtm);
         if (mark == null && rtm.getBoundary() != 104) {
            mark = this.tableMarkers == null ? null : this.tableMarkers.resolve(rtm);
         }
      }

      if (changedPosition) {
         rtm.changePositionTo(originalPosition);
      }

      if (mark == null) {
         log.debug("found no marker with name: " + name);
         return null;
      } else {
         rtm.bindMarker(mark);
         return rtm;
      }
   }

   public void registerMarkers(Map marks, boolean starting, boolean isfirst, boolean islast) {
      if (this.tableMarkers == null) {
         this.tableMarkers = new Markers();
      }

      this.tableMarkers.register(marks, starting, isfirst, islast);
      if (this.tableFragmentMarkers == null) {
         this.tableFragmentMarkers = new Markers();
      }

      this.tableFragmentMarkers.register(marks, starting, isfirst, islast);
   }

   protected void clearTableFragmentMarkers() {
      this.tableFragmentMarkers = null;
   }

   public void flagAsHavingRetrieveTableMarker() {
      this.hasRetrieveTableMarker = true;
   }

   protected void possiblyRegisterMarkersForTables(Map markers, boolean isStarting, boolean isFirst, boolean isLast) {
      if (this.hasRetrieveTableMarker) {
         this.registerMarkers(markers, isStarting, isFirst, isLast);
      }

      super.possiblyRegisterMarkersForTables(markers, isStarting, isFirst, isLast);
   }

   void setHeaderFootnotes(List footnotes) {
      this.headerFootnotes = footnotes;
   }

   List getHeaderFootnotes() {
      return this.headerFootnotes;
   }

   void setFooterFootnotes(List footnotes) {
      this.footerFootnotes = footnotes;
   }

   public void setRepeateHeader(boolean repeateHeader) {
      this.repeatedHeader = repeateHeader;
   }

   List getFooterFootnotes() {
      return this.footerFootnotes;
   }

   private static final class ColumnBackgroundInfo {
      private TableColumn column;
      private Block backgroundArea;
      private int xShift;

      private ColumnBackgroundInfo(TableColumn column, Block backgroundArea, int xShift) {
         this.column = column;
         this.backgroundArea = backgroundArea;
         this.xShift = xShift;
      }

      // $FF: synthetic method
      ColumnBackgroundInfo(TableColumn x0, Block x1, int x2, Object x3) {
         this(x0, x1, x2);
      }
   }
}
