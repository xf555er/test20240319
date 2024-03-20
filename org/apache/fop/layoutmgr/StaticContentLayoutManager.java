package org.apache.fop.layoutmgr;

import java.util.List;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.RegionReference;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.SideRegion;
import org.apache.fop.fo.pagination.StaticContent;

public class StaticContentLayoutManager extends BlockStackingLayoutManager {
   private RegionReference targetRegion;
   private Block targetBlock;
   private SideRegion regionFO;
   private int contentAreaIPD;
   private int contentAreaBPD = -1;

   public StaticContentLayoutManager(PageSequenceLayoutManager pslm, StaticContent node, SideRegion reg) {
      super(node);
      this.setParent(pslm);
      this.regionFO = reg;
      this.targetRegion = this.getCurrentPV().getRegionReference(this.regionFO.getNameId());
   }

   public StaticContentLayoutManager(PageSequenceLayoutManager pslm, StaticContent node, Block block) {
      super(node);
      this.setParent(pslm);
      this.targetBlock = block;
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      throw new IllegalStateException();
   }

   public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
      AreaAdditionUtil.addAreas(this, parentIter, layoutContext);
      this.flush();
      this.targetRegion = null;
   }

   public void addChildArea(Area childArea) {
      if (this.getStaticContentFO().getFlowName().equals("xsl-footnote-separator")) {
         this.targetBlock.addBlock((Block)childArea);
      } else {
         this.targetRegion.addBlock((Block)childArea);
      }

   }

   public Area getParentArea(Area childArea) {
      return (Area)(this.getStaticContentFO().getFlowName().equals("xsl-footnote-separator") ? this.targetBlock : this.targetRegion);
   }

   public void doLayout() {
      int targetIPD = false;
      int targetBPD = false;
      int targetAlign = true;
      boolean autoHeight = false;
      int targetIPD;
      int targetBPD;
      int targetAlign;
      if (this.getStaticContentFO().getFlowName().equals("xsl-footnote-separator")) {
         targetIPD = this.targetBlock.getIPD();
         targetBPD = this.targetBlock.getBPD();
         if (targetBPD == 0) {
            autoHeight = true;
         }

         targetAlign = 13;
      } else {
         targetIPD = this.targetRegion.getIPD();
         targetBPD = this.targetRegion.getBPD();
         targetAlign = this.regionFO.getDisplayAlign();
      }

      this.setContentAreaIPD(targetIPD);
      this.setContentAreaBPD(targetBPD);
      StaticContentBreaker breaker = new StaticContentBreaker(this, targetIPD, targetAlign);
      breaker.doLayout(targetBPD, autoHeight);
      if (breaker.isOverflow() && !autoHeight) {
         String page = this.getPSLM().getCurrentPage().getPageViewport().getPageNumberString();
         BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(this.getStaticContentFO().getUserAgent().getEventBroadcaster());
         boolean canRecover = this.regionFO.getOverflow() != 42;
         boolean needClip = this.regionFO.getOverflow() == 57 || this.regionFO.getOverflow() == 42;
         eventProducer.staticRegionOverflow(this, this.regionFO.getName(), page, breaker.getOverflowAmount(), needClip, canRecover, this.getStaticContentFO().getLocator());
      }

   }

   protected StaticContent getStaticContentFO() {
      return (StaticContent)this.fobj;
   }

   public int getContentAreaIPD() {
      return this.contentAreaIPD;
   }

   protected void setContentAreaIPD(int contentAreaIPD) {
      this.contentAreaIPD = contentAreaIPD;
   }

   public int getContentAreaBPD() {
      return this.contentAreaBPD;
   }

   private void setContentAreaBPD(int contentAreaBPD) {
      this.contentAreaBPD = contentAreaBPD;
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

   private class StaticContentBreaker extends LocalBreaker {
      public StaticContentBreaker(StaticContentLayoutManager lm, int ipd, int displayAlign) {
         super(lm, ipd, displayAlign);
      }

      protected void observeElementList(List elementList) {
         String elementListID = StaticContentLayoutManager.this.getStaticContentFO().getFlowName();
         String pageSequenceID = ((PageSequence)this.lm.getParent().getFObj()).getId();
         if (pageSequenceID != null && pageSequenceID.length() > 0) {
            elementListID = elementListID + "-" + pageSequenceID;
         }

         ElementListObserver.observe(elementList, "static-content", elementListID);
      }
   }
}
