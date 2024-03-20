package org.apache.fop.layoutmgr;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.area.Area;
import org.apache.fop.area.BlockParent;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.util.ListUtil;

public class FlowLayoutManager extends BlockStackingLayoutManager {
   private static Log log = LogFactory.getLog(FlowLayoutManager.class);
   private final BlockParent[] currentAreas = new BlockParent[6];
   private boolean handlingFloat;

   public FlowLayoutManager(PageSequenceLayoutManager pslm, Flow node) {
      super(node);
      this.setGeneratesBlockArea(true);
      this.setParent(pslm);
   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      return this.getNextKnuthElements(context, alignment, (Position)null, (LayoutManager)null);
   }

   List getNextKnuthElements(LayoutContext context, int alignment, Position restartPosition, LayoutManager restartLM) {
      List elements = new LinkedList();
      boolean isRestart = restartPosition != null;
      boolean doReset = isRestart;
      Stack lmStack = new Stack();
      LayoutManager currentChildLM;
      if (isRestart) {
         currentChildLM = restartPosition.getLM();
         if (currentChildLM == null) {
            throw new IllegalStateException("Cannot find layout manager to restart from");
         }

         if (restartLM != null && restartLM.getParent() == this) {
            currentChildLM = restartLM;
         } else {
            while(currentChildLM.getParent() != this) {
               lmStack.push(currentChildLM);
               currentChildLM = currentChildLM.getParent();
            }

            doReset = false;
         }

         this.setCurrentChildLM(currentChildLM);
      } else {
         currentChildLM = this.getChildLM();
      }

      for(; currentChildLM != null; currentChildLM = this.getChildLM()) {
         if (isRestart && !doReset) {
            if (this.addChildElements(elements, currentChildLM, context, alignment, lmStack, restartPosition, restartLM) != null) {
               return elements;
            }

            doReset = true;
         } else {
            if (doReset) {
               currentChildLM.reset();
            }

            if (this.addChildElements(elements, currentChildLM, context, alignment, (Stack)null, (Position)null, (LayoutManager)null) != null) {
               return elements;
            }
         }
      }

      SpaceResolver.resolveElementList(elements);
      this.setFinished(true);

      assert !elements.isEmpty();

      return elements;
   }

   private List addChildElements(List elements, LayoutManager childLM, LayoutContext context, int alignment, Stack lmStack, Position position, LayoutManager restartAtLM) {
      if (this.handleSpanChange(childLM, context)) {
         SpaceResolver.resolveElementList(elements);
         return elements;
      } else {
         LayoutContext childLC = this.makeChildLayoutContext(context);
         List childElements = this.getNextChildElements(childLM, context, childLC, alignment, lmStack, position, restartAtLM);
         if (elements.isEmpty()) {
            context.updateKeepWithPreviousPending(childLC.getKeepWithPreviousPending());
         }

         if (!elements.isEmpty() && !ElementListUtils.startsWithForcedBreak(childElements)) {
            this.addInBetweenBreak(elements, context, childLC);
         }

         context.updateKeepWithNextPending(childLC.getKeepWithNextPending());
         elements.addAll(childElements);
         if (ElementListUtils.endsWithForcedBreak(elements)) {
            if (childLM.isFinished() && !this.hasNextChildLM()) {
               this.setFinished(true);
            }

            SpaceResolver.resolveElementList(elements);
            return elements;
         } else {
            return null;
         }
      }
   }

   private boolean handleSpanChange(LayoutManager childLM, LayoutContext context) {
      int span = 95;
      int disableColumnBalancing = 48;
      if (childLM instanceof BlockLayoutManager) {
         span = ((BlockLayoutManager)childLM).getBlockFO().getSpan();
         disableColumnBalancing = ((BlockLayoutManager)childLM).getBlockFO().getDisableColumnBalancing();
      } else if (childLM instanceof BlockContainerLayoutManager) {
         span = ((BlockContainerLayoutManager)childLM).getBlockContainerFO().getSpan();
         disableColumnBalancing = ((BlockContainerLayoutManager)childLM).getBlockContainerFO().getDisableColumnBalancing();
      }

      int currentSpan = context.getCurrentSpan();
      if (currentSpan != span) {
         if (span == 5) {
            context.setDisableColumnBalancing(disableColumnBalancing);
         }

         log.debug("span change from " + currentSpan + " to " + span);
         context.signalSpanChange(span);
         return true;
      } else {
         return false;
      }
   }

   protected LayoutContext makeChildLayoutContext(LayoutContext context) {
      LayoutContext childLC = LayoutContext.newInstance();
      childLC.setStackLimitBP(context.getStackLimitBP());
      childLC.setRefIPD(context.getRefIPD());
      childLC.setWritingMode(this.getCurrentPage().getSimplePageMaster().getWritingMode());
      return childLC;
   }

   protected List getNextChildElements(LayoutManager childLM, LayoutContext context, LayoutContext childLC, int alignment, Stack lmStack, Position restartPosition, LayoutManager restartLM) {
      List childElements;
      if (lmStack == null) {
         childElements = childLM.getNextKnuthElements(childLC, alignment);
      } else {
         childElements = childLM.getNextKnuthElements(childLC, alignment, lmStack, restartPosition, restartLM);
      }

      assert !childElements.isEmpty();

      List tempList = childElements;
      List childElements = new LinkedList();
      this.wrapPositionElements(tempList, childElements);
      return childElements;
   }

