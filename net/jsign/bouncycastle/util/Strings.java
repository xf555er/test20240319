package net.jsign.bouncycastle.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import net.jsign.bouncycastle.util.encoders.UTF8;

public final class Strings {
   private static String LINE_SEPARATOR;

   public static String fromUTF8ByteArray(byte[] var0) {
      char[] var1 = new char[var0.length];
      int var2 = UTF8.transcodeToUTF16(var0, var1);
      if (var2 < 0) {
         throw new IllegalArgumentException("Invalid UTF-8 input");
      } else {
         return new String(var1, 0, var2);
      }
   }

   public static String toUpperCase(String var0) {
      boolean var1 = false;
      char[] var2 = var0.toCharArray();

      for(int var3 = 0; var3 != var2.length; ++var3) {
         char var4 = var2[var3];
         if ('a' <= var4 && 'z' >= var4) {
            var1 = true;
            var2[var3] = (char)(var4 - 97 + 65);
         }
      }

      if (var1) {
         return new String(var2);
      } else {
         return var0;
      }
   }

   public static String toLowerCase(String var0) {
      boolean var1 = false;
      char[] var2 = var0.toCharArray();

      for(int var3 = 0; var3 != var2.length; ++var3) {
         char var4 = var2[var3];
         if ('A' <= var4 && 'Z' >= var4) {
            var1 = true;
            var2[var3] = (char)(var4 - 65 + 97);
         }
      }

      if (var1) {
         return new String(var2);
      } else {
         return var0;
      }
   }

   public static byte[] toByteArray(String var0) {
      byte[] var1 = new byte[var0.length()];

      for(int var2 = 0; var2 != var1.length; ++var2) {
         char var3 = var0.charAt(var2);
         var1[var2] = (byte)var3;
      }

      return var1;
   }

   public static String fromByteArray(byte[] var0) {
      return new String(asCharArray(var0));
   }

   public static char[] asCharArray(byte[] var0) {
      char[] var1 = new char[var0.length];

      for(int var2 = 0; var2 != var1.length; ++var2) {
         var1[var2] = (char)(var0[var2] & 255);
      }

      return var1;
   }

   public static String lineSeparator() {
      return LINE_SEPARATOR;
   }

   static {
      try {
         LINE_SEPARATOR = (String)AccessController.doPrivileged(new PrivilegedAction() {
            public String run() {
               return System.getProperty("line.separator");
            }
         });
      } catch (Exception var3) {
         try {
            LINE_SEPARATOR = String.format("%n");
         } catch (Exception var2) {
            LINE_SEPARATOR = "\n";
         }
      }

   }
}
