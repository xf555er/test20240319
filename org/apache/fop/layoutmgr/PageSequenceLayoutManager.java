package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Area;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.area.AreaTreeModel;
import org.apache.fop.area.LineArea;
import org.apache.fop.complexscripts.bidi.BidiResolver;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.PageSequenceMaster;
import org.apache.fop.fo.pagination.Region;
import org.apache.fop.fo.pagination.RegionBody;
import org.apache.fop.fo.pagination.SideRegion;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.layoutmgr.inline.ContentLayoutManager;
import org.apache.fop.traits.MinOptMax;

public class PageSequenceLayoutManager extends AbstractPageSequenceLayoutManager {
   private static Log log = LogFactory.getLog(PageSequenceLayoutManager.class);
   private PageProvider pageProvider;
   private PageBreaker pageBreaker;
   private List tableHeaderFootnotes;
   private List tableFooterFootnotes;
   private int startIntrusionAdjustment;
   private int endIntrusionAdjustment;

   public PageSequenceLayoutManager(AreaTreeHandler ath, PageSequence pseq) {
      super(ath, pseq);
      this.pageProvider = new PageProvider(ath, pseq);
   }

   public PageProvider getPageProvider() {
      return this.pageProvider;
   }

   protected PageSequence getPageSequence() {
      return (PageSequence)this.pageSeq;
   }

   public PageSequenceLayoutManager getPSLM() {
      return this;
   }

   public FlowLayoutManager getFlowLayoutManager() {
      if (this.pageBreaker == null) {
         throw new IllegalStateException("This method can be called only during layout");
      } else {
         return this.pageBreaker.getCurrentChildLM();
      }
   }

   public void activateLayout() {
      this.initialize();
      if (this.areaTreeHandler.isComplexScriptFeaturesEnabled()) {
         BidiResolver.resolveInlineDirectionality(this.getPageSequence());
      }

      LineArea title = null;
      if (this.getPageSequence().getTitleFO() != null) {
         try {
            ContentLayoutManager clm = this.getLayoutManagerMaker().makeContentLayoutManager(this, this.getPageSequence().getTitleFO());
            Area parentArea = clm.getParentArea((Area)null);

            assert parentArea instanceof LineArea;

            title = (LineArea)parentArea;
         } catch (IllegalStateException var6) {
         }
      }

      AreaTreeModel areaTreeModel = this.areaTreeHandler.getAreaTreeModel();
      org.apache.fop.area.PageSequence pageSequenceAreaObject = new org.apache.fop.area.PageSequence(title);
      this.transferExtensions(pageSequenceAreaObject);
      pageSequenceAreaObject.setLocale(this.getPageSequence().getLocale());
      areaTreeModel.startPageSequence(pageSequenceAreaObject);
      if (log.isDebugEnabled()) {
         log.debug("Starting layout");
      }

      for(boolean finished = false; !finished; this.pageProvider.skipPagePositionOnly = true) {
         this.initialize();
         this.curPage = this.makeNewPage(false);
         this.pageBreaker = new PageBreaker(this);
         int flowBPD = this.getCurrentPV().getBodyRegion().getRemainingBPD();
         finished = this.pageBreaker.doLayout(flowBPD);
      }

      this.finishPage();
   }

   public void initialize() {
      super.initialize();
      this.pageProvider.initialize();
   }

   public void finishPageSequence() {
      if (this.pageSeq.hasId()) {
         this.idTracker.signalIDProcessed(this.pageSeq.getId());
      }

      this.pageSeq.getRoot().notifyPageSequenceFinished(this.currentPageNum, this.currentPageNum - this.startPageNum + 1);
      this.areaTreeHandler.notifyPageSequenceFinished(this.pageSeq, this.currentPageNum - this.startPageNum + 1);
      this.getPageSequence().releasePageSequence();
      String masterReference = this.getPageSequence().getMasterReference();
      PageSequenceMaster pageSeqMaster = this.pageSeq.getRoot().getLayoutMasterSet().getPageSequenceMaster(masterReference);
      if (pageSeqMaster != null) {
         pageSeqMaster.reset();
      }

      if (log.isDebugEnabled()) {
         log.debug("Ending layout");
      }

   }

   protected Page createPage(int pageNumber, boolean isBlank) {
      return this.pageProvider.getPage(isBlank, pageNumber, 0);
   }

   protected Page makeNewPage(boolean isBlank) {
      return this.makeNewPage(isBlank, false);
   }

   protected Page makeNewPage(boolean isBlank, boolean emptyContent) {
      Page newPage = super.makeNewPage(isBlank);
      if (!isBlank && !emptyContent) {
         for(int i = 0; !this.flowNameEquals(newPage, i > 0); ++i) {
            newPage = super.makeNewPage(isBlank);
         }
      }

      return newPage;
   }

   private boolean flowNameEquals(Page newPage, boolean strict) {
      String psName = this.getPageSequence().getMainFlow().getFlowName();
      Region body = newPage.getSimplePageMaster().getRegion(58);
      String name = body.getRegionName();
      if (strict && !name.equals(psName) && !name.equals(((RegionBody)body).getDefaultRegionName()) && this.getPageSequence().hasPagePositionLast()) {
         throw new RuntimeException("The flow-name \"" + name + "\" could not be mapped to a region-name in the layout-master-set");
      } else {
         return psName.equals(name);
      }
   }