   public int negotiateBPDAdjustment(int adj, KnuthElement lastElement) {
      log.debug(" FLM.negotiateBPDAdjustment> " + adj);
      Position lastPosition = lastElement.getPosition();
      if (lastPosition instanceof NonLeafPosition) {
         NonLeafPosition savedPos = (NonLeafPosition)lastPosition;
         lastElement.setPosition(savedPos.getPosition());
         int returnValue = ((BlockLevelLayoutManager)lastElement.getLayoutManager()).negotiateBPDAdjustment(adj, lastElement);
         lastElement.setPosition(savedPos);
         log.debug(" FLM.negotiateBPDAdjustment> result " + returnValue);
         return returnValue;
      } else {
         return 0;
      }
   }

   public void discardSpace(KnuthGlue spaceGlue) {
      log.debug(" FLM.discardSpace> ");
      Position gluePosition = spaceGlue.getPosition();
      if (gluePosition instanceof NonLeafPosition) {
         NonLeafPosition savedPos = (NonLeafPosition)gluePosition;
         spaceGlue.setPosition(savedPos.getPosition());
         ((BlockLevelLayoutManager)spaceGlue.getLayoutManager()).discardSpace(spaceGlue);
         spaceGlue.setPosition(savedPos);
      }

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

   public List getChangedKnuthElements(List oldList, int alignment) {
      ListIterator oldListIterator = oldList.listIterator();
      List returnedList = new LinkedList();
      List returnList = new LinkedList();
      KnuthElement prevElement = null;
      KnuthElement currElement = null;
      int fromIndex = 0;

      while(oldListIterator.hasNext()) {
         KnuthElement oldElement = (KnuthElement)oldListIterator.next();
         if (oldElement.getPosition() instanceof NonLeafPosition) {
            oldElement.setPosition(oldElement.getPosition().getPosition());
         } else {
            oldListIterator.remove();
         }
      }

      BlockLevelLayoutManager prevLM;
      for(oldListIterator = oldList.listIterator(); oldListIterator.hasNext(); prevElement = currElement) {
         currElement = (KnuthElement)oldListIterator.next();
         if (prevElement != null && prevElement.getLayoutManager() != currElement.getLayoutManager()) {
            prevLM = (BlockLevelLayoutManager)prevElement.getLayoutManager();
            BlockLevelLayoutManager currLM = (BlockLevelLayoutManager)currElement.getLayoutManager();
            returnedList.addAll(prevLM.getChangedKnuthElements(oldList.subList(fromIndex, oldListIterator.previousIndex()), alignment));
            fromIndex = oldListIterator.previousIndex();
            if (!prevLM.mustKeepWithNext() && !currLM.mustKeepWithPrevious()) {
               if (!((KnuthElement)ListUtil.getLast(returnedList)).isGlue()) {
                  returnedList.add(new KnuthPenalty(0, 0, false, new Position(this), false));
               }
            } else {
               returnedList.add(new KnuthPenalty(0, 1000, false, new Position(this), false));
            }
         }
      }

      if (currElement != null) {
         prevLM = (BlockLevelLayoutManager)currElement.getLayoutManager();
         returnedList.addAll(prevLM.getChangedKnuthElements(oldList.subList(fromIndex, oldList.size()), alignment));
      }

      KnuthElement aReturnedList;
      for(Iterator var13 = returnedList.iterator(); var13.hasNext(); returnList.add(aReturnedList)) {
         aReturnedList = (KnuthElement)var13.next();
         if (aReturnedList.getLayoutManager() != this) {
            aReturnedList.setPosition(new NonLeafPosition(this, aReturnedList.getPosition()));
         }
      }

      return returnList;
   }

   public void addAreas(PositionIterator parentIter, LayoutContext layoutContext) {
      AreaAdditionUtil.addAreas(this, parentIter, layoutContext);
      this.flush();
   }

   public void addChildArea(Area childArea) {
      if (childArea instanceof BlockParent && this.handlingFloat()) {
         BlockParent bp = (BlockParent)childArea;
         bp.setXOffset(this.getPSLM().getStartIntrusionAdjustment());
      }

      this.getParentArea(childArea);
      this.addChildToArea(childArea, this.currentAreas[childArea.getAreaClass()]);
   }

   public Area getParentArea(Area childArea) {
      BlockParent parentArea = null;
      int aclass = childArea.getAreaClass();
      if (aclass != 0 && aclass != 5) {
         if (aclass == 3) {
            parentArea = this.getCurrentPV().getBodyRegion().getBeforeFloat();
         } else {
            if (aclass != 4) {
               throw new IllegalStateException("(internal error) Invalid area class (" + aclass + ") requested.");
            }

            parentArea = this.getCurrentPV().getBodyRegion().getFootnote();
         }
      } else {
         parentArea = this.getCurrentPV().getCurrentFlow();
      }

      this.currentAreas[aclass] = (BlockParent)parentArea;
      this.setCurrentArea((BlockParent)parentArea);
      return (Area)parentArea;
   }

   public int getContentAreaIPD() {
      int flowIPD = this.getPSLM().getCurrentColumnWidth();
      return flowIPD;
   }

   public int getContentAreaBPD() {
      return this.getCurrentPV().getBodyRegion().getBPD();
   }

   public boolean isRestartable() {
      return true;
   }

   public void handleFloatOn() {
      this.handlingFloat = true;
   }

   public void handleFloatOff() {
      this.handlingFloat = false;
   }

   public boolean handlingFloat() {
      return this.handlingFloat;
   }
}
