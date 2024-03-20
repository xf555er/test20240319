package net.jsign.bouncycastle.pqc.asn1;

import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Integer;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.DEROctetString;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.util.Arrays;

public class XMSSPublicKey extends ASN1Object {
   private final byte[] publicSeed;
   private final byte[] root;

   public XMSSPublicKey(byte[] var1, byte[] var2) {
      this.publicSeed = Arrays.clone(var1);
      this.root = Arrays.clone(var2);
   }

   private XMSSPublicKey(ASN1Sequence var1) {
      if (!ASN1Integer.getInstance(var1.getObjectAt(0)).hasValue(0)) {
         throw new IllegalArgumentException("unknown version of sequence");
      } else {
         this.publicSeed = Arrays.clone(DEROctetString.getInstance(var1.getObjectAt(1)).getOctets());
         this.root = Arrays.clone(DEROctetString.getInstance(var1.getObjectAt(2)).getOctets());
      }
   }

   public static XMSSPublicKey getInstance(Object var0) {
      if (var0 instanceof XMSSPublicKey) {
         return (XMSSPublicKey)var0;
      } else {
         return var0 != null ? new XMSSPublicKey(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   public byte[] getPublicSeed() {
      return Arrays.clone(this.publicSeed);
   }

   public byte[] getRoot() {
      return Arrays.clone(this.root);
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector();
      var1.add(new ASN1Integer(0L));
      var1.add(new DEROctetString(this.publicSeed));
      var1.add(new DEROctetString(this.root));
      return new DERSequence(var1);
   }
}
