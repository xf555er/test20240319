package org.apache.fop.pdf;

import java.util.regex.Pattern;

public class PDFPageLabels extends PDFNumberTreeNode {
   private static final int DECIMAL = 1;
   private static final int LOWER_ALPHA = 2;
   private static final int UPPER_ALPHA = 3;
   private static final int LOWER_ROMAN = 4;
   private static final int UPPER_ROMAN = 5;
   private static final int PREFIX = 6;
   private static final PDFName S_DECIMAL = new PDFName("D");
   private static final PDFName S_UPPER_ROMAN = new PDFName("R");
   private static final PDFName S_LOWER_ROMAN = new PDFName("r");
   private static final PDFName S_UPPER_ALPHA = new PDFName("A");
   private static final PDFName S_LOWER_ALPHA = new PDFName("a");
   private static final Pattern MATCH_DECIMAL = Pattern.compile("\\d+");
   private static final Pattern MATCH_ROMAN = Pattern.compile("^M{0,3}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$", 2);
   private static final Pattern MATCH_LETTER = Pattern.compile("^[a-zA-Z]$");
   private int lastPageLabelType;
   private int lastPageNumber;
   private String lastZeroPaddingPrefix = "";

   public void addPageLabel(int index, String pageLabel) {
      boolean addNewPageLabel = false;
      String padding = "00000000";
      int currentPageNumber = 0;
      int currentPageLabelType = false;
      String currentZeroPaddingPrefix = "";
      byte currentPageLabelType;
      if (MATCH_DECIMAL.matcher(pageLabel).matches()) {
         currentPageLabelType = 1;
         currentPageNumber = Integer.parseInt(pageLabel);
         int zeroPadding = 0;
         if (pageLabel.charAt(zeroPadding) == '0') {
            do {
               ++zeroPadding;
            } while(pageLabel.charAt(zeroPadding) == '0');

            currentZeroPaddingPrefix = padding.substring(0, zeroPadding);
            if (currentZeroPaddingPrefix.length() != this.lastZeroPaddingPrefix.length()) {
               addNewPageLabel = true;
            }
         } else if (this.lastZeroPaddingPrefix.length() != 0) {
            addNewPageLabel = true;
         }
      } else if (MATCH_ROMAN.matcher(pageLabel).matches()) {
         if (pageLabel.toLowerCase().equals(pageLabel)) {
            currentPageLabelType = 4;
         } else {
            currentPageLabelType = 5;
         }

         currentPageNumber = this.romanToArabic(pageLabel);
      } else if (MATCH_LETTER.matcher(pageLabel).matches()) {
         char c = pageLabel.charAt(0);
         if (c > 'Z') {
            currentPageLabelType = 2;
         } else {
            currentPageLabelType = 3;
         }

         currentPageNumber = this.alphabeticToArabic(c);
      } else {
         currentPageLabelType = 6;
         addNewPageLabel = true;
      }

      if (this.lastPageLabelType != currentPageLabelType) {
         addNewPageLabel = true;
      }

      if (this.lastPageNumber != currentPageNumber - 1) {
         addNewPageLabel = true;
      }

      if (addNewPageLabel) {
         PDFNumsArray nums = this.getNums();
         PDFDictionary dict = new PDFDictionary(nums);
         PDFName pdfName = null;
         switch (currentPageLabelType) {
            case 6:
               dict.put("P", pageLabel);
               break;
            default:
               switch (currentPageLabelType) {
                  case 1:
                     pdfName = S_DECIMAL;
                     if (currentZeroPaddingPrefix.length() != 0) {
                        dict.put("P", currentZeroPaddingPrefix);
                     }
                     break;
                  case 2:
                     pdfName = S_LOWER_ALPHA;
                     break;
                  case 3:
                     pdfName = S_UPPER_ALPHA;
                     break;
                  case 4:
                     pdfName = S_LOWER_ROMAN;
                     break;
                  case 5:
                     pdfName = S_UPPER_ROMAN;
               }

               dict.put("S", pdfName);
               if (currentPageNumber != 1) {
                  dict.put("St", currentPageNumber);
               }
         }

         nums.put(index, dict);
      }

      this.lastPageLabelType = currentPageLabelType;
      this.lastPageNumber = currentPageNumber;
      this.lastZeroPaddingPrefix = currentZeroPaddingPrefix;
   }

   private int romanToArabic(String roman) {
      int arabic = 0;
      int previousValue = 0;
      int newValue = 0;
      String upperRoman = roman.toUpperCase();

      for(int i = 0; i < upperRoman.length(); ++i) {
         char romanDigit = upperRoman.charAt(i);
         switch (romanDigit) {
            case 'C':
               newValue = 100;
               break;
            case 'D':
               newValue = 500;
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'J':
            case 'K':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'W':
            default:
               break;
            case 'I':
               newValue = 1;
               break;
            case 'L':
               newValue = 50;
               break;
            case 'M':
               newValue = 1000;
               break;
            case 'V':
               newValue = 5;
               break;
            case 'X':
               newValue = 10;
         }

         if (previousValue < newValue) {
            arabic -= previousValue;
         } else {
            arabic += previousValue;
         }

         previousValue = newValue;
      }

      arabic += previousValue;
      return arabic;
   }

   private int alphabeticToArabic(char c) {
      int arabic = Character.toLowerCase(c) - 97 + 1;
      return arabic;
   }
}
