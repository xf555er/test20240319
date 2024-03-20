package org.apache.fop.traits;

public interface WritingModeTraitsSetter extends WritingModeTraitsGetter {
   void setInlineProgressionDirection(Direction var1);

   void setBlockProgressionDirection(Direction var1);

   void setColumnProgressionDirection(Direction var1);

   void setRowProgressionDirection(Direction var1);

   void setShiftDirection(Direction var1);

   void setWritingMode(WritingMode var1, boolean var2);

   void assignWritingModeTraits(WritingMode var1, boolean var2);
}
