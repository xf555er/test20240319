package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import net.jsign.bouncycastle.util.Arrays;

public class DERUniversalString extends ASN1Primitive implements ASN1String {
   private static final char[] table = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
   private final byte[] string;

   public DERUniversalString(byte[] var1) {
      this.string = Arrays.clone(var1);
   }

   public String getString() {
      StringBuffer var1 = new StringBuffer("#");

      byte[] var2;
      try {
         var2 = this.getEncoded();
      } catch (IOException var4) {
         throw new ASN1ParsingException("internal error encoding UniversalString");
      }

      for(int var3 = 0; var3 != var2.length; ++var3) {
         var1.append(table[var2[var3] >>> 4 & 15]);
         var1.append(table[var2[var3] & 15]);
      }

      return var1.toString();
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
      var1.writeEncoded(var2, 28, this.string);
   }

   boolean asn1Equals(ASN1Primitive var1) {
      return !(var1 instanceof DERUniversalString) ? false : Arrays.areEqual(this.string, ((DERUniversalString)var1).string);
   }

   public int hashCode() {
      return Arrays.hashCode(this.string);
   }
}
