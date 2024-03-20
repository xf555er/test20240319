package org.apache.fop.layoutmgr;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockParent;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.properties.BreakPropertySet;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.layoutmgr.inline.InlineContainerLayoutManager;
import org.apache.fop.layoutmgr.inline.InlineLayoutManager;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.util.ListUtil;

public abstract class BlockStackingLayoutManager extends AbstractLayoutManager implements BlockLevelLayoutManager {
   private static Log log = LogFactory.getLog(BlockStackingLayoutManager.class);
   protected BlockParent parentArea;
   protected int bpUnit;
   protected int adjustedSpaceBefore;
   protected int adjustedSpaceAfter;
   protected List storedList;
   protected boolean breakBeforeServed;
   protected boolean firstVisibleMarkServed;
   protected int referenceIPD;
   protected int startIndent;
   protected int endIndent;
   protected MinOptMax foSpaceBefore;
   protected MinOptMax foSpaceAfter;
   private Position auxiliaryPosition;
   private int contentAreaIPD;
   private boolean isRestartAtLM;

   public BlockStackingLayoutManager(FObj node) {
      super(node);
      this.setGeneratesBlockArea(true);
   }

   protected BlockParent getCurrentArea() {
      return this.parentArea;
   }

   protected void setCurrentArea(BlockParent parentArea) {
      this.parentArea = parentArea;
   }

   public void addBlockSpacing(double adjust, MinOptMax minoptmax) {
      int sp = TraitSetter.getEffectiveSpace(adjust, minoptmax);
      if (sp != 0) {
         Block spacer = new Block();
         spacer.setChangeBarList(this.getChangeBarList());
         spacer.setBPD(sp);
         this.parentLayoutManager.addChildArea(spacer);
      }

   }

   protected void addChildToArea(Area childArea, BlockParent parentArea) {
      parentArea.addBlock((Block)childArea);
      this.flush();
   }

   public void addChildArea(Area childArea) {
      this.addChildToArea(childArea, this.getCurrentArea());
   }

   protected void flush() {
      if (this.getCurrentArea() != null) {
         this.parentLayoutManager.addChildArea(this.getCurrentArea());
      }

   }

   protected Position getAuxiliaryPosition() {
      if (this.auxiliaryPosition == null) {
         this.auxiliaryPosition = new NonLeafPosition(this, (Position)null);
      }

      return this.auxiliaryPosition;
   }

   protected int neededUnits(int len) {
      return (int)Math.ceil((double)((float)len / (float)this.bpUnit));
   }

   protected int updateContentAreaIPDwithOverconstrainedAdjust() {
      int ipd = this.referenceIPD - (this.startIndent + this.endIndent);
      if (ipd < 0) {
         log.debug("Adjusting end-indent based on overconstrained geometry rules for " + this.fobj);
         BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(this.getFObj().getUserAgent().getEventBroadcaster());
         eventProducer.overconstrainedAdjustEndIndent(this, this.getFObj().getName(), ipd, this.getFObj().getLocator());
         this.endIndent += ipd;
         ipd = 0;
      }

      this.setContentAreaIPD(ipd);
      return ipd;
   }

   protected int updateContentAreaIPDwithOverconstrainedAdjust(int contentIPD) {
      int ipd = this.referenceIPD - (contentIPD + this.startIndent + this.endIndent);
      if (ipd < 0) {
         log.debug("Adjusting end-indent based on overconstrained geometry rules for " + this.fobj);
         BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(this.getFObj().getUserAgent().getEventBroadcaster());
         eventProducer.overconstrainedAdjustEndIndent(this, this.getFObj().getName(), ipd, this.getFObj().getLocator());
         this.endIndent += ipd;
      }

      this.setContentAreaIPD(contentIPD);
      return contentIPD;
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      return this.getNextKnuthElements(context, alignment, (Stack)null, (Position)null, (LayoutManager)null);
   }

