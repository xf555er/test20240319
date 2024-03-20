package org.apache.fop.render.afp;

import java.io.ObjectStreamException;

public enum AFPShadingMode {
   COLOR("COLOR"),
   DITHERED("DITHERED");

   private String name;

   private AFPShadingMode(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public static AFPShadingMode getValueOf(String name) {
      if (name != null && !"".equals(name) && !COLOR.getName().equalsIgnoreCase(name)) {
         if (DITHERED.getName().equalsIgnoreCase(name)) {
            return DITHERED;
         } else {
            throw new IllegalArgumentException("Illegal value for enumeration: " + name);
         }
      } else {
         return COLOR;
      }
   }

   private Object readResolve() throws ObjectStreamException {
      return valueOf(this.getName());
   }

   public String toString() {
      return this.getClass().getName() + ":" + this.name;
   }
}
