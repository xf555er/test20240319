package net.jsign.bouncycastle.asn1;

import java.io.IOException;

public abstract class ASN1TaggedObject extends ASN1Primitive implements ASN1TaggedObjectParser {
   final int tagNo;
   final boolean explicit;
   final ASN1Encodable obj;

   public static ASN1TaggedObject getInstance(ASN1TaggedObject var0, boolean var1) {
      if (var1) {
         return getInstance(var0.getObject());
      } else {
         throw new IllegalArgumentException("implicitly tagged tagged object");
      }
   }

   public static ASN1TaggedObject getInstance(Object var0) {
      if (var0 != null && !(var0 instanceof ASN1TaggedObject)) {
         if (var0 instanceof byte[]) {
            try {
               return getInstance(fromByteArray((byte[])((byte[])var0)));
            } catch (IOException var2) {
               throw new IllegalArgumentException("failed to construct tagged object from byte[]: " + var2.getMessage());
            }
         } else {
            throw new IllegalArgumentException("unknown object in getInstance: " + var0.getClass().getName());
         }
      } else {
         return (ASN1TaggedObject)var0;
      }
   }

   public ASN1TaggedObject(boolean var1, int var2, ASN1Encodable var3) {
      if (null == var3) {
         throw new NullPointerException("'obj' cannot be null");
      } else {
         this.tagNo = var2;
         this.explicit = var1 || var3 instanceof ASN1Choice;
         this.obj = var3;
      }
   }

   boolean asn1Equals(ASN1Primitive var1) {
      if (!(var1 instanceof ASN1TaggedObject)) {
         return false;
      } else {
         ASN1TaggedObject var2 = (ASN1TaggedObject)var1;
         if (this.tagNo == var2.tagNo && this.explicit == var2.explicit) {
            ASN1Primitive var3 = this.obj.toASN1Primitive();
            ASN1Primitive var4 = var2.obj.toASN1Primitive();
            return var3 == var4 || var3.asn1Equals(var4);
         } else {
            return false;
         }
      }
   }

   public int hashCode() {
      return this.tagNo ^ (this.explicit ? 15 : 240) ^ this.obj.toASN1Primitive().hashCode();
   }

   public int getTagNo() {
      return this.tagNo;
   }

   public boolean isExplicit() {
      return this.explicit;
   }

   public ASN1Primitive getObject() {
      return this.obj.toASN1Primitive();
   }

   public ASN1Primitive getLoadedObject() {
      return this.toASN1Primitive();
   }

   ASN1Primitive toDERObject() {
      return new DERTaggedObject(this.explicit, this.tagNo, this.obj);
   }

   ASN1Primitive toDLObject() {
      return new DLTaggedObject(this.explicit, this.tagNo, this.obj);
   }

   abstract void encode(ASN1OutputStream var1, boolean var2) throws IOException;

   public String toString() {
      return "[" + this.tagNo + "]" + this.obj;
   }
}
