package net.jsign.bouncycastle.crypto;

public interface Xof extends ExtendedDigest {
   int doFinal(byte[] var1, int var2, int var3);
}
