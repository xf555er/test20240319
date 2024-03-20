package org.apache.fop.layoutmgr.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.ListItemBody;
import org.apache.fop.fo.flow.ListItemLabel;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.layoutmgr.BlockLayoutManager;
import org.apache.fop.layoutmgr.BreakElement;
import org.apache.fop.layoutmgr.BreakOpportunity;
import org.apache.fop.layoutmgr.BreakOpportunityHelper;
import org.apache.fop.layoutmgr.ElementListObserver;
import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.FloatContentLayoutManager;
import org.apache.fop.layoutmgr.FootenoteUtil;
import org.apache.fop.layoutmgr.Keep;
import org.apache.fop.layoutmgr.KnuthBlockBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.PageProvider;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.SpaceResolver;
import org.apache.fop.layoutmgr.SpacedBorderedPaddedBlockLayoutManager;
import org.apache.fop.layoutmgr.TraitSetter;
import org.apache.fop.traits.SpaceVal;
import org.apache.fop.util.BreakUtil;

public class ListItemLayoutManager extends SpacedBorderedPaddedBlockLayoutManager implements BreakOpportunity {
   private static Log log = LogFactory.getLog(ListItemLayoutManager.class);
   private ListItemContentLayoutManager label;
   private ListItemContentLayoutManager body;
   private Block curBlockArea;
   private List labelList;
   private List bodyList;
   private Keep keepWithNextPendingOnLabel;
   private Keep keepWithNextPendingOnBody;

   public ListItemLayoutManager(ListItem node) {
      super(node);
      this.setLabel(node.getLabel());
      this.setBody(node.getBody());
   }

