package net.jsign.bouncycastle.pkcs;

import java.io.IOException;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.pkcs.Attribute;
import net.jsign.bouncycastle.asn1.pkcs.CertificationRequest;

public class PKCS10CertificationRequest {
   private static Attribute[] EMPTY_ARRAY = new Attribute[0];
   private CertificationRequest certificationRequest;

   private static CertificationRequest parseBytes(byte[] var0) throws IOException {
      try {
         CertificationRequest var1 = CertificationRequest.getInstance(ASN1Primitive.fromByteArray(var0));
         if (var1 == null) {
            throw new PKCSIOException("empty data passed to constructor");
         } else {
            return var1;
         }
      } catch (ClassCastException var2) {
         throw new PKCSIOException("malformed data: " + var2.getMessage(), var2);
      } catch (IllegalArgumentException var3) {
         throw new PKCSIOException("malformed data: " + var3.getMessage(), var3);
      }
   }

   public PKCS10CertificationRequest(CertificationRequest var1) {
      if (var1 == null) {
         throw new NullPointerException("certificationRequest cannot be null");
      } else {
         this.certificationRequest = var1;
      }
   }

   public PKCS10CertificationRequest(byte[] var1) throws IOException {
      this(parseBytes(var1));
   }

   public CertificationRequest toASN1Structure() {
      return this.certificationRequest;
   }

   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (!(var1 instanceof PKCS10CertificationRequest)) {
         return false;
      } else {
         PKCS10CertificationRequest var2 = (PKCS10CertificationRequest)var1;
         return this.toASN1Structure().equals(var2.toASN1Structure());
      }
   }

   public int hashCode() {
      return this.toASN1Structure().hashCode();
   }
}
