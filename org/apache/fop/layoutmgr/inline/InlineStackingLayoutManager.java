package org.apache.fop.layoutmgr.inline;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.apache.fop.area.Area;
import org.apache.fop.area.inline.Space;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.layoutmgr.AbstractLayoutManager;
import org.apache.fop.layoutmgr.BreakOpportunity;
import org.apache.fop.layoutmgr.BreakOpportunityHelper;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.traits.MinOptMax;

public abstract class InlineStackingLayoutManager extends AbstractLayoutManager implements InlineLevelLayoutManager, BreakOpportunity {
   protected MinOptMax extraBPD;
   private Area currentArea;
   protected LayoutContext childLC;

   protected InlineStackingLayoutManager(FObj node) {
      super(node);
      this.extraBPD = MinOptMax.ZERO;
   }

   public void setLMiter(ListIterator iter) {
      this.childLMiter = iter;
   }

   protected MinOptMax getExtraIPD(boolean bNotFirst, boolean bNotLast) {
      return MinOptMax.ZERO;
   }

   protected boolean hasLeadingFence(boolean bNotFirst) {
      return false;
   }

   protected boolean hasTrailingFence(boolean bNotLast) {
      return false;
   }

   protected SpaceProperty getSpaceStart() {
      return null;
   }

   protected SpaceProperty getSpaceEnd() {
      return null;
   }

   protected Area getCurrentArea() {
      return this.currentArea;
   }

   protected void setCurrentArea(Area area) {
      this.currentArea = area;
   }

   protected void setTraits(boolean bNotFirst, boolean bNotLast) {
   }

   protected void setChildContext(LayoutContext lc) {
      this.childLC = lc;
   }

   protected LayoutContext getContext() {
      return this.childLC;
   }

   protected void addSpace(Area parentArea, MinOptMax spaceRange, double spaceAdjust) {
      if (spaceRange != null) {
         int iAdjust = spaceRange.getOpt();
         if (spaceAdjust > 0.0) {
            iAdjust += (int)((double)spaceRange.getStretch() * spaceAdjust);
         } else if (spaceAdjust < 0.0) {
            iAdjust += (int)((double)spaceRange.getShrink() * spaceAdjust);
         }

         if (iAdjust != 0) {
            Space ls = new Space();
            ls.setChangeBarList(this.getChangeBarList());
            ls.setIPD(iAdjust);
            int level = parentArea.getBidiLevel();
            if (level >= 0) {
               ls.setBidiLevel(level);
            }

            parentArea.addChildArea(ls);
         }
      }

   }

   public List addALetterSpaceTo(List oldList) {
      return this.addALetterSpaceTo(oldList, 0);
   }

   public List addALetterSpaceTo(List oldList, int thisDepth) {
      ListIterator oldListIterator = oldList.listIterator(oldList.size());
      KnuthElement element = (KnuthElement)oldListIterator.previous();
      int depth = thisDepth + 1;
      Position pos = element.getPosition();
      InlineLevelLayoutManager lm = null;
      if (pos != null) {
         lm = (InlineLevelLayoutManager)pos.getLM(depth);
      }

      if (lm == null) {
         return oldList;
      } else {
         oldList = lm.addALetterSpaceTo(oldList, depth);
         oldListIterator = oldList.listIterator();

         while(oldListIterator.hasNext()) {
            element = (KnuthElement)oldListIterator.next();
            pos = element.getPosition();
            lm = null;
            if (pos != null) {
               lm = (InlineLevelLayoutManager)pos.getLM(thisDepth);
            }

            if (lm != this) {
               element.setPosition(this.notifyPos(new NonLeafPosition(this, element.getPosition())));
            }
         }

         return oldList;
      }
   }

   public String getWordChars(Position pos) {
      Position newPos = pos.getPosition();
      return ((InlineLevelLayoutManager)newPos.getLM()).getWordChars(newPos);
   }

   public void hyphenate(Position pos, HyphContext hc) {
      Position newPos = pos.getPosition();
      ((InlineLevelLayoutManager)newPos.getLM()).hyphenate(newPos, hc);
   }

   public boolean applyChanges(List oldList) {
      return this.applyChanges(oldList, 0);
   }

