package org.apache.fop.traits;

public interface WritingModeTraitsGetter {
   Direction getInlineProgressionDirection();

   Direction getBlockProgressionDirection();

   Direction getColumnProgressionDirection();

   Direction getRowProgressionDirection();

   Direction getShiftDirection();

   WritingMode getWritingMode();

   boolean getExplicitWritingMode();
}
