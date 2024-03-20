package pe;

public class FunctionInfo {
   private String B;
   private int C;
   private int A = 0;

   public FunctionInfo(String var1, int var2) {
      this.B = var1;
      this.C = var2;
   }

   public String getName() {
      return this.B;
   }

   public int getEntryPoint() {
      return this.C;
   }
}
