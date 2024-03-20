package org.apache.fop.area.inline;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;

public class InlineBlockParent extends InlineArea {
   private static final long serialVersionUID = -3661746143321407377L;
   protected Block child;

   public void addChildArea(Area childArea) {
      if (this.child != null) {
         throw new IllegalStateException("InlineBlockParent may have only one child area.");
      } else if (childArea instanceof Block) {
         this.child = (Block)childArea;
         this.setIPD(childArea.getAllocIPD());
         this.setBPD(childArea.getAllocBPD());
      } else {
         throw new IllegalArgumentException("The child of an InlineBlockParent must be a Block area");
      }
   }

   public Block getChildArea() {
      return this.child;
   }
}
