package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.area.Block;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.area.Footnote;
import org.apache.fop.area.PageViewport;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.pagination.RegionBody;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.layoutmgr.list.ListItemLayoutManager;
import org.apache.fop.traits.MinOptMax;

public class PageBreaker extends AbstractBreaker {
   private boolean firstPart = true;
   private boolean pageBreakHandled;
   private boolean needColumnBalancing;
   private PageProvider pageProvider;
   private Block separatorArea;
   private boolean spanAllActive;
   private boolean layoutRedone;
   private int previousIndex;
   private boolean handlingStartOfFloat;
   private boolean handlingEndOfFloat;
   private int floatHeight;
   private int floatYOffset;
   private List relayedFootnotesList;
   private List relayedLengthList;
   private int relayedTotalFootnotesLength;
   private int relayedInsertedFootnotesLength;
   private boolean relayedFootnotesPending;
   private boolean relayedNewFootnotes;
   private int relayedFirstNewFootnoteIndex;
   private int relayedFootnoteListIndex;
   private int relayedFootnoteElementIndex = -1;
   private MinOptMax relayedFootnoteSeparatorLength;
   private int previousFootnoteListIndex = -2;
   private int previousFootnoteElementIndex = -2;
   private int prevousColumnCount;
   private FlowLayoutManager childFLM;
   private StaticContentLayoutManager footnoteSeparatorLM;

   public PageBreaker(PageSequenceLayoutManager pslm) {
      this.pslm = pslm;
      this.pageProvider = pslm.getPageProvider();
      this.childFLM = pslm.getLayoutManagerMaker().makeFlowLayoutManager(pslm, pslm.getPageSequence().getMainFlow());
   }

   protected void updateLayoutContext(LayoutContext context) {
      int flowIPD = this.pslm.getCurrentColumnWidth();
      context.setRefIPD(flowIPD);
   }

   protected LayoutManager getTopLevelLM() {
      return this.pslm;
   }

   protected PageProvider getPageProvider() {
      return this.pslm.getPageProvider();
   }

   boolean doLayout(int flowBPD) {
      return this.doLayout(flowBPD, false);
   }

   protected PageBreakingAlgorithm.PageBreakingLayoutListener createLayoutListener() {
      return new PageBreakingAlgorithm.PageBreakingLayoutListener() {
         public void notifyOverflow(int part, int amount, FObj obj) {
            Page p = PageBreaker.this.pageProvider.getPageFromColumnIndex(part);
            RegionBody body = (RegionBody)p.getSimplePageMaster().getRegion(58);
            BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(body.getUserAgent().getEventBroadcaster());
            boolean canRecover = body.getOverflow() != 42;
            boolean needClip = body.getOverflow() == 57 || body.getOverflow() == 42;
            eventProducer.regionOverflow(this, body.getName(), p.getPageViewport().getPageNumberString(), amount, needClip, canRecover, body.getLocator());
         }
      };
   }

   protected int handleSpanChange(LayoutContext childLC, int nextSequenceStartsOn) {
      this.needColumnBalancing = false;
      if (childLC.getNextSpan() != 0) {
         nextSequenceStartsOn = childLC.getNextSpan();
         this.needColumnBalancing = childLC.getNextSpan() == 5 && childLC.getDisableColumnBalancing() == 48;
      }

      if (this.needColumnBalancing) {
         log.debug("Column balancing necessary for the next element list!!!");
      }

      return nextSequenceStartsOn;
   }

   protected int getNextBlockList(LayoutContext childLC, int nextSequenceStartsOn) {
      return this.getNextBlockList(childLC, nextSequenceStartsOn, (Position)null, (LayoutManager)null, (List)null);
   }

   protected int getNextBlockList(LayoutContext childLC, int nextSequenceStartsOn, Position positionAtIPDChange, LayoutManager restartLM, List firstElements) {
      if (!this.layoutRedone && !this.handlingFloat()) {
         if (!this.firstPart) {
            this.handleBreakTrait(nextSequenceStartsOn);
         }

         this.firstPart = false;
         this.pageBreakHandled = true;
         this.pageProvider.setStartOfNextElementList(this.pslm.getCurrentPageNum(), this.pslm.getCurrentPV().getCurrentSpan().getCurrentFlowIndex(), this.spanAllActive);
      }

      return super.getNextBlockList(childLC, nextSequenceStartsOn, positionAtIPDChange, restartLM, firstElements);
   }

