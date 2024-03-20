package org.apache.fop.traits;

import org.apache.fop.fo.FONode;

public class WritingModeTraits implements WritingModeTraitsSetter {
   private Direction inlineProgressionDirection;
   private Direction blockProgressionDirection;
   private Direction columnProgressionDirection;
   private Direction rowProgressionDirection;
   private Direction shiftDirection;
   private WritingMode writingMode;
   private boolean explicit;

   public WritingModeTraits() {
      this(WritingMode.LR_TB, false);
   }

   public WritingModeTraits(WritingMode writingMode, boolean explicit) {
      this.assignWritingModeTraits(writingMode, explicit);
   }

   public Direction getInlineProgressionDirection() {
      return this.inlineProgressionDirection;
   }

   public void setInlineProgressionDirection(Direction direction) {
      this.inlineProgressionDirection = direction;
   }

   public Direction getBlockProgressionDirection() {
      return this.blockProgressionDirection;
   }

   public void setBlockProgressionDirection(Direction direction) {
      this.blockProgressionDirection = direction;
   }

   public Direction getColumnProgressionDirection() {
      return this.columnProgressionDirection;
   }

   public void setColumnProgressionDirection(Direction direction) {
      this.columnProgressionDirection = direction;
   }

   public Direction getRowProgressionDirection() {
      return this.rowProgressionDirection;
   }

   public void setRowProgressionDirection(Direction direction) {
      this.rowProgressionDirection = direction;
   }

   public Direction getShiftDirection() {
      return this.shiftDirection;
   }

   public void setShiftDirection(Direction direction) {
      this.shiftDirection = direction;
   }

   public WritingMode getWritingMode() {
      return this.writingMode;
   }

   public boolean getExplicitWritingMode() {
      return this.explicit;
   }

   public void setWritingMode(WritingMode writingMode, boolean explicit) {
      this.writingMode = writingMode;
      this.explicit = explicit;
   }

   public void assignWritingModeTraits(WritingMode writingMode, boolean explicit) {
      writingMode.assignWritingModeTraits(this, explicit);
   }

   public static WritingModeTraitsGetter getWritingModeTraitsGetter(FONode fn) {
      for(FONode n = fn; n != null; n = n.getParent()) {
         if (n instanceof WritingModeTraitsGetter) {
            return (WritingModeTraitsGetter)n;
         }
      }

      return null;
   }
}
