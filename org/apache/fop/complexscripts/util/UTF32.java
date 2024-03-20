package org.apache.fop.complexscripts.util;

import org.apache.fop.util.CharUtilities;

public final class UTF32 {
   private UTF32() {
   }

   public static Integer[] toUTF32(String s, int substitution, boolean errorOnSubstitution) throws IllegalArgumentException {
      int n;
      if ((n = s.length()) == 0) {
         return new Integer[0];
      } else {
         Integer[] sa = new Integer[n];
         int k = 0;

         for(int i = 0; i < n; ++i) {
            int c = s.charAt(i);
            if (c >= 55296 && c < 57344) {
               int s2 = i + 1 < n ? s.charAt(i + 1) : 0;
               if (c < 56320) {
                  if (s2 >= '\udc00' && s2 < '\ue000') {
                     c = (c - '\ud800' << 10) + (s2 - '\udc00') + 65536;
                     ++i;
                  } else {
                     if (errorOnSubstitution) {
                        throw new IllegalArgumentException("isolated high (leading) surrogate");
                     }

                     c = substitution;
                  }
               } else {
                  if (errorOnSubstitution) {
                     throw new IllegalArgumentException("isolated low (trailing) surrogate");
                  }

                  c = substitution;
               }
            }

            sa[k++] = c;
         }

         if (k == n) {
            return sa;
         } else {
            Integer[] na = new Integer[k];
            System.arraycopy(sa, 0, na, 0, k);
            return na;
         }
      }
   }

   public static String fromUTF32(Integer[] sa) throws IllegalArgumentException {
      StringBuffer sb = new StringBuffer();
      Integer[] var2 = sa;
      int var3 = sa.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         int s = var2[var4];
         String ncr;
         if (s < 65535) {
            if (s >= 55296 && s <= 57343) {
               ncr = CharUtilities.charToNCRef(s);
               throw new IllegalArgumentException("illegal scalar value 0x" + ncr.substring(2, ncr.length() - 1) + "; cannot be UTF-16 surrogate");
            }

            sb.append((char)s);
         } else {
            if (s >= 1114112) {
               ncr = CharUtilities.charToNCRef(s);
               throw new IllegalArgumentException("illegal scalar value 0x" + ncr.substring(2, ncr.length() - 1) + "; out of range for UTF-16");
            }

            int s1 = (s - 65536 >> 10 & 1023) + '\ud800';
            int s2 = (s - 65536 >> 0 & 1023) + '\udc00';
            sb.append((char)s1);
            sb.append((char)s2);
         }
      }

      return sb.toString();
   }
}
