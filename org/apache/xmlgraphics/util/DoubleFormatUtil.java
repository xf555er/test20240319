package org.apache.xmlgraphics.util;

public final class DoubleFormatUtil {
   private static final long[] POWERS_OF_TEN_LONG = new long[19];
   private static final double[] POWERS_OF_TEN_DOUBLE = new double[30];

   private DoubleFormatUtil() {
   }

   public static void formatDouble(double source, int decimals, int precision, StringBuffer target) {
      int scale = Math.abs(source) >= 1.0 ? decimals : precision;
      if (!tooManyDigitsUsed(source, scale) && !tooCloseToRound(source, scale)) {
         formatDoubleFast(source, decimals, precision, target);
      } else {
         formatDoublePrecise(source, decimals, precision, target);
      }

   }

   public static void formatDoublePrecise(double source, int decimals, int precision, StringBuffer target) {
      if (isRoundedToZero(source, decimals, precision)) {
         target.append('0');
      } else if (!Double.isNaN(source) && !Double.isInfinite(source)) {
         boolean negative = source < 0.0;
         if (negative) {
            source = -source;
            target.append('-');
         }

         int scale = source >= 1.0 ? decimals : precision;
         String s = Double.toString(source);
         int dot;
         int exposant;
         if (source >= 0.001 && source < 1.0E7) {
            dot = s.indexOf(46);
            String decS = s.substring(dot + 1);
            exposant = decS.length();
            if (scale >= exposant) {
               if ("0".equals(decS)) {
                  target.append(s.substring(0, dot));
               } else {
                  target.append(s);

                  for(int l = target.length() - 1; l >= 0 && target.charAt(l) == '0'; --l) {
                     target.setLength(l);
                  }
               }

               return;
            }

            if (scale + 1 < exposant) {
               exposant = scale + 1;
               decS = decS.substring(0, exposant);
            }

            long intP = Long.parseLong(s.substring(0, dot));
            long decP = Long.parseLong(decS);
            format(target, scale, intP, decP);
         } else {
            dot = s.indexOf(46);

            assert dot >= 0;

            int exp = s.indexOf(69);

            assert exp >= 0;

            exposant = Integer.parseInt(s.substring(exp + 1));
            String intS = s.substring(0, dot);
            String decS = s.substring(dot + 1, exp);
            int decLength = decS.length();
            int digits;
            long decP;
            long decP;
            if (exposant >= 0) {
               digits = decLength - exposant;
               if (digits <= 0) {
                  target.append(intS);
                  target.append(decS);

                  for(int i = -digits; i > 0; --i) {
                     target.append('0');
                  }
               } else if (digits <= scale) {
                  target.append(intS);
                  target.append(decS.substring(0, exposant));
                  target.append('.');
                  target.append(decS.substring(exposant));
               } else {
                  decP = Long.parseLong(intS) * tenPow(exposant) + Long.parseLong(decS.substring(0, exposant));
                  decP = Long.parseLong(decS.substring(exposant, exposant + scale + 1));
                  format(target, scale, decP, decP);
               }
            } else {
               exposant = -exposant;
               digits = scale - exposant + 1;
               if (digits < 0) {
                  target.append('0');
               } else if (digits == 0) {
                  decP = Long.parseLong(intS);
                  format(target, scale, 0L, decP);
               } else if (decLength < digits) {
                  decP = Long.parseLong(intS) * tenPow(decLength + 1) + Long.parseLong(decS) * 10L;
                  format(target, exposant + decLength, 0L, decP);
               } else {
                  decP = Long.parseLong(decS.substring(0, digits));
                  decP = Long.parseLong(intS) * tenPow(digits) + decP;
                  format(target, scale, 0L, decP);
               }
            }
         }

      } else {
         target.append(Double.toString(source));
      }
   }

   private static boolean isRoundedToZero(double source, int decimals, int precision) {
      return source == 0.0 || Math.abs(source) < 4.999999999999999 / tenPowDouble(Math.max(decimals, precision) + 1);
   }

