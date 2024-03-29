package org.apache.fop.layoutmgr.inline;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Area;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.layoutmgr.AbstractLayoutManager;
import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;

public abstract class LeafNodeLayoutManager extends AbstractLayoutManager implements InlineLevelLayoutManager {
   protected static final Log log = LogFactory.getLog(LeafNodeLayoutManager.class);
   protected InlineArea curArea;
   protected CommonBorderPaddingBackground commonBorderPaddingBackground;
   protected AlignmentContext alignmentContext;
   protected boolean somethingChanged;
   protected AreaInfo areaInfo;

   public LeafNodeLayoutManager(FObj node) {
      super(node);
   }

   public LeafNodeLayoutManager() {
   }

   public InlineArea get(LayoutContext context) {
      return this.curArea;
   }

   public boolean resolved() {
      return false;
   }

   public void setCurrentArea(InlineArea ia) {
      this.curArea = ia;
   }

   public void addChildArea(Area childArea) {
      assert false;

   }

   public Area getParentArea(Area childArea) {
      assert false;

      return null;
   }

   protected void setCommonBorderPaddingBackground(CommonBorderPaddingBackground commonBorderPaddingBackground) {
      this.commonBorderPaddingBackground = commonBorderPaddingBackground;
   }

   protected MinOptMax getAllocationIPD(int refIPD) {
      return MinOptMax.getInstance(this.curArea.getIPD());
   }

   public void addAreas(PositionIterator posIter, LayoutContext context) {
      this.addId();
      InlineArea area = this.getEffectiveArea(context);
      if (area.getAllocIPD() > 0 || area.getAllocBPD() > 0) {
         this.offsetArea(area, context);
         this.widthAdjustArea(area, context);
         if (this.commonBorderPaddingBackground != null) {
            TraitSetter.setBorderPaddingTraits(area, this.commonBorderPaddingBackground, false, false, this);
            TraitSetter.addBackground(area, this.commonBorderPaddingBackground, this);
         }

         this.parentLayoutManager.addChildArea(area);
      }

      while(posIter.hasNext()) {
         posIter.next();
      }

   }

   protected InlineArea getEffectiveArea(LayoutContext layoutContext) {
      return this.curArea;
   }

   protected void offsetArea(InlineArea area, LayoutContext context) {
      area.setBlockProgressionOffset(this.alignmentContext.getOffset());
   }

   protected AlignmentContext makeAlignmentContext(LayoutContext context) {
      return context.getAlignmentContext();
   }

   protected void widthAdjustArea(InlineArea area, LayoutContext context) {
      double dAdjust = context.getIPDAdjust();
      int adjustment = 0;
      if (dAdjust < 0.0) {
         adjustment += (int)(dAdjust * (double)this.areaInfo.ipdArea.getShrink());
      } else if (dAdjust > 0.0) {
         adjustment += (int)(dAdjust * (double)this.areaInfo.ipdArea.getStretch());
      }

      area.setIPD(this.areaInfo.ipdArea.getOpt() + adjustment);
      area.setAdjustment(adjustment);
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      this.curArea = this.get(context);
      this.alignmentContext = this.makeAlignmentContext(context);
      MinOptMax ipd = this.getAllocationIPD(context.getRefIPD());
      this.areaInfo = new AreaInfo((short)0, ipd, false, this.alignmentContext);
      KnuthSequence seq = new InlineKnuthSequence();
      this.addKnuthElementsForBorderPaddingStart(seq);
      seq.add(new KnuthInlineBox(this.areaInfo.ipdArea.getOpt(), this.alignmentContext, this.notifyPos(new LeafPosition(this, 0)), false));
      this.addKnuthElementsForBorderPaddingEnd(seq);
      this.setFinished(true);
      return Collections.singletonList(seq);
   }

   public List addALetterSpaceTo(List oldList) {
      return oldList;
   }

   public List addALetterSpaceTo(List oldList, int depth) {
      return this.addALetterSpaceTo(oldList);
   }

   public String getWordChars(Position pos) {
      return "";
   }

   public void hyphenate(Position pos, HyphContext hyphContext) {
   }

   public boolean applyChanges(List oldList) {
      this.setFinished(false);
      return false;
   }

   public boolean applyChanges(List oldList, int depth) {
      return this.applyChanges(oldList);
   }

   public List getChangedKnuthElements(List oldList, int alignment, int depth) {
      return this.getChangedKnuthElements(oldList, alignment);
   }

   public List getChangedKnuthElements(List oldList, int alignment) {
      if (this.isFinished()) {
         return null;
      } else {
         LinkedList returnList = new LinkedList();
         this.addKnuthElementsForBorderPaddingStart(returnList);
         returnList.add(new KnuthInlineBox(this.areaInfo.ipdArea.getOpt(), this.areaInfo.alignmentContext, this.notifyPos(new LeafPosition(this, 0)), true));
         this.addKnuthElementsForBorderPaddingEnd(returnList);
         this.setFinished(true);
         return returnList;
      }
   }

   protected void addKnuthElementsForBorderPaddingStart(List returnList) {
      if (this.commonBorderPaddingBackground != null) {
         int ipStart = this.commonBorderPaddingBackground.getBorderStartWidth(false) + this.commonBorderPaddingBackground.getPaddingStart(false, this);
         if (ipStart > 0) {
            returnList.add(new KnuthPenalty(0, 1000, false, new LeafPosition(this, -1), true));
            returnList.add(new KnuthGlue(ipStart, 0, 0, new LeafPosition(this, -1), true));
         }
      }

   }

   protected void addKnuthElementsForBorderPaddingEnd(List returnList) {
      if (this.commonBorderPaddingBackground != null) {
         int ipEnd = this.commonBorderPaddingBackground.getBorderEndWidth(false) + this.commonBorderPaddingBackground.getPaddingEnd(false, this);
         if (ipEnd > 0) {
            returnList.add(new KnuthPenalty(0, 1000, false, new LeafPosition(this, -1), true));
            returnList.add(new KnuthGlue(ipEnd, 0, 0, new LeafPosition(this, -1), true));
         }
      }

   }

   protected class AreaInfo {
      protected short letterSpaces;
      protected MinOptMax ipdArea;
      protected boolean isHyphenated;
      protected AlignmentContext alignmentContext;

      public AreaInfo(short letterSpaces, MinOptMax ipd, boolean isHyphenated, AlignmentContext alignmentContext) {
         this.letterSpaces = letterSpaces;
         this.ipdArea = ipd;
         this.isHyphenated = isHyphenated;
         this.alignmentContext = alignmentContext;
      }
   }
}
