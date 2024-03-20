package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.Strings;

public class DERVisibleString extends ASN1Primitive implements ASN1String {
   private final byte[] string;

   DERVisibleString(byte[] var1) {
      this.string = var1;
   }

   public String getString() {
      return Strings.fromByteArray(this.string);
   }

   public String toString() {
      return this.getString();
   }

   boolean isConstructed() {
      return false;
   }

   int encodedLength() {
      return 1 + StreamUtil.calculateBodyLength(this.string.length) + this.string.length;
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      var1.writeEncoded(var2, 26, this.string);
   }

   boolean asn1Equals(ASN1Primitive var1) {
      return !(var1 instanceof DERVisibleString) ? false : Arrays.areEqual(this.string, ((DERVisibleString)var1).string);
   }

   public int hashCode() {
      return Arrays.hashCode(this.string);
   }
}
