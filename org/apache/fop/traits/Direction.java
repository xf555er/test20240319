package org.apache.fop.traits;

import java.io.ObjectStreamException;

public final class Direction extends TraitEnum {
   private static final long serialVersionUID = 1L;
   private static final String[] DIRECTION_NAMES = new String[]{"lr", "rl", "tb", "bt"};
   private static final int[] DIRECTION_VALUES = new int[]{199, 200, 201, 202};
   public static final Direction LR = new Direction(0);
   public static final Direction RL = new Direction(1);
   public static final Direction TB = new Direction(2);
   public static final Direction BT = new Direction(3);
   private static final Direction[] DIRECTIONS;

   private Direction(int index) {
      super(DIRECTION_NAMES[index], DIRECTION_VALUES[index]);
   }

   public boolean isVertical() {
      return this.getEnumValue() == 201 || this.getEnumValue() == 202;
   }

   public boolean isHorizontal() {
      return this.getEnumValue() == 199 || this.getEnumValue() == 200;
   }

   public static Direction valueOf(String name) {
      Direction[] var1 = DIRECTIONS;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Direction direction = var1[var3];
         if (direction.getName().equalsIgnoreCase(name)) {
            return direction;
         }
      }

      throw new IllegalArgumentException("Illegal direction: " + name);
   }

   public static Direction valueOf(int enumValue) {
      Direction[] var1 = DIRECTIONS;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Direction direction = var1[var3];
         if (direction.getEnumValue() == enumValue) {
            return direction;
         }
      }

      throw new IllegalArgumentException("Illegal direction: " + enumValue);
   }

   private Object readResolve() throws ObjectStreamException {
      return valueOf(this.getName());
   }

   public String toString() {
      return this.getName();
   }

   static {
      DIRECTIONS = new Direction[]{LR, RL, TB, BT};
   }
}