   public List getNextKnuthElements(LayoutContext context, int alignment, Stack lmStack, Position restartPosition, LayoutManager restartAtLM) {
      this.isRestartAtLM = restartAtLM != null;
      this.referenceIPD = context.getRefIPD();
      this.updateContentAreaIPDwithOverconstrainedAdjust();
      boolean isRestart = lmStack != null;
      boolean emptyStack = !isRestart || lmStack.isEmpty();
      List contentList = new LinkedList();
      List elements = new LinkedList();
      if (!this.breakBeforeServed(context, elements)) {
         return elements;
      } else {
         this.addFirstVisibleMarks(elements, context, alignment);
         BreakElement forcedBreakAfterLast = null;
         LayoutManager currentChildLM;
         if (isRestart) {
            if (emptyStack) {
               assert restartAtLM != null && restartAtLM.getParent() == this;

               currentChildLM = restartAtLM;
            } else {
               currentChildLM = (LayoutManager)lmStack.pop();
            }

            this.setCurrentChildLM(currentChildLM);
         } else {
            currentChildLM = this.getChildLM();
         }

         while(true) {
            label147: {
               if (currentChildLM != null) {
                  LayoutContext childLC = this.makeChildLayoutContext(context);
                  List childElements;
                  if (isRestart && !emptyStack) {
                     childElements = this.getNextChildElements(currentChildLM, context, childLC, alignment, lmStack, restartPosition, restartAtLM);
                     emptyStack = true;
                  } else {
                     if (isRestart) {
                        currentChildLM.reset();
                     }

                     childElements = this.getNextChildElements(currentChildLM, context, childLC, alignment, (Stack)null, (Position)null, (LayoutManager)null);
                  }

                  if (contentList.isEmpty()) {
                     context.updateKeepWithPreviousPending(childLC.getKeepWithPreviousPending());
                  }

                  if (childElements == null || childElements.isEmpty()) {
                     break label147;
                  }

                  if (!contentList.isEmpty() && !ElementListUtils.startsWithForcedBreak(childElements)) {
                     this.addInBetweenBreak(contentList, context, childLC);
                  }

                  if (childElements.size() == 1 && ElementListUtils.startsWithForcedBreak(childElements)) {
                     if (!currentChildLM.isFinished() || this.hasNextChildLM()) {
                        if (contentList.isEmpty()) {
                           elements.add(this.makeAuxiliaryZeroWidthBox());
                        }

                        contentList.addAll(childElements);
                        this.wrapPositionElements(contentList, elements);
                        return elements;
                     }

                     forcedBreakAfterLast = (BreakElement)childElements.get(0);
                     context.clearPendingMarks();
                  } else {
                     contentList.addAll(childElements);
                     if (!ElementListUtils.endsWithForcedBreak(childElements)) {
                        context.updateKeepWithNextPending(childLC.getKeepWithNextPending());
                        break label147;
                     }

                     if (!currentChildLM.isFinished() || this.hasNextChildLM()) {
                        this.wrapPositionElements(contentList, elements);
                        return elements;
                     }

                     forcedBreakAfterLast = (BreakElement)ListUtil.removeLast(contentList);
                     context.clearPendingMarks();
                  }
               }

               if (contentList.isEmpty()) {
                  if (forcedBreakAfterLast == null) {
                     elements.add(this.makeAuxiliaryZeroWidthBox());
                  }
               } else {
                  this.wrapPositionElements(contentList, elements);
               }

               this.addLastVisibleMarks(elements, context, alignment);
               if (forcedBreakAfterLast == null) {
                  this.addKnuthElementsForBreakAfter(elements, context);
               } else {
                  forcedBreakAfterLast.clearPendingMarks();
                  elements.add(forcedBreakAfterLast);
               }

               context.updateKeepWithNextPending(this.getKeepWithNext());
               this.setFinished(true);
               return elements;
            }

            currentChildLM = this.getChildLM();
         }
      }
   }

   protected LayoutContext makeChildLayoutContext(LayoutContext context) {
      LayoutContext childLC = LayoutContext.newInstance();
      childLC.copyPendingMarksFrom(context);
      childLC.setStackLimitBP(context.getStackLimitBP());
      childLC.setRefIPD(this.referenceIPD);
      return childLC;
   }

   protected void addFirstVisibleMarks(List elements, LayoutContext context, int alignment) {
      if (!this.firstVisibleMarkServed) {
         this.addKnuthElementsForSpaceBefore(elements, alignment);
         context.updateKeepWithPreviousPending(this.getKeepWithPrevious());
      }

      this.addKnuthElementsForBorderPaddingBefore(elements, !this.firstVisibleMarkServed);
      this.firstVisibleMarkServed = true;
      this.addPendingMarks(context);
   }

