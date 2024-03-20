package org.apache.fop.area.inline;

public class SpaceArea extends InlineArea {
   private static final long serialVersionUID = 2218803009825411416L;
   protected char space;
   protected boolean isAdjustable;

   public SpaceArea(int blockProgressionOffset, int bidiLevel, char space, boolean adjustable) {
      super(blockProgressionOffset, bidiLevel);
      this.space = space;
      this.isAdjustable = adjustable;
   }

   public String getSpace() {
      return String.valueOf(this.space);
   }

   public boolean isAdjustable() {
      return this.isAdjustable;
   }
}
