package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.area.PageViewport;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.RegionBody;
import org.apache.fop.fo.pagination.SimplePageMaster;

public class PageProvider implements Constants {
   private Log log = LogFactory.getLog(PageProvider.class);
   public static final int RELTO_PAGE_SEQUENCE = 0;
   public static final int RELTO_CURRENT_ELEMENT_LIST = 1;
   private int startPageOfPageSequence;
   private int startPageOfCurrentElementList;
   private int startColumnOfCurrentElementList;
   private boolean spanAllForCurrentElementList;
   private List cachedPages = new ArrayList();
   private int lastPageIndex = -1;
   private int indexOfCachedLastPage = -1;
   private int lastRequestedIndex = -1;
   private int lastReportedBPD = -1;
   private AreaTreeHandler areaTreeHandler;
   private PageSequence pageSeq;
   protected boolean skipPagePositionOnly;

   public PageProvider(AreaTreeHandler ath, PageSequence ps) {
      this.areaTreeHandler = ath;
      this.pageSeq = ps;
      this.startPageOfPageSequence = ps.getStartingPageNumber();
   }

   public void initialize() {
      this.cachedPages.clear();
   }

   public void setStartOfNextElementList(int startPage, int startColumn, boolean spanAll) {
      if (this.log.isDebugEnabled()) {
         this.log.debug("start of the next element list is: page=" + startPage + " col=" + startColumn + (spanAll ? ", column-spanning" : ""));
      }

      this.startPageOfCurrentElementList = startPage - this.startPageOfPageSequence + 1;
      this.startColumnOfCurrentElementList = startColumn;
      this.spanAllForCurrentElementList = spanAll;
      this.lastRequestedIndex = -1;
      this.lastReportedBPD = -1;
   }

   public void setLastPageIndex(int index) {
      this.lastPageIndex = index;
   }

   public int getAvailableBPD(int index) {
      if (this.lastRequestedIndex == index) {
         if (this.log.isTraceEnabled()) {
            this.log.trace("getAvailableBPD(" + index + ") -> (cached) " + this.lastReportedBPD);
         }

         return this.lastReportedBPD;
      } else {
         int pageIndexTmp = index;
         int pageIndex = 0;
         int colIndex = this.startColumnOfCurrentElementList;

         Page page;
         for(page = this.getPage(false, pageIndex, 1); pageIndexTmp > 0; --pageIndexTmp) {
            ++colIndex;
            if (colIndex >= page.getPageViewport().getCurrentSpan().getColumnCount()) {
               colIndex = 0;
               ++pageIndex;
               page = this.getPage(false, pageIndex, 1);
               BodyRegion br = page.getPageViewport().getBodyRegion();
               if (!this.pageSeq.getMainFlow().getFlowName().equals(br.getRegionName())) {
                  ++pageIndexTmp;
               }
            }
         }

         this.lastRequestedIndex = index;
         this.lastReportedBPD = page.getPageViewport().getBodyRegion().getRemainingBPD();
         if (this.log.isTraceEnabled()) {
            this.log.trace("getAvailableBPD(" + index + ") -> " + this.lastReportedBPD);
         }

         return this.lastReportedBPD;
      }
   }

   private Column getColumn(int index) {
      int columnCount = 0;
      int colIndex = this.startColumnOfCurrentElementList + index;
      int pageIndex = -1;

      Page page;
      do {
         colIndex -= columnCount;
         ++pageIndex;
         page = this.getPage(false, pageIndex, 1);
         columnCount = page.getPageViewport().getCurrentSpan().getColumnCount();
      } while(colIndex >= columnCount);

      return new Column(page, pageIndex, colIndex, columnCount);
   }

   public int compareIPDs(int index) {
      Column column = this.getColumn(index);
      if (column.colIndex + 1 < column.columnCount) {
         return 0;
      } else {
         Page nextPage = this.getPage(false, column.pageIndex + 1, 1);
         return column.page.getPageViewport().getBodyRegion().getColumnIPD() - nextPage.getPageViewport().getBodyRegion().getColumnIPD();
      }
   }

   boolean startPage(int index) {
      return this.getColumn(index).colIndex == 0;
   }

   boolean endPage(int index) {
      Column column = this.getColumn(index);
      return column.colIndex == column.columnCount - 1;
   }

   int getColumnCount(int index) {
      return this.getColumn(index).columnCount;
   }

   public int getStartingPartIndexForLastPage(int partCount) {
      int lastPartIndex = partCount - 1;
      return lastPartIndex - this.getColumn(lastPartIndex).colIndex;
   }

   Page getPageFromColumnIndex(int columnIndex) {
      return this.getColumn(columnIndex).page;
   }

   public Page getPage(boolean isBlank, int index, int relativeTo) {
      if (relativeTo == 0) {
         return this.getPage(isBlank, index);
      } else if (relativeTo == 1) {
         int effIndex = this.startPageOfCurrentElementList + index;
         effIndex += this.startPageOfPageSequence - 1;
         return this.getPage(isBlank, effIndex);
      } else {
         throw new IllegalArgumentException("Illegal value for relativeTo: " + relativeTo);
      }
   }

