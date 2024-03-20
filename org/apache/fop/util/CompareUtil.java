package org.apache.fop.util;

public final class CompareUtil {
   private static final Object TIE_LOCK = new Object();

   private CompareUtil() {
   }

   public static boolean equal(Object o1, Object o2) {
      int o1Hash = System.identityHashCode(o1);
      int o2Hash = System.identityHashCode(o2);
      if (o1Hash == o2Hash && o1 != o2 && o1Hash != 0) {
         synchronized(TIE_LOCK) {
            return o1.equals(o2);
         }
      } else {
         if (o1Hash > o2Hash) {
            Object tmp = o1;
            o1 = o2;
            o2 = tmp;
         }

         return o1 == null ? o2 == null : o1 == o2 || o1.equals(o2);
      }
   }

   public static int getHashCode(Object object) {
      return object == null ? 0 : object.hashCode();
   }

   public static boolean equal(double n1, double n2) {
      return Double.doubleToLongBits(n1) == Double.doubleToLongBits(n2);
   }

   public static int getHashCode(double number) {
      long bits = Double.doubleToLongBits(number);
      return (int)(bits ^ bits >>> 32);
   }
}
