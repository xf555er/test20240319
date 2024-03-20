package org.apache.fop.fonts;

public enum CIDFontType {
   CIDTYPE0("CIDFontType0", 0),
   CIDTYPE2("CIDFontType2", 2);

   private final String name;
   private final int value;

   private CIDFontType(String name, int value) {
      this.name = name;
      this.value = value;
   }

   public static CIDFontType byName(String name) {
      if (name.equalsIgnoreCase(CIDTYPE0.getName())) {
         return CIDTYPE0;
      } else if (name.equalsIgnoreCase(CIDTYPE2.getName())) {
         return CIDTYPE2;
      } else {
         throw new IllegalArgumentException("Invalid CID font type: " + name);
      }
   }

   public static CIDFontType byValue(int value) {
      if (value == CIDTYPE0.getValue()) {
         return CIDTYPE0;
      } else if (value == CIDTYPE2.getValue()) {
         return CIDTYPE2;
      } else {
         throw new IllegalArgumentException("Invalid CID font type: " + value);
      }
   }

   public String getName() {
      return this.name;
   }

   public int getValue() {
      return this.value;
   }
}