   protected void addLastVisibleMarks(List elements, LayoutContext context, int alignment) {
      this.addKnuthElementsForBorderPaddingAfter(elements, true);
      this.addKnuthElementsForSpaceAfter(elements, alignment);
      context.clearPendingMarks();
   }

   protected boolean breakBeforeServed(LayoutContext context, List elements) {
      if (!this.breakBeforeServed) {
         this.breakBeforeServed = true;
         if (!context.suppressBreakBefore() && this.addKnuthElementsForBreakBefore(elements, context)) {
            return false;
         }
      }

      return this.breakBeforeServed;
   }

   private KnuthBox makeZeroWidthBox() {
      return new KnuthBox(0, new NonLeafPosition(this, (Position)null), false);
   }

   private KnuthBox makeAuxiliaryZeroWidthBox() {
      return new KnuthBox(0, this.notifyPos(new Position(this)), true);
   }

   private KnuthPenalty makeZeroWidthPenalty(int penaltyValue) {
      return new KnuthPenalty(0, penaltyValue, false, new NonLeafPosition(this, (Position)null), false);
   }

   private KnuthGlue makeSpaceAdjustmentGlue(int width, Adjustment adjustmentClass, boolean isAuxiliary) {
      return new KnuthGlue(width, 0, 0, adjustmentClass, new NonLeafPosition(this, (Position)null), isAuxiliary);
   }

   protected List getNextChildElements(LayoutManager childLM, LayoutContext context, LayoutContext childLC, int alignment, Stack lmStack, Position restartPosition, LayoutManager restartAtLM) {
      if (childLM == this.childLMs.get(0)) {
         childLC.setFlags(2);
      }

      return lmStack == null ? childLM.getNextKnuthElements(childLC, alignment) : childLM.getNextKnuthElements(childLC, alignment, lmStack, restartPosition, restartAtLM);
   }

   protected void addInBetweenBreak(List contentList, LayoutContext parentLC, LayoutContext childLC) {
      if (!this.mustKeepTogether() && !parentLC.isKeepWithNextPending() && !childLC.isKeepWithPreviousPending()) {
         ListElement last = (ListElement)ListUtil.getLast(contentList);
         if (last.isGlue()) {
            log.warn("glue-type break possibility not handled properly, yet");
         } else if (!ElementListUtils.endsWithNonInfinitePenalty(contentList)) {
            contentList.add(new BreakElement(new Position(this), 0, 9, parentLC));
         }

      } else {
         Keep keep = this.getKeepTogether();
         keep = keep.compare(parentLC.getKeepWithNextPending());
         parentLC.clearKeepWithNextPending();
         keep = keep.compare(childLC.getKeepWithPreviousPending());
         childLC.clearKeepWithPreviousPending();
         contentList.add(new BreakElement(new Position(this), keep.getPenalty(), keep.getContext(), parentLC));
      }
   }

   public int negotiateBPDAdjustment(int adj, KnuthElement lastElement) {
      assert lastElement != null && lastElement.getPosition() != null;

      Position innerPosition = lastElement.getPosition().getPosition();
      if (innerPosition == null && lastElement.isGlue()) {
         if (((KnuthGlue)lastElement).getAdjustmentClass() == Adjustment.SPACE_BEFORE_ADJUSTMENT) {
            this.adjustedSpaceBefore += adj;
         } else {
            this.adjustedSpaceAfter += adj;
         }

         return adj;
      } else {
         int newAdjustment;
         if (innerPosition instanceof MappingPosition) {
            MappingPosition mappingPos = (MappingPosition)innerPosition;
            if (lastElement.isGlue()) {
               ListIterator storedListIterator = this.storedList.listIterator(mappingPos.getFirstIndex());
               newAdjustment = 0;

               while(storedListIterator.nextIndex() <= mappingPos.getLastIndex()) {
                  KnuthElement storedElement = (KnuthElement)storedListIterator.next();
                  if (storedElement.isGlue()) {
                     newAdjustment += ((BlockLevelLayoutManager)storedElement.getLayoutManager()).negotiateBPDAdjustment(adj - newAdjustment, storedElement);
                  }
               }

               newAdjustment = newAdjustment > 0 ? this.bpUnit * this.neededUnits(newAdjustment) : -this.bpUnit * this.neededUnits(-newAdjustment);
               return newAdjustment;
            } else {
               KnuthPenalty storedPenalty = (KnuthPenalty)this.storedList.get(mappingPos.getLastIndex());
               return storedPenalty.getWidth() > 0 ? ((BlockLevelLayoutManager)storedPenalty.getLayoutManager()).negotiateBPDAdjustment(storedPenalty.getWidth(), storedPenalty) : adj;
            }
         } else if (innerPosition != null && innerPosition.getLM() != this) {
            Position lastPosition = lastElement.getPosition();

            assert lastPosition instanceof NonLeafPosition;

            NonLeafPosition savedPos = (NonLeafPosition)lastPosition;
            lastElement.setPosition(innerPosition);
            newAdjustment = ((BlockLevelLayoutManager)lastElement.getLayoutManager()).negotiateBPDAdjustment(adj, lastElement);
            lastElement.setPosition(savedPos);
            return newAdjustment;
         } else {
            log.error("BlockLayoutManager.negotiateBPDAdjustment(): unexpected Position");
            return 0;
         }
      }
   }

