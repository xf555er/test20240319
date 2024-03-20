package net.jsign;

class StdOutConsole implements Console {
   private final int level;

   public StdOutConsole(int level) {
      this.level = level;
   }

   public void debug(String message) {
      if (this.level >= 2) {
         System.out.println(message);
      }

   }

   public void info(String message) {
      if (this.level >= 1) {
         System.out.println(message);
      }

   }
}
