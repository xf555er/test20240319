package org.apache.fop.layoutmgr.list;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.SpacedBorderedPaddedBlockLayoutManager;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

public class ListBlockLayoutManager extends SpacedBorderedPaddedBlockLayoutManager {
   private static Log log = LogFactory.getLog(ListBlockLayoutManager.class);
   private Block curBlockArea;

   public ListBlockLayoutManager(ListBlock node) {
      super(node);
   }

   protected CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
      return this.getListBlockFO().getCommonBorderPaddingBackground();
   }

   protected ListBlock getListBlockFO() {
      return (ListBlock)this.fobj;
   }

   public void initialize() {
      this.foSpaceBefore = (new SpaceVal(this.getListBlockFO().getCommonMarginBlock().spaceBefore, this)).getSpace();
      this.foSpaceAfter = (new SpaceVal(this.getListBlockFO().getCommonMarginBlock().spaceAfter, this)).getSpace();
      this.startIndent = this.getListBlockFO().getCommonMarginBlock().startIndent.getValue(this);
      this.endIndent = this.getListBlockFO().getCommonMarginBlock().endIndent.getValue(this);
   }

   private void resetSpaces() {
      this.discardBorderBefore = false;
      this.discardBorderAfter = false;
      this.discardPaddingBefore = false;
      this.discardPaddingAfter = false;
      this.effSpaceBefore = null;
      this.effSpaceAfter = null;
   }

   public List getNextKnuthElements(LayoutContext context, int alignment, Stack lmStack, Position restartPosition, LayoutManager restartAtLM) {
      this.resetSpaces();
      List returnList = super.getNextKnuthElements(context, alignment, lmStack, restartPosition, restartAtLM);
      int widowRowLimit = this.getListBlockFO().getWidowContentLimit().getValue();
      if (widowRowLimit != 0) {
         ElementListUtils.removeLegalBreaks(returnList, widowRowLimit);
      }

      int orphanRowLimit = this.getListBlockFO().getOrphanContentLimit().getValue();
      if (orphanRowLimit != 0) {
         ElementListUtils.removeLegalBreaksFromEnd(returnList, orphanRowLimit);
      }

      return returnList;
   }

   public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
      this.getParentArea((Area)null);
      if (layoutContext.getSpaceBefore() > 0) {
         this.addBlockSpacing(0.0, MinOptMax.getInstance(layoutContext.getSpaceBefore()));
      }

      this.addId();
      LayoutContext lc = LayoutContext.offspringOf(layoutContext);
      LayoutManager firstLM = null;
      LayoutManager lastLM = null;
      Position firstPos = null;
      Position lastPos = null;
      LinkedList positionList = new LinkedList();

      while(parentIter.hasNext()) {
         Position pos = parentIter.next();
         if (pos.getIndex() >= 0) {
            if (firstPos == null) {
               firstPos = pos;
            }

            lastPos = pos;
         }

         if (pos instanceof NonLeafPosition && pos.getPosition() != null && pos.getPosition().getLM() != this) {
            positionList.add(pos.getPosition());
            lastLM = pos.getPosition().getLM();
            if (firstLM == null) {
               firstLM = lastLM;
            }
         }
      }

      this.registerMarkers(true, this.isFirst(firstPos), this.isLast(lastPos));
      PositionIterator childPosIter = new PositionIterator(positionList.listIterator());

      LayoutManager childLM;
      while((childLM = childPosIter.getNextChildLM()) != null) {
         lc.setSpaceAdjust(layoutContext.getSpaceAdjust());
         lc.setFlags(4, childLM == firstLM);
         lc.setFlags(8, childLM == lastLM);
         lc.setStackLimitBP(layoutContext.getStackLimitBP());
         childLM.addAreas(childPosIter, lc);
      }

      this.registerMarkers(false, this.isFirst(firstPos), this.isLast(lastPos));
      TraitSetter.addBackground(this.curBlockArea, this.getListBlockFO().getCommonBorderPaddingBackground(), this);
      TraitSetter.addSpaceBeforeAfter(this.curBlockArea, layoutContext.getSpaceAdjust(), this.effSpaceBefore, this.effSpaceAfter);
      this.flush();
      this.curBlockArea = null;
      this.resetSpaces();
      this.checkEndOfLayout(lastPos);
   }

   public Area getParentArea(Area childArea) {
      if (this.curBlockArea == null) {
         this.curBlockArea = new Block();
         this.curBlockArea.setChangeBarList(this.getChangeBarList());
         this.parentLayoutManager.getParentArea(this.curBlockArea);
         TraitSetter.setProducerID(this.curBlockArea, this.getListBlockFO().getId());
         TraitSetter.addBorders(this.curBlockArea, this.getListBlockFO().getCommonBorderPaddingBackground(), this.discardBorderBefore, this.discardBorderAfter, false, false, this);
         TraitSetter.addPadding(this.curBlockArea, this.getListBlockFO().getCommonBorderPaddingBackground(), this.discardPaddingBefore, this.discardPaddingAfter, false, false, this);
         TraitSetter.addMargins(this.curBlockArea, this.getListBlockFO().getCommonBorderPaddingBackground(), this.getListBlockFO().getCommonMarginBlock(), this);
         TraitSetter.addBreaks(this.curBlockArea, this.getListBlockFO().getBreakBefore(), this.getListBlockFO().getBreakAfter());
         int contentIPD = this.referenceIPD - this.getIPIndents();
         this.curBlockArea.setIPD(contentIPD);
         this.curBlockArea.setBidiLevel(this.getListBlockFO().getBidiLevel());
         this.setCurrentArea(this.curBlockArea);
      }

      return this.curBlockArea;
   }

   public void addChildArea(Area childArea) {
      if (this.curBlockArea != null) {
         this.curBlockArea.addBlock((Block)childArea);
      }

   }

   public KeepProperty getKeepTogetherProperty() {
      return this.getListBlockFO().getKeepTogether();
   }

   public KeepProperty getKeepWithPreviousProperty() {
      return this.getListBlockFO().getKeepWithPrevious();
   }

   public KeepProperty getKeepWithNextProperty() {
      return this.getListBlockFO().getKeepWithNext();
   }

   public boolean isRestartable() {
      return true;
   }
}
