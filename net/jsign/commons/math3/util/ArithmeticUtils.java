package net.jsign.commons.math3.util;

import net.jsign.commons.math3.exception.MathArithmeticException;

public final class ArithmeticUtils {
   public static int mulAndCheck(int x, int y) throws MathArithmeticException {
      long m = (long)x * (long)y;
      if (m >= -2147483648L && m <= 2147483647L) {
         return (int)m;
      } else {
         throw new MathArithmeticException();
      }
   }

   public static long mulAndCheck(long a, long b) throws MathArithmeticException {
      long ret;
      if (a > b) {
         ret = mulAndCheck(b, a);
      } else if (a < 0L) {
         if (b < 0L) {
            if (a < Long.MAX_VALUE / b) {
               throw new MathArithmeticException();
            }

            ret = a * b;
         } else if (b > 0L) {
            if (Long.MIN_VALUE / b > a) {
               throw new MathArithmeticException();
            }

            ret = a * b;
         } else {
            ret = 0L;
         }
      } else if (a > 0L) {
         if (a > Long.MAX_VALUE / b) {
            throw new MathArithmeticException();
         }

         ret = a * b;
      } else {
         ret = 0L;
      }

      return ret;
   }
}
