package org.apache.fop.layoutmgr.inline;

import java.util.LinkedList;
import java.util.List;
import org.apache.fop.fo.flow.Float;
import org.apache.fop.layoutmgr.FloatContentLayoutManager;
import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthPossPosIter;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;
import org.apache.fop.layoutmgr.SpaceResolver;

public class FloatLayoutManager extends InlineStackingLayoutManager {
   private FloatContentLayoutManager floatContentLM;
   private KnuthInlineBox anchor;
   private List floatContentKnuthElements;
   private Float floatContent;
   private boolean floatContentAreaAdded;

   public FloatLayoutManager(Float node) {
      super(node);
      this.floatContent = node;
   }

   protected LayoutManager getChildLM() {
      return null;
   }

   public LinkedList getNextKnuthElements(LayoutContext context, int alignment) {
      if (!this.floatContentAreaAdded && !this.floatContent.isDisabled()) {
         this.floatContentLM = new FloatContentLayoutManager(this.floatContent);
         this.floatContentLM.setParent(this);
         this.floatContentLM.initialize();
         this.floatContentKnuthElements = this.floatContentLM.getNextKnuthElements(context, alignment);
         SpaceResolver.resolveElementList(this.floatContentKnuthElements);
      }

      LinkedList knuthElements = new LinkedList();
      KnuthSequence seq = new InlineKnuthSequence();
      this.anchor = new KnuthInlineBox(0, (AlignmentContext)null, (Position)null, true);
      if (!this.floatContentAreaAdded) {
         this.anchor.setFloatContentLM(this.floatContentLM);
      }

      this.anchor.setPosition(this.notifyPos(new Position(this)));
      seq.add(this.anchor);
      knuthElements.add(seq);
      this.setFinished(true);
      return knuthElements;
   }

   public void addAreas(PositionIterator posIter, LayoutContext context) {
      while(posIter.hasNext()) {
         posIter.next();
      }

   }

   public void processAreas(LayoutContext context) {
      PositionIterator contentPosIter = new KnuthPossPosIter(this.floatContentKnuthElements, 0, this.floatContentKnuthElements.size());
      this.floatContentLM.addAreas(contentPosIter, context);
      this.floatContentAreaAdded = true;
      this.anchor.setFloatContentLM((FloatContentLayoutManager)null);
   }
}
