package net.jsign.bouncycastle.crypto.params;

import net.jsign.bouncycastle.util.Arrays;

public class DHValidationParameters {
   private byte[] seed;
   private int counter;

   public DHValidationParameters(byte[] var1, int var2) {
      this.seed = Arrays.clone(var1);
      this.counter = var2;
   }

   public boolean equals(Object var1) {
      if (!(var1 instanceof DHValidationParameters)) {
         return false;
      } else {
         DHValidationParameters var2 = (DHValidationParameters)var1;
         return var2.counter != this.counter ? false : Arrays.areEqual(this.seed, var2.seed);
      }
   }

   public int hashCode() {
      return this.counter ^ Arrays.hashCode(this.seed);
   }
}
