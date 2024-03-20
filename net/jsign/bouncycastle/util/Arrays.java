package net.jsign.bouncycastle.util;

import java.util.NoSuchElementException;

public final class Arrays {
   public static boolean areEqual(byte[] var0, byte[] var1) {
      return java.util.Arrays.equals(var0, var1);
   }

   public static boolean areEqual(char[] var0, char[] var1) {
      return java.util.Arrays.equals(var0, var1);
   }

   public static boolean areEqual(int[] var0, int[] var1) {
      return java.util.Arrays.equals(var0, var1);
   }

   public static boolean areEqual(short[] var0, short[] var1) {
      return java.util.Arrays.equals(var0, var1);
   }

   public static boolean constantTimeAreEqual(byte[] var0, byte[] var1) {
      if (var0 != null && var1 != null) {
         if (var0 == var1) {
            return true;
         } else {
            int var2 = var0.length < var1.length ? var0.length : var1.length;
            int var3 = var0.length ^ var1.length;

            int var4;
            for(var4 = 0; var4 != var2; ++var4) {
               var3 |= var0[var4] ^ var1[var4];
            }

            for(var4 = var2; var4 < var1.length; ++var4) {
               var3 |= var1[var4] ^ ~var1[var4];
            }

            return var3 == 0;
         }
      } else {
         return false;
      }
   }

   public static void fill(byte[] var0, byte var1) {
      java.util.Arrays.fill(var0, var1);
   }

   public static int hashCode(byte[] var0) {
      if (var0 == null) {
         return 0;
      } else {
         int var1 = var0.length;
         int var2 = var1 + 1;

         while(true) {
            --var1;
            if (var1 < 0) {
               return var2;
            }

            var2 *= 257;
            var2 ^= var0[var1];
         }
      }
   }

   public static int hashCode(byte[] var0, int var1, int var2) {
      if (var0 == null) {
         return 0;
      } else {
         int var3 = var2;
         int var4 = var2 + 1;

         while(true) {
            --var3;
            if (var3 < 0) {
               return var4;
            }

            var4 *= 257;
            var4 ^= var0[var1 + var3];
         }
      }
   }

   public static int hashCode(char[] var0) {
      if (var0 == null) {
         return 0;
      } else {
         int var1 = var0.length;
         int var2 = var1 + 1;

         while(true) {
            --var1;
            if (var1 < 0) {
               return var2;
            }

            var2 *= 257;
            var2 ^= var0[var1];
         }
      }
   }

   public static int hashCode(int[] var0) {
      if (var0 == null) {
         return 0;
      } else {
         int var1 = var0.length;
         int var2 = var1 + 1;

         while(true) {
            --var1;
            if (var1 < 0) {
               return var2;
            }

            var2 *= 257;
            var2 ^= var0[var1];
         }
      }
   }

   public static int hashCode(short[][][] var0) {
      int var1 = 0;

      for(int var2 = 0; var2 != var0.length; ++var2) {
         var1 = var1 * 257 + hashCode(var0[var2]);
      }

      return var1;
   }

   public static int hashCode(short[][] var0) {
      int var1 = 0;

      for(int var2 = 0; var2 != var0.length; ++var2) {
         var1 = var1 * 257 + hashCode(var0[var2]);
      }

      return var1;
   }

   public static int hashCode(short[] var0) {
      if (var0 == null) {
         return 0;
      } else {
         int var1 = var0.length;
         int var2 = var1 + 1;

         while(true) {
            --var1;
            if (var1 < 0) {
               return var2;
            }

            var2 *= 257;
            var2 ^= var0[var1] & 255;
         }
      }
   }

   public static byte[] clone(byte[] var0) {
      return null == var0 ? null : (byte[])var0.clone();
   }

   public static int[] clone(int[] var0) {
      return null == var0 ? null : (int[])var0.clone();
   }

   public static long[] clone(long[] var0) {
      return null == var0 ? null : (long[])var0.clone();
   }

   public static short[] clone(short[] var0) {
      return null == var0 ? null : (short[])var0.clone();
   }

   public static byte[] copyOfRange(byte[] var0, int var1, int var2) {
      int var3 = getLength(var1, var2);
      byte[] var4 = new byte[var3];
      System.arraycopy(var0, var1, var4, 0, Math.min(var0.length - var1, var3));
      return var4;
   }

   private static int getLength(int var0, int var1) {
      int var2 = var1 - var0;
      if (var2 < 0) {
         StringBuffer var3 = new StringBuffer(var0);
         var3.append(" > ").append(var1);
         throw new IllegalArgumentException(var3.toString());
      } else {
         return var2;
      }
   }

   public static byte[] concatenate(byte[] var0, byte[] var1) {
      if (null == var0) {
         return clone(var1);
      } else if (null == var1) {
         return clone(var0);
      } else {
         byte[] var2 = new byte[var0.length + var1.length];
         System.arraycopy(var0, 0, var2, 0, var0.length);
         System.arraycopy(var1, 0, var2, var0.length, var1.length);
         return var2;
      }
   }

   public static boolean isNullOrContainsNull(Object[] var0) {
      if (null == var0) {
         return true;
      } else {
         int var1 = var0.length;

         for(int var2 = 0; var2 < var1; ++var2) {
            if (null == var0[var2]) {
               return true;
            }
         }

         return false;
      }
   }

   public static class Iterator implements java.util.Iterator {
      private final Object[] dataArray;
      private int position = 0;

      public Iterator(Object[] var1) {
         this.dataArray = var1;
      }

      public boolean hasNext() {
         return this.position < this.dataArray.length;
      }

      public Object next() {
         if (this.position == this.dataArray.length) {
            throw new NoSuchElementException("Out of elements: " + this.position);
         } else {
            return this.dataArray[this.position++];
         }
      }

      public void remove() {
         throw new UnsupportedOperationException("Cannot remove element from an Array.");
      }
   }
}
