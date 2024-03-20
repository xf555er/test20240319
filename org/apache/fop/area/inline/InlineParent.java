package org.apache.fop.area.inline;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.area.Area;

public class InlineParent extends InlineArea {
   private static final long serialVersionUID = -3047168298770354813L;
   protected List inlines = new ArrayList();
   protected transient boolean autoSize;
   protected int minChildOffset;
   private int maxAfterEdge;

   private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
      ois.defaultReadObject();
   }

   public void addChildArea(Area c) {
      assert c instanceof InlineArea;

      if (this.inlines.size() == 0) {
         this.autoSize = this.getIPD() == 0;
      }

      InlineArea childArea = (InlineArea)c;
      this.inlines.add(childArea);
      childArea.setParentArea(this);
      if (this.autoSize) {
         this.increaseIPD(childArea.getAllocIPD());
      }

      this.updateLevel(childArea.getBidiLevel());
      int childOffset = childArea.getVirtualOffset();
      this.minChildOffset = Math.min(this.minChildOffset, childOffset);
      this.maxAfterEdge = Math.max(this.maxAfterEdge, childOffset + childArea.getVirtualBPD());
   }

   int getVirtualOffset() {
      return this.getBlockProgressionOffset() + this.minChildOffset;
   }

   int getVirtualBPD() {
      return this.maxAfterEdge - this.minChildOffset;
   }

   public List getChildAreas() {
      return this.inlines;
   }

   public boolean applyVariationFactor(double variationFactor, int lineStretch, int lineShrink) {
      boolean hasUnresolvedAreas = false;
      int cumulativeIPD = 0;

      InlineArea inline;
      for(Iterator var7 = this.inlines.iterator(); var7.hasNext(); cumulativeIPD += inline.getIPD()) {
         inline = (InlineArea)var7.next();
         hasUnresolvedAreas |= inline.applyVariationFactor(variationFactor, lineStretch, lineShrink);
      }

      this.setIPD(cumulativeIPD);
      return hasUnresolvedAreas;
   }

   public List collectInlineRuns(List runs) {
      InlineArea ia;
      for(Iterator var2 = this.getChildAreas().iterator(); var2.hasNext(); runs = ia.collectInlineRuns(runs)) {
         ia = (InlineArea)var2.next();
      }

      return runs;
   }

   public void resetChildrenLevel() {
      Iterator var1 = this.inlines.iterator();

      while(var1.hasNext()) {
         InlineArea inline = (InlineArea)var1.next();
         inline.resetBidiLevel();
      }

   }

   private void updateLevel(int newLevel) {
      if (newLevel >= 0) {
         int curLevel = this.getBidiLevel();
         if (curLevel >= 0) {
            if (newLevel < curLevel) {
               this.setBidiLevel(newLevel);
            }
         } else {
            this.setBidiLevel(newLevel);
         }
      }

   }
}
