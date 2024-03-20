package org.apache.fop.layoutmgr;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.fop.events.EventBroadcaster;

class RestartAtLM {
   protected boolean invalidPosition;

   protected LayoutManager getRestartAtLM(AbstractBreaker breaker, PageBreakingAlgorithm alg, boolean ipdChangesOnNextPage, boolean onLastPageAndIPDChanges, boolean visitedBefore, AbstractBreaker.BlockSequence blockList, int start) {
      BreakingAlgorithm.KnuthNode optimalBreak = ipdChangesOnNextPage ? alg.getBestNodeBeforeIPDChange() : alg.getBestNodeForLastPage();
      if (onLastPageAndIPDChanges && visitedBefore && breaker.originalRestartAtLM == null) {
         optimalBreak = null;
      }

      int positionIndex = this.findPositionIndex(breaker, optimalBreak, alg, start);
      KnuthElement element;
      if (ipdChangesOnNextPage || breaker.positionAtBreak != null && breaker.positionAtBreak.getIndex() > -1) {
         breaker.firstElementsForRestart = Collections.EMPTY_LIST;
         if (ipdChangesOnNextPage && breaker.containsNonRestartableLM(breaker.positionAtBreak)) {
            if (alg.getIPDdifference() > 0) {
               EventBroadcaster eventBroadcaster = breaker.getCurrentChildLM().getFObj().getUserAgent().getEventBroadcaster();
               BlockLevelEventProducer eventProducer = BlockLevelEventProducer.Provider.get(eventBroadcaster);
               eventProducer.nonRestartableContentFlowingToNarrowerPage(this);
            }

            breaker.firstElementsForRestart = new LinkedList();
            boolean boxFound = false;
            Iterator iter = blockList.listIterator(positionIndex + 1);
            Position position = null;

            while(iter.hasNext() && (position == null || breaker.containsNonRestartableLM(position))) {
               ++positionIndex;
               element = (KnuthElement)iter.next();
               position = element.getPosition();
               if (element.isBox()) {
                  boxFound = true;
                  breaker.firstElementsForRestart.add(element);
               } else if (boxFound) {
                  breaker.firstElementsForRestart.add(element);
               }
            }

            if (position instanceof SpaceResolver.SpaceHandlingBreakPosition) {
               breaker.positionAtBreak = position.getPosition();
            } else {
               breaker.positionAtBreak = null;
            }
         }
      }

      LayoutManager restartAtLM = null;
      if (ipdChangesOnNextPage || breaker.positionAtBreak == null || breaker.positionAtBreak.getIndex() <= -1) {
         if (breaker.positionAtBreak != null && breaker.positionAtBreak.getIndex() == -1) {
            Iterator iter = blockList.listIterator(positionIndex + 1);

            Position position;
            do {
               do {
                  do {
                     element = (KnuthElement)iter.next();
                     position = element.getPosition();
                  } while(position == null);
               } while(position instanceof SpaceResolver.SpaceHandlingPosition);
            } while(position instanceof SpaceResolver.SpaceHandlingBreakPosition && position.getPosition().getIndex() == -1);

            for(LayoutManager surroundingLM = breaker.positionAtBreak.getLM(); position.getLM() != surroundingLM; position = position.getPosition()) {
            }

            if (position.getPosition() == null) {
               position.getLM().getFObj().setForceKeepTogether(true);
               this.invalidPosition = true;
               return null;
            }

            restartAtLM = position.getPosition().getLM();
         }

         if (onLastPageAndIPDChanges && restartAtLM != null) {
            if (breaker.originalRestartAtLM == null) {
               breaker.originalRestartAtLM = restartAtLM;
            } else {
               restartAtLM = breaker.originalRestartAtLM;
            }

            breaker.firstElementsForRestart = Collections.EMPTY_LIST;
         }
      }

      if (onLastPageAndIPDChanges && !visitedBefore && breaker.positionAtBreak.getPosition() != null) {
         restartAtLM = breaker.positionAtBreak.getPosition().getLM();
      }

      return restartAtLM;
   }

   private int findPositionIndex(AbstractBreaker breaker, BreakingAlgorithm.KnuthNode optimalBreak, PageBreakingAlgorithm alg, int start) {
      int positionIndex = optimalBreak != null ? optimalBreak.position : start;

      for(int i = positionIndex; i < alg.par.size(); ++i) {
         KnuthElement elementAtBreak = alg.getElement(i);
         if (elementAtBreak.getPosition() == null) {
            elementAtBreak = alg.getElement(0);
         }

         breaker.positionAtBreak = elementAtBreak.getPosition();
         breaker.positionAtBreak = breaker.positionAtBreak.getPosition();
         if (breaker.positionAtBreak != null) {
            return i;
         }
      }

      return positionIndex;
   }
}
