package net.jsign.bouncycastle.asn1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.Strings;
import net.jsign.bouncycastle.util.encoders.Hex;

public abstract class ASN1OctetString extends ASN1Primitive implements ASN1OctetStringParser {
   byte[] string;

   public static ASN1OctetString getInstance(ASN1TaggedObject var0, boolean var1) {
      if (var1) {
         if (!var0.isExplicit()) {
            throw new IllegalArgumentException("object implicit - explicit expected.");
         } else {
            return getInstance(var0.getObject());
         }
      } else {
         ASN1Primitive var2 = var0.getObject();
         ASN1OctetString var4;
         if (var0.isExplicit()) {
            var4 = getInstance(var2);
            return (ASN1OctetString)(var0 instanceof BERTaggedObject ? new BEROctetString(new ASN1OctetString[]{var4}) : (ASN1OctetString)(new BEROctetString(new ASN1OctetString[]{var4})).toDLObject());
         } else if (var2 instanceof ASN1OctetString) {
            var4 = (ASN1OctetString)var2;
            return var0 instanceof BERTaggedObject ? var4 : (ASN1OctetString)var4.toDLObject();
         } else if (var2 instanceof ASN1Sequence) {
            ASN1Sequence var3 = (ASN1Sequence)var2;
            return (ASN1OctetString)(var0 instanceof BERTaggedObject ? BEROctetString.fromSequence(var3) : (ASN1OctetString)BEROctetString.fromSequence(var3).toDLObject());
         } else {
            throw new IllegalArgumentException("unknown object in getInstance: " + var0.getClass().getName());
         }
      }
   }

   public static ASN1OctetString getInstance(Object var0) {
      if (var0 != null && !(var0 instanceof ASN1OctetString)) {
         if (var0 instanceof byte[]) {
            try {
               return getInstance(fromByteArray((byte[])((byte[])var0)));
            } catch (IOException var2) {
               throw new IllegalArgumentException("failed to construct OCTET STRING from byte[]: " + var2.getMessage());
            }
         } else {
            if (var0 instanceof ASN1Encodable) {
               ASN1Primitive var1 = ((ASN1Encodable)var0).toASN1Primitive();
               if (var1 instanceof ASN1OctetString) {
                  return (ASN1OctetString)var1;
               }
            }

            throw new IllegalArgumentException("illegal object in getInstance: " + var0.getClass().getName());
         }
      } else {
         return (ASN1OctetString)var0;
      }
   }

   public ASN1OctetString(byte[] var1) {
      if (var1 == null) {
         throw new NullPointerException("'string' cannot be null");
      } else {
         this.string = var1;
      }
   }

   public InputStream getOctetStream() {
      return new ByteArrayInputStream(this.string);
   }

   public byte[] getOctets() {
      return this.string;
   }

   public int hashCode() {
      return Arrays.hashCode(this.getOctets());
   }

   boolean asn1Equals(ASN1Primitive var1) {
      if (!(var1 instanceof ASN1OctetString)) {
         return false;
      } else {
         ASN1OctetString var2 = (ASN1OctetString)var1;
         return Arrays.areEqual(this.string, var2.string);
      }
   }

   public ASN1Primitive getLoadedObject() {
      return this.toASN1Primitive();
   }

   ASN1Primitive toDERObject() {
      return new DEROctetString(this.string);
   }

   ASN1Primitive toDLObject() {
      return new DEROctetString(this.string);
   }

   abstract void encode(ASN1OutputStream var1, boolean var2) throws IOException;

   public String toString() {
      return "#" + Strings.fromByteArray(Hex.encode(this.string));
   }
}
