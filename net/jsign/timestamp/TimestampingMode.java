package net.jsign.timestamp;

public enum TimestampingMode {
   AUTHENTICODE,
   RFC3161;

   public static TimestampingMode of(String mode) {
      TimestampingMode[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         TimestampingMode m = var1[var3];
         if (m.name().equalsIgnoreCase(mode)) {
            return m;
         }
      }

      if ("tsp".equalsIgnoreCase(mode)) {
         return RFC3161;
      } else {
         throw new IllegalArgumentException("Unknown timestamping mode: " + mode);
      }
   }
}
