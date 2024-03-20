package net.jsign.bouncycastle.pqc.asn1;

import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Integer;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.DEROctetString;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.util.Arrays;

public class XMSSMTPublicKey extends ASN1Object {
   private final byte[] publicSeed;
   private final byte[] root;

   public XMSSMTPublicKey(byte[] var1, byte[] var2) {
      this.publicSeed = Arrays.clone(var1);
      this.root = Arrays.clone(var2);
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector();
      var1.add(new ASN1Integer(0L));
      var1.add(new DEROctetString(this.publicSeed));
      var1.add(new DEROctetString(this.root));
      return new DERSequence(var1);
   }
}
