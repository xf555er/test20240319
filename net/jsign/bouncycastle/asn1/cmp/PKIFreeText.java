package net.jsign.bouncycastle.asn1.cmp;

import java.util.Enumeration;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.DERUTF8String;

public class PKIFreeText extends ASN1Object {
   ASN1Sequence strings;

   public static PKIFreeText getInstance(Object var0) {
      if (var0 instanceof PKIFreeText) {
         return (PKIFreeText)var0;
      } else {
         return var0 != null ? new PKIFreeText(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   private PKIFreeText(ASN1Sequence var1) {
      Enumeration var2 = var1.getObjects();

      do {
         if (!var2.hasMoreElements()) {
            this.strings = var1;
            return;
         }
      } while(var2.nextElement() instanceof DERUTF8String);

      throw new IllegalArgumentException("attempt to insert non UTF8 STRING into PKIFreeText");
   }

   public int size() {
      return this.strings.size();
   }

   public DERUTF8String getStringAt(int var1) {
      return (DERUTF8String)this.strings.getObjectAt(var1);
   }

   public ASN1Primitive toASN1Primitive() {
      return this.strings;
   }
}