   private void layoutSideRegion(int regionID) {
      SideRegion reg = (SideRegion)this.curPage.getSimplePageMaster().getRegion(regionID);
      if (reg != null) {
         StaticContent sc = this.getPageSequence().getStaticContent(reg.getRegionName());
         if (sc != null) {
            StaticContentLayoutManager lm = this.getLayoutManagerMaker().makeStaticContentLayoutManager(this, sc, reg);
            lm.doLayout();
         }
      }
   }

   protected void finishPage() {
      this.layoutSideRegion(57);
      this.layoutSideRegion(56);
      this.layoutSideRegion(61);
      this.layoutSideRegion(59);
      super.finishPage();
   }

   protected int getForcedLastPageNum(int lastPageNum) {
      int forcedLastPageNum = lastPageNum;
      int relativeLastPage = lastPageNum - this.startPageNum + 1;
      if (this.getPageSequence().getForcePageCount() == 43) {
         if (relativeLastPage % 2 != 0) {
            forcedLastPageNum = lastPageNum + 1;
         }
      } else if (this.getPageSequence().getForcePageCount() == 99) {
         if (relativeLastPage % 2 == 0) {
            forcedLastPageNum = lastPageNum + 1;
         }
      } else if (this.getPageSequence().getForcePageCount() == 40) {
         if (lastPageNum % 2 != 0) {
            forcedLastPageNum = lastPageNum + 1;
         }
      } else if (this.getPageSequence().getForcePageCount() == 41 && lastPageNum % 2 == 0) {
         forcedLastPageNum = lastPageNum + 1;
      }

      return forcedLastPageNum;
   }

   boolean isOnFirstPage(int partIndex) {
      return this.pageProvider.isOnFirstPage(partIndex);
   }

   protected int getLastPageNumber() {
      return this.pageProvider.getLastPageIndex();
   }

   protected int getWidthOfCurrentPage() {
      return this.curPage != null ? (int)this.curPage.getPageViewport().getViewArea().getWidth() : 0;
   }

   public void addTableHeaderFootnotes(List headerFootnotes) {
      if (this.tableHeaderFootnotes == null) {
         this.tableHeaderFootnotes = new ArrayList();
      }

      this.tableHeaderFootnotes.addAll(headerFootnotes);
   }

   public List getTableHeaderFootnotes() {
      return this.getTableFootnotes(this.tableHeaderFootnotes);
   }

   public void addTableFooterFootnotes(List footerFootnotes) {
      if (this.tableFooterFootnotes == null) {
         this.tableFooterFootnotes = new ArrayList();
      }

      this.tableFooterFootnotes.addAll(footerFootnotes);
   }

   public List getTableFooterFootnotes() {
      return this.getTableFootnotes(this.tableFooterFootnotes);
   }

   private List getTableFootnotes(List tableFootnotes) {
      if (tableFootnotes == null) {
         List emptyList = Collections.emptyList();
         return emptyList;
      } else {
         return tableFootnotes;
      }
   }

   public void clearTableHeadingFootnotes() {
      if (this.tableHeaderFootnotes != null) {
         this.tableHeaderFootnotes.clear();
      }

      if (this.tableFooterFootnotes != null) {
         this.tableFooterFootnotes.clear();
      }

   }

   public void setStartIntrusionAdjustment(int sia) {
      this.startIntrusionAdjustment = sia;
   }

   public void setEndIntrusionAdjustment(int eia) {
      this.endIntrusionAdjustment = eia;
   }

   public int getStartIntrusionAdjustment() {
      return this.startIntrusionAdjustment;
   }

   public int getEndIntrusionAdjustment() {
      return this.endIntrusionAdjustment;
   }

   public void recordEndOfFloat(int fHeight) {
      this.pageBreaker.handleEndOfFloat(fHeight);
   }

   public boolean handlingEndOfFloat() {
      return this.pageBreaker.handlingEndOfFloat();
   }

   public int getOffsetDueToFloat() {
      return this.pageBreaker.getOffsetDueToFloat();
   }

   public void recordStartOfFloat(int fHeight, int fYOffset) {
      this.pageBreaker.handleStartOfFloat(fHeight, fYOffset);
   }

   public boolean handlingStartOfFloat() {
      return this.pageBreaker.handlingStartOfFloat();
   }

   public int getFloatHeight() {
      return this.pageBreaker.getFloatHeight();
   }

   public int getFloatYOffset() {
      return this.pageBreaker.getFloatYOffset();
   }

   public int getCurrentColumnWidth() {
      int flowIPD = this.getCurrentPV().getCurrentSpan().getColumnWidth();
      flowIPD -= this.startIntrusionAdjustment + this.endIntrusionAdjustment;
      return flowIPD;
   }

   public void holdFootnotes(List fl, List ll, int tfl, int ifl, boolean fp, boolean nf, int fnfi, int fli, int fei, MinOptMax fsl, int pfli, int pfei) {
      if (fl != null && fl.size() > 0) {
         this.pageBreaker.holdFootnotes(fl, ll, tfl, ifl, fp, nf, fnfi, fli, fei, fsl, pfli, pfei);
      }

   }

   public void retrieveFootnotes(PageBreakingAlgorithm alg) {
      this.pageBreaker.retrieveFootones(alg);
   }
}