   private boolean containsFootnotes(List contentList, LayoutContext context) {
      boolean containsFootnotes = false;
      if (contentList != null) {
         Iterator var4 = contentList.iterator();

         while(true) {
            ListElement element;
            do {
               do {
                  if (!var4.hasNext()) {
                     return containsFootnotes;
                  }

                  Object aContentList = var4.next();
                  element = (ListElement)aContentList;
               } while(!(element instanceof KnuthBlockBox));
            } while(!((KnuthBlockBox)element).hasAnchors());

            containsFootnotes = true;
            KnuthBlockBox box = (KnuthBlockBox)element;
            List footnotes = getFootnoteKnuthElements(this.childFLM, context, box.getFootnoteBodyLMs());
            Iterator var9 = footnotes.iterator();

            while(var9.hasNext()) {
               List footnote = (List)var9.next();
               box.addElementList(footnote);
            }
         }
      } else {
         return containsFootnotes;
      }
   }

   public static List getFootnoteKnuthElements(FlowLayoutManager flowLM, LayoutContext context, List footnoteBodyLMs) {
      List footnotes = new ArrayList();
      LayoutContext footnoteContext = LayoutContext.copyOf(context);
      footnoteContext.setStackLimitBP(context.getStackLimitBP());
      footnoteContext.setRefIPD(flowLM.getPSLM().getCurrentPV().getRegionReference(58).getIPD());
      Iterator var5 = footnoteBodyLMs.iterator();

      while(var5.hasNext()) {
         FootnoteBodyLayoutManager fblm = (FootnoteBodyLayoutManager)var5.next();
         fblm.setParent(flowLM);
         fblm.initialize();
         List footnote = fblm.getNextKnuthElements(footnoteContext, 135);
         SpaceResolver.resolveElementList(footnote);
         footnotes.add(footnote);
      }

      return footnotes;
   }

   private void handleFootnoteSeparator() {
      StaticContent footnoteSeparator = this.pslm.getPageSequence().getStaticContent("xsl-footnote-separator");
      if (footnoteSeparator != null) {
         this.separatorArea = new Block();
         this.separatorArea.setIPD(this.pslm.getCurrentPV().getRegionReference(58).getIPD());
         this.footnoteSeparatorLM = this.pslm.getLayoutManagerMaker().makeStaticContentLayoutManager(this.pslm, footnoteSeparator, this.separatorArea);
         this.footnoteSeparatorLM.doLayout();
         this.footnoteSeparatorLength = MinOptMax.getInstance(this.separatorArea.getBPD());
      }

   }

   protected List getNextKnuthElements(LayoutContext context, int alignment) {
      List contentList;
      for(contentList = null; !this.childFLM.isFinished() && contentList == null; contentList = this.childFLM.getNextKnuthElements(context, alignment)) {
      }

      if (this.containsFootnotes(contentList, context)) {
         this.handleFootnoteSeparator();
      }

      return contentList;
   }

   protected List getNextKnuthElements(LayoutContext context, int alignment, Position positionAtIPDChange, LayoutManager restartAtLM) {
      List contentList = null;

      do {
         contentList = this.childFLM.getNextKnuthElements(context, alignment, positionAtIPDChange, restartAtLM);
      } while(!this.childFLM.isFinished() && contentList == null);

      if (this.containsFootnotes(contentList, context)) {
         this.handleFootnoteSeparator();
      }

      return contentList;
   }

   protected int getCurrentDisplayAlign() {
      return this.pslm.getCurrentPage().getSimplePageMaster().getRegion(58).getDisplayAlign();
   }

   protected boolean hasMoreContent() {
      return !this.childFLM.isFinished();
   }

   protected void addAreas(PositionIterator posIter, LayoutContext context) {
      if (this.footnoteSeparatorLM != null) {
         StaticContent footnoteSeparator = this.pslm.getPageSequence().getStaticContent("xsl-footnote-separator");
         this.separatorArea = new Block();
         this.separatorArea.setIPD(this.pslm.getCurrentPV().getRegionReference(58).getIPD());
         this.footnoteSeparatorLM = this.pslm.getLayoutManagerMaker().makeStaticContentLayoutManager(this.pslm, footnoteSeparator, this.separatorArea);
         this.footnoteSeparatorLM.doLayout();
      }

      this.childFLM.addAreas(posIter, context);
   }

