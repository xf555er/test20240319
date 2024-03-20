package net.jsign.bouncycastle.jcajce;

public class PKCS12Key implements PBKDFKey {
   private final char[] password;
   private final boolean useWrongZeroLengthConversion;

   public PKCS12Key(char[] var1) {
      this(var1, false);
   }

   public PKCS12Key(char[] var1, boolean var2) {
      if (var1 == null) {
         var1 = new char[0];
      }

      this.password = new char[var1.length];
      this.useWrongZeroLengthConversion = var2;
      System.arraycopy(var1, 0, this.password, 0, var1.length);
   }
}
