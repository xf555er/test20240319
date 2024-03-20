package org.apache.fop.area.inline;

import org.apache.fop.area.Block;

public class InlineBlock extends InlineParent {
   private static final long serialVersionUID = -3725062353292109517L;
   private final Block block;

   public InlineBlock(Block block) {
      this.block = block;
   }

   public Block getBlock() {
      return this.block;
   }
}