   protected void doPhase3(PageBreakingAlgorithm alg, int partCount, AbstractBreaker.BlockSequence originalList, AbstractBreaker.BlockSequence effectiveList) {
      if (this.needColumnBalancing) {
         this.redoLayout(alg, partCount, originalList, effectiveList);
      } else if (this.shouldRedoLayout(partCount)) {
         this.redoLayout(alg, partCount, originalList, effectiveList);
      } else {
         this.addAreas(alg, partCount, originalList, effectiveList);
      }
   }

   protected void prepareToRedoLayout(PageBreakingAlgorithm alg, int partCount, AbstractBreaker.BlockSequence originalList, AbstractBreaker.BlockSequence effectiveList) {
      int newStartPos = 0;
      int restartPoint = this.pageProvider.getStartingPartIndexForLastPage(partCount);
      if (restartPoint > 0 && !this.layoutRedone) {
         this.addAreas(alg, restartPoint, originalList, effectiveList);
         AbstractBreaker.PageBreakPosition pbp = (AbstractBreaker.PageBreakPosition)alg.getPageBreaks().get(restartPoint - 1);
         newStartPos = alg.par.getFirstBoxIndex(pbp.getLeafPos() + 1);
         if (newStartPos > 0) {
            this.handleBreakTrait(104);
         }
      }

      this.pageBreakHandled = true;
      int currentPageNum = this.pslm.getCurrentPageNum();
      int currentColumn = this.pslm.getCurrentPV().getCurrentSpan().getCurrentFlowIndex();
      this.pageProvider.setStartOfNextElementList(currentPageNum, currentColumn, this.spanAllActive);
      effectiveList.ignoreAtStart = newStartPos;
      if (!this.layoutRedone) {
         this.setLastPageIndex(currentPageNum);
         this.pslm.setCurrentPage(this.pageProvider.getPage(false, currentPageNum));
         this.previousIndex = this.pageProvider.getIndexOfCachedLastPage();
      } else {
         this.setLastPageIndex(currentPageNum + 1);
         this.pageProvider.discardCacheStartingWith(this.previousIndex);
         this.pslm.setCurrentPage(this.pageProvider.getPage(false, currentPageNum));
      }

      this.layoutRedone = true;
   }

   private void redoLayout(PageBreakingAlgorithm alg, int partCount, AbstractBreaker.BlockSequence originalList, AbstractBreaker.BlockSequence effectiveList) {
      int newStartPos = 0;
      int restartPoint = this.pageProvider.getStartingPartIndexForLastPage(partCount);
      if (restartPoint > 0) {
         this.addAreas(alg, restartPoint, originalList, effectiveList);
         AbstractBreaker.PageBreakPosition pbp = (AbstractBreaker.PageBreakPosition)alg.getPageBreaks().get(restartPoint - 1);
         newStartPos = alg.par.getFirstBoxIndex(pbp.getLeafPos() + 1);
         if (newStartPos > 0) {
            this.handleBreakTrait(104);
         }
      }

      log.debug("Restarting at " + restartPoint + ", new start position: " + newStartPos);
      this.pageBreakHandled = true;
      int currentPageNum = this.pslm.getCurrentPageNum();
      this.pageProvider.setStartOfNextElementList(currentPageNum, this.pslm.getCurrentPV().getCurrentSpan().getCurrentFlowIndex(), this.spanAllActive);
      effectiveList.ignoreAtStart = newStartPos;
      Object algRestart;
      if (this.needColumnBalancing) {
         log.debug("Column balancing now!!!");
         log.debug("===================================================");
         algRestart = new BalancingColumnBreakingAlgorithm(this.getTopLevelLM(), this.getPageProvider(), this.createLayoutListener(), this.alignment, 135, this.footnoteSeparatorLength, this.isPartOverflowRecoveryActivated(), this.pslm.getCurrentPV().getBodyRegion().getColumnCount());
         log.debug("===================================================");
      } else {
         BodyRegion currentBody = this.pageProvider.getPage(false, currentPageNum).getPageViewport().getBodyRegion();
         this.setLastPageIndex(currentPageNum);
         BodyRegion lastBody = this.pageProvider.getPage(false, currentPageNum).getPageViewport().getBodyRegion();
         lastBody.getMainReference().setSpans(currentBody.getMainReference().getSpans());
         log.debug("Last page handling now!!!");
         log.debug("===================================================");
         algRestart = new PageBreakingAlgorithm(this.getTopLevelLM(), this.getPageProvider(), this.createLayoutListener(), alg.getAlignment(), alg.getAlignmentLast(), this.footnoteSeparatorLength, this.isPartOverflowRecoveryActivated(), false, false);
         log.debug("===================================================");
      }

      int optimalPageCount = ((PageBreakingAlgorithm)algRestart).findBreakingPoints(effectiveList, newStartPos, 1.0, true, 0);
      log.debug("restart: optimalPageCount= " + optimalPageCount + " pageBreaks.size()= " + ((PageBreakingAlgorithm)algRestart).getPageBreaks().size());
      boolean fitsOnePage = optimalPageCount <= this.pslm.getCurrentPV().getBodyRegion().getMainReference().getCurrentSpan().getColumnCount();
      if (this.needColumnBalancing) {
         if (!fitsOnePage) {
            log.warn("Breaking algorithm produced more columns than are available.");
         }
      } else {
         boolean ipdChange = ((PageBreakingAlgorithm)algRestart).getIPDdifference() != 0;
         if (!fitsOnePage || ipdChange) {
            this.addAreas(alg, restartPoint, partCount - restartPoint, originalList, effectiveList);
            if (!ipdChange) {
               this.setLastPageIndex(currentPageNum + 1);
               this.pslm.setCurrentPage(this.pslm.makeNewPage(true));
            }

            return;
         }

         this.pslm.setCurrentPage(this.pageProvider.getPage(false, currentPageNum));
      }

      this.addAreas((PageBreakingAlgorithm)algRestart, optimalPageCount, originalList, effectiveList);
   }

