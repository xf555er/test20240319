package org.apache.fop.datatypes;

import java.io.UnsupportedEncodingException;

public final class URISpecification {
   private static final String PUNCT = ",;:$&+=";
   private static final String RESERVED = ",;:$&+=?/[]@";
   private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

   private URISpecification() {
   }

   public static String getURL(String href) {
      href = href.trim();
      if (href.startsWith("url(") && href.indexOf(")") != -1) {
         href = href.substring(4, href.lastIndexOf(")")).trim();
         if (href.startsWith("'") && href.endsWith("'")) {
            href = href.substring(1, href.length() - 1);
         } else if (href.startsWith("\"") && href.endsWith("\"")) {
            href = href.substring(1, href.length() - 1);
         }
      }

      return href;
   }

   private static boolean isDigit(char ch) {
      return ch >= '0' && ch <= '9';
   }

   private static boolean isAlpha(char ch) {
      return ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z';
   }

   private static boolean isHexDigit(char ch) {
      return ch >= '0' && ch <= '9' || ch >= 'A' && ch <= 'F' || ch >= 'a' && ch <= 'f';
   }

   private static boolean isReserved(char ch) {
      if (",;:$&+=?/[]@".indexOf(ch) >= 0) {
         return true;
      } else {
         return '#' == ch;
      }
   }

   private static boolean isUnreserved(char ch) {
      if (!isDigit(ch) && !isAlpha(ch)) {
         return "_-!.~'()*".indexOf(ch) >= 0;
      } else {
         return true;
      }
   }

   private static void appendEscape(StringBuffer sb, byte b) {
      sb.append('%').append(HEX_DIGITS[b >> 4 & 15]).append(HEX_DIGITS[b >> 0 & 15]);
   }

   public static String escapeURI(String uri) {
      uri = getURL(uri);
      StringBuffer sb = new StringBuffer();
      int i = 0;

      for(int c = uri.length(); i < c; ++i) {
         char ch = uri.charAt(i);
         if (ch == '%' && i < c - 3 && isHexDigit(uri.charAt(i + 1)) && isHexDigit(uri.charAt(i + 2))) {
            sb.append(ch);
         } else if (!isReserved(ch) && !isUnreserved(ch)) {
            try {
               byte[] utf8 = Character.toString(ch).getBytes("UTF-8");
               byte[] var6 = utf8;
               int var7 = utf8.length;

               for(int var8 = 0; var8 < var7; ++var8) {
                  byte anUtf8 = var6[var8];
                  appendEscape(sb, anUtf8);
               }
            } catch (UnsupportedEncodingException var10) {
               throw new Error("Incompatible JVM. UTF-8 not supported.");
            }
         } else {
            sb.append(ch);
         }
      }

      return sb.toString();
   }
}
