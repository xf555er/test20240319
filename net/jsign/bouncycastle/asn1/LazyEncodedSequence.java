package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

class LazyEncodedSequence extends ASN1Sequence {
   private byte[] encoded;

   LazyEncodedSequence(byte[] var1) throws IOException {
      this.encoded = var1;
   }

   public synchronized ASN1Encodable getObjectAt(int var1) {
      this.force();
      return super.getObjectAt(var1);
   }

   public synchronized Enumeration getObjects() {
      return (Enumeration)(null != this.encoded ? new LazyConstructionEnumeration(this.encoded) : super.getObjects());
   }

   public synchronized int hashCode() {
      this.force();
      return super.hashCode();
   }

   public synchronized Iterator iterator() {
      this.force();
      return super.iterator();
   }

   public synchronized int size() {
      this.force();
      return super.size();
   }

   ASN1Encodable[] toArrayInternal() {
      this.force();
      return super.toArrayInternal();
   }

   synchronized int encodedLength() throws IOException {
      return null != this.encoded ? 1 + StreamUtil.calculateBodyLength(this.encoded.length) + this.encoded.length : super.toDLObject().encodedLength();
   }

   synchronized void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      if (null != this.encoded) {
         var1.writeEncoded(var2, 48, this.encoded);
      } else {
         super.toDLObject().encode(var1, var2);
      }

   }

   synchronized ASN1Primitive toDERObject() {
      this.force();
      return super.toDERObject();
   }

   synchronized ASN1Primitive toDLObject() {
      this.force();
      return super.toDLObject();
   }

   private void force() {
      if (null != this.encoded) {
         ASN1EncodableVector var1 = new ASN1EncodableVector();
         LazyConstructionEnumeration var2 = new LazyConstructionEnumeration(this.encoded);

         while(var2.hasMoreElements()) {
            var1.add((ASN1Primitive)var2.nextElement());
         }

         this.elements = var1.takeElements();
         this.encoded = null;
      }

   }
}
