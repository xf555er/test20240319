package net.jsign.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import net.jsign.bouncycastle.util.Encodable;

public abstract class ASN1Object implements ASN1Encodable, Encodable {
   public void encodeTo(OutputStream var1) throws IOException {
      ASN1OutputStream.create(var1).writeObject((ASN1Encodable)this);
   }

   public void encodeTo(OutputStream var1, String var2) throws IOException {
      ASN1OutputStream.create(var1, var2).writeObject((ASN1Encodable)this);
   }

   public byte[] getEncoded() throws IOException {
      ByteArrayOutputStream var1 = new ByteArrayOutputStream();
      this.encodeTo(var1);
      return var1.toByteArray();
   }

   public byte[] getEncoded(String var1) throws IOException {
      ByteArrayOutputStream var2 = new ByteArrayOutputStream();
      this.encodeTo(var2, var1);
      return var2.toByteArray();
   }

   public int hashCode() {
      return this.toASN1Primitive().hashCode();
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof ASN1Encodable)) {
         return false;
      } else {
         ASN1Encodable var2 = (ASN1Encodable)var1;
         return this.toASN1Primitive().equals(var2.toASN1Primitive());
      }
   }

   protected static boolean hasEncodedTagValue(Object var0, int var1) {
      return var0 instanceof byte[] && ((byte[])((byte[])var0))[0] == var1;
   }

   public abstract ASN1Primitive toASN1Primitive();
}
