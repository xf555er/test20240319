package org.apache.fop.area.inline;

import java.util.ArrayList;
import java.util.List;
import org.apache.fop.area.Area;
import org.apache.fop.area.Block;

public class Container extends Area {
   private static final long serialVersionUID = 5256423939348189260L;
   protected List blocks = new ArrayList();
   protected int width;

   public void addChildArea(Area child) {
      if (!(child instanceof Block)) {
         throw new IllegalArgumentException("Container only accepts block areas");
      } else {
         this.blocks.add((Block)child);
      }
   }

   public List getBlocks() {
      return this.blocks;
   }

   public int getWidth() {
      return this.width;
   }
}
