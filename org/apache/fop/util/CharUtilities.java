package org.apache.fop.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CharUtilities {
   public static final char CODE_EOT = '\u0000';
   public static final int UCWHITESPACE = 0;
   public static final int LINEFEED = 1;
   public static final int EOT = 2;
   public static final int NONWHITESPACE = 3;
   public static final int XMLWHITESPACE = 4;
   public static final char NULL_CHAR = '\u0000';
   public static final char LINEFEED_CHAR = '\n';
   public static final char CARRIAGE_RETURN = '\r';
   public static final char TAB = '\t';
   public static final char SPACE = ' ';
   public static final char NBSPACE = ' ';
   public static final char NEXT_LINE = '\u0085';
   public static final char ZERO_WIDTH_SPACE = '\u200b';
   public static final char WORD_JOINER = '\u2060';
   public static final char ZERO_WIDTH_JOINER = '\u200d';
   public static final char LRM = '\u200e';
   public static final char RLM = ' ';
   public static final char LRE = '\u202a';
   public static final char RLE = '\u202b';
   public static final char PDF = '\u202c';
   public static final char LRO = '\u202d';
   public static final char RLO = '\u202e';
   public static final char ZERO_WIDTH_NOBREAK_SPACE = '\ufeff';
   public static final char SOFT_HYPHEN = '\u00ad';
   public static final char LINE_SEPARATOR = '\u2028';
   public static final char PARAGRAPH_SEPARATOR = '\u2029';
   public static final char MISSING_IDEOGRAPH = '□';
   public static final char IDEOGRAPHIC_SPACE = '　';
   public static final char OBJECT_REPLACEMENT_CHARACTER = '￼';
   public static final char NOT_A_CHARACTER = '\uffff';

   protected CharUtilities() {
      throw new UnsupportedOperationException();
   }

   public static int classOf(int c) {
      switch (c) {
         case 0:
            return 2;
         case 9:
         case 13:
         case 32:
            return 4;
         case 10:
            return 1;
         default:
            return isAnySpace(c) ? 0 : 3;
      }
   }

   public static boolean isBreakableSpace(int c) {
      return c == 32 || isFixedWidthSpace(c);
   }

   public static boolean isZeroWidthSpace(int c) {
      return c == 8203 || c == 8288 || c == 65279;
   }

   public static boolean isFixedWidthSpace(int c) {
      return c >= 8192 && c <= 8203 || c == 12288;
   }

   public static boolean isNonBreakableSpace(int c) {
      return c == 160 || c == 8239 || c == 12288 || c == 8288 || c == 65279;
   }

   public static boolean isAdjustableSpace(int c) {
      return c == 32 || c == 160;
   }

   public static boolean isAnySpace(int c) {
      return isBreakableSpace(c) || isNonBreakableSpace(c);
   }

   public static boolean isAlphabetic(int c) {
      int generalCategory = Character.getType((char)c);
      switch (generalCategory) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 10:
            return true;
         case 6:
         case 7:
         case 8:
         case 9:
         default:
            return false;
      }
   }

   public static boolean isExplicitBreak(int c) {
      return c == 10 || c == 13 || c == 133 || c == 8232 || c == 8233;
   }

   public static String charToNCRef(int c) {
      StringBuffer sb = new StringBuffer();
      int i = 0;

      for(int nDigits = c > 65535 ? 6 : 4; i < nDigits; c >>= 4) {
         int d = c & 15;
         char hd;
         if (d < 10) {
            hd = (char)(48 + d);
         } else {
            hd = (char)(65 + (d - 10));
         }

         sb.append(hd);
         ++i;
      }

      return "&#x" + sb.reverse() + ";";
   }

   public static String toNCRefs(String s) {
      StringBuffer sb = new StringBuffer();
      if (s != null) {
         for(int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c >= ' ' && c < 127) {
               if (c == '<') {
                  sb.append("&lt;");
               } else if (c == '>') {
                  sb.append("&gt;");
               } else if (c == '&') {
                  sb.append("&amp;");
               } else {
                  sb.append(c);
               }
            } else {
               sb.append(charToNCRef(c));
            }
         }
      }

      return sb.toString();
   }

   public static String padLeft(String s, int width, char pad) {
      StringBuffer sb = new StringBuffer();

      for(int i = s.length(); i < width; ++i) {
         sb.append(pad);
      }

      sb.append(s);
      return sb.toString();
   }

   public static String format(int c) {
      return c < 1114112 ? "0x" + padLeft(Integer.toString(c, 16), c < 65536 ? 4 : 6, '0') : "!NOT A CHARACTER!";
   }

   public static boolean isSameSequence(CharSequence cs1, CharSequence cs2) {
      assert cs1 != null;

      assert cs2 != null;

      if (cs1.length() != cs2.length()) {
         return false;
      } else {
         int i = 0;

         for(int n = cs1.length(); i < n; ++i) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean isBmpCodePoint(int codePoint) {
      return codePoint >>> 16 == 0;
   }

   public static int incrementIfNonBMP(int codePoint) {
      return isBmpCodePoint(codePoint) ? 0 : 1;
   }

   public static boolean isSurrogatePair(char ch) {
      return Character.isHighSurrogate(ch) || Character.isLowSurrogate(ch);
   }

   public static boolean containsSurrogatePairAt(CharSequence chars, int index) {
      char ch = chars.charAt(index);
      if (Character.isHighSurrogate(ch)) {
         if (index + 1 > chars.length()) {
            throw new IllegalArgumentException("ill-formed UTF-16 sequence, contains isolated high surrogate at end of sequence");
         } else if (Character.isLowSurrogate(chars.charAt(index + 1))) {
            return true;
         } else {
            throw new IllegalArgumentException("ill-formed UTF-16 sequence, contains isolated high surrogate at index " + index);
         }
      } else if (Character.isLowSurrogate(ch)) {
         throw new IllegalArgumentException("ill-formed UTF-16 sequence, contains isolated low surrogate at index " + index);
      } else {
         return false;
      }
   }

   public static Iterable codepointsIter(CharSequence s) {
      return codepointsIter(s, 0, s.length());
   }

   public static Iterable codepointsIter(final CharSequence s, final int beginIndex, final int endIndex) {
      if (beginIndex < 0) {
         throw new StringIndexOutOfBoundsException(beginIndex);
      } else if (endIndex > s.length()) {
         throw new StringIndexOutOfBoundsException(endIndex);
      } else {
         int subLen = endIndex - beginIndex;
         if (subLen < 0) {
            throw new StringIndexOutOfBoundsException(subLen);
         } else {
            return new Iterable() {
               public Iterator iterator() {
                  return new Iterator() {
                     int nextIndex = beginIndex;

                     public boolean hasNext() {
                        return this.nextIndex < endIndex;
                     }

                     public Integer next() {
                        if (!this.hasNext()) {
                           throw new NoSuchElementException();
                        } else {
                           int result = Character.codePointAt(s, this.nextIndex);
                           this.nextIndex += Character.charCount(result);
                           return result;
                        }
                     }

                     public void remove() {
                        throw new UnsupportedOperationException();
                     }
                  };
               }
            };
         }
      }
   }
}
