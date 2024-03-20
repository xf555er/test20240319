package org.apache.batik.css.parser;

public class ScannerUtilities {
   protected static final int[] IDENTIFIER_START = new int[]{0, 0, -2013265922, 134217726};
   protected static final int[] NAME = new int[]{0, 67051520, -2013265922, 134217726};
   protected static final int[] HEXADECIMAL = new int[]{0, 67043328, 126, 126};
   protected static final int[] STRING = new int[]{512, -133, -1, Integer.MAX_VALUE};
   protected static final int[] URI = new int[]{0, -902, -1, Integer.MAX_VALUE};

   protected ScannerUtilities() {
   }

   public static boolean isCSSSpace(char c) {
      return c <= ' ' && (4294981120L >> c & 1L) != 0L;
   }

   public static boolean isCSSIdentifierStartCharacter(char c) {
      return c >= 128 || (IDENTIFIER_START[c >> 5] & 1 << (c & 31)) != 0;
   }

   public static boolean isCSSNameCharacter(char c) {
      return c >= 128 || (NAME[c >> 5] & 1 << (c & 31)) != 0;
   }

   public static boolean isCSSHexadecimalCharacter(char c) {
      return c < 128 && (HEXADECIMAL[c >> 5] & 1 << (c & 31)) != 0;
   }

   public static boolean isCSSStringCharacter(char c) {
      return c >= 128 || (STRING[c >> 5] & 1 << (c & 31)) != 0;
   }

   public static boolean isCSSURICharacter(char c) {
      return c >= 128 || (URI[c >> 5] & 1 << (c & 31)) != 0;
   }
}