   private void setLastPageIndex(int currentPageNum) {
      int lastPageIndex = this.pslm.getForcedLastPageNum(currentPageNum);
      this.pageProvider.setLastPageIndex(lastPageIndex);
   }

   protected void startPart(AbstractBreaker.BlockSequence list, int breakClass, boolean emptyContent) {
      log.debug("startPart() breakClass=" + getBreakClassName(breakClass));
      if (this.pslm.getCurrentPage() == null) {
         throw new IllegalStateException("curPage must not be null");
      } else {
         if (!this.pageBreakHandled) {
            if (!this.firstPart) {
               this.handleBreakTrait(breakClass, emptyContent);
            }

            this.pageProvider.setStartOfNextElementList(this.pslm.getCurrentPageNum(), this.pslm.getCurrentPV().getCurrentSpan().getCurrentFlowIndex(), this.spanAllActive);
         }

         this.pageBreakHandled = false;
         this.firstPart = false;
      }
   }

   protected void handleEmptyContent() {
      this.pslm.getCurrentPV().getPage().fakeNonEmpty();
   }

   protected void finishPart(PageBreakingAlgorithm alg, AbstractBreaker.PageBreakPosition pbp) {
      if (!this.pslm.getTableHeaderFootnotes().isEmpty() || pbp.footnoteFirstListIndex < pbp.footnoteLastListIndex || pbp.footnoteFirstElementIndex <= pbp.footnoteLastElementIndex || !this.pslm.getTableFooterFootnotes().isEmpty()) {
         Iterator var3 = this.pslm.getTableHeaderFootnotes().iterator();

         List footnote;
         while(var3.hasNext()) {
            footnote = (List)var3.next();
            this.addFootnoteAreas(footnote);
         }

         for(int i = pbp.footnoteFirstListIndex; i <= pbp.footnoteLastListIndex; ++i) {
            footnote = alg.getFootnoteList(i);
            int firstIndex = i == pbp.footnoteFirstListIndex ? pbp.footnoteFirstElementIndex : 0;
            int lastIndex = i == pbp.footnoteLastListIndex ? pbp.footnoteLastElementIndex : footnote.size() - 1;
            this.addFootnoteAreas(footnote, firstIndex, lastIndex + 1);
         }

         var3 = this.pslm.getTableFooterFootnotes().iterator();

         while(var3.hasNext()) {
            footnote = (List)var3.next();
            this.addFootnoteAreas(footnote);
         }

         Footnote parentArea = this.pslm.getCurrentPV().getBodyRegion().getFootnote();
         int topOffset = this.pslm.getCurrentPV().getBodyRegion().getBPD() - parentArea.getBPD();
         if (this.separatorArea != null) {
            topOffset -= this.separatorArea.getBPD();
         }

         parentArea.setTop(topOffset);
         parentArea.setSeparator(this.separatorArea);
      }

      this.pslm.getCurrentPV().getCurrentSpan().notifyFlowsFinished();
      this.pslm.clearTableHeadingFootnotes();
   }

