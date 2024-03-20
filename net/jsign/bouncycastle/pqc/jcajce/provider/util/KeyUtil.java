package net.jsign.bouncycastle.pqc.jcajce.provider.util;

import net.jsign.bouncycastle.asn1.ASN1Encodable;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

public class KeyUtil {
   public static byte[] getEncodedSubjectPublicKeyInfo(AlgorithmIdentifier var0, ASN1Encodable var1) {
      try {
         return getEncodedSubjectPublicKeyInfo(new SubjectPublicKeyInfo(var0, var1));
      } catch (Exception var3) {
         return null;
      }
   }

   public static byte[] getEncodedSubjectPublicKeyInfo(SubjectPublicKeyInfo var0) {
      try {
         return var0.getEncoded("DER");
      } catch (Exception var2) {
         return null;
      }
   }
}
