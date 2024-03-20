package org.apache.fop.layoutmgr.inline;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.fop.area.Area;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.FilledArea;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.MinOptMax;

public class LeaderLayoutManager extends LeafNodeLayoutManager {
   private Leader fobj;
   private Font font;
   private List contentList;
   private ContentLayoutManager clm;
   private int contentAreaIPD;

   public LeaderLayoutManager(Leader node) {
      super(node);
      this.fobj = node;
   }

   public void initialize() {
      FontInfo fi = this.fobj.getFOEventHandler().getFontInfo();
      FontTriplet[] fontkeys = this.fobj.getCommonFont().getFontState(fi);
      this.font = fi.getFontInstance(fontkeys[0], this.fobj.getCommonFont().fontSize.getValue(this));
      this.setCommonBorderPaddingBackground(this.fobj.getCommonBorderPaddingBackground());
   }

   public InlineArea get(LayoutContext context) {
      return this.getLeaderInlineArea(context);
   }

   protected MinOptMax getAllocationIPD(int refIPD) {
      return this.getLeaderAllocIPD(refIPD);
   }

   private MinOptMax getLeaderAllocIPD(int ipd) {
      int borderPaddingWidth = 0;
      if (this.commonBorderPaddingBackground != null) {
         borderPaddingWidth = this.commonBorderPaddingBackground.getIPPaddingAndBorder(false, this);
      }

      this.setContentAreaIPD(ipd - borderPaddingWidth);
      int opt = this.fobj.getLeaderLength().getOptimum(this).getLength().getValue(this) - borderPaddingWidth;
      int min = this.fobj.getLeaderLength().getMinimum(this).getLength().getValue(this) - borderPaddingWidth;
      int max = this.fobj.getLeaderLength().getMaximum(this).getLength().getValue(this) - borderPaddingWidth;
      return MinOptMax.getInstance(min, opt, max);
   }

   private InlineArea getLeaderInlineArea(LayoutContext context) {
      InlineArea leaderArea = null;
      int level = this.fobj.getBidiLevel();
      if (this.fobj.getLeaderPattern() == 123) {
         if (this.fobj.getRuleStyle() != 95) {
            org.apache.fop.area.inline.Leader leader = new org.apache.fop.area.inline.Leader();
            leader.setRuleStyle(this.fobj.getRuleStyle());
            leader.setRuleThickness(this.fobj.getRuleThickness().getValue(this));
            leaderArea = leader;
         } else {
            leaderArea = new Space();
            if (level >= 0) {
               ((InlineArea)leaderArea).setBidiLevel(level);
            }
         }

         ((InlineArea)leaderArea).setBPD(this.fobj.getRuleThickness().getValue(this));
         ((InlineArea)leaderArea).addTrait(Trait.COLOR, this.fobj.getColor());
         if (level >= 0) {
            ((InlineArea)leaderArea).setBidiLevel(level);
         }
      } else if (this.fobj.getLeaderPattern() == 134) {
         leaderArea = new Space();
         ((InlineArea)leaderArea).setBPD(this.fobj.getRuleThickness().getValue(this));
         if (level >= 0) {
            ((InlineArea)leaderArea).setBidiLevel(level);
         }
      } else {
         Space spacer;
         if (this.fobj.getLeaderPattern() == 35) {
            TextArea t = new TextArea();
            char dot = '.';
            int width = this.font.getCharWidth(dot);
            int[] levels = level < 0 ? null : new int[]{level};
            t.addWord("" + dot, width, (int[])null, levels, (int[][])null, 0);
            t.setIPD(width);
            t.setBPD(width);
            t.setBaselineOffset(width);
            TraitSetter.addFontTraits(t, this.font);
            t.addTrait(Trait.COLOR, this.fobj.getColor());
            spacer = null;
            int widthLeaderPattern = this.fobj.getLeaderPatternWidth().getValue(this);
            if (widthLeaderPattern > width) {
               spacer = new Space();
               spacer.setIPD(widthLeaderPattern - width);
               if (level >= 0) {
                  spacer.setBidiLevel(level);
               }

               width = widthLeaderPattern;
            }

            FilledArea fa = new FilledArea();
            fa.setUnitWidth(width);
            fa.addChildArea(t);
            if (spacer != null) {
               fa.addChildArea(spacer);
            }

            fa.setBPD(t.getBPD());
            leaderArea = fa;
         } else if (this.fobj.getLeaderPattern() == 158) {
            if (this.fobj.getChildNodes() == null) {
               InlineLevelEventProducer eventProducer = InlineLevelEventProducer.Provider.get(this.getFObj().getUserAgent().getEventBroadcaster());
               eventProducer.leaderWithoutContent(this, this.getFObj().getLocator());
               return null;
            }

            this.fobjIter = null;
            FilledArea fa = new FilledArea();
            this.clm = new ContentLayoutManager(fa, this);
            this.addChildLM(this.clm);
            InlineLayoutManager lm = new InlineLayoutManager(this.fobj);
            this.clm.addChildLM(lm);
            lm.initialize();
            LayoutContext childContext = LayoutContext.newInstance();
            childContext.setAlignmentContext(context.getAlignmentContext());
            this.contentList = this.clm.getNextKnuthElements(childContext, 0);
            int width = this.clm.getStackingSize();
            if (width != 0) {
               spacer = null;
               if (this.fobj.getLeaderPatternWidth().getValue(this) > width) {
                  spacer = new Space();
                  spacer.setIPD(this.fobj.getLeaderPatternWidth().getValue(this) - width);
                  if (level >= 0) {
                     spacer.setBidiLevel(level);
                  }

                  width = this.fobj.getLeaderPatternWidth().getValue(this);
               }

               fa.setUnitWidth(width);
               if (spacer != null) {
                  fa.addChildArea(spacer);
               }

               leaderArea = fa;
            } else {
               leaderArea = new Space();
               ((InlineArea)leaderArea).setBPD(this.fobj.getRuleThickness().getValue(this));
               ((InlineArea)leaderArea).setBidiLevel(this.fobj.getBidiLevelRecursive());
            }
         }
      }

      assert leaderArea != null;

      ((InlineArea)leaderArea).setChangeBarList(this.getChangeBarList());
      TraitSetter.setProducerID((Area)leaderArea, this.fobj.getId());
      return (InlineArea)leaderArea;
   }

