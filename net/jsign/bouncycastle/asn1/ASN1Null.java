package net.jsign.bouncycastle.asn1;

import java.io.IOException;

public abstract class ASN1Null extends ASN1Primitive {
   ASN1Null() {
   }

   public int hashCode() {
      return -1;
   }

   boolean asn1Equals(ASN1Primitive var1) {
      return var1 instanceof ASN1Null;
   }

   abstract void encode(ASN1OutputStream var1, boolean var2) throws IOException;

   public String toString() {
      return "NULL";
   }
}
