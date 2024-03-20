package org.apache.batik.gvt.text;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Map;

public final class ArabicTextHandler {
   private static final int arabicStart = 1536;
   private static final int arabicEnd = 1791;
   private static final AttributedCharacterIterator.Attribute ARABIC_FORM;
   private static final Integer ARABIC_NONE;
   private static final Integer ARABIC_ISOLATED;
   private static final Integer ARABIC_TERMINAL;
   private static final Integer ARABIC_INITIAL;
   private static final Integer ARABIC_MEDIAL;
   static int singleCharFirst;
   static int singleCharLast;
   static int[][] singleCharRemappings;
   static int doubleCharFirst;
   static int doubleCharLast;
   static int[][][] doubleCharRemappings;

   private ArabicTextHandler() {
   }

   public static AttributedString assignArabicForms(AttributedString as) {
      if (!containsArabic(as)) {
         return as;
      } else {
         AttributedCharacterIterator aci = as.getIterator();
         int numChars = aci.getEndIndex() - aci.getBeginIndex();
         int[] charOrder = null;
         int runStart;
         int idx;
         int end;
         int i;
         int start;
         if (numChars >= 3) {
            runStart = aci.first();
            idx = aci.next();
            end = 1;

            for(i = aci.next(); i != 65535; ++end) {
               if (arabicCharTransparent((char)idx) && hasSubstitute((char)runStart, (char)i)) {
                  if (charOrder == null) {
                     charOrder = new int[numChars];

                     for(start = 0; start < numChars; ++start) {
                        charOrder[start] = start + aci.getBeginIndex();
                     }
                  }

                  start = charOrder[end];
                  charOrder[end] = charOrder[end - 1];
                  charOrder[end - 1] = start;
               }

               runStart = idx;
               idx = i;
               i = aci.next();
            }
         }

         if (charOrder != null) {
            StringBuffer reorderedString = new StringBuffer(numChars);

            for(end = 0; end < numChars; ++end) {
               idx = aci.setIndex(charOrder[end]);
               reorderedString.append((char)idx);
            }

            AttributedString reorderedAS = new AttributedString(reorderedString.toString());

            for(i = 0; i < numChars; ++i) {
               aci.setIndex(charOrder[i]);
               Map attributes = aci.getAttributes();
               reorderedAS.addAttributes(attributes, i, i + 1);
            }

            if (charOrder[0] != aci.getBeginIndex()) {
               aci.setIndex(charOrder[0]);
               Float x = (Float)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.X);
               Float y = (Float)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.Y);
               if (x != null && !x.isNaN()) {
                  reorderedAS.addAttribute(GVTAttributedCharacterIterator.TextAttribute.X, Float.NaN, charOrder[0], charOrder[0] + 1);
                  reorderedAS.addAttribute(GVTAttributedCharacterIterator.TextAttribute.X, x, 0, 1);
               }

               if (y != null && !y.isNaN()) {
                  reorderedAS.addAttribute(GVTAttributedCharacterIterator.TextAttribute.Y, Float.NaN, charOrder[0], charOrder[0] + 1);
                  reorderedAS.addAttribute(GVTAttributedCharacterIterator.TextAttribute.Y, y, 0, 1);
               }
            }

            as = reorderedAS;
         }

         aci = as.getIterator();
         runStart = -1;
         idx = aci.getBeginIndex();

         for(int c = aci.first(); c != '\uffff'; ++idx) {
            if (c >= 1536 && c <= 1791) {
               if (runStart == -1) {
                  runStart = idx;
               }
            } else if (runStart != -1) {
               as.addAttribute(ARABIC_FORM, ARABIC_NONE, runStart, idx);
               runStart = -1;
            }

            c = aci.next();
         }

         if (runStart != -1) {
            as.addAttribute(ARABIC_FORM, ARABIC_NONE, runStart, idx);
         }

         aci = as.getIterator();
         end = aci.getBeginIndex();
         Integer currentForm = ARABIC_NONE;

