package net.jsign.bouncycastle.asn1.x509;

import java.io.IOException;
import net.jsign.bouncycastle.asn1.ASN1Choice;
import net.jsign.bouncycastle.asn1.ASN1Encodable;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1OctetString;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.ASN1TaggedObject;
import net.jsign.bouncycastle.asn1.DERIA5String;
import net.jsign.bouncycastle.asn1.DERTaggedObject;
import net.jsign.bouncycastle.asn1.x500.X500Name;

public class GeneralName extends ASN1Object implements ASN1Choice {
   private ASN1Encodable obj;
   private int tag;

   public GeneralName(X500Name var1) {
      this.obj = var1;
      this.tag = 4;
   }

   public GeneralName(int var1, ASN1Encodable var2) {
      this.obj = var2;
      this.tag = var1;
   }

   public static GeneralName getInstance(Object var0) {
      if (var0 != null && !(var0 instanceof GeneralName)) {
         if (var0 instanceof ASN1TaggedObject) {
            ASN1TaggedObject var1 = (ASN1TaggedObject)var0;
            int var2 = var1.getTagNo();
            switch (var2) {
               case 0:
               case 3:
               case 5:
                  return new GeneralName(var2, ASN1Sequence.getInstance(var1, false));
               case 1:
               case 2:
               case 6:
                  return new GeneralName(var2, DERIA5String.getInstance(var1, false));
               case 4:
                  return new GeneralName(var2, X500Name.getInstance(var1, true));
               case 7:
                  return new GeneralName(var2, ASN1OctetString.getInstance(var1, false));
               case 8:
                  return new GeneralName(var2, ASN1ObjectIdentifier.getInstance(var1, false));
               default:
                  throw new IllegalArgumentException("unknown tag: " + var2);
            }
         } else if (var0 instanceof byte[]) {
            try {
               return getInstance(ASN1Primitive.fromByteArray((byte[])((byte[])var0)));
            } catch (IOException var3) {
               throw new IllegalArgumentException("unable to parse encoded general name");
            }
         } else {
            throw new IllegalArgumentException("unknown object in getInstance: " + var0.getClass().getName());
         }
      } else {
         return (GeneralName)var0;
      }
   }

   public static GeneralName getInstance(ASN1TaggedObject var0, boolean var1) {
      return getInstance(ASN1TaggedObject.getInstance(var0, true));
   }

   public String toString() {
      StringBuffer var1 = new StringBuffer();
      var1.append(this.tag);
      var1.append(": ");
      switch (this.tag) {
         case 1:
         case 2:
         case 6:
            var1.append(DERIA5String.getInstance(this.obj).getString());
            break;
         case 3:
         case 5:
         default:
            var1.append(this.obj.toString());
            break;
         case 4:
            var1.append(X500Name.getInstance(this.obj).toString());
      }

      return var1.toString();
   }

   public ASN1Primitive toASN1Primitive() {
      boolean var1 = this.tag == 4;
      return new DERTaggedObject(var1, this.tag, this.obj);
   }
}