   private void addFootnoteAreas(List footnote) {
      this.addFootnoteAreas(footnote, 0, footnote.size());
   }

   private void addFootnoteAreas(List footnote, int startIndex, int endIndex) {
      SpaceResolver.performConditionalsNotification(footnote, startIndex, endIndex - 1, -1);
      LayoutContext childLC = LayoutContext.newInstance();
      AreaAdditionUtil.addAreas((AbstractLayoutManager)null, new KnuthPossPosIter(footnote, startIndex, endIndex), childLC);
   }

   protected FlowLayoutManager getCurrentChildLM() {
      return this.childFLM;
   }

   protected void observeElementList(List elementList) {
      ElementListObserver.observe(elementList, "breaker", this.pslm.getFObj().getId());
   }

   private void handleBreakTrait(int breakVal) {
      this.handleBreakTrait(breakVal, false);
   }

   private void handleBreakTrait(int breakVal, boolean emptyContent) {
      Page curPage = this.pslm.getCurrentPage();
      switch (breakVal) {
         case -1:
         case 9:
         case 28:
         case 104:
            PageViewport pv = curPage.getPageViewport();
            boolean forceNewPageWithSpan = false;
            RegionBody rb = (RegionBody)curPage.getSimplePageMaster().getRegion(58);
            forceNewPageWithSpan = rb.getColumnCount() > 1 && pv.getCurrentSpan().getColumnCount() == 1;
            if (forceNewPageWithSpan) {
               log.trace("Forcing new page with span");
               curPage = this.pslm.makeNewPage(false);
               curPage.getPageViewport().createSpan(true);
            } else if (breakVal == 104) {
               this.handleBreakBeforeFollowingPage(breakVal);
            } else if (pv.getCurrentSpan().hasMoreFlows()) {
               log.trace("Moving to next flow");
               pv.getCurrentSpan().moveToNextFlow();
            } else {
               log.trace("Making new page");
               this.pslm.makeNewPage(false, emptyContent);
            }

            return;
         case 5:
            curPage.getPageViewport().createSpan(true);
            this.spanAllActive = true;
            return;
         case 95:
            curPage.getPageViewport().createSpan(false);
            this.spanAllActive = false;
            return;
         default:
            this.handleBreakBeforeFollowingPage(breakVal);
      }
   }

   private void handleBreakBeforeFollowingPage(int breakVal) {
      log.debug("handling break-before after page " + this.pslm.getCurrentPageNum() + " breakVal=" + getBreakClassName(breakVal));
      if (this.needBlankPageBeforeNew(breakVal)) {
         log.trace("Inserting blank page");
         this.pslm.makeNewPage(true);
      }

      if (this.needNewPage(breakVal)) {
         log.trace("Making new page");
         this.pslm.makeNewPage(false);
      }

   }

   private boolean needBlankPageBeforeNew(int breakVal) {
      if (breakVal != 104 && !this.pslm.getCurrentPage().getPageViewport().getPage().isEmpty()) {
         if (this.pslm.getCurrentPageNum() % 2 == 0) {
            return breakVal == 44;
         } else {
            return breakVal == 100;
         }
      } else {
         return false;
      }
   }

   private boolean needNewPage(int breakVal) {
      if (this.pslm.getCurrentPage().getPageViewport().getPage().isEmpty()) {
         if (breakVal == 104) {
            return false;
         } else if (this.pslm.getCurrentPageNum() % 2 == 0) {
            return breakVal == 100;
         } else {
            return breakVal == 44;
         }
      } else {
         return true;
      }
   }

   protected boolean shouldRedoLayout() {
      return this.shouldRedoLayout(-1);
   }