   public static long tenPow(int n) {
      assert n >= 0;

      return n < POWERS_OF_TEN_LONG.length ? POWERS_OF_TEN_LONG[n] : (long)Math.pow(10.0, (double)n);
   }

   private static double tenPowDouble(int n) {
      assert n >= 0;

      return n < POWERS_OF_TEN_DOUBLE.length ? POWERS_OF_TEN_DOUBLE[n] : Math.pow(10.0, (double)n);
   }

   private static void format(StringBuffer target, int scale, long intP, long decP) {
      if (decP != 0L) {
         decP += 5L;
         decP /= 10L;
         if ((double)decP >= tenPowDouble(scale)) {
            ++intP;
            decP -= tenPow(scale);
         }

         if (decP != 0L) {
            while(decP % 10L == 0L) {
               decP /= 10L;
               --scale;
            }
         }
      }

      target.append(intP);
      if (decP != 0L) {
         target.append('.');

         for(; scale > 0; target.append('0')) {
            if (scale > 18) {
               double var10000 = (double)decP;
               --scale;
               if (!(var10000 < tenPowDouble(scale))) {
                  break;
               }
            } else {
               --scale;
               if (decP >= tenPow(scale)) {
                  break;
               }
            }
         }

         target.append(decP);
      }

   }

   public static void formatDoubleFast(double source, int decimals, int precision, StringBuffer target) {
      if (isRoundedToZero(source, decimals, precision)) {
         target.append('0');
      } else if (!Double.isNaN(source) && !Double.isInfinite(source)) {
         boolean isPositive = source >= 0.0;
         source = Math.abs(source);
         int scale = source >= 1.0 ? decimals : precision;
         long intPart = (long)Math.floor(source);
         double tenScale = tenPowDouble(scale);
         double fracUnroundedPart = (source - (double)intPart) * tenScale;
         long fracPart = Math.round(fracUnroundedPart);
         if ((double)fracPart >= tenScale) {
            ++intPart;
            fracPart = Math.round((double)fracPart - tenScale);
         }

         if (fracPart != 0L) {
            while(fracPart % 10L == 0L) {
               fracPart /= 10L;
               --scale;
            }
         }

         if (intPart == 0L && fracPart == 0L) {
            target.append('0');
         } else {
            if (!isPositive) {
               target.append('-');
            }

            target.append(intPart);
            if (fracPart != 0L) {
               target.append('.');

               while(scale > 0) {
                  double var10000 = (double)fracPart;
                  --scale;
                  if (!(var10000 < tenPowDouble(scale))) {
                     break;
                  }

                  target.append('0');
               }

               target.append(fracPart);
            }
         }

      } else {
         target.append(Double.toString(source));
      }
   }

   public static int getExponant(double value) {
      long exp = Double.doubleToRawLongBits(value) & 9218868437227405312L;
      exp >>= 52;
      return (int)(exp - 1023L);
   }

   private static boolean tooManyDigitsUsed(double source, int scale) {
      double decExp = Math.log10(source);
      return scale >= 308 || decExp + (double)scale >= 14.5;
   }

   private static boolean tooCloseToRound(double source, int scale) {
      source = Math.abs(source);
      long intPart = (long)Math.floor(source);
      double fracPart = (source - (double)intPart) * tenPowDouble(scale);
      double decExp = Math.log10(source);
      double range = decExp + (double)scale >= 12.0 ? 0.1 : 0.001;
      double distanceToRound1 = Math.abs(fracPart - Math.floor(fracPart));
      double distanceToRound2 = Math.abs(fracPart - Math.floor(fracPart) - 0.5);
      return distanceToRound1 <= range || distanceToRound2 <= range;
   }

   static {
      POWERS_OF_TEN_LONG[0] = 1L;

      int i;
      for(i = 1; i < POWERS_OF_TEN_LONG.length; ++i) {
         POWERS_OF_TEN_LONG[i] = POWERS_OF_TEN_LONG[i - 1] * 10L;
      }

      for(i = 0; i < POWERS_OF_TEN_DOUBLE.length; ++i) {
         POWERS_OF_TEN_DOUBLE[i] = Double.parseDouble("1e" + i);
      }

   }
}
