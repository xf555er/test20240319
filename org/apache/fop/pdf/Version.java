package org.apache.fop.pdf;

public enum Version {
   V1_0("1.0"),
   V1_1("1.1"),
   V1_2("1.2"),
   V1_3("1.3"),
   V1_4("1.4"),
   V1_5("1.5"),
   V1_6("1.6"),
   V1_7("1.7");

   private String version;

   private Version(String version) {
      this.version = version;
   }

   public static Version getValueOf(String version) {
      Version[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Version pdfVersion = var1[var3];
         if (pdfVersion.toString().equals(version)) {
            return pdfVersion;
         }
      }

      throw new IllegalArgumentException("Invalid PDF version given: " + version);
   }

   public String toString() {
      return this.version;
   }
}
