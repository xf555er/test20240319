package net.jsign.bouncycastle.cert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.jsign.bouncycastle.asn1.ASN1Primitive;

class CertUtils {
   private static Set EMPTY_SET = Collections.unmodifiableSet(new HashSet());
   private static List EMPTY_LIST = Collections.unmodifiableList(new ArrayList());

   static ASN1Primitive parseNonEmptyASN1(byte[] var0) throws IOException {
      ASN1Primitive var1 = ASN1Primitive.fromByteArray(var0);
      if (var1 == null) {
         throw new IOException("no content found");
      } else {
         return var1;
      }
   }
}
