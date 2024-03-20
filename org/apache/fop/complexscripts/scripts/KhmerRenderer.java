package org.apache.fop.complexscripts.scripts;

public class KhmerRenderer {
   private static final int XX = 0;
   private static final int CC_COENG = 7;
   private static final int CC_CONSONANT = 1;
   private static final int CC_CONSONANT_SHIFTER = 5;
   private static final int CC_CONSONANT2 = 2;
   private static final int CC_CONSONANT3 = 3;
   private static final int CC_DEPENDENT_VOWEL = 8;
   private static final int CC_ROBAT = 6;
   private static final int CC_SIGN_ABOVE = 9;
   private static final int CC_SIGN_AFTER = 10;
   private static final int CF_ABOVE_VOWEL = 536870912;
   private static final int CF_CLASS_MASK = 65535;
   private static final int CF_COENG = 134217728;
   private static final int CF_CONSONANT = 16777216;
   private static final int CF_DOTTED_CIRCLE = 67108864;
   private static final int CF_POS_ABOVE = 131072;
   private static final int CF_POS_AFTER = 65536;
   private static final int CF_POS_BEFORE = 524288;
   private static final int CF_POS_BELOW = 262144;
   private static final int CF_SHIFTER = 268435456;
   private static final int CF_SPLIT_VOWEL = 33554432;
   private static final int C1 = 16777217;
   private static final int C2 = 16777218;
   private static final int C3 = 16777219;
   private static final int CO = 201326599;
   private static final int CS = 335544325;
   private static final int DA = 604110856;
   private static final int DB = 67371016;
   private static final int DL = 67633160;
   private static final int DR = 67174408;
   private static final int RB = 67239942;
   private static final int SA = 67239945;
   private static final int SP = 67174410;
   private static final int VA = 637665288;
   private static final int VR = 100728840;
   private static final char BA = 'ប';
   private static final char COENG = '្';
   private static final String CONYO = Character.toString('្').concat(Character.toString('ញ'));
   private static final String CORO = Character.toString('្').concat(Character.toString('រ'));
   private int[] khmerCharClasses = new int[]{16777217, 16777217, 16777217, 16777219, 16777217, 16777217, 16777217, 16777217, 16777219, 16777217, 16777217, 16777217, 16777217, 16777219, 16777217, 16777217, 16777217, 16777217, 16777217, 16777217, 16777219, 16777217, 16777217, 16777217, 16777217, 16777219, 16777218, 16777217, 16777217, 16777217, 16777219, 16777219, 16777217, 16777219, 16777217, 16777217, 16777217, 16777217, 16777217, 16777217, 16777217, 16777217, 16777217, 16777217, 16777217, 16777217, 16777217, 16777217, 16777217, 16777217, 16777217, 16777217, 67174408, 67174408, 67174408, 604110856, 604110856, 604110856, 604110856, 67371016, 67371016, 67371016, 637665288, 100728840, 100728840, 67633160, 67633160, 67633160, 100728840, 100728840, 67239945, 67174410, 67174410, 335544325, 335544325, 67239945, 67239942, 67239945, 67239945, 67239945, 67239945, 67239945, 201326599, 67239945, 0, 0, 0, 0, 0, 0, 0, 0, 0, 67239945, 0, 0};
   private short[][] khmerStateTable = new short[][]{{1, 2, 2, 2, 1, 1, 1, 6, 1, 1, 1, 2}, {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, {-1, -1, -1, -1, 3, 4, 5, 6, 16, 17, 1, -1}, {-1, -1, -1, -1, -1, 4, -1, -1, 16, -1, -1, -1}, {-1, -1, -1, -1, 15, -1, -1, 6, 16, 17, 1, 14}, {-1, -1, -1, -1, -1, -1, -1, -1, 20, -1, 1, -1}, {-1, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, -1}, {-1, -1, -1, -1, 12, 13, -1, 10, 16, 17, 1, 14}, {-1, -1, -1, -1, 12, 13, -1, -1, 16, 17, 1, 14}, {-1, -1, -1, -1, 12, 13, -1, 10, 16, 17, 1, 14}, {-1, 11, 11, 11, -1, -1, -1, -1, -1, -1, -1, -1}, {-1, -1, -1, -1, 15, -1, -1, -1, 16, 17, 1, 14}, {-1, -1, -1, -1, -1, 13, -1, -1, 16, -1, -1, -1}, {-1, -1, -1, -1, 15, -1, -1, -1, 16, 17, 1, 14}, {-1, -1, -1, -1, -1, -1, -1, -1, 16, -1, -1, -1}, {-1, -1, -1, -1, -1, -1, -1, -1, 16, -1, -1, -1}, {-1, -1, -1, -1, -1, -1, -1, -1, -1, 17, 1, 18}, {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 18}, {-1, -1, -1, -1, -1, -1, -1, 19, -1, -1, -1, -1}, {-1, 1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1}, {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1}};
   private static final char MARK = '\u17ea';
   private static final char NYO = 'ញ';
   private static final char SA_C = 'ស';
   private static final char SRAAA = 'ា';
   private static final char SRAAU = 'ៅ';
   private static final char SRAE = 'េ';
   private static final char SRAIE = 'ៀ';
   private static final char SRAII = 'ី';
   private static final char SRAOE = 'ើ';
   private static final char SRAOO = 'ោ';
   private static final char SRAU = 'ុ';
   private static final char SRAYA = 'ឿ';
   private static final char TRIISAP = '៊';
   private static final char YO = 'យ';

   private char strEcombining(char chrInput) {
      char retChar = ' ';
      if (chrInput == 6078) {
         retChar = 6072;
      } else if (chrInput == 6079) {
         retChar = 6079;
      } else if (chrInput == 6080) {
         retChar = 6080;
      } else if (chrInput == 6084) {
         retChar = 6070;
      } else if (chrInput == 6085) {
         retChar = 6085;
      }

      return retChar;
   }

   private int getCharClass(char uniChar) {
      int retValue = 0;
      if (uniChar > 255 && uniChar >= 6016) {
         int ch = uniChar - 6016;
         if (ch < this.khmerCharClasses.length) {
            retValue = this.khmerCharClasses[ch];
         }
      }

      return retValue;
   }

   public String render(String strInput) {
      int cursor = 0;
      short state = 0;
      int charCount = strInput.length();

      StringBuilder result;
      for(result = new StringBuilder(); cursor < charCount; state = 0) {
         String reserved = "";
         String signAbove = "";
         String signAfter = "";
         String base = "";
         String robat = "";
         String shifter = "";
         String vowelBefore = "";
         String vowelBelow = "";
         String vowelAbove = "";
         String vowelAfter = "";
         boolean coeng = false;
         String coeng1 = "";
         String coeng2 = "";

         boolean shifterAfterCoeng;
         for(shifterAfterCoeng = false; cursor < charCount; ++cursor) {
            char curChar = strInput.charAt(cursor);
            int kChar = this.getCharClass(curChar);
            int charClass = kChar & '\uffff';

            try {
               state = this.khmerStateTable[state][charClass];
            } catch (Exception var28) {
               state = -1;
            }

            if (state < 0) {
               break;
            }

            if (kChar == 0) {
               reserved = Character.toString(curChar);
            } else if (kChar == 67239945) {
               signAbove = Character.toString(curChar);
            } else if (kChar == 67174410) {
               signAfter = Character.toString(curChar);
            } else if (kChar != 16777217 && kChar != 16777218 && kChar != 16777219) {
               if (kChar == 67239942) {
                  robat = Character.toString(curChar);
               } else if (kChar == 335544325) {
                  if (!"".equalsIgnoreCase(coeng1)) {
                     shifterAfterCoeng = true;
                  }

                  shifter = Character.toString(curChar);
               } else if (kChar == 67633160) {
                  vowelBefore = Character.toString(curChar);
               } else if (kChar == 67371016) {
                  vowelBelow = Character.toString(curChar);
               } else if (kChar == 604110856) {
                  vowelAbove = Character.toString(curChar);
               } else if (kChar == 67174408) {
                  vowelAfter = Character.toString(curChar);
               } else if (kChar == 201326599) {
                  coeng = true;
               } else if (kChar == 637665288) {
                  vowelBefore = Character.toString('េ');
                  vowelAbove = Character.toString(this.strEcombining(curChar));
               } else if (kChar == 100728840) {
                  vowelBefore = Character.toString('េ');
                  vowelAfter = Character.toString(this.strEcombining(curChar));
               }
            } else if (coeng) {
               if ("".equalsIgnoreCase(coeng1)) {
                  coeng1 = Character.toString('្').concat(Character.toString(curChar));
               } else {
                  coeng2 = Character.toString('្').concat(Character.toString(curChar));
               }

               coeng = false;
            } else {
               base = Character.toString(curChar);
            }
         }

         String coengBefore = "";
         if (CORO.equalsIgnoreCase(coeng1)) {
            coengBefore = coeng1;
            coeng1 = "";
         } else if (CORO.equalsIgnoreCase(coeng2)) {
            coengBefore = coeng2;
            coeng2 = "";
         }

         if (!"".equalsIgnoreCase(base) && !"".equalsIgnoreCase(shifter) && !"".equalsIgnoreCase(vowelAbove)) {
            shifter = "";
            vowelBelow = Character.toString('ុ');
         }

         if (coeng && "".equalsIgnoreCase(coeng1)) {
            coeng1 = Character.toString('្');
         } else if (coeng && "".equalsIgnoreCase(coeng2)) {
            coeng2 = Character.toString('\u17ea').concat(Character.toString('្'));
         }

         String shifter1 = "";
         String shifter2 = "";
         if (shifterAfterCoeng) {
            shifter2 = shifter;
         } else {
            shifter1 = shifter;
         }

         boolean specialCaseBA = false;
         String strMARKSRAAA = Character.toString('\u17ea').concat(Character.toString('ា'));
         String strMARKSRAAU = Character.toString('\u17ea').concat(Character.toString('ៅ'));
         if (Character.toString('ប').equalsIgnoreCase(base) && (Character.toString('ា').equalsIgnoreCase(vowelAfter) || Character.toString('ៅ').equalsIgnoreCase(vowelAfter) || strMARKSRAAA.equalsIgnoreCase(vowelAfter) || strMARKSRAAU.equalsIgnoreCase(vowelAfter))) {
            specialCaseBA = true;
            if (!"".equalsIgnoreCase(coeng1)) {
               String coeng1Complete = coeng1.substring(0, coeng1.length() - 1);
               if (Character.toString('ប').equalsIgnoreCase(coeng1Complete) || Character.toString('យ').equalsIgnoreCase(coeng1Complete) || Character.toString('ស').equalsIgnoreCase(coeng1Complete)) {
                  specialCaseBA = false;
               }
            }
         }

         String cluster;
         if (specialCaseBA) {
            cluster = vowelBefore + coengBefore + base + vowelAfter + robat + shifter1 + coeng1 + coeng2 + shifter2 + vowelBelow + vowelAbove + signAbove + signAfter;
         } else {
            cluster = vowelBefore + coengBefore + base + robat + shifter1 + coeng1 + coeng2 + shifter2 + vowelBelow + vowelAbove + vowelAfter + signAbove + signAfter;
         }

         result.append(cluster + reserved);
      }

      return result.toString();
   }
}
