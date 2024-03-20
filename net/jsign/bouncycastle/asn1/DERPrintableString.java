package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.Strings;

public class DERPrintableString extends ASN1Primitive implements ASN1String {
   private final byte[] string;

   DERPrintableString(byte[] var1) {
      this.string = var1;
   }

   public String getString() {
      return Strings.fromByteArray(this.string);
   }

   boolean isConstructed() {
      return false;
   }

   int encodedLength() {
      return 1 + StreamUtil.calculateBodyLength(this.string.length) + this.string.length;
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      var1.writeEncoded(var2, 19, this.string);
   }

   public int hashCode() {
      return Arrays.hashCode(this.string);
   }

   boolean asn1Equals(ASN1Primitive var1) {
      if (!(var1 instanceof DERPrintableString)) {
         return false;
      } else {
         DERPrintableString var2 = (DERPrintableString)var1;
         return Arrays.areEqual(this.string, var2.string);
      }
   }

   public String toString() {
      return this.getString();
   }
}
