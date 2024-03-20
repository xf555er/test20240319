package net.jsign.bouncycastle.pqc.math.linearalgebra;

import net.jsign.bouncycastle.util.Arrays;

public class GF2Matrix extends Matrix {
   private int[][] matrix;
   private int length;

   public GF2Matrix(byte[] var1) {
      if (var1.length < 9) {
         throw new ArithmeticException("given array is not an encoded matrix over GF(2)");
      } else {
         this.numRows = LittleEndianConversions.OS2IP(var1, 0);
         this.numColumns = LittleEndianConversions.OS2IP(var1, 4);
         int var2 = (this.numColumns + 7 >>> 3) * this.numRows;
         if (this.numRows > 0 && var2 == var1.length - 8) {
            this.length = this.numColumns + 31 >>> 5;
            this.matrix = new int[this.numRows][this.length];
            int var3 = this.numColumns >> 5;
            int var4 = this.numColumns & 31;
            int var5 = 8;

            for(int var6 = 0; var6 < this.numRows; ++var6) {
               int var7;
               for(var7 = 0; var7 < var3; var5 += 4) {
                  this.matrix[var6][var7] = LittleEndianConversions.OS2IP(var1, var5);
                  ++var7;
               }

               for(var7 = 0; var7 < var4; var7 += 8) {
                  int[] var10000 = this.matrix[var6];
                  var10000[var3] ^= (var1[var5++] & 255) << var7;
               }
            }

         } else {
            throw new ArithmeticException("given array is not an encoded matrix over GF(2)");
         }
      }
   }

   public GF2Matrix(int var1, int[][] var2) {
      if (var2[0].length != var1 + 31 >> 5) {
         throw new ArithmeticException("Int array does not match given number of columns.");
      } else {
         this.numColumns = var1;
         this.numRows = var2.length;
         this.length = var2[0].length;
         int var3 = var1 & 31;
         int var4;
         if (var3 == 0) {
            var4 = -1;
         } else {
            var4 = (1 << var3) - 1;
         }

         for(int var5 = 0; var5 < this.numRows; ++var5) {
            int[] var10000 = var2[var5];
            int var10001 = this.length - 1;
            var10000[var10001] &= var4;
         }

         this.matrix = var2;
      }
   }

   public GF2Matrix(GF2Matrix var1) {
      this.numColumns = var1.getNumColumns();
      this.numRows = var1.getNumRows();
      this.length = var1.length;
      this.matrix = new int[var1.matrix.length][];

      for(int var2 = 0; var2 < this.matrix.length; ++var2) {
         this.matrix[var2] = IntUtils.clone(var1.matrix[var2]);
      }

   }

   public byte[] getEncoded() {
      int var1 = this.numColumns + 7 >>> 3;
      var1 *= this.numRows;
      var1 += 8;
      byte[] var2 = new byte[var1];
      LittleEndianConversions.I2OSP(this.numRows, var2, 0);
      LittleEndianConversions.I2OSP(this.numColumns, var2, 4);
      int var3 = this.numColumns >>> 5;
      int var4 = this.numColumns & 31;
      int var5 = 8;

      for(int var6 = 0; var6 < this.numRows; ++var6) {
         int var7;
         for(var7 = 0; var7 < var3; var5 += 4) {
            LittleEndianConversions.I2OSP(this.matrix[var6][var7], var2, var5);
            ++var7;
         }

         for(var7 = 0; var7 < var4; var7 += 8) {
            var2[var5++] = (byte)(this.matrix[var6][var3] >>> var7 & 255);
         }
      }

      return var2;
   }

   public boolean equals(Object var1) {
      if (!(var1 instanceof GF2Matrix)) {
         return false;
      } else {
         GF2Matrix var2 = (GF2Matrix)var1;
         if (this.numRows == var2.numRows && this.numColumns == var2.numColumns && this.length == var2.length) {
            for(int var3 = 0; var3 < this.numRows; ++var3) {
               if (!IntUtils.equals(this.matrix[var3], var2.matrix[var3])) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public int hashCode() {
      int var1 = (this.numRows * 31 + this.numColumns) * 31 + this.length;

      for(int var2 = 0; var2 < this.numRows; ++var2) {
         var1 = var1 * 31 + Arrays.hashCode(this.matrix[var2]);
      }

      return var1;
   }

   public String toString() {
      int var1 = this.numColumns & 31;
      int var2;
      if (var1 == 0) {
         var2 = this.length;
      } else {
         var2 = this.length - 1;
      }

      StringBuffer var3 = new StringBuffer();

      for(int var4 = 0; var4 < this.numRows; ++var4) {
         var3.append(var4 + ": ");

         int var5;
         int var6;
         int var7;
         for(var5 = 0; var5 < var2; ++var5) {
            var6 = this.matrix[var4][var5];

            for(var7 = 0; var7 < 32; ++var7) {
               int var8 = var6 >>> var7 & 1;
               if (var8 == 0) {
                  var3.append('0');
               } else {
                  var3.append('1');
               }
            }

            var3.append(' ');
         }

         var5 = this.matrix[var4][this.length - 1];

         for(var6 = 0; var6 < var1; ++var6) {
            var7 = var5 >>> var6 & 1;
            if (var7 == 0) {
               var3.append('0');
            } else {
               var3.append('1');
            }
         }

         var3.append('\n');
      }

      return var3.toString();
   }
}