   public void discardSpace(KnuthGlue spaceGlue) {
      assert spaceGlue != null && spaceGlue.getPosition() != null;

      Position mainPosition = spaceGlue.getPosition();
      Position innerPosition = mainPosition.getPosition();
      if (innerPosition != null && innerPosition.getLM() != this) {
         assert mainPosition instanceof NonLeafPosition;

         NonLeafPosition savedPos = (NonLeafPosition)mainPosition;
         spaceGlue.setPosition(innerPosition);
         ((BlockLevelLayoutManager)spaceGlue.getLayoutManager()).discardSpace(spaceGlue);
         spaceGlue.setPosition(savedPos);
      } else if (spaceGlue.getAdjustmentClass() == Adjustment.SPACE_BEFORE_ADJUSTMENT) {
         this.adjustedSpaceBefore = 0;
         this.foSpaceBefore = MinOptMax.ZERO;
      } else {
         this.adjustedSpaceAfter = 0;
         this.foSpaceAfter = MinOptMax.ZERO;
      }

   }

   public List getChangedKnuthElements(List oldList, int alignment) {
      ListIterator oldListIterator = oldList.listIterator();
      KnuthElement currElement = null;
      KnuthElement prevElement = null;
      List returnedList = new LinkedList();
      List returnList = new LinkedList();
      int fromIndex = 0;

      while(oldListIterator.hasNext()) {
         KnuthElement oldElement = (KnuthElement)oldListIterator.next();

         assert oldElement.getPosition() != null;

         Position innerPosition = oldElement.getPosition().getPosition();
         if (innerPosition != null) {
            oldElement.setPosition(innerPosition);
         } else {
            oldElement.setPosition(new Position(this));
         }
      }

      for(ListIterator workListIterator = oldList.listIterator(); workListIterator.hasNext(); prevElement = currElement) {
         currElement = (KnuthElement)workListIterator.next();
         if (prevElement != null && prevElement.getLayoutManager() != currElement.getLayoutManager()) {
            BlockLevelLayoutManager prevLM = (BlockLevelLayoutManager)prevElement.getLayoutManager();
            BlockLevelLayoutManager currLM = (BlockLevelLayoutManager)currElement.getLayoutManager();
            boolean somethingAdded = false;
            if (prevLM != this) {
               returnedList.addAll(prevLM.getChangedKnuthElements(oldList.subList(fromIndex, workListIterator.previousIndex()), alignment));
               somethingAdded = true;
            }

            fromIndex = workListIterator.previousIndex();
            if (somethingAdded && (this.mustKeepTogether() || prevLM.mustKeepWithNext() || currLM.mustKeepWithPrevious())) {
               returnedList.add(this.makeZeroWidthPenalty(1000));
            } else if (somethingAdded && !((KnuthElement)ListUtil.getLast(returnedList)).isGlue()) {
               returnedList.add(this.makeZeroWidthPenalty(1000));
            }
         }
      }

      if (currElement != null) {
         LayoutManager currLM = currElement.getLayoutManager();
         if (currLM != this) {
            returnedList.addAll(currLM.getChangedKnuthElements(oldList.subList(fromIndex, oldList.size()), alignment));
         } else if (!returnedList.isEmpty()) {
            ListUtil.removeLast(returnedList);
         }
      }

      boolean spaceBeforeIsConditional = true;
      if (this.fobj instanceof org.apache.fop.fo.flow.Block) {
         spaceBeforeIsConditional = this.getSpaceBeforeProperty().isDiscard();
      }

      if (this.adjustedSpaceBefore != 0) {
         if (!spaceBeforeIsConditional) {
            returnList.add(this.makeZeroWidthBox());
            returnList.add(this.makeZeroWidthPenalty(1000));
         }

         returnList.add(this.makeSpaceAdjustmentGlue(this.adjustedSpaceBefore, Adjustment.SPACE_BEFORE_ADJUSTMENT, false));
      }

      Iterator var17 = returnedList.iterator();

      while(var17.hasNext()) {
         KnuthElement el = (KnuthElement)var17.next();
         el.setPosition(new NonLeafPosition(this, el.getPosition()));
         returnList.add(el);
      }

      boolean spaceAfterIsConditional = true;
      if (this.fobj instanceof org.apache.fop.fo.flow.Block) {
         spaceAfterIsConditional = this.getSpaceAfterProperty().isDiscard();
      }

      if (this.adjustedSpaceAfter != 0) {
         if (!spaceAfterIsConditional) {
            returnList.add(this.makeZeroWidthPenalty(1000));
         }

         returnList.add(this.makeSpaceAdjustmentGlue(this.adjustedSpaceAfter, Adjustment.SPACE_AFTER_ADJUSTMENT, spaceAfterIsConditional));
         if (!spaceAfterIsConditional) {
            returnList.add(this.makeZeroWidthBox());
         }
      }

      return returnList;
   }

