package net.jsign.bouncycastle.pqc.crypto.lms;

import java.util.HashMap;
import java.util.Map;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.nist.NISTObjectIdentifiers;

public class LMOtsParameters {
   public static final LMOtsParameters sha256_n32_w1;
   public static final LMOtsParameters sha256_n32_w2;
   public static final LMOtsParameters sha256_n32_w4;
   public static final LMOtsParameters sha256_n32_w8;
   private static final Map suppliers;
   private final int type;
   private final int n;
   private final int w;
   private final int p;
   private final int ls;
   private final int sigLen;
   private final ASN1ObjectIdentifier digestOID;

   protected LMOtsParameters(int var1, int var2, int var3, int var4, int var5, int var6, ASN1ObjectIdentifier var7) {
      this.type = var1;
      this.n = var2;
      this.w = var3;
      this.p = var4;
      this.ls = var5;
      this.sigLen = var6;
      this.digestOID = var7;
   }

   public int getType() {
      return this.type;
   }

   public int getN() {
      return this.n;
   }

   public int getW() {
      return this.w;
   }

   public int getP() {
      return this.p;
   }

   public ASN1ObjectIdentifier getDigestOID() {
      return this.digestOID;
   }

   public static LMOtsParameters getParametersForType(int var0) {
      return (LMOtsParameters)suppliers.get(var0);
   }

   static {
      sha256_n32_w1 = new LMOtsParameters(1, 32, 1, 265, 7, 8516, NISTObjectIdentifiers.id_sha256);
      sha256_n32_w2 = new LMOtsParameters(2, 32, 2, 133, 6, 4292, NISTObjectIdentifiers.id_sha256);
      sha256_n32_w4 = new LMOtsParameters(3, 32, 4, 67, 4, 2180, NISTObjectIdentifiers.id_sha256);
      sha256_n32_w8 = new LMOtsParameters(4, 32, 8, 34, 0, 1124, NISTObjectIdentifiers.id_sha256);
      suppliers = new HashMap() {
         {
            this.put(LMOtsParameters.sha256_n32_w1.type, LMOtsParameters.sha256_n32_w1);
            this.put(LMOtsParameters.sha256_n32_w2.type, LMOtsParameters.sha256_n32_w2);
            this.put(LMOtsParameters.sha256_n32_w4.type, LMOtsParameters.sha256_n32_w4);
            this.put(LMOtsParameters.sha256_n32_w8.type, LMOtsParameters.sha256_n32_w8);
         }
      };
   }
}
