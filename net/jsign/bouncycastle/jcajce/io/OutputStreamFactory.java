package net.jsign.bouncycastle.jcajce.io;

import java.io.OutputStream;
import java.security.Signature;

public class OutputStreamFactory {
   public static OutputStream createStream(Signature var0) {
      return new SignatureUpdatingOutputStream(var0);
   }
}
