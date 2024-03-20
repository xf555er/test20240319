package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.LineArea;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.layoutmgr.inline.InlineLevelLayoutManager;
import org.apache.fop.layoutmgr.inline.LineLayoutManager;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

public class BlockLayoutManager extends SpacedBorderedPaddedBlockLayoutManager implements BreakOpportunity {
   private static Log log = LogFactory.getLog(BlockLayoutManager.class);
   private Block curBlockArea;
   protected ListIterator proxyLMiter = new ProxyLMiter();
   private int lead = 12000;
   private Length lineHeight;
   private int follow = 2000;

   public BlockLayoutManager(org.apache.fop.fo.flow.Block inBlock) {
      super(inBlock);
   }

   public void initialize() {
      super.initialize();
      org.apache.fop.fo.flow.Block fo = this.getBlockFO();
      FontInfo fi = fo.getFOEventHandler().getFontInfo();
      FontTriplet[] fontkeys = fo.getCommonFont().getFontState(fi);
      Font initFont = fi.getFontInstance(fontkeys[0], this.getBlockFO().getCommonFont().fontSize.getValue(this));
      this.lead = initFont.getAscender();
      this.follow = -initFont.getDescender();
      this.lineHeight = fo.getLineHeight().getOptimum(this).getLength();
      this.startIndent = fo.getCommonMarginBlock().startIndent.getValue(this);
      this.endIndent = fo.getCommonMarginBlock().endIndent.getValue(this);
      this.foSpaceBefore = (new SpaceVal(fo.getCommonMarginBlock().spaceBefore, this)).getSpace();
      this.foSpaceAfter = (new SpaceVal(fo.getCommonMarginBlock().spaceAfter, this)).getSpace();
      this.adjustedSpaceBefore = fo.getCommonMarginBlock().spaceBefore.getSpace().getOptimum(this).getLength().getValue(this);
      this.adjustedSpaceAfter = fo.getCommonMarginBlock().spaceAfter.getSpace().getOptimum(this).getLength().getValue(this);
   }

