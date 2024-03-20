package net.jsign.bouncycastle.asn1.ocsp;

import net.jsign.bouncycastle.asn1.ASN1Enumerated;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;

public class OCSPResponseStatus extends ASN1Object {
   private ASN1Enumerated value;

   private OCSPResponseStatus(ASN1Enumerated var1) {
      this.value = var1;
   }

   public static OCSPResponseStatus getInstance(Object var0) {
      if (var0 instanceof OCSPResponseStatus) {
         return (OCSPResponseStatus)var0;
      } else {
         return var0 != null ? new OCSPResponseStatus(ASN1Enumerated.getInstance(var0)) : null;
      }
   }

   public int getIntValue() {
      return this.value.intValueExact();
   }

   public ASN1Primitive toASN1Primitive() {
      return this.value;
   }
}
