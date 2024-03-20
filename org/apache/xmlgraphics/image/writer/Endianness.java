package org.apache.xmlgraphics.image.writer;

public enum Endianness {
   DEFAULT,
   LITTLE_ENDIAN,
   BIG_ENDIAN;

   public static Endianness getEndianType(String value) {
      if (value != null) {
         Endianness[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            Endianness endianValue = var1[var3];
            if (endianValue.toString().equalsIgnoreCase(value)) {
               return endianValue;
            }
         }
      }

      return null;
   }
}
