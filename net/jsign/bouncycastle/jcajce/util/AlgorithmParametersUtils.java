package net.jsign.bouncycastle.jcajce.util;

import java.io.IOException;
import java.security.AlgorithmParameters;
import net.jsign.bouncycastle.asn1.ASN1Encodable;

public class AlgorithmParametersUtils {
   public static void loadParameters(AlgorithmParameters var0, ASN1Encodable var1) throws IOException {
      try {
         var0.init(var1.toASN1Primitive().getEncoded(), "ASN.1");
      } catch (Exception var3) {
         var0.init(var1.toASN1Primitive().getEncoded());
      }

   }
}