   protected Keep getParentKeepTogether() {
      Keep keep = Keep.KEEP_AUTO;
      if (this.getParent() instanceof BlockLevelLayoutManager) {
         keep = ((BlockLevelLayoutManager)this.getParent()).getKeepTogether();
      } else if (this.getParent() instanceof InlineLayoutManager && ((InlineLayoutManager)this.getParent()).mustKeepTogether()) {
         keep = Keep.KEEP_ALWAYS;
      }

      return keep;
   }

   public boolean mustKeepTogether() {
      return !this.getKeepTogether().isAuto();
   }

   public boolean mustKeepWithPrevious() {
      return !this.getKeepWithPrevious().isAuto();
   }

   public boolean mustKeepWithNext() {
      return !this.getKeepWithNext().isAuto();
   }

   public Keep getKeepTogether() {
      Keep keep = Keep.getKeep(this.getKeepTogetherProperty());
      keep = keep.compare(this.getParentKeepTogether());
      if (this.getFObj().isForceKeepTogether()) {
         keep = Keep.KEEP_ALWAYS;
      }

      return keep;
   }

   public Keep getKeepWithPrevious() {
      return Keep.getKeep(this.getKeepWithPreviousProperty());
   }

   public Keep getKeepWithNext() {
      return Keep.getKeep(this.getKeepWithNextProperty());
   }

   public KeepProperty getKeepTogetherProperty() {
      throw new IllegalStateException();
   }

   public KeepProperty getKeepWithPreviousProperty() {
      throw new IllegalStateException();
   }

   public KeepProperty getKeepWithNextProperty() {
      throw new IllegalStateException();
   }

   protected void addPendingMarks(LayoutContext context) {
      CommonBorderPaddingBackground borderAndPadding = this.getBorderPaddingBackground();
      if (borderAndPadding != null) {
         if (borderAndPadding.getBorderBeforeWidth(false) > 0) {
            context.addPendingBeforeMark(new BorderElement(this.getAuxiliaryPosition(), borderAndPadding.getBorderInfo(0).getWidth(), RelSide.BEFORE, false, false, this));
         }

         if (borderAndPadding.getPaddingBefore(false, this) > 0) {
            context.addPendingBeforeMark(new PaddingElement(this.getAuxiliaryPosition(), borderAndPadding.getPaddingLengthProperty(0), RelSide.BEFORE, false, false, this));
         }

         if (borderAndPadding.getBorderAfterWidth(false) > 0) {
            context.addPendingAfterMark(new BorderElement(this.getAuxiliaryPosition(), borderAndPadding.getBorderInfo(1).getWidth(), RelSide.AFTER, false, false, this));
         }

         if (borderAndPadding.getPaddingAfter(false, this) > 0) {
            context.addPendingAfterMark(new PaddingElement(this.getAuxiliaryPosition(), borderAndPadding.getPaddingLengthProperty(1), RelSide.AFTER, false, false, this));
         }
      }

   }

