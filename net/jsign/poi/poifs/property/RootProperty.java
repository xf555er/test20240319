package net.jsign.poi.poifs.property;

import net.jsign.commons.math3.util.ArithmeticUtils;

public final class RootProperty extends DirectoryProperty {
   RootProperty() {
      super("Root Entry");
      this.setNodeColor((byte)1);
      this.setPropertyType((byte)5);
      this.setStartBlock(-2);
   }

   RootProperty(int index, byte[] array, int offset) {
      super(index, array, offset);
   }

   public void setSize(int size) {
      int BLOCK_SHIFT = true;
      int _block_size = true;
      super.setSize(ArithmeticUtils.mulAndCheck(size, 64));
   }

   public String getName() {
      return "Root Entry";
   }
}
