package org.apache.fop.fonts.type1;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public enum AdobeStandardEncoding {
   space(32, 32, "SPACE", "space"),
   space_nobreak(160, 32, "NO-BREAK SPACE", "space"),
   exclam(33, 33, "EXCLAMATION MARK", "exclam"),
   quotedbl(34, 34, "QUOTATION MARK", "quotedbl"),
   numersign(35, 35, "NUMBER SIGN", "numbersign"),
   dollar(36, 36, "DOLLAR SIGN", "dollar"),
   percent(37, 37, "PERCENT SIGN", "percent"),
   ampersand(38, 38, "AMPERSAND", "ampersand"),
   quoteright(8217, 39, "RIGHT SINGLE QUOTATION MARK", "quoteright"),
   parenleft(40, 40, "LEFT PARENTHESIS", "parenleft"),
   parenright(41, 41, "RIGHT PARENTHESIS", "parenright"),
   asterisk(42, 42, "ASTERISK", "asterisk"),
   plus(43, 43, "PLUS SIGN", "plus"),
   comma(44, 44, "COMMA", "comma"),
   hyphen(45, 45, "HYPHEN-MINUS", "hyphen"),
   hyphen_soft(173, 45, "SOFT HYPHEN", "hyphen"),
   period(46, 46, "FULL STOP", "period"),
   slash(47, 47, "SOLIDUS", "slash"),
   zero(48, 48, "DIGIT ZERO", "zero"),
   one(49, 49, "DIGIT ONE", "one"),
   two(50, 50, "DIGIT TWO", "two"),
   three(51, 51, "DIGIT THREE", "three"),
   four(52, 52, "DIGIT FOUR", "four"),
   five(53, 53, "DIGIT FIVE", "five"),
   six(54, 54, "DIGIT SIX", "six"),
   seven(55, 55, "DIGIT SEVEN", "seven"),
   eight(56, 56, "DIGIT EIGHT", "eight"),
   nine(57, 57, "DIGIT NINE", "nine"),
   colon(58, 58, "COLON", "colon"),
   semicolon(59, 59, "SEMICOLON", "semicolon"),
   less(60, 60, "LESS-THAN SIGN", "less"),
   equal(61, 61, "EQUALS SIGN", "equal"),
   greater(62, 62, "GREATER-THAN SIGN", "greater"),
   question(63, 63, "QUESTION MARK", "question"),
   at(64, 64, "COMMERCIAL AT", "at"),
   A(65, 65, "LATIN CAPITAL LETTER A", "A"),
   B(66, 66, "LATIN CAPITAL LETTER B", "B"),
   C(67, 67, "LATIN CAPITAL LETTER C", "C"),
   D(68, 68, "LATIN CAPITAL LETTER D", "D"),
   E(69, 69, "LATIN CAPITAL LETTER E", "E"),
   F(70, 70, "LATIN CAPITAL LETTER F", "F"),
   G(71, 71, "LATIN CAPITAL LETTER G", "G"),
   H(72, 72, "LATIN CAPITAL LETTER H", "H"),
   I(73, 73, "LATIN CAPITAL LETTER I", "I"),
   J(74, 74, "LATIN CAPITAL LETTER J", "J"),
   K(75, 75, "LATIN CAPITAL LETTER K", "K"),
   L(76, 76, "LATIN CAPITAL LETTER L", "L"),
   M(77, 77, "LATIN CAPITAL LETTER M", "M"),
   N(78, 78, "LATIN CAPITAL LETTER N", "N"),
   O(79, 79, "LATIN CAPITAL LETTER O", "O"),
   P(80, 80, "LATIN CAPITAL LETTER P", "P"),
   Q(81, 81, "LATIN CAPITAL LETTER Q", "Q"),
   R(82, 82, "LATIN CAPITAL LETTER R", "R"),
   S(83, 83, "LATIN CAPITAL LETTER S", "S"),
   T(84, 84, "LATIN CAPITAL LETTER T", "T"),
   U(85, 85, "LATIN CAPITAL LETTER U", "U"),
   V(86, 86, "LATIN CAPITAL LETTER V", "V"),
   W(87, 87, "LATIN CAPITAL LETTER W", "W"),
   X(88, 88, "LATIN CAPITAL LETTER X", "X"),
   Y(89, 89, "LATIN CAPITAL LETTER Y", "Y"),
   Z(90, 90, "LATIN CAPITAL LETTER Z", "Z"),
   bracketleft(91, 91, "LEFT SQUARE BRACKET", "bracketleft"),
   backslash(92, 92, "REVERSE SOLIDUS", "backslash"),
   bracketright(93, 93, "RIGHT SQUARE BRACKET", "bracketright"),
   asciicircum(94, 94, "CIRCUMFLEX ACCENT", "asciicircum"),
   underscore(95, 95, "LOW LINE", "underscore"),
   quoteleft(8216, 96, "LEFT SINGLE QUOTATION MARK", "quoteleft"),
   a(97, 97, "LATIN SMALL LETTER A", "a"),
   b(98, 98, "LATIN SMALL LETTER B", "b"),
   c(99, 99, "LATIN SMALL LETTER C", "c"),
   d(100, 100, "LATIN SMALL LETTER D", "d"),
   e(101, 101, "LATIN SMALL LETTER E", "e"),
   f(102, 102, "LATIN SMALL LETTER F", "f"),
   g(103, 103, "LATIN SMALL LETTER G", "g"),
   h(104, 104, "LATIN SMALL LETTER H", "h"),
   i(105, 105, "LATIN SMALL LETTER I", "i"),
   j(106, 106, "LATIN SMALL LETTER J", "j"),
   k(107, 107, "LATIN SMALL LETTER K", "k"),
   l(108, 108, "LATIN SMALL LETTER L", "l"),
   m(109, 109, "LATIN SMALL LETTER M", "m"),
   n(110, 110, "LATIN SMALL LETTER N", "n"),
   o(111, 111, "LATIN SMALL LETTER O", "o"),
   p(112, 112, "LATIN SMALL LETTER P", "p"),
   q(113, 113, "LATIN SMALL LETTER Q", "q"),
   r(114, 114, "LATIN SMALL LETTER R", "r"),
   s(115, 115, "LATIN SMALL LETTER S", "s"),
   t(116, 116, "LATIN SMALL LETTER T", "t"),
   u(117, 117, "LATIN SMALL LETTER U", "u"),
   v(118, 118, "LATIN SMALL LETTER V", "v"),
   w(119, 119, "LATIN SMALL LETTER W", "w"),
   x(120, 120, "LATIN SMALL LETTER X", "x"),
   y(121, 121, "LATIN SMALL LETTER Y", "y"),
   z(122, 122, "LATIN SMALL LETTER Z", "z"),
   braceleft(123, 123, "LEFT CURLY BRACKET", "braceleft"),
   bar(124, 124, "VERTICAL LINE", "bar"),
   braceright(125, 125, "RIGHT CURLY BRACKET", "braceright"),
   asciitilde(126, 126, "TILDE", "asciitilde"),
   exclamdown(161, 161, "INVERTED EXCLAMATION MARK", "exclamdown"),
   cent(162, 162, "CENT SIGN", "cent"),
   sterling(163, 163, "POUND SIGN", "sterling"),
   fraction(8260, 164, "FRACTION SLASH", "fraction"),
   fraction_division_slash(8725, 164, "DIVISION SLASH", "fraction"),
   yen(165, 165, "YEN SIGN", "yen"),
   florin(402, 166, "LATIN SMALL LETTER F WITH HOOK", "florin"),
   section(167, 167, "SECTION SIGN", "section"),
   currency(164, 168, "CURRENCY SIGN", "currency"),
   quotesingle(39, 169, "APOSTROPHE", "quotesingle"),
   quotedblleft(8220, 170, "LEFT DOUBLE QUOTATION MARK", "quotedblleft"),
   guillemotleft(171, 171, "LEFT-POINTING DOUBLE ANGLE QUOTATION MARK", "guillemotleft"),
   guilsinglleft(8249, 172, "SINGLE LEFT-POINTING ANGLE QUOTATION MARK", "guilsinglleft"),
   guilsinglright(8250, 173, "SINGLE RIGHT-POINTING ANGLE QUOTATION MARK", "guilsinglright"),
   fi(64257, 174, "LATIN SMALL LIGATURE FI", "fi"),
   fl(64258, 175, "LATIN SMALL LIGATURE FL", "fl"),
   endash(8211, 177, "EN DASH", "endash"),
   dagger(8224, 178, "DAGGER", "dagger"),
   daggerdbl(8225, 179, "DOUBLE DAGGER", "daggerdbl"),
   periodcentered(183, 180, "MIDDLE DOT", "periodcentered"),
   periodcentered_bullet_operator(8729, 180, "BULLET OPERATOR", "periodcentered"),
   paragraph(182, 182, "PILCROW SIGN", "paragraph"),
   bullet(8226, 183, "BULLET", "bullet"),
   quotesinglbase(8218, 184, "SINGLE LOW-9 QUOTATION MARK", "quotesinglbase"),
   quotedblbase(8222, 185, "DOUBLE LOW-9 QUOTATION MARK", "quotedblbase"),
   quotedblright(8221, 186, "RIGHT DOUBLE QUOTATION MARK", "quotedblright"),
   guillemotright(187, 187, "RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK", "guillemotright"),
   ellipsis(8230, 188, "HORIZONTAL ELLIPSIS", "ellipsis"),
   perthousand(8240, 189, "PER MILLE SIGN", "perthousand"),
   questiondown(191, 191, "INVERTED QUESTION MARK", "questiondown"),
   grave(96, 193, "GRAVE ACCENT", "grave"),
   acute(180, 194, "ACUTE ACCENT", "acute"),
   circumflex(710, 195, "MODIFIER LETTER CIRCUMFLEX ACCENT", "circumflex"),
   tilde(732, 196, "SMALL TILDE", "tilde"),
   macron(175, 197, "MACRON", "macron"),
   macron_modifier_letter(713, 197, "MODIFIER LETTER MACRON", "macron"),
   breve(728, 198, "BREVE", "breve"),
   dotaccent(729, 199, "DOT ABOVE", "dotaccent"),
   dieresis(168, 200, "DIAERESIS", "dieresis"),
   ring(730, 202, "RING ABOVE", "ring"),
   cedilla(184, 203, "CEDILLA", "cedilla"),
   hungarumlaut(733, 205, "DOUBLE ACUTE ACCENT", "hungarumlaut"),
   ogonek(731, 206, "OGONEK", "ogonek"),
   caron(711, 207, "CARON", "caron"),
   emdash(8212, 208, "EM DASH", "emdash"),
   AE(198, 225, "LATIN CAPITAL LETTER AE", "AE"),
   ordfeminine(170, 227, "FEMININE ORDINAL INDICATOR", "ordfeminine"),
   Lslash(321, 232, "LATIN CAPITAL LETTER L WITH STROKE", "Lslash"),
   Oslash(216, 233, "LATIN CAPITAL LETTER O WITH STROKE", "Oslash"),
   OE(338, 234, "LATIN CAPITAL LIGATURE OE", "OE"),
   ordmasculine(186, 235, "MASCULINE ORDINAL INDICATOR", "ordmasculine"),
   ae(230, 241, "LATIN SMALL LETTER AE", "ae"),
   dotlessi(305, 245, "LATIN SMALL LETTER DOTLESS I", "dotlessi"),
   lslash(322, 248, "LATIN SMALL LETTER L WITH STROKE", "lslash"),
   oslash(248, 249, "LATIN SMALL LETTER O WITH STROKE", "oslash"),
   oe(339, 250, "LATIN SMALL LIGATURE OE", "oe"),
   germandbls(223, 251, "LATIN SMALL LETTER SHARP S", "germandbls");

   private final int unicodeIndex;
   private final int adobeCodePoint;
   private final String unicodeName;
   private final String adobeName;
   public static final String NAME = "AdobeStandardEncoding";
   private static final Map CACHE = new HashMap();

   private AdobeStandardEncoding(int unicodeIndex, int adobeCodePoint, String unicodeName, String adobeName) {
      this.unicodeIndex = unicodeIndex;
      this.adobeCodePoint = adobeCodePoint;
      this.unicodeName = unicodeName;
      this.adobeName = adobeName;
   }

   int getUnicodeIndex() {
      return this.unicodeIndex;
   }

   int getAdobeCodePoint() {
      return this.adobeCodePoint;
   }

   String getUnicodeName() {
      return this.unicodeName;
   }

   String getAdobeName() {
      return this.adobeName;
   }

   public static int getAdobeCodePoint(String adobeName) {
      AdobeStandardEncoding encoding = (AdobeStandardEncoding)CACHE.get(adobeName);
      return encoding != null ? encoding.getAdobeCodePoint() : -1;
   }

   public static String getCharFromCodePoint(int codePoint) {
      Iterator var1 = CACHE.values().iterator();

      AdobeStandardEncoding encoding;
      do {
         if (!var1.hasNext()) {
            return "";
         }

         encoding = (AdobeStandardEncoding)var1.next();
      } while(encoding.getAdobeCodePoint() != codePoint);

      return encoding.getAdobeName();
   }

   public static char getUnicodeFromCodePoint(int codePoint) {
      Iterator var1 = CACHE.values().iterator();

      AdobeStandardEncoding encoding;
      do {
         if (!var1.hasNext()) {
            return '\uffff';
         }

         encoding = (AdobeStandardEncoding)var1.next();
      } while(encoding.getAdobeCodePoint() != codePoint);

      return (char)encoding.getUnicodeIndex();
   }

   static {
      AdobeStandardEncoding[] var0 = values();
      int var1 = var0.length;

      for(int var2 = 0; var2 < var1; ++var2) {
         AdobeStandardEncoding encoding = var0[var2];
         CACHE.put(encoding.getAdobeName(), encoding);
      }

   }
}