   protected CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
      return this.getListItemFO().getCommonBorderPaddingBackground();
   }

   protected ListItem getListItemFO() {
      return (ListItem)this.fobj;
   }

   public void setLabel(ListItemLabel node) {
      this.label = new ListItemContentLayoutManager(node);
      this.label.setParent(this);
   }

   public void setBody(ListItemBody node) {
      this.body = new ListItemContentLayoutManager(node);
      this.body.setParent(this);
   }

   public void initialize() {
      this.foSpaceBefore = (new SpaceVal(this.getListItemFO().getCommonMarginBlock().spaceBefore, this)).getSpace();
      this.foSpaceAfter = (new SpaceVal(this.getListItemFO().getCommonMarginBlock().spaceAfter, this)).getSpace();
      this.startIndent = this.getListItemFO().getCommonMarginBlock().startIndent.getValue(this);
      this.endIndent = this.getListItemFO().getCommonMarginBlock().endIndent.getValue(this);
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
      this.referenceIPD = context.getRefIPD();
      List returnList = new LinkedList();
      if (!this.breakBeforeServed(context, returnList)) {
         return returnList;
      } else {
         this.addFirstVisibleMarks(returnList, context, alignment);
         LayoutContext childLC = this.makeChildLayoutContext(context);
         childLC.setFlags(2);
         this.label.initialize();
         boolean labelDone = false;
         Stack labelLMStack = null;
         Position labelRestartPosition = null;
         LayoutManager labelRestartLM = null;
         if (restartPosition != null && restartPosition instanceof ListItemPosition) {
            ListItemPosition lip = (ListItemPosition)restartPosition;
            if (lip.labelLastIndex <= lip.labelFirstIndex) {
               labelDone = true;
            } else {
               labelRestartPosition = lip.getOriginalLabelPosition();
               labelRestartLM = labelRestartPosition.getLM();
               LayoutManager lm = labelRestartLM;
               labelLMStack = new Stack();

               while(lm != this) {
                  labelLMStack.push(lm);
                  lm = lm.getParent();
                  if (lm instanceof ListItemContentLayoutManager) {
                     lm = lm.getParent();
                  }
               }
            }
         }

         this.labelList = (List)(!labelDone ? this.label.getNextKnuthElements(childLC, alignment, labelLMStack, labelRestartPosition, labelRestartLM) : new LinkedList());
         SpaceResolver.resolveElementList(this.labelList);
         ElementListObserver.observe(this.labelList, "list-item-label", this.label.getPartFO().getId());
         context.updateKeepWithPreviousPending(childLC.getKeepWithPreviousPending());
         this.keepWithNextPendingOnLabel = childLC.getKeepWithNextPending();
         childLC = this.makeChildLayoutContext(context);
         childLC.setFlags(2);
         this.body.initialize();
         boolean bodyDone = false;
         Stack bodyLMStack = null;
         Position bodyRestartPosition = null;
         LayoutManager bodyRestartLM = null;
         if (restartPosition != null && restartPosition instanceof ListItemPosition) {
            ListItemPosition lip = (ListItemPosition)restartPosition;
            if (lip.bodyLastIndex <= lip.bodyFirstIndex) {
               bodyDone = true;
            } else {
               bodyRestartPosition = lip.getOriginalBodyPosition();
               bodyRestartLM = bodyRestartPosition.getLM();
               LayoutManager lm = bodyRestartLM;
               bodyLMStack = new Stack();

               while(lm != this) {
                  bodyLMStack.push(lm);
                  lm = lm.getParent();
                  if (lm instanceof ListItemContentLayoutManager) {
                     lm = lm.getParent();
                  }
               }
            }
         }

         this.bodyList = (List)(!bodyDone ? this.body.getNextKnuthElements(childLC, alignment, bodyLMStack, bodyRestartPosition, bodyRestartLM) : new LinkedList());
         SpaceResolver.resolveElementList(this.bodyList);
         ElementListObserver.observe(this.bodyList, "list-item-body", this.body.getPartFO().getId());
         context.updateKeepWithPreviousPending(childLC.getKeepWithPreviousPending());
         this.keepWithNextPendingOnBody = childLC.getKeepWithNextPending();
         List returnedList = new LinkedList();
         List floats;
         Keep keep;
         KnuthBlockBox kbb;
         if (!this.labelList.isEmpty() && this.labelList.get(0) instanceof KnuthBlockBox) {
            kbb = (KnuthBlockBox)this.labelList.get(0);
            if (kbb.getWidth() == 0 && kbb.hasFloatAnchors()) {
               floats = kbb.getFloatContentLMs();
               returnedList.add(new KnuthBlockBox(0, Collections.emptyList(), (Position)null, false, floats));
               keep = this.getKeepTogether();
               returnedList.add(new BreakElement(new LeafPosition(this, 0), keep.getPenalty(), keep.getContext(), context));
               this.labelList.remove(0);
               this.labelList.remove(0);
            }
         }

         if (!this.bodyList.isEmpty() && this.bodyList.get(0) instanceof KnuthBlockBox) {
            kbb = (KnuthBlockBox)this.bodyList.get(0);
            if (kbb.getWidth() == 0 && kbb.hasFloatAnchors()) {
               floats = kbb.getFloatContentLMs();
               returnedList.add(new KnuthBlockBox(0, Collections.emptyList(), (Position)null, false, floats));
               keep = this.getKeepTogether();
               returnedList.add(new BreakElement(new LeafPosition(this, 0), keep.getPenalty(), keep.getContext(), context));
               this.bodyList.remove(0);
               this.bodyList.remove(0);
            }
         }

         returnedList.addAll(this.getCombinedKnuthElementsForListItem(this.labelList, this.bodyList, context));
         this.wrapPositionElements(returnedList, returnList, true);
         this.addLastVisibleMarks(returnList, context, alignment);
         this.addKnuthElementsForBreakAfter(returnList, context);
         context.updateKeepWithNextPending(this.keepWithNextPendingOnLabel);
         context.updateKeepWithNextPending(this.keepWithNextPendingOnBody);
         context.updateKeepWithNextPending(this.getKeepWithNext());
         context.updateKeepWithPreviousPending(this.getKeepWithPrevious());
         this.setFinished(true);
         this.resetSpaces();
         return returnList;
      }
   }

   protected void addFirstVisibleMarks(List elements, LayoutContext context, int alignment) {
      this.addKnuthElementsForSpaceBefore(elements, alignment);
      this.addKnuthElementsForBorderPaddingBefore(elements, !this.firstVisibleMarkServed);
      this.firstVisibleMarkServed = true;
      this.addPendingMarks(context);
   }

   private List getCombinedKnuthElementsForListItem(List labelElements, List bodyElements, LayoutContext context) {
      List[] elementLists = new List[]{new ArrayList(labelElements), new ArrayList(bodyElements)};
      int[] fullHeights = new int[]{ElementListUtils.calcContentLength(elementLists[0]), ElementListUtils.calcContentLength(elementLists[1])};
      int[] partialHeights = new int[]{0, 0};
      int[] start = new int[]{-1, -1};
      int[] end = new int[]{-1, -1};
      int totalHeight = Math.max(fullHeights[0], fullHeights[1]);
      int addedBoxHeight = 0;
      Keep keepWithNextActive = Keep.KEEP_AUTO;
      LinkedList returnList = new LinkedList();

      int step;
      while((step = this.getNextStep(elementLists, start, end, partialHeights)) > 0) {
         if (end[0] + 1 == elementLists[0].size()) {
            keepWithNextActive = keepWithNextActive.compare(this.keepWithNextPendingOnLabel);
         }

         if (end[1] + 1 == elementLists[1].size()) {
            keepWithNextActive = keepWithNextActive.compare(this.keepWithNextPendingOnBody);
         }

         int penaltyHeight = step + this.getMaxRemainingHeight(fullHeights, partialHeights) - totalHeight;
         int additionalPenaltyHeight = 0;
         int stepPenalty = 0;
         int breakClass = 9;
         KnuthElement endEl = elementLists[0].size() > 0 ? (KnuthElement)elementLists[0].get(end[0]) : null;
         Position originalLabelPosition = endEl != null && endEl.getPosition() != null ? endEl.getPosition().getPosition() : null;
         if (endEl instanceof KnuthPenalty) {
            additionalPenaltyHeight = endEl.getWidth();
            stepPenalty = endEl.getPenalty() == -1000 ? -1000 : Math.max(stepPenalty, endEl.getPenalty());
            breakClass = BreakUtil.compareBreakClasses(breakClass, ((KnuthPenalty)endEl).getBreakClass());
         }

         endEl = elementLists[1].size() > 0 ? (KnuthElement)elementLists[1].get(end[1]) : null;
         Position originalBodyPosition = endEl != null && endEl.getPosition() != null ? endEl.getPosition().getPosition() : null;
         if (endEl instanceof KnuthPenalty) {
            additionalPenaltyHeight = Math.max(additionalPenaltyHeight, endEl.getWidth());
            stepPenalty = endEl.getPenalty() == -1000 ? -1000 : Math.max(stepPenalty, endEl.getPenalty());
            breakClass = BreakUtil.compareBreakClasses(breakClass, ((KnuthPenalty)endEl).getBreakClass());
         }

         int boxHeight = step - addedBoxHeight - penaltyHeight;
         penaltyHeight += additionalPenaltyHeight;
         LinkedList footnoteList = new LinkedList();

         for(int i = 0; i < elementLists.length; ++i) {
            footnoteList.addAll(FootenoteUtil.getFootnotes(elementLists[i], start[i], end[i]));
         }

         LinkedList floats = new LinkedList();

         for(int i = 0; i < elementLists.length; ++i) {
            floats.addAll(FloatContentLayoutManager.checkForFloats(elementLists[i], start[i], end[i]));
         }

         addedBoxHeight += boxHeight;
         ListItemPosition stepPosition = new ListItemPosition(this, start[0], end[0], start[1], end[1]);
         stepPosition.setOriginalLabelPosition(originalLabelPosition);
         stepPosition.setOriginalBodyPosition(originalBodyPosition);
         Keep keep;
         if (floats.isEmpty()) {
            returnList.add(new KnuthBlockBox(boxHeight, footnoteList, stepPosition, false));
         } else {
            returnList.add(new KnuthBlockBox(0, Collections.emptyList(), stepPosition, false, floats));
            keep = this.getKeepTogether();
            returnList.add(new BreakElement(stepPosition, keep.getPenalty(), keep.getContext(), context));
            returnList.add(new KnuthBlockBox(boxHeight, footnoteList, stepPosition, false));
         }

         if (originalBodyPosition != null && this.getKeepWithPrevious().isAuto() && this.shouldWeAvoidBreak(returnList, originalBodyPosition.getLM())) {
            ++stepPenalty;
         }

         if (addedBoxHeight < totalHeight) {
            keep = keepWithNextActive.compare(this.getKeepTogether());
            int p = stepPenalty;
            if (stepPenalty > -1000) {
               p = Math.max(stepPenalty, keep.getPenalty());
               breakClass = keep.getContext();
            }

            returnList.add(new BreakElement(stepPosition, penaltyHeight, p, breakClass, context));
         }
      }

      return returnList;
   }

   private boolean shouldWeAvoidBreak(List returnList, LayoutManager lm) {
      if (this.isChangingIPD(lm)) {
         if (lm instanceof BlockLayoutManager) {
            return true;
         }

         if (lm instanceof ListBlockLayoutManager) {
            int penaltyShootout = 0;
            Iterator var4 = returnList.iterator();

            while(var4.hasNext()) {
               Object o = var4.next();
               if (o instanceof BreakElement) {
                  if (((BreakElement)o).getPenaltyValue() > 0) {
                     ++penaltyShootout;
                  } else {
                     --penaltyShootout;
                  }
               }
            }

            return penaltyShootout > 0;
         }
      }

      return false;
   }

   private boolean isChangingIPD(LayoutManager lm) {
      PageProvider pageProvider = lm.getPSLM().getPageProvider();
      int currentIPD = pageProvider.getCurrentIPD();
      int nextIPD = pageProvider.getNextIPD();
      return nextIPD != currentIPD;
   }

   private int getNextStep(List[] elementLists, int[] start, int[] end, int[] partialHeights) {
      int[] backupHeights = new int[]{partialHeights[0], partialHeights[1]};
      start[0] = end[0] + 1;
      start[1] = end[1] + 1;
      int seqCount = 0;

      int step;
      for(step = 0; step < start.length; ++step) {
         while(end[step] + 1 < elementLists[step].size()) {
            int var10002 = end[step]++;
            KnuthElement el = (KnuthElement)elementLists[step].get(end[step]);
            if (el.isPenalty()) {
               if (el.getPenalty() < 1000) {
                  break;
               }
            } else if (el.isGlue()) {
               if (end[step] > 0) {
                  KnuthElement prev = (KnuthElement)elementLists[step].get(end[step] - 1);
                  if (prev.isBox()) {
                     break;
                  }
               }

               partialHeights[step] += el.getWidth();
            } else {
               partialHeights[step] += el.getWidth();
            }
         }

         if (end[step] < start[step]) {
            partialHeights[step] = backupHeights[step];
         } else {
            ++seqCount;
         }
      }

      if (seqCount == 0) {
         return 0;
      } else {
         if (backupHeights[0] == 0 && backupHeights[1] == 0) {
            step = Math.max(end[0] >= start[0] ? partialHeights[0] : Integer.MIN_VALUE, end[1] >= start[1] ? partialHeights[1] : Integer.MIN_VALUE);
         } else {
            step = Math.min(end[0] >= start[0] ? partialHeights[0] : Integer.MAX_VALUE, end[1] >= start[1] ? partialHeights[1] : Integer.MAX_VALUE);
         }

         for(int i = 0; i < partialHeights.length; ++i) {
            if (partialHeights[i] > step) {
               partialHeights[i] = backupHeights[i];
               end[i] = start[i] - 1;
            }
         }

         return step;
      }
   }

   private int getMaxRemainingHeight(int[] fullHeights, int[] partialHeights) {
      return Math.max(fullHeights[0] - partialHeights[0], fullHeights[1] - partialHeights[1]);
   }

   public List getChangedKnuthElements(List oldList, int alignment) {
      this.labelList = this.label.getChangedKnuthElements(this.labelList, alignment);
      ListIterator oldListIterator = oldList.listIterator();

      while(oldListIterator.hasNext()) {
         KnuthElement oldElement = (KnuthElement)oldListIterator.next();
         Position innerPosition = oldElement.getPosition().getPosition();
         if (innerPosition != null) {
            oldElement.setPosition(innerPosition);
         } else {
            oldElement.setPosition(new Position(this));
         }
      }

      List returnedList = this.body.getChangedKnuthElements(oldList, alignment);
      List tempList = returnedList;
      List returnedList = new LinkedList();
      Iterator var8 = tempList.iterator();

      while(var8.hasNext()) {
         Object aTempList = var8.next();
         KnuthElement tempElement = (KnuthElement)aTempList;
         tempElement.setPosition(new NonLeafPosition(this, tempElement.getPosition()));
         returnedList.add(tempElement);
      }

      return returnedList;
   }

   public boolean hasLineAreaDescendant() {
      return this.label.hasLineAreaDescendant() || this.body.hasLineAreaDescendant();
   }

   public int getBaselineOffset() {
      if (this.label.hasLineAreaDescendant()) {
         return this.label.getBaselineOffset();
      } else if (this.body.hasLineAreaDescendant()) {
         return this.body.getBaselineOffset();
      } else {
         throw this.newNoLineAreaDescendantException();
      }
   }

   public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
      this.getParentArea((Area)null);
      this.addId();
      LayoutContext lc = LayoutContext.offspringOf(layoutContext);
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

         if (pos instanceof NonLeafPosition && pos.getPosition() != null) {
            positionList.add(pos.getPosition());
         }
      }

      if (positionList.isEmpty()) {
         this.reset();
      } else {
         this.registerMarkers(true, this.isFirst(firstPos), this.isLast(lastPos));
         int labelFirstIndex = ((ListItemPosition)positionList.getFirst()).getLabelFirstIndex();
         int labelLastIndex = ((ListItemPosition)positionList.getLast()).getLabelLastIndex();
         int bodyFirstIndex = ((ListItemPosition)positionList.getFirst()).getBodyFirstIndex();
         int bodyLastIndex = ((ListItemPosition)positionList.getLast()).getBodyLastIndex();
         int previousBreak = ElementListUtils.determinePreviousBreak(this.labelList, labelFirstIndex);
         SpaceResolver.performConditionalsNotification(this.labelList, labelFirstIndex, labelLastIndex, previousBreak);
         previousBreak = ElementListUtils.determinePreviousBreak(this.bodyList, bodyFirstIndex);
         SpaceResolver.performConditionalsNotification(this.bodyList, bodyFirstIndex, bodyLastIndex, previousBreak);
         KnuthPossPosIter bodyIter;
         if (labelFirstIndex <= labelLastIndex) {
            bodyIter = new KnuthPossPosIter(this.labelList, labelFirstIndex, labelLastIndex + 1);
            lc.setFlags(4, layoutContext.isFirstArea());
            lc.setFlags(8, layoutContext.isLastArea());
            lc.setSpaceAdjust(layoutContext.getSpaceAdjust());
            lc.setStackLimitBP(layoutContext.getStackLimitBP());
            this.label.addAreas(bodyIter, lc);
         }

         if (bodyFirstIndex <= bodyLastIndex) {
            bodyIter = new KnuthPossPosIter(this.bodyList, bodyFirstIndex, bodyLastIndex + 1);
            lc.setFlags(4, layoutContext.isFirstArea());
            lc.setFlags(8, layoutContext.isLastArea());
            lc.setSpaceAdjust(layoutContext.getSpaceAdjust());
            lc.setStackLimitBP(layoutContext.getStackLimitBP());
            this.body.addAreas(bodyIter, lc);
         }

         int childCount = this.curBlockArea.getChildAreas().size();

         assert childCount >= 1 && childCount <= 2;

         int itemBPD = ((Block)this.curBlockArea.getChildAreas().get(0)).getAllocBPD();
         if (childCount == 2) {
            itemBPD = Math.max(itemBPD, ((Block)this.curBlockArea.getChildAreas().get(1)).getAllocBPD());
         }

         this.curBlockArea.setBPD(itemBPD);
         this.registerMarkers(false, this.isFirst(firstPos), this.isLast(lastPos));
         TraitSetter.addBackground(this.curBlockArea, this.getListItemFO().getCommonBorderPaddingBackground(), this);
         TraitSetter.addSpaceBeforeAfter(this.curBlockArea, layoutContext.getSpaceAdjust(), this.effSpaceBefore, this.effSpaceAfter);
         this.flush();
         this.curBlockArea = null;
         this.resetSpaces();
         this.checkEndOfLayout(lastPos);
      }
   }

   public Area getParentArea(Area childArea) {
      if (this.curBlockArea == null) {
         this.curBlockArea = new Block();
         this.curBlockArea.setChangeBarList(this.getChangeBarList());
         this.parentLayoutManager.getParentArea(this.curBlockArea);
         ListItem fo = this.getListItemFO();
         TraitSetter.setProducerID(this.curBlockArea, fo.getId());
         TraitSetter.addBorders(this.curBlockArea, fo.getCommonBorderPaddingBackground(), this.discardBorderBefore, this.discardBorderAfter, false, false, this);
         TraitSetter.addPadding(this.curBlockArea, fo.getCommonBorderPaddingBackground(), this.discardPaddingBefore, this.discardPaddingAfter, false, false, this);
         TraitSetter.addMargins(this.curBlockArea, fo.getCommonBorderPaddingBackground(), fo.getCommonMarginBlock(), this);
         TraitSetter.addBreaks(this.curBlockArea, fo.getBreakBefore(), fo.getBreakAfter());
         int contentIPD = this.referenceIPD - this.getIPIndents();
         this.curBlockArea.setIPD(contentIPD);
         this.curBlockArea.setBidiLevel(fo.getBidiLevel());
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
      return this.getListItemFO().getKeepTogether();
   }

   public KeepProperty getKeepWithPreviousProperty() {
      return this.getListItemFO().getKeepWithPrevious();
   }

   public KeepProperty getKeepWithNextProperty() {
      return this.getListItemFO().getKeepWithNext();
   }

   public void reset() {
      super.reset();
      this.label.reset();
      this.body.reset();
   }

   public int getBreakBefore() {
      int breakBefore = BreakOpportunityHelper.getBreakBefore(this);
      breakBefore = BreakUtil.compareBreakClasses(breakBefore, this.label.getBreakBefore());
      breakBefore = BreakUtil.compareBreakClasses(breakBefore, this.body.getBreakBefore());
      return breakBefore;
   }

   public boolean isRestartable() {
      return true;
   }

   public class ListItemPosition extends Position {
      private int labelFirstIndex;
      private int labelLastIndex;
      private int bodyFirstIndex;
      private int bodyLastIndex;
      private Position originalLabelPosition;
      private Position originalBodyPosition;

      public ListItemPosition(LayoutManager lm, int labelFirst, int labelLast, int bodyFirst, int bodyLast) {
         super(lm);
         this.labelFirstIndex = labelFirst;
         this.labelLastIndex = labelLast;
         this.bodyFirstIndex = bodyFirst;
         this.bodyLastIndex = bodyLast;
      }

      public int getLabelFirstIndex() {
         return this.labelFirstIndex;
      }

      public int getLabelLastIndex() {
         return this.labelLastIndex;
      }

      public int getBodyFirstIndex() {
         return this.bodyFirstIndex;
      }

      public int getBodyLastIndex() {
         return this.bodyLastIndex;
      }

      public boolean generatesAreas() {
         return true;
      }

      public String toString() {
         StringBuffer sb = new StringBuffer("ListItemPosition:");
         sb.append(this.getIndex()).append("(");
         sb.append("label:").append(this.labelFirstIndex).append("-").append(this.labelLastIndex);
         sb.append(" body:").append(this.bodyFirstIndex).append("-").append(this.bodyLastIndex);
         sb.append(")");
         return sb.toString();
      }

      public Position getOriginalLabelPosition() {
         return this.originalLabelPosition;
      }

      public void setOriginalLabelPosition(Position originalLabelPosition) {
         this.originalLabelPosition = originalLabelPosition;
      }

      public Position getOriginalBodyPosition() {
         return this.originalBodyPosition;
      }

      public void setOriginalBodyPosition(Position originalBodyPosition) {
         this.originalBodyPosition = originalBodyPosition;
      }
   }
}
