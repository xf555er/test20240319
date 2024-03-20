package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.Iterable;

public abstract class ASN1Sequence extends ASN1Primitive implements Iterable {
   ASN1Encodable[] elements;

   public static ASN1Sequence getInstance(Object var0) {
      if (var0 != null && !(var0 instanceof ASN1Sequence)) {
         if (var0 instanceof ASN1SequenceParser) {
            return getInstance(((ASN1SequenceParser)var0).toASN1Primitive());
         } else if (var0 instanceof byte[]) {
            try {
               return getInstance(fromByteArray((byte[])((byte[])var0)));
            } catch (IOException var2) {
               throw new IllegalArgumentException("failed to construct sequence from byte[]: " + var2.getMessage());
            }
         } else {
            if (var0 instanceof ASN1Encodable) {
               ASN1Primitive var1 = ((ASN1Encodable)var0).toASN1Primitive();
               if (var1 instanceof ASN1Sequence) {
                  return (ASN1Sequence)var1;
               }
            }

            throw new IllegalArgumentException("unknown object in getInstance: " + var0.getClass().getName());
         }
      } else {
         return (ASN1Sequence)var0;
      }
   }

   public static ASN1Sequence getInstance(ASN1TaggedObject var0, boolean var1) {
      if (var1) {
         if (!var0.isExplicit()) {
            throw new IllegalArgumentException("object implicit - explicit expected.");
         } else {
            return getInstance(var0.getObject());
         }
      } else {
         ASN1Primitive var2 = var0.getObject();
         if (var0.isExplicit()) {
            return (ASN1Sequence)(var0 instanceof BERTaggedObject ? new BERSequence(var2) : new DLSequence(var2));
         } else if (var2 instanceof ASN1Sequence) {
            ASN1Sequence var3 = (ASN1Sequence)var2;
            return var0 instanceof BERTaggedObject ? var3 : (ASN1Sequence)var3.toDLObject();
         } else {
            throw new IllegalArgumentException("unknown object in getInstance: " + var0.getClass().getName());
         }
      }
   }

   protected ASN1Sequence() {
      this.elements = ASN1EncodableVector.EMPTY_ELEMENTS;
   }

   protected ASN1Sequence(ASN1Encodable var1) {
      if (null == var1) {
         throw new NullPointerException("'element' cannot be null");
      } else {
         this.elements = new ASN1Encodable[]{var1};
      }
   }

   protected ASN1Sequence(ASN1EncodableVector var1) {
      if (null == var1) {
         throw new NullPointerException("'elementVector' cannot be null");
      } else {
         this.elements = var1.takeElements();
      }
   }

   protected ASN1Sequence(ASN1Encodable[] var1) {
      if (Arrays.isNullOrContainsNull(var1)) {
         throw new NullPointerException("'elements' cannot be null, or contain null");
      } else {
         this.elements = ASN1EncodableVector.cloneElements(var1);
      }
   }

   ASN1Sequence(ASN1Encodable[] var1, boolean var2) {
      this.elements = var2 ? ASN1EncodableVector.cloneElements(var1) : var1;
   }

   ASN1Encodable[] toArrayInternal() {
      return this.elements;
   }

   public Enumeration getObjects() {
      return new Enumeration() {
         private int pos = 0;

         public boolean hasMoreElements() {
            return this.pos < ASN1Sequence.this.elements.length;
         }

         public Object nextElement() {
            if (this.pos < ASN1Sequence.this.elements.length) {
               return ASN1Sequence.this.elements[this.pos++];
            } else {
               throw new NoSuchElementException();
            }
         }
      };
   }

   public ASN1Encodable getObjectAt(int var1) {
      return this.elements[var1];
   }

   public int size() {
      return this.elements.length;
   }

   public int hashCode() {
      int var1 = this.elements.length;
      int var2 = var1 + 1;

      while(true) {
         --var1;
         if (var1 < 0) {
            return var2;
         }

         var2 *= 257;
         var2 ^= this.elements[var1].toASN1Primitive().hashCode();
      }
   }

   boolean asn1Equals(ASN1Primitive var1) {
      if (!(var1 instanceof ASN1Sequence)) {
         return false;
      } else {
         ASN1Sequence var2 = (ASN1Sequence)var1;
         int var3 = this.size();
         if (var2.size() != var3) {
            return false;
         } else {
            for(int var4 = 0; var4 < var3; ++var4) {
               ASN1Primitive var5 = this.elements[var4].toASN1Primitive();
               ASN1Primitive var6 = var2.elements[var4].toASN1Primitive();
               if (var5 != var6 && !var5.asn1Equals(var6)) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   ASN1Primitive toDERObject() {
      return new DERSequence(this.elements, false);
   }

   ASN1Primitive toDLObject() {
      return new DLSequence(this.elements, false);
   }

   boolean isConstructed() {
      return true;
   }

   abstract void encode(ASN1OutputStream var1, boolean var2) throws IOException;

   public String toString() {
      int var1 = this.size();
      if (0 == var1) {
         return "[]";
      } else {
         StringBuffer var2 = new StringBuffer();
         var2.append('[');
         int var3 = 0;

         while(true) {
            var2.append(this.elements[var3]);
            ++var3;
            if (var3 >= var1) {
               var2.append(']');
               return var2.toString();
            }

            var2.append(", ");
         }
      }
   }

   public Iterator iterator() {
      return new Arrays.Iterator(this.elements);
   }
}
