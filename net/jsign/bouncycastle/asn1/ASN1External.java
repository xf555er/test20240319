package net.jsign.bouncycastle.asn1;

import java.io.IOException;

public abstract class ASN1External extends ASN1Primitive {
   protected ASN1ObjectIdentifier directReference;
   protected ASN1Integer indirectReference;
   protected ASN1Primitive dataValueDescriptor;
   protected int encoding;
   protected ASN1Primitive externalContent;

   public ASN1External(ASN1EncodableVector var1) {
      int var2 = 0;
      ASN1Primitive var3 = this.getObjFromVector(var1, var2);
      if (var3 instanceof ASN1ObjectIdentifier) {
         this.directReference = (ASN1ObjectIdentifier)var3;
         ++var2;
         var3 = this.getObjFromVector(var1, var2);
      }

      if (var3 instanceof ASN1Integer) {
         this.indirectReference = (ASN1Integer)var3;
         ++var2;
         var3 = this.getObjFromVector(var1, var2);
      }

      if (!(var3 instanceof ASN1TaggedObject)) {
         this.dataValueDescriptor = var3;
         ++var2;
         var3 = this.getObjFromVector(var1, var2);
      }

      if (var1.size() != var2 + 1) {
         throw new IllegalArgumentException("input vector too large");
      } else if (!(var3 instanceof ASN1TaggedObject)) {
         throw new IllegalArgumentException("No tagged object found in vector. Structure doesn't seem to be of type External");
      } else {
         ASN1TaggedObject var4 = (ASN1TaggedObject)var3;
         this.setEncoding(var4.getTagNo());
         this.externalContent = var4.getObject();
      }
   }

   private ASN1Primitive getObjFromVector(ASN1EncodableVector var1, int var2) {
      if (var1.size() <= var2) {
         throw new IllegalArgumentException("too few objects in input vector");
      } else {
         return var1.get(var2).toASN1Primitive();
      }
   }

   public ASN1External(ASN1ObjectIdentifier var1, ASN1Integer var2, ASN1Primitive var3, int var4, ASN1Primitive var5) {
      this.setDirectReference(var1);
      this.setIndirectReference(var2);
      this.setDataValueDescriptor(var3);
      this.setEncoding(var4);
      this.setExternalContent(var5.toASN1Primitive());
   }

   ASN1Primitive toDERObject() {
      return new DERExternal(this.directReference, this.indirectReference, this.dataValueDescriptor, this.encoding, this.externalContent);
   }

   ASN1Primitive toDLObject() {
      return new DLExternal(this.directReference, this.indirectReference, this.dataValueDescriptor, this.encoding, this.externalContent);
   }

   public int hashCode() {
      int var1 = 0;
      if (this.directReference != null) {
         var1 = this.directReference.hashCode();
      }

      if (this.indirectReference != null) {
         var1 ^= this.indirectReference.hashCode();
      }

      if (this.dataValueDescriptor != null) {
         var1 ^= this.dataValueDescriptor.hashCode();
      }

      var1 ^= this.externalContent.hashCode();
      return var1;
   }

   boolean isConstructed() {
      return true;
   }

   int encodedLength() throws IOException {
      return this.getEncoded().length;
   }

   boolean asn1Equals(ASN1Primitive var1) {
      if (!(var1 instanceof ASN1External)) {
         return false;
      } else if (this == var1) {
         return true;
      } else {
         ASN1External var2 = (ASN1External)var1;
         if (this.directReference != null && (var2.directReference == null || !var2.directReference.equals(this.directReference))) {
            return false;
         } else if (this.indirectReference == null || var2.indirectReference != null && var2.indirectReference.equals(this.indirectReference)) {
            return this.dataValueDescriptor == null || var2.dataValueDescriptor != null && var2.dataValueDescriptor.equals(this.dataValueDescriptor) ? this.externalContent.equals(var2.externalContent) : false;
         } else {
            return false;
         }
      }
   }

   private void setDataValueDescriptor(ASN1Primitive var1) {
      this.dataValueDescriptor = var1;
   }

   private void setDirectReference(ASN1ObjectIdentifier var1) {
      this.directReference = var1;
   }

   private void setEncoding(int var1) {
      if (var1 >= 0 && var1 <= 2) {
         this.encoding = var1;
      } else {
         throw new IllegalArgumentException("invalid encoding value: " + var1);
      }
   }

   private void setExternalContent(ASN1Primitive var1) {
      this.externalContent = var1;
   }

   private void setIndirectReference(ASN1Integer var1) {
      this.indirectReference = var1;
   }
}