   protected boolean shouldRedoLayout(int partCount) {
      boolean lastPageMasterDefined = this.pslm.getPageSequence().hasPagePositionLast();
      if (!lastPageMasterDefined && partCount != -1) {
         lastPageMasterDefined = this.pslm.getPageSequence().hasPagePositionOnly() && this.pslm.isOnFirstPage(partCount - 1);
      }

      return !this.hasMoreContent() && lastPageMasterDefined && !this.layoutRedone;
   }

   protected boolean wasLayoutRedone() {
      return this.layoutRedone;
   }

   protected boolean lastPageHasIPDChange(int optimalPageCount) {
      boolean lastPageMasterDefined = this.pslm.getPageSequence().hasPagePositionLast();
      boolean onlyPageMasterDefined = this.pslm.getPageSequence().hasPagePositionOnly();
      if (lastPageMasterDefined && !onlyPageMasterDefined) {
         int currentColumnCount = this.pageProvider.getCurrentColumnCount();
         boolean changeInColumnCount = this.prevousColumnCount > 0 && this.prevousColumnCount != currentColumnCount;
         this.prevousColumnCount = currentColumnCount;
         if ((currentColumnCount <= 1 || optimalPageCount % currentColumnCount != 0) && !changeInColumnCount) {
            int currentIPD = this.pageProvider.getCurrentIPD();
            int lastPageIPD = this.pageProvider.getLastPageIPD();
            return lastPageIPD != -1 && currentIPD != lastPageIPD;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   protected boolean handlingStartOfFloat() {
      return this.handlingStartOfFloat;
   }

   protected void handleStartOfFloat(int fHeight, int fYOffset) {
      this.handlingStartOfFloat = true;
      this.handlingEndOfFloat = false;
      this.floatHeight = fHeight;
      this.floatYOffset = fYOffset;
      this.childFLM.handleFloatOn();
   }

   protected int getFloatHeight() {
      return this.floatHeight;
   }

   protected int getFloatYOffset() {
      return this.floatYOffset;
   }

   protected boolean handlingEndOfFloat() {
      return this.handlingEndOfFloat;
   }

   protected void handleEndOfFloat(int fHeight) {
      this.handlingEndOfFloat = true;
      this.handlingStartOfFloat = false;
      this.floatHeight = fHeight;
      this.childFLM.handleFloatOff();
   }

   protected boolean handlingFloat() {
      return this.handlingStartOfFloat || this.handlingEndOfFloat;
   }

   public int getOffsetDueToFloat() {
      this.handlingEndOfFloat = false;
      return this.floatHeight + this.floatYOffset;
   }

   protected int handleFloatLayout(PageBreakingAlgorithm alg, int optimalPageCount, AbstractBreaker.BlockSequence blockList, LayoutContext childLC) {
      this.pageBreakHandled = true;
      List firstElements = Collections.EMPTY_LIST;
      BreakingAlgorithm.KnuthNode floatNode = alg.getBestFloatEdgeNode();
      int floatPosition = floatNode.position;
      KnuthElement floatElem = alg.getElement(floatPosition);
      Position positionAtBreak = floatElem.getPosition();
      if (!(positionAtBreak instanceof SpaceResolver.SpaceHandlingBreakPosition)) {
         throw new UnsupportedOperationException("Don't know how to restart at position" + positionAtBreak);
      } else {
         positionAtBreak = positionAtBreak.getPosition();
         this.addAreas(alg, optimalPageCount, blockList, blockList);
         this.blockLists.clear();
         this.blockListIndex = -1;
         LayoutManager restartAtLM = null;
         if (positionAtBreak != null && positionAtBreak.getIndex() == -1) {
            if (positionAtBreak instanceof ListItemLayoutManager.ListItemPosition) {
               restartAtLM = positionAtBreak.getLM();
            } else {
               Iterator iter = blockList.listIterator(floatPosition + 1);

               Position position;
               do {
                  do {
                     KnuthElement nextElement = (KnuthElement)iter.next();
                     position = nextElement.getPosition();
                  } while(position == null);
               } while(position instanceof SpaceResolver.SpaceHandlingPosition || position instanceof SpaceResolver.SpaceHandlingBreakPosition && position.getPosition().getIndex() == -1);

               for(LayoutManager surroundingLM = positionAtBreak.getLM(); position.getLM() != surroundingLM; position = position.getPosition()) {
               }

               restartAtLM = position.getPosition().getLM();
            }
         }

         int nextSequenceStartsOn = this.getNextBlockList(childLC, 28, positionAtBreak, restartAtLM, firstElements);
         return nextSequenceStartsOn;
      }
   }

   protected void addAreasForFloats(PageBreakingAlgorithm alg, int startPart, int partCount, AbstractBreaker.BlockSequence originalList, AbstractBreaker.BlockSequence effectiveList, LayoutContext childLC, int lastBreak, int startElementIndex, int endElementIndex) {
      AbstractBreaker.FloatPosition pbp = alg.getFloatPosition();
      int lastBreakClass;
      if (startElementIndex == 0) {
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
      log.debug("PLM> part: " + (startPart + partCount + 1) + ", start at pos " + startElementIndex + ", break at pos " + endElementIndex + ", break class = " + getBreakClassName(lastBreakClass));
      this.startPart(effectiveList, lastBreakClass, false);
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
         if (alg.handlingStartOfFloat()) {
            for(int k = startElementIndex; k < endElementIndex + 1; ++k) {
               ListElement le = effectiveList.getElement(k);
               if (le instanceof KnuthBlockBox) {
                  KnuthBlockBox kbb = (KnuthBlockBox)le;
                  Iterator var17 = kbb.getFloatContentLMs().iterator();

                  while(var17.hasNext()) {
                     FloatContentLayoutManager fclm = (FloatContentLayoutManager)var17.next();
                     fclm.processAreas(childLC);
                     int floatHeight = fclm.getFloatHeight();
                     int floatYOffset = fclm.getFloatYOffset();
                     PageSequenceLayoutManager pslm = (PageSequenceLayoutManager)this.getTopLevelLM();
                     pslm.recordStartOfFloat(floatHeight, floatYOffset);
                  }
               }
            }
         }

         PageSequenceLayoutManager pslm;
         if (alg.handlingEndOfFloat()) {
            pslm = (PageSequenceLayoutManager)this.getTopLevelLM();
            pslm.setEndIntrusionAdjustment(0);
            pslm.setStartIntrusionAdjustment(0);
            int effectiveFloatHeight = alg.getFloatHeight();
            pslm.recordEndOfFloat(effectiveFloatHeight);
         }

         if (alg.handlingFloat()) {
            pslm = (PageSequenceLayoutManager)this.getTopLevelLM();
            alg.relayFootnotes(pslm);
         }
      } else {
         this.handleEmptyContent();
      }

      this.pageBreakHandled = true;
   }

   public void holdFootnotes(List fl, List ll, int tfl, int ifl, boolean fp, boolean nf, int fnfi, int fli, int fei, MinOptMax fsl, int pfli, int pfei) {
      this.relayedFootnotesList = fl;
      this.relayedLengthList = ll;
      this.relayedTotalFootnotesLength = tfl;
      this.relayedInsertedFootnotesLength = ifl;
      this.relayedFootnotesPending = fp;
      this.relayedNewFootnotes = nf;
      this.relayedFirstNewFootnoteIndex = fnfi;
      this.relayedFootnoteListIndex = fli;
      this.relayedFootnoteElementIndex = fei;
      this.relayedFootnoteSeparatorLength = fsl;
      this.previousFootnoteListIndex = pfli;
      this.previousFootnoteElementIndex = pfei;
   }

   public void retrieveFootones(PageBreakingAlgorithm alg) {
      if (this.relayedFootnotesList != null && this.relayedFootnotesList.size() > 0) {
         alg.loadFootnotes(this.relayedFootnotesList, this.relayedLengthList, this.relayedTotalFootnotesLength, this.relayedInsertedFootnotesLength, this.relayedFootnotesPending, this.relayedNewFootnotes, this.relayedFirstNewFootnoteIndex, this.relayedFootnoteListIndex, this.relayedFootnoteElementIndex, this.relayedFootnoteSeparatorLength, this.previousFootnoteListIndex, this.previousFootnoteElementIndex);
         this.relayedFootnotesList = null;
         this.relayedLengthList = null;
         this.relayedTotalFootnotesLength = 0;
         this.relayedInsertedFootnotesLength = 0;
         this.relayedFootnotesPending = false;
         this.relayedNewFootnotes = false;
         this.relayedFirstNewFootnoteIndex = 0;
         this.relayedFootnoteListIndex = 0;
         this.relayedFootnoteElementIndex = -1;
         this.relayedFootnoteSeparatorLength = null;
      }

   }
}
