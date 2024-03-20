package org.apache.fop.traits;

import java.io.ObjectStreamException;

public final class Visibility extends TraitEnum {
   private static final long serialVersionUID = 1L;
   private static final String[] VISIBILITY_NAMES = new String[]{"visible", "hidden", "collapse"};
   private static final int[] VISIBILITY_VALUES = new int[]{159, 57, 26};
   public static final Visibility VISIBLE = new Visibility(0);
   public static final Visibility HIDDEN = new Visibility(1);
   public static final Visibility COLLAPSE = new Visibility(2);
   private static final Visibility[] VISIBILITIES;

   private Visibility(int index) {
      super(VISIBILITY_NAMES[index], VISIBILITY_VALUES[index]);
   }

   public static Visibility valueOf(String name) {
      Visibility[] var1 = VISIBILITIES;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Visibility v = var1[var3];
         if (v.getName().equalsIgnoreCase(name)) {
            return v;
         }
      }

      throw new IllegalArgumentException("Illegal visibility value: " + name);
   }

   private Object readResolve() throws ObjectStreamException {
      return valueOf(this.getName());
   }

   public String toString() {
      return this.getName();
   }

   static {
      VISIBILITIES = new Visibility[]{VISIBLE, HIDDEN, COLLAPSE};
   }
}
