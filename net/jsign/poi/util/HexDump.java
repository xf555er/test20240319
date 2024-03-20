package net.jsign.poi.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class HexDump {
   public static final String EOL = System.getProperty("line.separator");
   public static final Charset UTF8;

   public static String longToHex(long value) {
      StringBuilder sb = new StringBuilder(18);
      writeHex(sb, value, 16, "0x");
      return sb.toString();
   }

   private static void writeHex(StringBuilder sb, long value, int nDigits, String prefix) {
      sb.append(prefix);
      char[] buf = new char[nDigits];
      long acc = value;

      for(int i = nDigits - 1; i >= 0; --i) {
         int digit = Math.toIntExact(acc & 15L);
         buf[i] = (char)(digit < 10 ? 48 + digit : 65 + digit - 10);
         acc >>>= 4;
      }

      sb.append(buf);
   }

   static {
      UTF8 = StandardCharsets.UTF_8;
   }
}