   public boolean applyChanges(List oldList, int depth) {
      ListIterator oldListIterator = oldList.listIterator();
      ++depth;
      InlineLevelLayoutManager prevLM = null;
      int fromIndex = 0;
      boolean bSomethingChanged = false;

      while(true) {
         while(true) {
            while(true) {
               InlineLevelLayoutManager currLM;
               do {
                  if (!oldListIterator.hasNext()) {
                     return bSomethingChanged;
                  }

                  KnuthElement oldElement = (KnuthElement)oldListIterator.next();
                  Position pos = oldElement.getPosition();
                  if (pos == null) {
                     currLM = null;
                  } else {
                     currLM = (InlineLevelLayoutManager)pos.getLM(depth);
                  }

                  if (prevLM == null) {
                     prevLM = currLM;
                  }
               } while(currLM == prevLM && oldListIterator.hasNext());

               if (prevLM != this && currLM != this) {
                  if (oldListIterator.hasNext()) {
                     bSomethingChanged = prevLM.applyChanges(oldList.subList(fromIndex, oldListIterator.previousIndex()), depth) || bSomethingChanged;
                     prevLM = currLM;
                     fromIndex = oldListIterator.previousIndex();
                  } else if (currLM == prevLM) {
                     bSomethingChanged = prevLM != null && prevLM.applyChanges(oldList.subList(fromIndex, oldList.size()), depth) || bSomethingChanged;
                  } else {
                     bSomethingChanged = prevLM.applyChanges(oldList.subList(fromIndex, oldListIterator.previousIndex()), depth) || bSomethingChanged;
                     if (currLM != null) {
                        bSomethingChanged = currLM.applyChanges(oldList.subList(oldListIterator.previousIndex(), oldList.size()), depth) || bSomethingChanged;
                     }
                  }
               } else {
                  prevLM = currLM;
               }
            }
         }
      }
   }

   public List getChangedKnuthElements(List oldList, int alignment) {
      return this.getChangedKnuthElements(oldList, alignment, 0);
   }

   public List getChangedKnuthElements(List oldList, int alignment, int depth) {
      ListIterator oldListIterator = oldList.listIterator();
      ++depth;
      LinkedList returnedList = new LinkedList();
      LinkedList returnList = new LinkedList();
      InlineLevelLayoutManager prevLM = null;
      int fromIndex = 0;

      while(oldListIterator.hasNext()) {
         KnuthElement oldElement = (KnuthElement)oldListIterator.next();
         Position pos = oldElement.getPosition();
         InlineLevelLayoutManager currLM;
         if (pos == null) {
            currLM = null;
         } else {
            currLM = (InlineLevelLayoutManager)pos.getLM(depth);
         }

         if (prevLM == null) {
            prevLM = currLM;
         }

         if (currLM != prevLM || !oldListIterator.hasNext()) {
            if (oldListIterator.hasNext()) {
               returnedList.addAll(prevLM.getChangedKnuthElements(oldList.subList(fromIndex, oldListIterator.previousIndex()), alignment, depth));
               prevLM = currLM;
               fromIndex = oldListIterator.previousIndex();
            } else if (currLM == prevLM) {
               returnedList.addAll(prevLM.getChangedKnuthElements(oldList.subList(fromIndex, oldList.size()), alignment, depth));
            } else {
               returnedList.addAll(prevLM.getChangedKnuthElements(oldList.subList(fromIndex, oldListIterator.previousIndex()), alignment, depth));
               if (currLM != null) {
                  returnedList.addAll(currLM.getChangedKnuthElements(oldList.subList(oldListIterator.previousIndex(), oldList.size()), alignment, depth));
               }
            }
         }
      }

      Iterator var14 = returnedList.iterator();

      while(var14.hasNext()) {
         Object aReturnedList = var14.next();
         KnuthElement returnedElement = (KnuthElement)aReturnedList;
         returnedElement.setPosition(this.notifyPos(new NonLeafPosition(this, returnedElement.getPosition())));
         returnList.add(returnedElement);
      }

      return returnList;
   }

   public int getBreakBefore() {
      return BreakOpportunityHelper.getBreakBefore(this);
   }
}