         while(true) {
            char currentChar;
            do {
               if (aci.setIndex(end) == '\uffff') {
                  return as;
               }

               start = aci.getRunStart(ARABIC_FORM);
               end = aci.getRunLimit(ARABIC_FORM);
               currentChar = aci.setIndex(start);
               currentForm = (Integer)aci.getAttribute(ARABIC_FORM);
            } while(currentForm == null);

            int currentIndex = start;

            for(int prevCharIndex = start - 1; currentIndex < end; prevCharIndex = currentIndex++) {
               char prevChar = currentChar;

               for(currentChar = aci.setIndex(currentIndex); arabicCharTransparent(currentChar) && currentIndex < end; currentChar = aci.setIndex(currentIndex)) {
                  ++currentIndex;
               }

               if (currentIndex >= end) {
                  break;
               }

               Integer prevForm = currentForm;
               currentForm = ARABIC_NONE;
               if (prevCharIndex >= start) {
                  if (arabicCharShapesRight(prevChar) && arabicCharShapesLeft(currentChar)) {
                     prevForm = prevForm + 1;
                     as.addAttribute(ARABIC_FORM, prevForm, prevCharIndex, prevCharIndex + 1);
                     currentForm = ARABIC_INITIAL;
                  } else if (arabicCharShaped(currentChar)) {
                     currentForm = ARABIC_ISOLATED;
                  }
               } else if (arabicCharShaped(currentChar)) {
                  currentForm = ARABIC_ISOLATED;
               }

               if (currentForm != ARABIC_NONE) {
                  as.addAttribute(ARABIC_FORM, currentForm, currentIndex, currentIndex + 1);
               }
            }
         }
      }
   }

   public static boolean arabicChar(char c) {
      return c >= 1536 && c <= 1791;
   }

   public static boolean containsArabic(AttributedString as) {
      return containsArabic(as.getIterator());
   }

   public static boolean containsArabic(AttributedCharacterIterator aci) {
      for(char c = aci.first(); c != '\uffff'; c = aci.next()) {
         if (arabicChar(c)) {
            return true;
         }
      }

      return false;
   }

   public static boolean arabicCharTransparent(char c) {
      if (c >= 1611 && c <= 1773) {
         return c <= 1621 || c == 1648 || c >= 1750 && c <= 1764 || c >= 1767 && c <= 1768 || c >= 1770;
      } else {
         return false;
      }
   }

   private static boolean arabicCharShapesRight(char c) {
      return c >= 1570 && c <= 1573 || c == 1575 || c == 1577 || c >= 1583 && c <= 1586 || c == 1608 || c >= 1649 && c <= 1651 || c >= 1653 && c <= 1655 || c >= 1672 && c <= 1689 || c == 1728 || c >= 1730 && c <= 1739 || c == 1741 || c == 1743 || c >= 1746 && c <= 1747 || arabicCharShapesDuel(c);
   }

   private static boolean arabicCharShapesDuel(char c) {
      return c == 1574 || c == 1576 || c >= 1578 && c <= 1582 || c >= 1587 && c <= 1594 || c >= 1601 && c <= 1607 || c >= 1609 && c <= 1610 || c >= 1656 && c <= 1671 || c >= 1690 && c <= 1727 || c == 1729 || c == 1740 || c == 1742 || c >= 1744 && c <= 1745 || c >= 1786 && c <= 1788;
   }

   private static boolean arabicCharShapesLeft(char c) {
      return arabicCharShapesDuel(c);
   }

   private static boolean arabicCharShaped(char c) {
      return arabicCharShapesRight(c);
   }

   public static boolean hasSubstitute(char ch1, char ch2) {
      if (ch1 >= doubleCharFirst && ch1 <= doubleCharLast) {
         int[][] remaps = doubleCharRemappings[ch1 - doubleCharFirst];
         if (remaps == null) {
            return false;
         } else {
            int[][] var3 = remaps;
            int var4 = remaps.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               int[] remap = var3[var5];
               if (remap[0] == ch2) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public static int getSubstituteChar(char ch1, char ch2, int form) {
      if (form == 0) {
         return -1;
      } else if (ch1 >= doubleCharFirst && ch1 <= doubleCharLast) {
         int[][] remaps = doubleCharRemappings[ch1 - doubleCharFirst];
         if (remaps == null) {
            return -1;
         } else {
            int[][] var4 = remaps;
            int var5 = remaps.length;

            for(int var6 = 0; var6 < var5; ++var6) {
               int[] remap = var4[var6];
               if (remap[0] == ch2) {
                  return remap[form];
               }
            }

            return -1;
         }
      } else {
         return -1;
      }
   }

   public static int getSubstituteChar(char ch, int form) {
      if (form == 0) {
         return -1;
      } else if (ch >= singleCharFirst && ch <= singleCharLast) {
         int[] chars = singleCharRemappings[ch - singleCharFirst];
         return chars == null ? -1 : chars[form - 1];
      } else {
         return -1;
      }
   }

   public static String createSubstituteString(AttributedCharacterIterator aci) {
      int start = aci.getBeginIndex();
      int end = aci.getEndIndex();
      int numChar = end - start;
      StringBuffer substString = new StringBuffer(numChar);

      for(int i = start; i < end; ++i) {
         char c = aci.setIndex(i);
         if (!arabicChar(c)) {
            substString.append(c);
         } else {
            Integer form = (Integer)aci.getAttribute(ARABIC_FORM);
            int nextChar;
            if (charStartsLigature(c) && i + 1 < end) {
               nextChar = aci.setIndex(i + 1);
               Integer nextForm = (Integer)aci.getAttribute(ARABIC_FORM);
               if (form != null && nextForm != null) {
                  int substChar;
                  if (form.equals(ARABIC_TERMINAL) && nextForm.equals(ARABIC_INITIAL)) {
                     substChar = getSubstituteChar(c, (char)nextChar, ARABIC_ISOLATED);
                     if (substChar > -1) {
                        substString.append((char)substChar);
                        ++i;
                        continue;
                     }
                  } else if (form.equals(ARABIC_TERMINAL)) {
                     substChar = getSubstituteChar(c, (char)nextChar, ARABIC_TERMINAL);
                     if (substChar > -1) {
                        substString.append((char)substChar);
                        ++i;
                        continue;
                     }
                  } else if (form.equals(ARABIC_MEDIAL) && nextForm.equals(ARABIC_MEDIAL)) {
                     substChar = getSubstituteChar(c, (char)nextChar, ARABIC_MEDIAL);
                     if (substChar > -1) {
                        substString.append((char)substChar);
                        ++i;
                        continue;
                     }
                  }
               }
            }

            if (form != null && form > 0) {
               nextChar = getSubstituteChar(c, form);
               if (nextChar > -1) {
                  c = (char)nextChar;
               }
            }

            substString.append(c);
         }
      }

      return substString.toString();
   }

   public static boolean charStartsLigature(char c) {
      return c == 1611 || c == 1612 || c == 1613 || c == 1614 || c == 1615 || c == 1616 || c == 1617 || c == 1618 || c == 1570 || c == 1571 || c == 1573 || c == 1575;
   }

   public static int getNumChars(char c) {
      return isLigature(c) ? 2 : 1;
   }

   public static boolean isLigature(char c) {
      if (c >= 'ﹰ' && c <= 'ﻼ') {
         return c <= 'ﹲ' || c == 'ﹴ' || c >= 'ﹶ' && c <= 'ﹿ' || c >= 'ﻵ';
      } else {
         return false;
      }
   }

   static {
      ARABIC_FORM = GVTAttributedCharacterIterator.TextAttribute.ARABIC_FORM;
      ARABIC_NONE = GVTAttributedCharacterIterator.TextAttribute.ARABIC_NONE;
      ARABIC_ISOLATED = GVTAttributedCharacterIterator.TextAttribute.ARABIC_ISOLATED;
      ARABIC_TERMINAL = GVTAttributedCharacterIterator.TextAttribute.ARABIC_TERMINAL;
      ARABIC_INITIAL = GVTAttributedCharacterIterator.TextAttribute.ARABIC_INITIAL;
      ARABIC_MEDIAL = GVTAttributedCharacterIterator.TextAttribute.ARABIC_MEDIAL;
      singleCharFirst = 1569;
      singleCharLast = 1610;
      singleCharRemappings = new int[][]{{65152, -1, -1, -1}, {65153, 65154, -1, -1}, {65155, 65156, -1, -1}, {65157, 65158, -1, -1}, {65159, 65160, -1, -1}, {65161, 65162, 65163, 65164}, {65165, 65166, -1, -1}, {65167, 65168, 65169, 65170}, {65171, 65172, -1, -1}, {65173, 65174, 65175, 65176}, {65177, 65178, 65179, 65180}, {65181, 65182, 65183, 65184}, {65185, 65186, 65187, 65188}, {65189, 65190, 65191, 65192}, {65193, 65194, -1, -1}, {65195, 65196, -1, -1}, {65197, 65198, -1, -1}, {65199, 65200, -1, -1}, {65201, 65202, 65203, 65204}, {65205, 65206, 65207, 65208}, {65209, 65210, 65211, 65212}, {65213, 65214, 65215, 65216}, {65217, 65218, 65219, 65220}, {65221, 65222, 65223, 65224}, {65225, 65226, 65227, 65228}, {65229, 65230, 65231, 65232}, null, null, null, null, null, null, {65233, 65234, 65235, 65236}, {65237, 65238, 65239, 65240}, {65241, 65242, 65243, 65244}, {65245, 65246, 65247, 65248}, {65249, 65250, 65251, 65252}, {65253, 65254, 65255, 65256}, {65257, 65258, 65259, 65260}, {65261, 65262, -1, -1}, {65263, 65264, -1, -1}, {65265, 65266, 65267, 65268}};
      doubleCharFirst = 1570;
      doubleCharLast = 1618;
      doubleCharRemappings = new int[][][]{{{1604, 65269, 65270, -1, -1}}, {{1604, 65271, 65272, -1, -1}}, (int[][])null, {{1604, 65273, 65274, -1, -1}}, (int[][])null, {{1604, 65275, 65276, -1, -1}}, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, (int[][])null, {{32, 65136, -1, -1, -1}, {1600, -1, -1, -1, 65137}}, {{32, 65138, -1, -1, -1}}, {{32, 65140, -1, -1, -1}}, {{32, 65142, -1, -1, -1}, {1600, -1, -1, -1, 65143}}, {{32, 65144, -1, -1, -1}, {1600, -1, -1, -1, 65145}}, {{32, 65146, -1, -1, -1}, {1600, -1, -1, -1, 65147}}, {{32, 65148, -1, -1, -1}, {1600, -1, -1, -1, 65149}}, {{32, 65150, -1, -1, -1}, {1600, -1, -1, -1, 65151}}};
   }
}
