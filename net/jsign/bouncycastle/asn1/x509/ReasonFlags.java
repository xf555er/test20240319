package net.jsign.bouncycastle.asn1.x509;

import net.jsign.bouncycastle.asn1.DERBitString;

public class ReasonFlags extends DERBitString {
   public ReasonFlags(DERBitString var1) {
      super(var1.getBytes(), var1.getPadBits());
   }
}