   protected CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
      return this.getBlockFO().getCommonBorderPaddingBackground();
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      return this.getNextKnuthElements(context, alignment, (Stack)null, (Position)null, (LayoutManager)null);
   }

   public List getNextKnuthElements(LayoutContext context, int alignment, Stack lmStack, Position restartPosition, LayoutManager restartAtLM) {
      this.resetSpaces();
      return super.getNextKnuthElements(context, alignment, lmStack, restartPosition, restartAtLM);
   }

   protected List getNextChildElements(LayoutManager childLM, LayoutContext context, LayoutContext childLC, int alignment, Stack lmStack, Position restartPosition, LayoutManager restartAtLM) {
      childLC.copyPendingMarksFrom(context);
      if (childLM instanceof LineLayoutManager) {
         childLC.setRefIPD(this.getContentAreaIPD());
      }

      if (childLM == this.childLMs.get(0)) {
         childLC.setFlags(2);
      }

      if (lmStack == null) {
         return childLM.getNextKnuthElements(childLC, alignment);
      } else if (childLM instanceof LineLayoutManager) {
         assert restartPosition instanceof LeafPosition;

         return ((LineLayoutManager)childLM).getNextKnuthElements(childLC, alignment, (LeafPosition)restartPosition);
      } else {
         return childLM.getNextKnuthElements(childLC, alignment, lmStack, restartPosition, restartAtLM);
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

   public boolean createNextChildLMs(int pos) {
      while(true) {
         if (this.proxyLMiter.hasNext()) {
            LayoutManager lm = (LayoutManager)this.proxyLMiter.next();
            if (lm instanceof InlineLevelLayoutManager) {
               LineLayoutManager lineLM = this.createLineManager(lm);
               this.addChildLM(lineLM);
            } else {
               this.addChildLM(lm);
            }

            if (pos >= this.childLMs.size()) {
               continue;
            }

            return true;
         }

         return false;
      }
   }

   private LineLayoutManager createLineManager(LayoutManager firstlm) {
      LineLayoutManager llm = new LineLayoutManager(this.getBlockFO(), this.lineHeight, this.lead, this.follow);
      List inlines = new ArrayList();
      inlines.add(firstlm);

      while(this.proxyLMiter.hasNext()) {
         LayoutManager lm = (LayoutManager)this.proxyLMiter.next();
         if (!(lm instanceof InlineLevelLayoutManager)) {
            this.proxyLMiter.previous();
            break;
         }

         inlines.add(lm);
      }

      llm.addChildLMs(inlines);
      return llm;
   }

   public KeepProperty getKeepTogetherProperty() {
      return this.getBlockFO().getKeepTogether();
   }

   public KeepProperty getKeepWithPreviousProperty() {
      return this.getBlockFO().getKeepWithPrevious();
   }

   public KeepProperty getKeepWithNextProperty() {
      return this.getBlockFO().getKeepWithNext();
   }

   public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
      this.getParentArea((Area)null);
      if (layoutContext.getSpaceBefore() > 0) {
         this.addBlockSpacing(0.0, MinOptMax.getInstance(layoutContext.getSpaceBefore()));
      }

      LayoutManager lastLM = null;
      LayoutContext lc = LayoutContext.offspringOf(layoutContext);
      lc.setSpaceAdjust(layoutContext.getSpaceAdjust());
      if (layoutContext.getSpaceAfter() > 0) {
         lc.setSpaceAfter(layoutContext.getSpaceAfter());
      }

      LinkedList positionList = new LinkedList();
      Position firstPos = null;
      Position lastPos = null;

      while(true) {
         Position innerPosition;
         do {
            do {
               if (!parentIter.hasNext()) {
                  this.addId();
                  this.registerMarkers(true, this.isFirst(firstPos), this.isLast(lastPos));
                  PositionIterator childPosIter = new PositionIterator(positionList.listIterator());

                  LayoutManager childLM;
                  while((childLM = childPosIter.getNextChildLM()) != null) {
                     lc.setFlags(8, layoutContext.isLastArea() && childLM == lastLM);
                     lc.setStackLimitBP(layoutContext.getStackLimitBP());
                     childLM.addAreas(childPosIter, lc);
                  }

                  this.registerMarkers(false, this.isFirst(firstPos), this.isLast(lastPos));
                  TraitSetter.addSpaceBeforeAfter(this.curBlockArea, layoutContext.getSpaceAdjust(), this.effSpaceBefore, this.effSpaceAfter);
                  TraitSetter.setVisibility(this.curBlockArea, this.getBlockFO().getVisibility());
                  this.flush();
                  this.curBlockArea = null;
                  this.resetSpaces();
                  this.checkEndOfLayout(lastPos);
                  return;
               }

               Position pos = parentIter.next();
               if (pos.getIndex() >= 0) {
                  if (firstPos == null) {
                     firstPos = pos;
                  }

                  lastPos = pos;
               }

               innerPosition = pos;
               if (pos instanceof NonLeafPosition) {
                  innerPosition = pos.getPosition();
               }
            } while(innerPosition == null);
         } while(innerPosition.getLM() == this && !(innerPosition instanceof BlockStackingLayoutManager.MappingPosition));

         positionList.add(innerPosition);
         lastLM = innerPosition.getLM();
      }
   }

   public Area getParentArea(Area childArea) {
      if (this.curBlockArea == null) {
         this.curBlockArea = new Block();
         this.curBlockArea.setChangeBarList(this.getChangeBarList());
         this.curBlockArea.setIPD(super.getContentAreaIPD());
         this.curBlockArea.setBidiLevel(this.getBlockFO().getBidiLevelRecursive());
         TraitSetter.addBreaks(this.curBlockArea, this.getBlockFO().getBreakBefore(), this.getBlockFO().getBreakAfter());
         this.parentLayoutManager.getParentArea(this.curBlockArea);
         TraitSetter.setProducerID(this.curBlockArea, this.getBlockFO().getId());
         TraitSetter.addBorders(this.curBlockArea, this.getBlockFO().getCommonBorderPaddingBackground(), this.discardBorderBefore, this.discardBorderAfter, false, false, this);
         TraitSetter.addPadding(this.curBlockArea, this.getBlockFO().getCommonBorderPaddingBackground(), this.discardPaddingBefore, this.discardPaddingAfter, false, false, this);
         TraitSetter.addMargins(this.curBlockArea, this.getBlockFO().getCommonBorderPaddingBackground(), this.startIndent, this.endIndent, this);
         TraitSetter.setLayer(this.curBlockArea, this.getBlockFO().getLayer());
         this.curBlockArea.setLocale(this.getBlockFO().getCommonHyphenation().getLocale());
         this.curBlockArea.setLocation(FONode.getLocatorString(this.getBlockFO().getLocator()));
         this.setCurrentArea(this.curBlockArea);
      }

      return this.curBlockArea;
   }

   public void addChildArea(Area childArea) {
      if (this.curBlockArea != null) {
         if (childArea instanceof LineArea) {
            this.curBlockArea.addLineArea((LineArea)childArea);
         } else {
            this.curBlockArea.addBlock((Block)childArea);
         }
      }

   }

   protected void flush() {
      if (this.curBlockArea != null) {
         TraitSetter.addBackground(this.curBlockArea, this.getBlockFO().getCommonBorderPaddingBackground(), this);
         super.flush();
      }

   }

   protected org.apache.fop.fo.flow.Block getBlockFO() {
      return (org.apache.fop.fo.flow.Block)this.fobj;
   }

   public int getContentAreaIPD() {
      return this.curBlockArea != null ? this.curBlockArea.getIPD() : super.getContentAreaIPD();
   }

   public int getContentAreaBPD() {
      return this.curBlockArea != null ? this.curBlockArea.getBPD() : -1;
   }

   public boolean getGeneratesBlockArea() {
      return true;
   }

   public boolean isRestartable() {
      return true;
   }

   protected class ProxyLMiter extends LMiter {
      public ProxyLMiter() {
         super(BlockLayoutManager.this);
         this.listLMs = new ArrayList(10);
      }

      public boolean hasNext() {
         return this.curPos < this.listLMs.size() || this.createNextChildLMs(this.curPos);
      }

      protected boolean createNextChildLMs(int pos) {
         List newLMs = BlockLayoutManager.this.createChildLMs(pos + 1 - this.listLMs.size());
         if (newLMs != null) {
            this.listLMs.addAll(newLMs);
         }

         return pos < this.listLMs.size();
      }
   }
}
