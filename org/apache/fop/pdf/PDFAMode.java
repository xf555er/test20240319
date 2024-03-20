package org.apache.fop.pdf;

public enum PDFAMode {
   DISABLED("PDF/A disabled"),
   PDFA_1A(1, 'A'),
   PDFA_1B(1, 'B'),
   PDFA_2A(2, 'A'),
   PDFA_2B(2, 'B'),
   PDFA_2U(2, 'U'),
   PDFA_3A(3, 'A'),
   PDFA_3B(3, 'B'),
   PDFA_3U(3, 'U');

   private final String name;
   private final int part;
   private final char level;

   private PDFAMode(String name) {
      this.name = name;
      this.part = 0;
      this.level = 0;
   }

   private PDFAMode(int part, char level) {
      this.name = "PDF/A-" + part + Character.toLowerCase(level);
      this.part = part;
      this.level = level;
   }

   public String getName() {
      return this.name;
   }

   public boolean isEnabled() {
      return this != DISABLED;
   }

   public int getPart() {
      return this.part;
   }

   public boolean isPart1() {
      return this.part == 1;
   }

   public boolean isPart2() {
      return this.part == 1 || this.part == 2;
   }

   public char getConformanceLevel() {
      return this.level;
   }

   public boolean isLevelA() {
      return this.level == 'A';
   }

   public static PDFAMode getValueOf(String s) {
      PDFAMode[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         PDFAMode mode = var1[var3];
         if (mode.name.equalsIgnoreCase(s)) {
            return mode;
         }
      }

      return DISABLED;
   }

   public String toString() {
      return this.name;
   }
}
