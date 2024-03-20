package net.jsign.bouncycastle.pqc.crypto.lms;

import java.util.HashMap;
import java.util.Map;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.nist.NISTObjectIdentifiers;

public class LMSigParameters {
   public static final LMSigParameters lms_sha256_n32_h5;
   public static final LMSigParameters lms_sha256_n32_h10;
   public static final LMSigParameters lms_sha256_n32_h15;
   public static final LMSigParameters lms_sha256_n32_h20;
   public static final LMSigParameters lms_sha256_n32_h25;
   private static Map paramBuilders;
   private final int type;
   private final int m;
   private final int h;
   private final ASN1ObjectIdentifier digestOid;

   protected LMSigParameters(int var1, int var2, int var3, ASN1ObjectIdentifier var4) {
      this.type = var1;
      this.m = var2;
      this.h = var3;
      this.digestOid = var4;
   }

   public int getType() {
      return this.type;
   }

   public int getH() {
      return this.h;
   }

   public int getM() {
      return this.m;
   }

   public ASN1ObjectIdentifier getDigestOID() {
      return this.digestOid;
   }

   static LMSigParameters getParametersForType(int var0) {
      return (LMSigParameters)paramBuilders.get(var0);
   }

   static {
      lms_sha256_n32_h5 = new LMSigParameters(5, 32, 5, NISTObjectIdentifiers.id_sha256);
      lms_sha256_n32_h10 = new LMSigParameters(6, 32, 10, NISTObjectIdentifiers.id_sha256);
      lms_sha256_n32_h15 = new LMSigParameters(7, 32, 15, NISTObjectIdentifiers.id_sha256);
      lms_sha256_n32_h20 = new LMSigParameters(8, 32, 20, NISTObjectIdentifiers.id_sha256);
      lms_sha256_n32_h25 = new LMSigParameters(9, 32, 25, NISTObjectIdentifiers.id_sha256);
      paramBuilders = new HashMap() {
         {
            this.put(LMSigParameters.lms_sha256_n32_h5.type, LMSigParameters.lms_sha256_n32_h5);
            this.put(LMSigParameters.lms_sha256_n32_h10.type, LMSigParameters.lms_sha256_n32_h10);
            this.put(LMSigParameters.lms_sha256_n32_h15.type, LMSigParameters.lms_sha256_n32_h15);
            this.put(LMSigParameters.lms_sha256_n32_h20.type, LMSigParameters.lms_sha256_n32_h20);
            this.put(LMSigParameters.lms_sha256_n32_h25.type, LMSigParameters.lms_sha256_n32_h25);
         }
      };
   }
}
