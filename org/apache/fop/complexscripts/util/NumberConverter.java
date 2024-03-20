package org.apache.fop.complexscripts.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NumberConverter {
   public static final int LETTER_VALUE_ALPHABETIC = 1;
   public static final int LETTER_VALUE_TRADITIONAL = 2;
   private static final int TOKEN_NONE = 0;
   private static final int TOKEN_ALPHANUMERIC = 1;
   private static final int TOKEN_NONALPHANUMERIC = 2;
   private static final Integer[] DEFAULT_TOKEN = new Integer[]{49};
   private static final Integer[] DEFAULT_SEPARATOR = new Integer[]{46};
   private static final String DEFAULT_LANGUAGE = "eng";
   private Integer[] prefix;
   private Integer[] suffix;
   private Integer[][] tokens;
   private Integer[][] separators;
   private int groupingSeparator;
   private int groupingSize;
   private int letterValue;
   private String features;
   private String language;
   private String country;
   private static String[][] equivalentLanguages = new String[][]{{"eng", "en"}, {"fra", "fre", "fr"}, {"spa", "es"}};
   private static int[][] supportedAlphabeticSequences = new int[][]{{65, 26}, {97, 26}};
   private static int[][] supportedSpecials = new int[][]{{73}, {105}, {913}, {945}, {1488}, {1571}, {1575}, {3585}, {12354}, {12356}, {12450}, {12452}};
   private static String[] englishWordOnes = new String[]{"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};
   private static String[] englishWordTeens = new String[]{"ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
   private static String[] englishWordTens = new String[]{"", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
   private static String[] englishWordOthers = new String[]{"hundred", "thousand", "million", "billion"};
   private static String[] englishWordOnesOrd = new String[]{"none", "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth"};
   private static String[] englishWordTeensOrd = new String[]{"tenth", "eleventh", "twelfth", "thirteenth", "fourteenth", "fifteenth", "sixteenth", "seventeenth", "eighteenth", "nineteenth"};
   private static String[] englishWordTensOrd = new String[]{"", "tenth", "twentieth", "thirtieth", "fortieth", "fiftieth", "sixtieth", "seventieth", "eightieth", "ninetith"};
   private static String[] englishWordOthersOrd = new String[]{"hundredth", "thousandth", "millionth", "billionth"};
   private static String[] frenchWordOnes = new String[]{"zéro", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf"};
   private static String[] frenchWordTeens = new String[]{"dix", "onze", "douze", "treize", "quatorze", "quinze", "seize", "dix-sept", "dix-huit", "dix-neuf"};
   private static String[] frenchWordTens = new String[]{"", "dix", "vingt", "trente", "quarante", "cinquante", "soixante", "soixante-dix", "quatre-vingt", "quatre-vingt-dix"};
   private static String[] frenchWordOthers = new String[]{"cent", "cents", "mille", "million", "millions", "milliard", "milliards"};
   private static String[] frenchWordOnesOrdMale = new String[]{"premier", "deuxième", "troisième", "quatrième", "cinquième", "sixième", "septième", "huitième", "neuvième", "dixième"};
   private static String[] frenchWordOnesOrdFemale = new String[]{"première", "deuxième", "troisième", "quatrième", "cinquième", "sixième", "septième", "huitième", "neuvième", "dixième"};
   private static String[] spanishWordOnes = new String[]{"cero", "uno", "dos", "tres", "cuatro", "cinco", "seise", "siete", "ocho", "nueve"};
   private static String[] spanishWordTeens = new String[]{"diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve"};
   private static String[] spanishWordTweens = new String[]{"veinte", "veintiuno", "veintidós", "veintitrés", "veinticuatro", "veinticinco", "veintiséis", "veintisiete", "veintiocho", "veintinueve"};
   private static String[] spanishWordTens = new String[]{"", "diez", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"};
   private static String[] spanishWordHundreds = new String[]{"", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"};
   private static String[] spanishWordOthers = new String[]{"un", "cien", "mil", "millón", "millones"};
   private static String[] spanishWordOnesOrdMale = new String[]{"ninguno", "primero", "segundo", "tercero", "cuarto", "quinto", "sexto", "séptimo", "octavo", "novento", "décimo"};
   private static String[] spanishWordOnesOrdFemale = new String[]{"ninguna", "primera", "segunda", "tercera", "cuarta", "quinta", "sexta", "séptima", "octava", "noventa", "décima"};
   private static int[] romanMapping = new int[]{100000, 90000, 50000, 40000, 10000, 9000, 5000, 4000, 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
   private static String[] romanStandardForms = new String[]{null, null, null, null, null, null, null, null, "m", "cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", null, null, null, "v", "iv", null, null, "i"};
   private static String[] romanLargeForms = new String[]{"ↈ", "ↂↈ", "ↇ", "ↂↇ", "ↂ", "ↀↂ", "ↁ", "ↀↁ", "m", "cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", null, null, null, "v", "iv", null, null, "i"};
   private static String[] romanNumberForms = new String[]{"ↈ", "ↂↈ", "ↇ", "ↂↇ", "ↂ", "ↀↂ", "ↁ", "ↀↁ", "Ⅿ", "ⅭⅯ", "Ⅾ", "ⅭⅮ", "Ⅽ", "ⅩⅭ", "Ⅼ", "ⅩⅬ", "Ⅹ", "Ⅸ", "Ⅷ", "Ⅶ", "Ⅵ", "Ⅴ", "Ⅳ", "Ⅲ", "Ⅱ", "Ⅰ"};
   private static int[] hebrewGematriaAlphabeticMap = new int[]{1488, 1489, 1490, 1491, 1492, 1493, 1494, 1495, 1496, 1497, 1499, 1500, 1502, 1504, 1505, 1506, 1508, 1510, 1511, 1512, 1513, 1514, 1498, 1501, 1503, 1507, 1509};
   private static int[] arabicAbjadiAlphabeticMap = new int[]{1571, 1576, 1580, 1583, 1607, 1608, 1586, 1581, 1591, 1609, 1603, 1604, 1605, 1606, 1587, 1593, 1601, 1589, 1602, 1585, 1588, 1578, 1579, 1582, 1584, 1590, 1592, 1594};
   private static int[] arabicHijaiAlphabeticMap = new int[]{1571, 1576, 1578, 1579, 1580, 1581, 1582, 1583, 1584, 1585, 1586, 1587, 1588, 1589, 1590, 1591, 1592, 1593, 1594, 1601, 1602, 1603, 1604, 1605, 1606, 1607, 1608, 1609};
   private static int[] hiraganaGojuonAlphabeticMap = new int[]{12354, 12356, 12358, 12360, 12362, 12363, 12365, 12367, 12369, 12371, 12373, 12375, 12377, 12379, 12381, 12383, 12385, 12388, 12390, 12392, 12394, 12395, 12396, 12397, 12398, 12399, 12402, 12405, 12408, 12411, 12414, 12415, 12416, 12417, 12418, 12420, 12422, 12424, 12425, 12426, 12427, 12428, 12429, 12431, 12432, 12433, 12434, 12435};
   private static int[] katakanaGojuonAlphabeticMap = new int[]{12450, 12452, 12454, 12456, 12458, 12459, 12461, 12463, 12465, 12467, 12469, 12471, 12473, 12475, 12477, 12479, 12481, 12484, 12486, 12488, 12490, 12491, 12492, 12493, 12494, 12495, 12498, 12501, 12504, 12507, 12510, 12511, 12512, 12513, 12514, 12516, 12518, 12520, 12521, 12522, 12523, 12524, 12525, 12527, 12528, 12529, 12530, 12531};
   private static int[] thaiAlphabeticMap = new int[]{3585, 3586, 3587, 3588, 3589, 3590, 3591, 3592, 3593, 3594, 3595, 3596, 3597, 3598, 3599, 3600, 3601, 3602, 3603, 3604, 3605, 3606, 3607, 3608, 3609, 3610, 3611, 3612, 3613, 3614, 3615, 3616, 3617, 3618, 3619, 3621, 3623, 3624, 3625, 3626, 3627, 3628, 3629, 3630};

   public NumberConverter(String format, int groupingSeparator, int groupingSize, int letterValue, String features, String language, String country) throws IllegalArgumentException {
      this.groupingSeparator = groupingSeparator;
      this.groupingSize = groupingSize;
      this.letterValue = letterValue;
      this.features = features;
      this.language = language != null ? language.toLowerCase() : null;
      this.country = country != null ? country.toLowerCase() : null;
      this.parseFormatTokens(format);
   }

   public String convert(long number) {
      List numbers = new ArrayList();
      numbers.add(number);
      return this.convert(numbers);
   }

   public String convert(List numbers) {
      List scalars = new ArrayList();
      if (this.prefix != null) {
         appendScalars(scalars, this.prefix);
      }

      this.convertNumbers(scalars, numbers);
      if (this.suffix != null) {
         appendScalars(scalars, this.suffix);
      }

      return scalarsToString(scalars);
   }

   private void parseFormatTokens(String format) throws IllegalArgumentException {
      List tokens = new ArrayList();
      List separators = new ArrayList();
      if (format == null || format.length() == 0) {
         format = "1";
      }

      int tokenType = 0;
      List token = new ArrayList();
      Integer[] ca = UTF32.toUTF32(format, 0, true);
      Integer[] var7 = ca;
      int var8 = ca.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         Integer c = var7[var9];
         int tokenTypeNew = isAlphaNumeric(c) ? 1 : 2;
         if (tokenTypeNew != tokenType) {
            if (token.size() > 0) {
               if (tokenType == 1) {
                  tokens.add(token.toArray(new Integer[token.size()]));
               } else {
                  separators.add(token.toArray(new Integer[token.size()]));
               }

               token.clear();
            }

            tokenType = tokenTypeNew;
         }

         token.add(c);
      }

      if (token.size() > 0) {
         if (tokenType == 1) {
            tokens.add(token.toArray(new Integer[token.size()]));
         } else {
            separators.add(token.toArray(new Integer[token.size()]));
         }
      }

      if (!separators.isEmpty()) {
         this.prefix = (Integer[])separators.remove(0);
      }

      if (!separators.isEmpty()) {
         this.suffix = (Integer[])separators.remove(separators.size() - 1);
      }

      this.separators = (Integer[][])separators.toArray(new Integer[separators.size()][]);
      this.tokens = (Integer[][])tokens.toArray(new Integer[tokens.size()][]);
   }

   private static boolean isAlphaNumeric(int var0) {
      // $FF: Couldn't be decompiled
   }

   private void convertNumbers(List scalars, List numbers) {
      Integer[] tknLast = DEFAULT_TOKEN;
      int tknIndex = 0;
      int tknCount = this.tokens.length;
      int sepIndex = 0;
      int sepCount = this.separators.length;
      int numIndex = 0;

      for(Iterator var9 = numbers.iterator(); var9.hasNext(); ++numIndex) {
         Long number = (Long)var9.next();
         Integer[] sep = null;
         Integer[] tkn;
         if (tknIndex < tknCount) {
            if (numIndex > 0) {
               if (sepIndex < sepCount) {
                  sep = this.separators[sepIndex++];
               } else {
                  sep = DEFAULT_SEPARATOR;
               }
            }

            tkn = this.tokens[tknIndex++];
         } else {
            tkn = tknLast;
         }

         appendScalars(scalars, this.convertNumber(number, sep, tkn));
         tknLast = tkn;
      }

   }

   private Integer[] convertNumber(long number, Integer[] separator, Integer[] token) {
      List sl = new ArrayList();
      if (separator != null) {
         appendScalars(sl, separator);
      }

      if (token != null) {
         appendScalars(sl, this.formatNumber(number, token));
      }

      return (Integer[])sl.toArray(new Integer[sl.size()]);
   }

   private Integer[] formatNumber(long number, Integer[] token) {
      Integer[] fn = null;

      assert token.length > 0;

      if (number < 0L) {
         throw new IllegalArgumentException("number must be non-negative");
      } else {
         int s;
         if (token.length == 1) {
            s = token[0];
            switch (s) {
               case 49:
                  fn = this.formatNumberAsDecimal(number, 49, 1);
                  break;
               case 65:
               case 73:
               case 97:
               case 105:
               default:
                  if (isStartOfDecimalSequence(s)) {
                     fn = this.formatNumberAsDecimal(number, s, 1);
                  } else if (isStartOfAlphabeticSequence(s)) {
                     fn = this.formatNumberAsSequence(number, s, getSequenceBase(s), (int[])null);
                  } else if (isStartOfNumericSpecial(s)) {
                     fn = this.formatNumberAsSpecial(number, s);
                  } else {
                     fn = null;
                  }
                  break;
               case 87:
               case 119:
                  fn = this.formatNumberAsWord(number, s == 87 ? 1 : 2);
            }
         } else if (token.length == 2 && token[0] == 87 && token[1] == 119) {
            fn = this.formatNumberAsWord(number, 3);
         } else {
            if (!isPaddedOne(token)) {
               throw new IllegalArgumentException("invalid format token: \"" + UTF32.fromUTF32(token) + "\"");
            }

            s = token[token.length - 1];
            fn = this.formatNumberAsDecimal(number, s, token.length);
         }

         if (fn == null) {
            fn = this.formatNumber(number, DEFAULT_TOKEN);
         }

         assert fn != null;

         return fn;
      }
   }

   private Integer[] formatNumberAsDecimal(long number, int one, int width) {
      assert Character.getNumericValue(one) == 1;

      assert Character.getNumericValue(one - 1) == 0;

      assert Character.getNumericValue(one + 8) == 9;

      List sl = new ArrayList();

      int zero;
      for(zero = one - 1; number > 0L; number /= 10L) {
         long digit = number % 10L;
         ((List)sl).add(0, zero + (int)digit);
      }

      while(width > ((List)sl).size()) {
         ((List)sl).add(0, zero);
      }

      if (this.groupingSize != 0 && this.groupingSeparator != 0) {
         sl = performGrouping((List)sl, this.groupingSize, this.groupingSeparator);
      }

      return (Integer[])((List)sl).toArray(new Integer[((List)sl).size()]);
   }

   private static List performGrouping(List sl, int groupingSize, int groupingSeparator) {
      assert groupingSize > 0;

      assert groupingSeparator != 0;

      if (sl.size() > groupingSize) {
         List gl = new ArrayList();
         int i = 0;
         int n = sl.size();

         for(int g = 0; i < n; ++i) {
            int k = n - i - 1;
            if (g == groupingSize) {
               gl.add(0, groupingSeparator);
               g = 1;
            } else {
               ++g;
            }

            gl.add(0, sl.get(k));
         }

         return gl;
      } else {
         return sl;
      }
   }

   private Integer[] formatNumberAsSequence(long number, int one, int base, int[] map) {
      assert base > 1;

      assert map == null || map.length >= base;

      List sl = new ArrayList();
      if (number == 0L) {
         return null;
      } else {
         for(long n = number; n > 0L; n = (n - 1L) / (long)base) {
            int d = (int)((n - 1L) % (long)base);
            int s = map != null ? map[d] : one + d;
            sl.add(0, s);
         }

         return (Integer[])sl.toArray(new Integer[sl.size()]);
      }
   }

   private Integer[] formatNumberAsSpecial(long number, int one) {
      SpecialNumberFormatter f = this.getSpecialFormatter(one, this.letterValue, this.features, this.language, this.country);
      return f != null ? f.format(number, one, this.letterValue, this.features, this.language, this.country) : null;
   }

   private Integer[] formatNumberAsWord(long number, int caseType) {
      SpecialNumberFormatter f = null;
      if (this.isLanguage("eng")) {
         f = new EnglishNumberAsWordFormatter(caseType);
      } else if (this.isLanguage("spa")) {
         f = new SpanishNumberAsWordFormatter(caseType);
      } else if (this.isLanguage("fra")) {
         f = new FrenchNumberAsWordFormatter(caseType);
      } else {
         f = new EnglishNumberAsWordFormatter(caseType);
      }

      return ((SpecialNumberFormatter)f).format(number, 0, this.letterValue, this.features, this.language, this.country);
   }

   private boolean isLanguage(String iso3Code) {
      if (this.language == null) {
         return false;
      } else {
         return this.language.equals(iso3Code) ? true : isSameLanguage(iso3Code, this.language);
      }
   }

   private static boolean isSameLanguage(String i3c, String lc) {
      String[][] var2 = equivalentLanguages;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String[] el = var2[var4];

         assert el.length >= 2;

         if (el[0].equals(i3c)) {
            String[] var6 = el;
            int var7 = el.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               String anEl = var6[var8];
               if (anEl.equals(lc)) {
                  return true;
               }
            }

            return false;
         }
      }

      return false;
   }

   private static boolean hasFeature(String features, String feature) {
      if (features != null) {
         assert feature != null;

         assert feature.length() != 0;

         String[] fa = features.split(",");
         String[] var3 = fa;
         int var4 = fa.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String f = var3[var5];
            String[] fp = f.split("=");

            assert fp.length > 0;

            String fn = fp[0];
            String var10000;
            if (fp.length > 1) {
               var10000 = fp[1];
            } else {
               var10000 = "";
            }

            if (fn.equals(feature)) {
               return true;
            }
         }
      }

      return false;
   }

   private static void appendScalars(List scalars, Integer[] sa) {
      Collections.addAll(scalars, sa);
   }

   private static String scalarsToString(List scalars) {
      Integer[] sa = (Integer[])scalars.toArray(new Integer[scalars.size()]);
      return UTF32.fromUTF32(sa);
   }

   private static boolean isPaddedOne(Integer[] token) {
      if (getDecimalValue(token[token.length - 1]) != 1) {
         return false;
      } else {
         int i = 0;

         for(int n = token.length - 1; i < n; ++i) {
            if (getDecimalValue(token[i]) != 0) {
               return false;
            }
         }

         return true;
      }
   }

   private static int getDecimalValue(Integer scalar) {
      int s = scalar;
      return Character.getType(s) == 9 ? Character.getNumericValue(s) : -1;
   }

   private static boolean isStartOfDecimalSequence(int s) {
      return Character.getNumericValue(s) == 1 && Character.getNumericValue(s - 1) == 0 && Character.getNumericValue(s + 8) == 9;
   }

   private static boolean isStartOfAlphabeticSequence(int s) {
      int[][] var1 = supportedAlphabeticSequences;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         int[] ss = var1[var3];

         assert ss.length >= 2;

         if (ss[0] == s) {
            return true;
         }
      }

      return false;
   }

   private static int getSequenceBase(int s) {
      int[][] var1 = supportedAlphabeticSequences;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         int[] ss = var1[var3];

         assert ss.length >= 2;

         if (ss[0] == s) {
            return ss[1];
         }
      }

      return 0;
   }

   private static boolean isStartOfNumericSpecial(int s) {
      int[][] var1 = supportedSpecials;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         int[] ss = var1[var3];

         assert ss.length >= 1;

         if (ss[0] == s) {
            return true;
         }
      }

      return false;
   }

   private SpecialNumberFormatter getSpecialFormatter(int one, int letterValue, String features, String language, String country) {
      if (one == 73) {
         return new RomanNumeralsFormatter();
      } else if (one == 105) {
         return new RomanNumeralsFormatter();
      } else if (one == 913) {
         return new IsopsephryNumeralsFormatter();
      } else if (one == 945) {
         return new IsopsephryNumeralsFormatter();
      } else if (one == 1488) {
         return new GematriaNumeralsFormatter();
      } else if (one == 1571) {
         return new ArabicNumeralsFormatter();
      } else if (one == 1575) {
         return new ArabicNumeralsFormatter();
      } else if (one == 3585) {
         return new ThaiNumeralsFormatter();
      } else if (one == 12354) {
         return new KanaNumeralsFormatter();
      } else if (one == 12356) {
         return new KanaNumeralsFormatter();
      } else if (one == 12450) {
         return new KanaNumeralsFormatter();
      } else {
         return one == 12452 ? new KanaNumeralsFormatter() : null;
      }
   }

   private static Integer[] toUpperCase(Integer[] sa) {
      assert sa != null;

      int i = 0;

      for(int n = sa.length; i < n; ++i) {
         Integer s = sa[i];
         sa[i] = Character.toUpperCase(s);
      }

      return sa;
   }

   private static Integer[] toLowerCase(Integer[] sa) {
      assert sa != null;

      int i = 0;

      for(int n = sa.length; i < n; ++i) {
         Integer s = sa[i];
         sa[i] = Character.toLowerCase(s);
      }

      return sa;
   }

   private static List convertWordCase(List words, int caseType) {
      List wl = new ArrayList();
      Iterator var3 = words.iterator();

      while(var3.hasNext()) {
         String w = (String)var3.next();
         wl.add(convertWordCase(w, caseType));
      }

      return wl;
   }

   private static String convertWordCase(String word, int caseType) {
      if (caseType == 1) {
         return word.toUpperCase();
      } else if (caseType == 2) {
         return word.toLowerCase();
      } else if (caseType == 3) {
         StringBuffer sb = new StringBuffer();
         int i = 0;

         for(int n = word.length(); i < n; ++i) {
            String s = word.substring(i, i + 1);
            if (i == 0) {
               sb.append(s.toUpperCase());
            } else {
               sb.append(s.toLowerCase());
            }
         }

         return sb.toString();
      } else {
         return word;
      }
   }

   private static String joinWords(List words, String separator) {
      StringBuffer sb = new StringBuffer();

      String w;
      for(Iterator var3 = words.iterator(); var3.hasNext(); sb.append(w)) {
         w = (String)var3.next();
         if (sb.length() > 0) {
            sb.append(separator);
         }
      }

      return sb.toString();
   }

   private class ThaiNumeralsFormatter implements SpecialNumberFormatter {
      private ThaiNumeralsFormatter() {
      }

      public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
         return one == 3585 && letterValue == 1 ? NumberConverter.this.formatNumberAsSequence(number, one, NumberConverter.thaiAlphabeticMap.length, NumberConverter.thaiAlphabeticMap) : null;
      }

      // $FF: synthetic method
      ThaiNumeralsFormatter(Object x1) {
         this();
      }
   }

   private class KanaNumeralsFormatter implements SpecialNumberFormatter {
      private KanaNumeralsFormatter() {
      }

      public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
         if (one == 12354 && letterValue == 1) {
            return NumberConverter.this.formatNumberAsSequence(number, one, NumberConverter.hiraganaGojuonAlphabeticMap.length, NumberConverter.hiraganaGojuonAlphabeticMap);
         } else {
            return one == 12450 && letterValue == 1 ? NumberConverter.this.formatNumberAsSequence(number, one, NumberConverter.katakanaGojuonAlphabeticMap.length, NumberConverter.katakanaGojuonAlphabeticMap) : null;
         }
      }

      // $FF: synthetic method
      KanaNumeralsFormatter(Object x1) {
         this();
      }
   }

   private class ArabicNumeralsFormatter implements SpecialNumberFormatter {
      private ArabicNumeralsFormatter() {
      }

      public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
         if (one == 1575) {
            int[] map;
            if (letterValue == 2) {
               map = NumberConverter.arabicAbjadiAlphabeticMap;
            } else if (letterValue == 1) {
               map = NumberConverter.arabicHijaiAlphabeticMap;
            } else {
               map = NumberConverter.arabicAbjadiAlphabeticMap;
            }

            return NumberConverter.this.formatNumberAsSequence(number, one, map.length, map);
         } else if (one == 1571) {
            return number != 0L && number <= 1999L ? this.formatAsAbjadiNumber(number, features, language, country) : null;
         } else {
            return null;
         }
      }

      private Integer[] formatAsAbjadiNumber(long number, String features, String language, String country) {
         List sl = new ArrayList();

         assert NumberConverter.arabicAbjadiAlphabeticMap.length == 28;

         assert NumberConverter.arabicAbjadiAlphabeticMap[0] == 1571;

         assert NumberConverter.arabicAbjadiAlphabeticMap[27] == 1594;

         assert number != 0L;

         assert number < 2000L;

         int[] map = NumberConverter.arabicAbjadiAlphabeticMap;
         int thousands = (int)(number / 1000L % 10L);
         int hundreds = (int)(number / 100L % 10L);
         int tens = (int)(number / 10L % 10L);
         int ones = (int)(number / 1L % 10L);
         if (thousands > 0) {
            assert thousands < 2;

            sl.add(map[27 + (thousands - 1)]);
         }

         if (hundreds > 0) {
            assert thousands < 10;

            sl.add(map[18 + (hundreds - 1)]);
         }

         if (tens > 0) {
            assert tens < 10;

            sl.add(map[9 + (tens - 1)]);
         }

         if (ones > 0) {
            assert ones < 10;

            sl.add(map[0 + (ones - 1)]);
         }

         return (Integer[])sl.toArray(new Integer[sl.size()]);
      }

      // $FF: synthetic method
      ArabicNumeralsFormatter(Object x1) {
         this();
      }
   }

   private class GematriaNumeralsFormatter implements SpecialNumberFormatter {
      private GematriaNumeralsFormatter() {
      }

      public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
         if (one == 1488) {
            if (letterValue == 1) {
               return NumberConverter.this.formatNumberAsSequence(number, one, NumberConverter.hebrewGematriaAlphabeticMap.length, NumberConverter.hebrewGematriaAlphabeticMap);
            } else if (letterValue == 2) {
               return number != 0L && number <= 1999L ? this.formatAsGematriaNumber(number, features, language, country) : null;
            } else {
               return null;
            }
         } else {
            return null;
         }
      }

      private Integer[] formatAsGematriaNumber(long number, String features, String language, String country) {
         List sl = new ArrayList();

         assert NumberConverter.hebrewGematriaAlphabeticMap.length == 27;

         assert NumberConverter.hebrewGematriaAlphabeticMap[0] == 1488;

         assert NumberConverter.hebrewGematriaAlphabeticMap[21] == 1514;

         assert number != 0L;

         assert number < 2000L;

         int[] map = NumberConverter.hebrewGematriaAlphabeticMap;
         int thousands = (int)(number / 1000L % 10L);
         int hundreds = (int)(number / 100L % 10L);
         int tens = (int)(number / 10L % 10L);
         int ones = (int)(number / 1L % 10L);
         if (thousands > 0) {
            sl.add(map[0 + (thousands - 1)]);
            sl.add(1523);
         }

         if (hundreds > 0) {
            if (hundreds < 5) {
               sl.add(map[18 + (hundreds - 1)]);
            } else if (hundreds < 9) {
               sl.add(map[21]);
               sl.add(1524);
               sl.add(map[18 + (hundreds - 5)]);
            } else if (hundreds == 9) {
               sl.add(map[21]);
               sl.add(map[21]);
               sl.add(1524);
               sl.add(map[18 + (hundreds - 9)]);
            }

            assert hundreds < 10;
         }

         if (number == 15L) {
            sl.add(map[8]);
            sl.add(1524);
            sl.add(map[5]);
         } else if (number == 16L) {
            sl.add(map[8]);
            sl.add(1524);
            sl.add(map[6]);
         } else {
            if (tens > 0) {
               assert tens < 10;

               sl.add(map[9 + (tens - 1)]);
            }

            if (ones > 0) {
               assert ones < 10;

               sl.add(map[0 + (ones - 1)]);
            }
         }

         return (Integer[])sl.toArray(new Integer[sl.size()]);
      }

      // $FF: synthetic method
      GematriaNumeralsFormatter(Object x1) {
         this();
      }
   }

   private static class IsopsephryNumeralsFormatter implements SpecialNumberFormatter {
      private IsopsephryNumeralsFormatter() {
      }

      public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
         return null;
      }

      // $FF: synthetic method
      IsopsephryNumeralsFormatter(Object x0) {
         this();
      }
   }

   private static class RomanNumeralsFormatter implements SpecialNumberFormatter {
      private RomanNumeralsFormatter() {
      }

      public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
         List sl = new ArrayList();
         if (number == 0L) {
            return null;
         } else {
            String[] forms;
            int maxNumber;
            if (NumberConverter.hasFeature(features, "unicode-number-forms")) {
               forms = NumberConverter.romanNumberForms;
               maxNumber = 199999;
            } else if (NumberConverter.hasFeature(features, "large")) {
               forms = NumberConverter.romanLargeForms;
               maxNumber = 199999;
            } else {
               forms = NumberConverter.romanStandardForms;
               maxNumber = 4999;
            }

            if (number > (long)maxNumber) {
               return null;
            } else {
               while(true) {
                  while(number > 0L) {
                     int i = 0;

                     for(int n = NumberConverter.romanMapping.length; i < n; ++i) {
                        int d = NumberConverter.romanMapping[i];
                        if (number >= (long)d && forms[i] != null) {
                           NumberConverter.appendScalars(sl, UTF32.toUTF32(forms[i], 0, true));
                           number -= (long)d;
                           break;
                        }
                     }
                  }

                  if (one == 73) {
                     return NumberConverter.toUpperCase((Integer[])sl.toArray(new Integer[sl.size()]));
                  }

                  if (one == 105) {
                     return NumberConverter.toLowerCase((Integer[])sl.toArray(new Integer[sl.size()]));
                  }

                  return null;
               }
            }
         }
      }

      // $FF: synthetic method
      RomanNumeralsFormatter(Object x0) {
         this();
      }
   }

   private static class SpanishNumberAsWordFormatter implements SpecialNumberFormatter {
      private int caseType = 1;

      SpanishNumberAsWordFormatter(int caseType) {
         this.caseType = caseType;
      }

      public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
         List wl = new ArrayList();
         if (number >= 1000000000000L) {
            return null;
         } else {
            boolean ordinal = NumberConverter.hasFeature(features, "ordinal");
            if (number == 0L) {
               ((List)wl).add(NumberConverter.spanishWordOnes[0]);
            } else if (ordinal && number <= 10L) {
               boolean female = NumberConverter.hasFeature(features, "female");
               if (female) {
                  ((List)wl).add(NumberConverter.spanishWordOnesOrdFemale[(int)number]);
               } else {
                  ((List)wl).add(NumberConverter.spanishWordOnesOrdMale[(int)number]);
               }
            } else {
               int ones = (int)(number % 1000L);
               int thousands = (int)(number / 1000L % 1000L);
               int millions = (int)(number / 1000000L % 1000L);
               int billions = (int)(number / 1000000000L % 1000L);
               if (billions > 0) {
                  if (billions > 1) {
                     wl = this.formatOnesInThousand((List)wl, billions);
                  }

                  ((List)wl).add(NumberConverter.spanishWordOthers[2]);
                  ((List)wl).add(NumberConverter.spanishWordOthers[4]);
               }

               if (millions > 0) {
                  if (millions == 1) {
                     ((List)wl).add(NumberConverter.spanishWordOthers[0]);
                  } else {
                     wl = this.formatOnesInThousand((List)wl, millions);
                  }

                  if (millions > 1) {
                     ((List)wl).add(NumberConverter.spanishWordOthers[4]);
                  } else {
                     ((List)wl).add(NumberConverter.spanishWordOthers[3]);
                  }
               }

               if (thousands > 0) {
                  if (thousands > 1) {
                     wl = this.formatOnesInThousand((List)wl, thousands);
                  }

                  ((List)wl).add(NumberConverter.spanishWordOthers[2]);
               }

               if (ones > 0) {
                  wl = this.formatOnesInThousand((List)wl, ones);
               }
            }

            List wl = NumberConverter.convertWordCase((List)wl, this.caseType);
            return UTF32.toUTF32(NumberConverter.joinWords(wl, " "), 0, true);
         }
      }

      private List formatOnesInThousand(List wl, int number) {
         assert number < 1000;

         int ones = number % 10;
         int tens = number / 10 % 10;
         int hundreds = number / 100 % 10;
         if (hundreds > 0) {
            if (hundreds == 1 && tens == 0 && ones == 0) {
               wl.add(NumberConverter.spanishWordOthers[1]);
            } else {
               wl.add(NumberConverter.spanishWordHundreds[hundreds]);
            }
         }

         if (tens > 0) {
            if (tens == 1) {
               wl.add(NumberConverter.spanishWordTeens[ones]);
            } else if (tens == 2) {
               wl.add(NumberConverter.spanishWordTweens[ones]);
            } else {
               wl.add(NumberConverter.spanishWordTens[tens]);
               if (ones > 0) {
                  wl.add("y");
                  wl.add(NumberConverter.spanishWordOnes[ones]);
               }
            }
         } else if (ones > 0) {
            wl.add(NumberConverter.spanishWordOnes[ones]);
         }

         return wl;
      }
   }

   private static class FrenchNumberAsWordFormatter implements SpecialNumberFormatter {
      private int caseType = 1;

      FrenchNumberAsWordFormatter(int caseType) {
         this.caseType = caseType;
      }

      public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
         List wl = new ArrayList();
         if (number >= 1000000000000L) {
            return null;
         } else {
            boolean ordinal = NumberConverter.hasFeature(features, "ordinal");
            if (number == 0L) {
               ((List)wl).add(NumberConverter.frenchWordOnes[0]);
            } else if (ordinal && number <= 10L) {
               boolean female = NumberConverter.hasFeature(features, "female");
               if (female) {
                  ((List)wl).add(NumberConverter.frenchWordOnesOrdFemale[(int)number]);
               } else {
                  ((List)wl).add(NumberConverter.frenchWordOnesOrdMale[(int)number]);
               }
            } else {
               int ones = (int)(number % 1000L);
               int thousands = (int)(number / 1000L % 1000L);
               int millions = (int)(number / 1000000L % 1000L);
               int billions = (int)(number / 1000000000L % 1000L);
               if (billions > 0) {
                  wl = this.formatOnesInThousand((List)wl, billions);
                  if (billions == 1) {
                     ((List)wl).add(NumberConverter.frenchWordOthers[5]);
                  } else {
                     ((List)wl).add(NumberConverter.frenchWordOthers[6]);
                  }
               }

               if (millions > 0) {
                  wl = this.formatOnesInThousand((List)wl, millions);
                  if (millions == 1) {
                     ((List)wl).add(NumberConverter.frenchWordOthers[3]);
                  } else {
                     ((List)wl).add(NumberConverter.frenchWordOthers[4]);
                  }
               }

               if (thousands > 0) {
                  if (thousands > 1) {
                     wl = this.formatOnesInThousand((List)wl, thousands);
                  }

                  ((List)wl).add(NumberConverter.frenchWordOthers[2]);
               }

               if (ones > 0) {
                  wl = this.formatOnesInThousand((List)wl, ones);
               }
            }

            List wl = NumberConverter.convertWordCase((List)wl, this.caseType);
            return UTF32.toUTF32(NumberConverter.joinWords(wl, " "), 0, true);
         }
      }

      private List formatOnesInThousand(List wl, int number) {
         assert number < 1000;

         int ones = number % 10;
         int tens = number / 10 % 10;
         int hundreds = number / 100 % 10;
         if (hundreds > 0) {
            if (hundreds > 1) {
               wl.add(NumberConverter.frenchWordOnes[hundreds]);
            }

            if (hundreds > 1 && tens == 0 && ones == 0) {
               wl.add(NumberConverter.frenchWordOthers[1]);
            } else {
               wl.add(NumberConverter.frenchWordOthers[0]);
            }
         }

         if (tens > 0) {
            if (tens == 1) {
               wl.add(NumberConverter.frenchWordTeens[ones]);
            } else {
               StringBuffer sb;
               if (tens < 7) {
                  if (ones == 1) {
                     wl.add(NumberConverter.frenchWordTens[tens]);
                     wl.add("et");
                     wl.add(NumberConverter.frenchWordOnes[ones]);
                  } else {
                     sb = new StringBuffer();
                     sb.append(NumberConverter.frenchWordTens[tens]);
                     if (ones > 0) {
                        sb.append('-');
                        sb.append(NumberConverter.frenchWordOnes[ones]);
                     }

                     wl.add(sb.toString());
                  }
               } else if (tens == 7) {
                  if (ones == 1) {
                     wl.add(NumberConverter.frenchWordTens[6]);
                     wl.add("et");
                     wl.add(NumberConverter.frenchWordTeens[ones]);
                  } else {
                     sb = new StringBuffer();
                     sb.append(NumberConverter.frenchWordTens[6]);
                     sb.append('-');
                     sb.append(NumberConverter.frenchWordTeens[ones]);
                     wl.add(sb.toString());
                  }
               } else if (tens == 8) {
                  sb = new StringBuffer();
                  sb.append(NumberConverter.frenchWordTens[tens]);
                  if (ones > 0) {
                     sb.append('-');
                     sb.append(NumberConverter.frenchWordOnes[ones]);
                  } else {
                     sb.append('s');
                  }

                  wl.add(sb.toString());
               } else if (tens == 9) {
                  sb = new StringBuffer();
                  sb.append(NumberConverter.frenchWordTens[8]);
                  sb.append('-');
                  sb.append(NumberConverter.frenchWordTeens[ones]);
                  wl.add(sb.toString());
               }
            }
         } else if (ones > 0) {
            wl.add(NumberConverter.frenchWordOnes[ones]);
         }

         return wl;
      }
   }

   private static class EnglishNumberAsWordFormatter implements SpecialNumberFormatter {
      private int caseType = 1;

      EnglishNumberAsWordFormatter(int caseType) {
         this.caseType = caseType;
      }

      public Integer[] format(long number, int one, int letterValue, String features, String language, String country) {
         List wl = new ArrayList();
         if (number >= 1000000000000L) {
            return null;
         } else {
            boolean ordinal = NumberConverter.hasFeature(features, "ordinal");
            if (number == 0L) {
               ((List)wl).add(NumberConverter.englishWordOnes[0]);
            } else if (ordinal && number < 10L) {
               ((List)wl).add(NumberConverter.englishWordOnesOrd[(int)number]);
            } else {
               int ones = (int)(number % 1000L);
               int thousands = (int)(number / 1000L % 1000L);
               int millions = (int)(number / 1000000L % 1000L);
               int billions = (int)(number / 1000000000L % 1000L);
               if (billions > 0) {
                  wl = this.formatOnesInThousand((List)wl, billions);
                  if (ordinal && number % 1000000000L == 0L) {
                     ((List)wl).add(NumberConverter.englishWordOthersOrd[3]);
                  } else {
                     ((List)wl).add(NumberConverter.englishWordOthers[3]);
                  }
               }

               if (millions > 0) {
                  wl = this.formatOnesInThousand((List)wl, millions);
                  if (ordinal && number % 1000000L == 0L) {
                     ((List)wl).add(NumberConverter.englishWordOthersOrd[2]);
                  } else {
                     ((List)wl).add(NumberConverter.englishWordOthers[2]);
                  }
               }

               if (thousands > 0) {
                  wl = this.formatOnesInThousand((List)wl, thousands);
                  if (ordinal && number % 1000L == 0L) {
                     ((List)wl).add(NumberConverter.englishWordOthersOrd[1]);
                  } else {
                     ((List)wl).add(NumberConverter.englishWordOthers[1]);
                  }
               }

               if (ones > 0) {
                  wl = this.formatOnesInThousand((List)wl, ones, ordinal);
               }
            }

            List wl = NumberConverter.convertWordCase((List)wl, this.caseType);
            return UTF32.toUTF32(NumberConverter.joinWords(wl, " "), 0, true);
         }
      }

      private List formatOnesInThousand(List wl, int number) {
         return this.formatOnesInThousand(wl, number, false);
      }

      private List formatOnesInThousand(List wl, int number, boolean ordinal) {
         assert number < 1000;

         int ones = number % 10;
         int tens = number / 10 % 10;
         int hundreds = number / 100 % 10;
         if (hundreds > 0) {
            wl.add(NumberConverter.englishWordOnes[hundreds]);
            if (ordinal && number % 100 == 0) {
               wl.add(NumberConverter.englishWordOthersOrd[0]);
            } else {
               wl.add(NumberConverter.englishWordOthers[0]);
            }
         }

         if (tens > 0) {
            if (tens == 1) {
               if (ordinal) {
                  wl.add(NumberConverter.englishWordTeensOrd[ones]);
               } else {
                  wl.add(NumberConverter.englishWordTeens[ones]);
               }
            } else {
               if (ordinal && ones == 0) {
                  wl.add(NumberConverter.englishWordTensOrd[tens]);
               } else {
                  wl.add(NumberConverter.englishWordTens[tens]);
               }

               if (ones > 0) {
                  if (ordinal) {
                     wl.add(NumberConverter.englishWordOnesOrd[ones]);
                  } else {
                     wl.add(NumberConverter.englishWordOnes[ones]);
                  }
               }
            }
         } else if (ones > 0) {
            if (ordinal) {
               wl.add(NumberConverter.englishWordOnesOrd[ones]);
            } else {
               wl.add(NumberConverter.englishWordOnes[ones]);
            }
         }

         return wl;
      }
   }

   interface SpecialNumberFormatter {
      Integer[] format(long var1, int var3, int var4, String var5, String var6, String var7);
   }
}