   private CommonBorderPaddingBackground getBorderPaddingBackground() {
      if (this.fobj instanceof org.apache.fop.fo.flow.Block) {
         return ((org.apache.fop.fo.flow.Block)this.fobj).getCommonBorderPaddingBackground();
      } else if (this.fobj instanceof BlockContainer) {
         return ((BlockContainer)this.fobj).getCommonBorderPaddingBackground();
      } else if (this.fobj instanceof ListBlock) {
         return ((ListBlock)this.fobj).getCommonBorderPaddingBackground();
      } else if (this.fobj instanceof ListItem) {
         return ((ListItem)this.fobj).getCommonBorderPaddingBackground();
      } else {
         return this.fobj instanceof Table ? ((Table)this.fobj).getCommonBorderPaddingBackground() : null;
      }
   }

   protected SpaceProperty getSpaceBeforeProperty() {
      if (this.fobj instanceof org.apache.fop.fo.flow.Block) {
         return ((org.apache.fop.fo.flow.Block)this.fobj).getCommonMarginBlock().spaceBefore;
      } else if (this.fobj instanceof BlockContainer) {
         return ((BlockContainer)this.fobj).getCommonMarginBlock().spaceBefore;
      } else if (this.fobj instanceof ListBlock) {
         return ((ListBlock)this.fobj).getCommonMarginBlock().spaceBefore;
      } else if (this.fobj instanceof ListItem) {
         return ((ListItem)this.fobj).getCommonMarginBlock().spaceBefore;
      } else {
         return this.fobj instanceof Table ? ((Table)this.fobj).getCommonMarginBlock().spaceBefore : null;
      }
   }

   protected SpaceProperty getSpaceAfterProperty() {
      if (this.fobj instanceof org.apache.fop.fo.flow.Block) {
         return ((org.apache.fop.fo.flow.Block)this.fobj).getCommonMarginBlock().spaceAfter;
      } else if (this.fobj instanceof BlockContainer) {
         return ((BlockContainer)this.fobj).getCommonMarginBlock().spaceAfter;
      } else if (this.fobj instanceof ListBlock) {
         return ((ListBlock)this.fobj).getCommonMarginBlock().spaceAfter;
      } else if (this.fobj instanceof ListItem) {
         return ((ListItem)this.fobj).getCommonMarginBlock().spaceAfter;
      } else {
         return this.fobj instanceof Table ? ((Table)this.fobj).getCommonMarginBlock().spaceAfter : null;
      }
   }

   protected void addKnuthElementsForBorderPaddingBefore(List returnList, boolean isFirst) {
      CommonBorderPaddingBackground borderAndPadding = this.getBorderPaddingBackground();
      if (borderAndPadding != null) {
         if (borderAndPadding.getBorderBeforeWidth(false) > 0) {
            returnList.add(new BorderElement(this.getAuxiliaryPosition(), borderAndPadding.getBorderInfo(0).getWidth(), RelSide.BEFORE, isFirst, false, this));
         }

         if (borderAndPadding.getPaddingBefore(false, this) > 0) {
            returnList.add(new PaddingElement(this.getAuxiliaryPosition(), borderAndPadding.getPaddingLengthProperty(0), RelSide.BEFORE, isFirst, false, this));
         }
      }

   }

   protected void addKnuthElementsForBorderPaddingAfter(List returnList, boolean isLast) {
      CommonBorderPaddingBackground borderAndPadding = this.getBorderPaddingBackground();
      if (borderAndPadding != null) {
         if (borderAndPadding.getPaddingAfter(false, this) > 0) {
            returnList.add(new PaddingElement(this.getAuxiliaryPosition(), borderAndPadding.getPaddingLengthProperty(1), RelSide.AFTER, false, isLast, this));
         }

         if (borderAndPadding.getBorderAfterWidth(false) > 0) {
            returnList.add(new BorderElement(this.getAuxiliaryPosition(), borderAndPadding.getBorderInfo(1).getWidth(), RelSide.AFTER, false, isLast, this));
         }
      }

   }

