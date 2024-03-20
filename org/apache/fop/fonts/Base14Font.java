package org.apache.fop.fonts;

public abstract class Base14Font extends Typeface {
   private static final int LINE_THICKNESS = 50;

   public int getStrikeoutPosition(int size) {
      return this.getXHeight(size) / 2;
   }

   public int getStrikeoutThickness(int size) {
      return size * 50;
   }
}
