package net.jsign.msi;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class MSIStreamName implements Comparable {
   private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz._".toCharArray();
   private final String name;
   private final byte[] nameUTF16;

   public MSIStreamName(String name) {
      this.name = name;
      this.nameUTF16 = name.getBytes(StandardCharsets.UTF_16LE);
   }

   public String decode() {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      char[] var2 = this.name.toCharArray();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         char c = var2[var4];
         if (c >= 14336 && c < 18496) {
            if (c < 18432) {
               c = (char)(c - 14336);
               out.write(ALPHABET[c & 63]);
               out.write(ALPHABET[c >> 6 & 63]);
            } else {
               out.write(ALPHABET[c - 18432]);
            }
         } else {
            out.write(c);
         }
      }

      return new String(out.toByteArray(), Charset.forName("UTF-8"));
   }

   public String toString() {
      return this.decode();
   }

   public int compareTo(MSIStreamName other) {
      byte[] a = this.nameUTF16;
      byte[] b = other.nameUTF16;
      int size = Math.min(a.length, b.length);

      for(int i = 0; i < size; ++i) {
         if (a[i] != b[i]) {
            return (a[i] & 255) - (b[i] & 255);
         }
      }

      return a.length - b.length;
   }
}
