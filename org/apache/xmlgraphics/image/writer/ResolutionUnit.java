package org.apache.xmlgraphics.image.writer;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public enum ResolutionUnit {
   NONE(1, "None"),
   INCH(2, "Inch"),
   CENTIMETER(3, "Centimeter");

   private static final Map LOOKUP = new HashMap();
   private final int value;
   private final String description;

   private ResolutionUnit(int value, String description) {
      this.value = value;
      this.description = description;
   }

   public int getValue() {
      return this.value;
   }

   public String getDescription() {
      return this.description;
   }

   public static ResolutionUnit get(int value) {
      return (ResolutionUnit)LOOKUP.get(value);
   }

   static {
      Iterator var0 = EnumSet.allOf(ResolutionUnit.class).iterator();

      while(var0.hasNext()) {
         ResolutionUnit unit = (ResolutionUnit)var0.next();
         LOOKUP.put(unit.getValue(), unit);
      }

   }
}