   protected Page getPage(boolean isBlank, int index) {
      boolean isLastPage = this.lastPageIndex >= 0 && index == this.lastPageIndex;
      if (this.log.isTraceEnabled()) {
         this.log.trace("getPage(" + index + " " + (isBlank ? "blank" : "non-blank") + (isLastPage ? " <LAST>" : "") + ")");
      }

      int intIndex = index - this.startPageOfPageSequence;
      if (this.log.isTraceEnabled()) {
         if (isBlank) {
            this.log.trace("blank page requested: " + index);
         }

         if (isLastPage) {
            this.log.trace("last page requested: " + index);
         }
      }

      if (intIndex > this.cachedPages.size()) {
         throw new UnsupportedOperationException("Cannot handle holes in page cache");
      } else {
         if (intIndex == this.cachedPages.size()) {
            if (this.log.isTraceEnabled()) {
               this.log.trace("Caching " + index);
            }

            this.cacheNextPage(index, isBlank, isLastPage, this.spanAllForCurrentElementList);
         }

         Page page = (Page)this.cachedPages.get(intIndex);
         boolean replace = false;
         if (page.getPageViewport().isBlank() != isBlank) {
            this.log.debug("blank condition doesn't match. Replacing PageViewport.");
            replace = true;
         }

         if (page.getPageViewport().getCurrentSpan().getColumnCount() == 1 && !this.spanAllForCurrentElementList) {
            RegionBody rb = (RegionBody)page.getSimplePageMaster().getRegion(58);
            int colCount = rb.getColumnCount();
            if (colCount > 1) {
               this.log.debug("Span doesn't match. Replacing PageViewport.");
               replace = true;
            }
         }

         if (isLastPage && this.indexOfCachedLastPage != intIndex || !isLastPage && this.indexOfCachedLastPage >= 0) {
            this.log.debug("last page condition doesn't match. Replacing PageViewport.");
            replace = true;
            this.indexOfCachedLastPage = isLastPage ? intIndex : -1;
         }

         if (replace) {
            this.discardCacheStartingWith(intIndex);
            PageViewport oldPageVP = page.getPageViewport();
            page = this.cacheNextPage(index, isBlank, isLastPage, this.spanAllForCurrentElementList);
            PageViewport newPageVP = page.getPageViewport();
            newPageVP.replace(oldPageVP);
            this.areaTreeHandler.getIDTracker().replacePageViewPort(oldPageVP, newPageVP);
         }

         return page;
      }
   }

   protected void discardCacheStartingWith(int index) {
      while(index < this.cachedPages.size()) {
         this.cachedPages.remove(this.cachedPages.size() - 1);
         if (!this.pageSeq.goToPreviousSimplePageMaster()) {
            this.log.warn("goToPreviousSimplePageMaster() on the first page called!");
         }
      }

   }

   private Page cacheNextPage(int index, boolean isBlank, boolean isLastPage, boolean spanAll) {
      String pageNumberString = this.pageSeq.makeFormattedPageNumber(index);
      boolean isFirstPage = this.startPageOfPageSequence == index;
      SimplePageMaster spm = this.pageSeq.getNextSimplePageMaster(index, isFirstPage, isLastPage, isBlank);
      boolean isPagePositionOnly = this.pageSeq.hasPagePositionOnly() && !this.skipPagePositionOnly;
      if (isPagePositionOnly) {
         spm = this.pageSeq.getNextSimplePageMaster(index, isFirstPage, true, isBlank);
      }

      Page page = new Page(spm, index, pageNumberString, isBlank, spanAll, isPagePositionOnly);
      page.getPageViewport().setKey(this.areaTreeHandler.generatePageViewportKey());
      page.getPageViewport().setForeignAttributes(spm.getForeignAttributes());
      page.getPageViewport().setWritingModeTraits(this.pageSeq);
      this.cachedPages.add(page);
      if (isLastPage) {
         this.pageSeq.getRoot().setLastSeq(this.pageSeq);
      } else if (!isFirstPage) {
         this.pageSeq.getRoot().setLastSeq((PageSequence)null);
      }

      return page;
   }

   public int getIndexOfCachedLastPage() {
      return this.indexOfCachedLastPage;
   }

   public int getLastPageIndex() {
      return this.lastPageIndex;
   }

   public int getLastPageIPD() {
      int index = this.cachedPages.size();
      boolean isFirstPage = this.startPageOfPageSequence == index;
      SimplePageMaster spm = this.pageSeq.getLastSimplePageMaster(index, isFirstPage, false);
      Page page = new Page(spm, index, "", false, false, false);
      return this.pageSeq.getRoot().getLastSeq() != null && this.pageSeq.getRoot().getLastSeq() != this.pageSeq ? -1 : page.getPageViewport().getBodyRegion().getColumnIPD();
   }

   public int getCurrentIPD() {
      Page page = this.getPageFromColumnIndex(this.startColumnOfCurrentElementList);
      return page.getPageViewport().getBodyRegion().getColumnIPD();
   }

   public int getNextIPD() {
      Page page = this.getPageFromColumnIndex(this.startColumnOfCurrentElementList + 1);
      return page.getPageViewport().getBodyRegion().getColumnIPD();
   }

   public int getCurrentColumnCount() {
      Page page = this.getPageFromColumnIndex(this.startColumnOfCurrentElementList);
      return page.getPageViewport().getCurrentSpan().getColumnCount();
   }

   boolean isOnFirstPage(int partIndex) {
      Column column = this.getColumn(partIndex);
      return this.startPageOfCurrentElementList + column.pageIndex == this.startPageOfPageSequence;
   }

   private static class Column {
      final Page page;
      final int pageIndex;
      final int colIndex;
      final int columnCount;

      Column(Page page, int pageIndex, int colIndex, int columnCount) {
         this.page = page;
         this.pageIndex = pageIndex;
         this.colIndex = colIndex;
         this.columnCount = columnCount;
      }
   }
}
