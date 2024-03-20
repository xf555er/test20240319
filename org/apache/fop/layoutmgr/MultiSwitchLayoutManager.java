package org.apache.fop.layoutmgr;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.fop.area.Area;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.MultiSwitch;

public class MultiSwitchLayoutManager extends BlockStackingLayoutManager {
   private KnuthElementsGenerator knuthGen;

   public MultiSwitchLayoutManager(FObj node) {
      super(node);
      MultiSwitch multiSwitchNode = (MultiSwitch)node;
      if (multiSwitchNode.getAutoToggle() == 205) {
         this.knuthGen = new WhitespaceManagement();
      } else {
         this.knuthGen = new DefaultKnuthListGenerator();
      }

   }

   public List getNextKnuthElements(LayoutContext context, int alignment) {
      this.referenceIPD = context.getRefIPD();
      List knuthList = this.knuthGen.getKnuthElements(context, alignment);
      this.setFinished(true);
      return knuthList;
   }

   public Area getParentArea(Area childArea) {
      return this.parentLayoutManager.getParentArea(childArea);
   }

   public void addChildArea(Area childArea) {
      this.parentLayoutManager.addChildArea(childArea);
   }

   public void addAreas(PositionIterator posIter, LayoutContext context) {
      LinkedList positionList = new LinkedList();

      while(posIter.hasNext()) {
         Position pos = posIter.next();
         if (pos instanceof WhitespaceManagementPosition) {
            positionList.addAll(((WhitespaceManagementPosition)pos).getPositionList());
         } else {
            positionList.add(pos);
         }
      }

      PositionIterator newPosIter = new PositionIterator(positionList.listIterator());
      AreaAdditionUtil.addAreas(this, newPosIter, context);
      this.flush();
   }

   private class WhitespaceManagement implements KnuthElementsGenerator {
      private WhitespaceManagement() {
      }

      public List getKnuthElements(LayoutContext context, int alignment) {
         MultiSwitchLayoutManager mslm = MultiSwitchLayoutManager.this;
         List knuthList = new LinkedList();
         WhitespaceManagementPenalty penalty = new WhitespaceManagementPenalty(new WhitespaceManagementPosition(mslm));

         LayoutManager childLM;
         while((childLM = MultiSwitchLayoutManager.this.getChildLM()) != null) {
            LayoutContext childLC = MultiSwitchLayoutManager.this.makeChildLayoutContext(context);
            List childElements = new LinkedList();

            while(!childLM.isFinished()) {
               childElements.addAll(childLM.getNextKnuthElements(childLC, alignment));
            }

            List wrappedElements = new LinkedList();
            MultiSwitchLayoutManager.this.wrapPositionElements(childElements, wrappedElements);
            SpaceResolver.resolveElementList(wrappedElements);
            int contentLength = ElementListUtils.calcContentLength(wrappedElements);
            penalty.addVariant(penalty.new Variant(wrappedElements, contentLength));
         }

         knuthList.add(new KnuthBox(0, new Position(mslm), false));
         knuthList.add(penalty);
         knuthList.add(new KnuthBox(0, new Position(mslm), false));
         return knuthList;
      }

      // $FF: synthetic method
      WhitespaceManagement(Object x1) {
         this();
      }
   }

   private class DefaultKnuthListGenerator implements KnuthElementsGenerator {
      private DefaultKnuthListGenerator() {
      }

      public List getKnuthElements(LayoutContext context, int alignment) {
         List knuthList = new LinkedList();
         LayoutManager childLM = MultiSwitchLayoutManager.this.getChildLM();

         while(!childLM.isFinished()) {
            LayoutContext childLC = MultiSwitchLayoutManager.this.makeChildLayoutContext(context);
            List childElements = childLM.getNextKnuthElements(childLC, alignment);
            if (childElements != null) {
               List newList = new LinkedList();
               MultiSwitchLayoutManager.this.wrapPositionElements(childElements, newList);
               knuthList.addAll(newList);
            }
         }

         return knuthList;
      }

      // $FF: synthetic method
      DefaultKnuthListGenerator(Object x1) {
         this();
      }
   }

   private interface KnuthElementsGenerator {
      List getKnuthElements(LayoutContext var1, int var2);
   }

   static class WhitespaceManagementPosition extends Position {
      private List knuthList;

      public WhitespaceManagementPosition(LayoutManager lm) {
         super(lm);
      }

      public List getPositionList() {
         List positions = new LinkedList();
         if (this.knuthList != null && !this.knuthList.isEmpty()) {
            SpaceResolver.performConditionalsNotification(this.knuthList, 0, this.knuthList.size() - 1, -1);
            Iterator var2 = this.knuthList.iterator();

            while(var2.hasNext()) {
               ListElement el = (ListElement)var2.next();
               if (el.getPosition() != null) {
                  positions.add(el.getPosition());
               }
            }
         }

         return positions;
      }

      public void setKnuthList(List knuthList) {
         this.knuthList = knuthList;
      }

      public List getKnuthList() {
         return this.knuthList;
      }
   }
}
