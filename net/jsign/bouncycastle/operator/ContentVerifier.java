package net.jsign.bouncycastle.operator;

import java.io.OutputStream;

public interface ContentVerifier {
   OutputStream getOutputStream();

   boolean verify(byte[] var1);
}