   protected boolean addKnuthElementsForBreakBefore(List returnList, LayoutContext context) {
      int breakBefore = this.getBreakBefore();
      if (breakBefore != 104 && breakBefore != 28 && breakBefore != 44 && breakBefore != 100) {
         return false;
      } else {
         returnList.add(new BreakElement(this.getAuxiliaryPosition(), 0, -1000, breakBefore, context));
         return true;
      }
   }

   public int getBreakBefore() {
      return BreakOpportunityHelper.getBreakBefore(this);
   }

   protected boolean addKnuthElementsForBreakAfter(List returnList, LayoutContext context) {
      int breakAfter = -1;
      if (this.fobj instanceof BreakPropertySet) {
         breakAfter = ((BreakPropertySet)this.fobj).getBreakAfter();
      }

      if (breakAfter != 104 && breakAfter != 28 && breakAfter != 44 && breakAfter != 100) {
         return false;
      } else {
         returnList.add(new BreakElement(this.getAuxiliaryPosition(), 0, -1000, breakAfter, context));
         return true;
      }
   }

   protected void addKnuthElementsForSpaceBefore(List returnList, int alignment) {
      SpaceProperty spaceBefore = this.getSpaceBeforeProperty();
      if (spaceBefore != null && (spaceBefore.getMinimum(this).getLength().getValue(this) != 0 || spaceBefore.getMaximum(this).getLength().getValue(this) != 0)) {
         returnList.add(new SpaceElement(this.getAuxiliaryPosition(), spaceBefore, RelSide.BEFORE, true, false, this));
      }

   }

   protected void addKnuthElementsForSpaceAfter(List returnList, int alignment) {
      SpaceProperty spaceAfter = this.getSpaceAfterProperty();
      if (spaceAfter != null && (spaceAfter.getMinimum(this).getLength().getValue(this) != 0 || spaceAfter.getMaximum(this).getLength().getValue(this) != 0)) {
         returnList.add(new SpaceElement(this.getAuxiliaryPosition(), spaceAfter, RelSide.AFTER, false, true, this));
      }

   }

   protected void wrapPositionElements(List sourceList, List targetList) {
      this.wrapPositionElements(sourceList, targetList, false);
   }

   protected void wrapPositionElements(List sourceList, List targetList, boolean force) {
      ListIterator listIter = sourceList.listIterator();

      while(listIter.hasNext()) {
         Object tempElement = listIter.next();
         if (tempElement instanceof ListElement) {
            this.wrapPositionElement((ListElement)tempElement, targetList, force);
         } else if (tempElement instanceof List) {
            this.wrapPositionElements((List)tempElement, targetList, force);
         }
      }

   }

   protected void wrapPositionElement(ListElement el, List targetList, boolean force) {
      if (force || el.getLayoutManager() != this) {
         el.setPosition(this.notifyPos(new NonLeafPosition(this, el.getPosition())));
      }

      targetList.add(el);
   }

   protected int getIPIndents() {
      return this.startIndent + this.endIndent;
   }

   public int getContentAreaIPD() {
      return this.contentAreaIPD;
   }

   protected void setContentAreaIPD(int contentAreaIPD) {
      this.contentAreaIPD = contentAreaIPD;
   }

   public int getContentAreaBPD() {
      return -1;
   }

   public void reset() {
      super.reset();
      this.breakBeforeServed = false;
      this.firstVisibleMarkServed = false;
   }

   public boolean handleOverflow(int milliPoints) {
      if (this.getParent() instanceof BlockStackingLayoutManager) {
         return ((BlockStackingLayoutManager)this.getParent()).handleOverflow(milliPoints);
      } else {
         return this.getParent() instanceof InlineContainerLayoutManager ? ((InlineContainerLayoutManager)this.getParent()).handleOverflow(milliPoints) : false;
      }
   }

   public boolean isRestartAtLM() {
      return this.isRestartAtLM;
   }

   protected static class MappingPosition extends Position {
      private int firstIndex;
      private int lastIndex;

      public MappingPosition(LayoutManager lm, int first, int last) {
         super(lm);
         this.firstIndex = first;
         this.lastIndex = last;
      }

      public int getFirstIndex() {
         return this.firstIndex;
      }

      public int getLastIndex() {
         return this.lastIndex;
      }
   }
}
