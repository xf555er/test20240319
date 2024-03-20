package org.apache.fop.traits;

import java.io.ObjectStreamException;

public final class WritingMode extends TraitEnum {
   private static final long serialVersionUID = 1L;
   private static final String[] WRITING_MODE_NAMES = new String[]{"lr-tb", "rl-tb", "tb-lr", "tb-rl"};
   private static final int[] WRITING_MODE_VALUES = new int[]{79, 121, 203, 140};
   public static final WritingMode LR_TB = new WritingMode(0);
   public static final WritingMode RL_TB = new WritingMode(1);
   public static final WritingMode TB_LR = new WritingMode(2);
   public static final WritingMode TB_RL = new WritingMode(3);
   private static final WritingMode[] WRITING_MODES;

   private WritingMode(int index) {
      super(WRITING_MODE_NAMES[index], WRITING_MODE_VALUES[index]);
   }

   public void assignWritingModeTraits(WritingModeTraitsSetter wms, boolean explicit) {
      Direction inlineProgressionDirection;
      Direction blockProgressionDirection;
      Direction columnProgressionDirection;
      Direction rowProgressionDirection;
      Direction shiftDirection;
      switch (this.getEnumValue()) {
         case 79:
         default:
            inlineProgressionDirection = Direction.LR;
            blockProgressionDirection = Direction.TB;
            columnProgressionDirection = Direction.LR;
            rowProgressionDirection = Direction.TB;
            shiftDirection = Direction.BT;
            break;
         case 121:
            inlineProgressionDirection = Direction.RL;
            blockProgressionDirection = Direction.TB;
            columnProgressionDirection = Direction.RL;
            rowProgressionDirection = Direction.TB;
            shiftDirection = Direction.BT;
            break;
         case 140:
            inlineProgressionDirection = Direction.TB;
            blockProgressionDirection = Direction.RL;
            columnProgressionDirection = Direction.TB;
            rowProgressionDirection = Direction.RL;
            shiftDirection = Direction.LR;
            break;
         case 203:
            inlineProgressionDirection = Direction.TB;
            blockProgressionDirection = Direction.LR;
            columnProgressionDirection = Direction.TB;
            rowProgressionDirection = Direction.LR;
            shiftDirection = Direction.RL;
      }

      wms.setInlineProgressionDirection(inlineProgressionDirection);
      wms.setBlockProgressionDirection(blockProgressionDirection);
      wms.setColumnProgressionDirection(columnProgressionDirection);
      wms.setRowProgressionDirection(rowProgressionDirection);
      wms.setShiftDirection(shiftDirection);
      wms.setWritingMode(this, explicit);
   }

   public boolean isHorizontal() {
      switch (this.getEnumValue()) {
         case 79:
         case 121:
            return true;
         case 140:
         case 203:
            return false;
         default:
            assert false;

            return true;
      }
   }

   public boolean isVertical() {
      return !this.isHorizontal();
   }

   public static WritingMode valueOf(String name) {
      WritingMode[] var1 = WRITING_MODES;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         WritingMode writingMode = var1[var3];
         if (writingMode.getName().equalsIgnoreCase(name)) {
            return writingMode;
         }
      }

      throw new IllegalArgumentException("Illegal writing mode: " + name);
   }

   public static WritingMode valueOf(int enumValue) {
      WritingMode[] var1 = WRITING_MODES;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         WritingMode writingMode = var1[var3];
         if (writingMode.getEnumValue() == enumValue) {
            return writingMode;
         }
      }

      throw new IllegalArgumentException("Illegal writing mode: " + enumValue);
   }

   private Object readResolve() throws ObjectStreamException {
      return valueOf(this.getName());
   }

   public String toString() {
      return this.getName();
   }

   static {
      WRITING_MODES = new WritingMode[]{LR_TB, RL_TB, TB_LR, TB_RL};
   }
}