   public void addAreas(PositionIterator posIter, LayoutContext context) {
      if (this.fobj.getLeaderPattern() != 158) {
         super.addAreas(posIter, context);
      } else {
         this.addId();
         this.widthAdjustArea(this.curArea, context);
         if (this.commonBorderPaddingBackground != null) {
            TraitSetter.setBorderPaddingTraits(this.curArea, this.commonBorderPaddingBackground, false, false, this);
            TraitSetter.addBackground(this.curArea, this.commonBorderPaddingBackground, this);
         }

         KnuthPossPosIter contentIter = new KnuthPossPosIter(this.contentList, 0, this.contentList.size());
         this.clm.addAreas(contentIter, context);
         this.parentLayoutManager.addChildArea(this.curArea);

         while(posIter.hasNext()) {
            posIter.next();
         }
      }

   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      this.curArea = this.get(context);
      KnuthSequence seq = new InlineKnuthSequence();
      if (this.curArea == null) {
         this.setFinished(true);
         return null;
      } else {
         this.alignmentContext = new AlignmentContext(this.curArea.getBPD(), this.fobj.getAlignmentAdjust(), this.fobj.getAlignmentBaseline(), this.fobj.getBaselineShift(), this.fobj.getDominantBaseline(), context.getAlignmentContext());
         MinOptMax ipd = this.getAllocationIPD(context.getRefIPD());
         if (this.fobj.getLeaderPattern() == 158 && this.curArea instanceof FilledArea) {
            int unitWidth = ((FilledArea)this.curArea).getUnitWidth();
            if (ipd.getOpt() < unitWidth && unitWidth <= ipd.getMax()) {
               ipd = MinOptMax.getInstance(ipd.getMin(), unitWidth, ipd.getMax());
            }
         }

         this.areaInfo = new LeafNodeLayoutManager.AreaInfo((short)0, ipd, false, context.getAlignmentContext());
         this.curArea.setAdjustingInfo(ipd.getStretch(), ipd.getShrink(), 0);
         this.addKnuthElementsForBorderPaddingStart(seq);
         seq.add(new KnuthInlineBox(0, this.alignmentContext, new LeafPosition(this, -1), true));
         seq.add(new KnuthPenalty(0, 1000, false, new LeafPosition(this, -1), true));
         if (alignment != 70 && alignment != 0) {
            seq.add(new KnuthGlue(this.areaInfo.ipdArea.getOpt(), 0, 0, new LeafPosition(this, 0), false));
         } else {
            seq.add(new KnuthGlue(this.areaInfo.ipdArea, new LeafPosition(this, 0), false));
         }

         seq.add(new KnuthInlineBox(0, this.alignmentContext, new LeafPosition(this, -1), true));
         this.addKnuthElementsForBorderPaddingEnd(seq);
         this.setFinished(true);
         return Collections.singletonList(seq);
      }
   }

   public void hyphenate(Position pos, HyphContext hc) {
      super.hyphenate(pos, hc);
   }

   public boolean applyChanges(List oldList) {
      this.setFinished(false);
      return false;
   }

   public List getChangedKnuthElements(List oldList, int alignment) {
      if (this.isFinished()) {
         return null;
      } else {
         List returnList = new LinkedList();
         this.addKnuthElementsForBorderPaddingStart(returnList);
         returnList.add(new KnuthInlineBox(0, this.areaInfo.alignmentContext, new LeafPosition(this, -1), true));
         returnList.add(new KnuthPenalty(0, 1000, false, new LeafPosition(this, -1), true));
         if (alignment != 70 && alignment != 0) {
            returnList.add(new KnuthGlue(this.areaInfo.ipdArea.getOpt(), 0, 0, new LeafPosition(this, 0), false));
         } else {
            returnList.add(new KnuthGlue(this.areaInfo.ipdArea, new LeafPosition(this, 0), false));
         }

         returnList.add(new KnuthInlineBox(0, this.areaInfo.alignmentContext, new LeafPosition(this, -1), true));
         this.addKnuthElementsForBorderPaddingEnd(returnList);
         this.setFinished(true);
         return returnList;
      }
   }

   public int getBaseLength(int lengthBase, FObj fobj) {
      return this.getParent().getBaseLength(lengthBase, this.getParent().getFObj());
   }

   public int getContentAreaIPD() {
      return this.contentAreaIPD;
   }

   private void setContentAreaIPD(int contentAreaIPD) {
      this.contentAreaIPD = contentAreaIPD;
   }

   public void reset() {
      this.childLMs.clear();
      super.reset();
   }
}
