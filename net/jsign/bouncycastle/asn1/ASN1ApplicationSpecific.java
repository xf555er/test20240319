package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.encoders.Hex;

public abstract class ASN1ApplicationSpecific extends ASN1Primitive {
   protected final boolean isConstructed;
   protected final int tag;
   protected final byte[] octets;

   ASN1ApplicationSpecific(boolean var1, int var2, byte[] var3) {
      this.isConstructed = var1;
      this.tag = var2;
      this.octets = Arrays.clone(var3);
   }

   public boolean isConstructed() {
      return this.isConstructed;
   }

   public int getApplicationTag() {
      return this.tag;
   }

   int encodedLength() throws IOException {
      return StreamUtil.calculateTagLength(this.tag) + StreamUtil.calculateBodyLength(this.octets.length) + this.octets.length;
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      int var3 = 64;
      if (this.isConstructed) {
         var3 |= 32;
      }

      var1.writeEncoded(var2, var3, this.tag, this.octets);
   }

   boolean asn1Equals(ASN1Primitive var1) {
      if (!(var1 instanceof ASN1ApplicationSpecific)) {
         return false;
      } else {
         ASN1ApplicationSpecific var2 = (ASN1ApplicationSpecific)var1;
         return this.isConstructed == var2.isConstructed && this.tag == var2.tag && Arrays.areEqual(this.octets, var2.octets);
      }
   }

   public int hashCode() {
      return (this.isConstructed ? 1 : 0) ^ this.tag ^ Arrays.hashCode(this.octets);
   }

   public String toString() {
      StringBuffer var1 = new StringBuffer();
      var1.append("[");
      if (this.isConstructed()) {
         var1.append("CONSTRUCTED ");
      }

      var1.append("APPLICATION ");
      var1.append(Integer.toString(this.getApplicationTag()));
      var1.append("]");
      if (this.octets != null) {
         var1.append(" #");
         var1.append(Hex.toHexString(this.octets));
      } else {
         var1.append(" #null");
      }

      var1.append(" ");
      return var1.toString();
   }
}
