package net.jsign.bouncycastle.pqc.crypto.rainbow;

import net.jsign.bouncycastle.pqc.crypto.rainbow.util.RainbowUtil;
import net.jsign.bouncycastle.util.Arrays;

public class Layer {
   private int vi;
   private int viNext;
   private int oi;
   private short[][][] coeff_alpha;
   private short[][][] coeff_beta;
   private short[][] coeff_gamma;
   private short[] coeff_eta;

   public Layer(byte var1, byte var2, short[][][] var3, short[][][] var4, short[][] var5, short[] var6) {
      this.vi = var1 & 255;
      this.viNext = var2 & 255;
      this.oi = this.viNext - this.vi;
      this.coeff_alpha = var3;
      this.coeff_beta = var4;
      this.coeff_gamma = var5;
      this.coeff_eta = var6;
   }

   public int getVi() {
      return this.vi;
   }

   public int getViNext() {
      return this.viNext;
   }

   public int getOi() {
      return this.oi;
   }

   public short[][][] getCoeffAlpha() {
      return this.coeff_alpha;
   }

   public short[][][] getCoeffBeta() {
      return this.coeff_beta;
   }

   public short[][] getCoeffGamma() {
      return this.coeff_gamma;
   }

   public short[] getCoeffEta() {
      return this.coeff_eta;
   }

   public boolean equals(Object var1) {
      if (var1 != null && var1 instanceof Layer) {
         Layer var2 = (Layer)var1;
         return this.vi == var2.getVi() && this.viNext == var2.getViNext() && this.oi == var2.getOi() && RainbowUtil.equals(this.coeff_alpha, var2.getCoeffAlpha()) && RainbowUtil.equals(this.coeff_beta, var2.getCoeffBeta()) && RainbowUtil.equals(this.coeff_gamma, var2.getCoeffGamma()) && RainbowUtil.equals(this.coeff_eta, var2.getCoeffEta());
      } else {
         return false;
      }
   }

   public int hashCode() {
      int var1 = this.vi;
      var1 = var1 * 37 + this.viNext;
      var1 = var1 * 37 + this.oi;
      var1 = var1 * 37 + Arrays.hashCode(this.coeff_alpha);
      var1 = var1 * 37 + Arrays.hashCode(this.coeff_beta);
      var1 = var1 * 37 + Arrays.hashCode(this.coeff_gamma);
      var1 = var1 * 37 + Arrays.hashCode(this.coeff_eta);
      return var1;
   }
}
