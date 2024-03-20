package net.jsign.bouncycastle.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import net.jsign.bouncycastle.math.raw.Mod;
import net.jsign.bouncycastle.math.raw.Nat;

public final class BigIntegers {
   public static final BigInteger ZERO = BigInteger.valueOf(0L);
   public static final BigInteger ONE = BigInteger.valueOf(1L);
   public static final BigInteger TWO = BigInteger.valueOf(2L);
   private static final BigInteger THREE = BigInteger.valueOf(3L);
   private static final BigInteger SMALL_PRIMES_PRODUCT = new BigInteger("8138e8a0fcf3a4e84a771d40fd305d7f4aa59306d7251de54d98af8fe95729a1f73d893fa424cd2edc8636a6c3285e022b0e3866a565ae8108eed8591cd4fe8d2ce86165a978d719ebf647f362d33fca29cd179fb42401cbaf3df0c614056f9c8f3cfd51e474afb6bc6974f78db8aba8e9e517fded658591ab7502bd41849462f", 16);
   private static final int MAX_SMALL = BigInteger.valueOf(743L).bitLength();

   public static BigInteger modOddInverse(BigInteger var0, BigInteger var1) {
      if (!var0.testBit(0)) {
         throw new IllegalArgumentException("'M' must be odd");
      } else if (var0.signum() != 1) {
         throw new ArithmeticException("BigInteger: modulus not positive");
      } else {
         if (var1.signum() < 0 || var1.compareTo(var0) >= 0) {
            var1 = var1.mod(var0);
         }

         int var2 = var0.bitLength();
         int[] var3 = Nat.fromBigInteger(var2, var0);
         int[] var4 = Nat.fromBigInteger(var2, var1);
         int var5 = var3.length;
         int[] var6 = Nat.create(var5);
         if (0 == Mod.modOddInverse(var3, var4, var6)) {
            throw new ArithmeticException("BigInteger not invertible.");
         } else {
            return Nat.toBigInteger(var5, var6);
         }
      }
   }

   public static BigInteger createRandomBigInteger(int var0, SecureRandom var1) {
      return new BigInteger(1, createRandom(var0, var1));
   }

   private static byte[] createRandom(int var0, SecureRandom var1) throws IllegalArgumentException {
      if (var0 < 1) {
         throw new IllegalArgumentException("bitLength must be at least 1");
      } else {
         int var2 = (var0 + 7) / 8;
         byte[] var3 = new byte[var2];
         var1.nextBytes(var3);
         int var4 = 8 * var2 - var0;
         var3[0] &= (byte)(255 >>> var4);
         return var3;
      }
   }
}
