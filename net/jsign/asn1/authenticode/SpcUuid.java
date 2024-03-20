package net.jsign.asn1.authenticode;

import java.math.BigInteger;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.DEROctetString;

public class SpcUuid extends ASN1Object {
   private final byte[] uuid = new byte[16];

   public SpcUuid(String uuid) {
      byte[] tmp = (new BigInteger(uuid.replaceAll("-", ""), 16)).toByteArray();
      System.arraycopy(tmp, tmp.length - this.uuid.length, this.uuid, 0, this.uuid.length);
   }

   public ASN1Primitive toASN1Primitive() {
      return new DEROctetString(this.uuid);
   }
}
