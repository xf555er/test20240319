package net.jsign.bouncycastle.jcajce;

import net.jsign.bouncycastle.crypto.CharToByteConverter;

public class PBKDF1Key implements PBKDFKey {
   private final char[] password;
   private final CharToByteConverter converter;

   public PBKDF1Key(char[] var1, CharToByteConverter var2) {
      this.password = new char[var1.length];
      this.converter = var2;
      System.arraycopy(var1, 0, this.password, 0, var1.length);
   }
}
