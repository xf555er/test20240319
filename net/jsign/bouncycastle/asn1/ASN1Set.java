package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.Iterable;

public abstract class ASN1Set extends ASN1Primitive implements Iterable {
   protected final ASN1Encodable[] elements;
   protected final boolean isSorted;

   public static ASN1Set getInstance(Object var0) {
      if (var0 != null && !(var0 instanceof ASN1Set)) {
         if (var0 instanceof ASN1SetParser) {
            return getInstance(((ASN1SetParser)var0).toASN1Primitive());
         } else if (var0 instanceof byte[]) {
            try {
               return getInstance(ASN1Primitive.fromByteArray((byte[])((byte[])var0)));
            } catch (IOException var2) {
               throw new IllegalArgumentException("failed to construct set from byte[]: " + var2.getMessage());
            }
         } else {
            if (var0 instanceof ASN1Encodable) {
               ASN1Primitive var1 = ((ASN1Encodable)var0).toASN1Primitive();
               if (var1 instanceof ASN1Set) {
                  return (ASN1Set)var1;
               }
            }

            throw new IllegalArgumentException("unknown object in getInstance: " + var0.getClass().getName());
         }
      } else {
         return (ASN1Set)var0;
      }
   }

   public static ASN1Set getInstance(ASN1TaggedObject var0, boolean var1) {
      if (var1) {
         if (!var0.isExplicit()) {
            throw new IllegalArgumentException("object implicit - explicit expected.");
         } else {
            return getInstance(var0.getObject());
         }
      } else {
         ASN1Primitive var2 = var0.getObject();
         if (var0.isExplicit()) {
            return (ASN1Set)(var0 instanceof BERTaggedObject ? new BERSet(var2) : new DLSet(var2));
         } else if (var2 instanceof ASN1Set) {
            ASN1Set var5 = (ASN1Set)var2;
            return var0 instanceof BERTaggedObject ? var5 : (ASN1Set)var5.toDLObject();
         } else if (var2 instanceof ASN1Sequence) {
            ASN1Sequence var3 = (ASN1Sequence)var2;
            ASN1Encodable[] var4 = var3.toArrayInternal();
            return (ASN1Set)(var0 instanceof BERTaggedObject ? new BERSet(false, var4) : new DLSet(false, var4));
         } else {
            throw new IllegalArgumentException("unknown object in getInstance: " + var0.getClass().getName());
         }
      }
   }

   protected ASN1Set() {
      this.elements = ASN1EncodableVector.EMPTY_ELEMENTS;
      this.isSorted = true;
   }

   protected ASN1Set(ASN1Encodable var1) {
      if (null == var1) {
         throw new NullPointerException("'element' cannot be null");
      } else {
         this.elements = new ASN1Encodable[]{var1};
         this.isSorted = true;
      }
   }

   protected ASN1Set(ASN1EncodableVector var1, boolean var2) {
      if (null == var1) {
         throw new NullPointerException("'elementVector' cannot be null");
      } else {
         ASN1Encodable[] var3;
         if (var2 && var1.size() >= 2) {
            var3 = var1.copyElements();
            sort(var3);
         } else {
            var3 = var1.takeElements();
         }

         this.elements = var3;
         this.isSorted = var2 || var3.length < 2;
      }
   }

   protected ASN1Set(ASN1Encodable[] var1, boolean var2) {
      if (Arrays.isNullOrContainsNull(var1)) {
         throw new NullPointerException("'elements' cannot be null, or contain null");
      } else {
         ASN1Encodable[] var3 = ASN1EncodableVector.cloneElements(var1);
         if (var2 && var3.length >= 2) {
            sort(var3);
         }

         this.elements = var3;
         this.isSorted = var2 || var3.length < 2;
      }
   }

   ASN1Set(boolean var1, ASN1Encodable[] var2) {
      this.elements = var2;
      this.isSorted = var1 || var2.length < 2;
   }

   public Enumeration getObjects() {
      return new Enumeration() {
         private int pos = 0;

         public boolean hasMoreElements() {
            return this.pos < ASN1Set.this.elements.length;
         }

         public Object nextElement() {
            if (this.pos < ASN1Set.this.elements.length) {
               return ASN1Set.this.elements[this.pos++];
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

   public ASN1Encodable[] toArray() {
      return ASN1EncodableVector.cloneElements(this.elements);
   }

   public int hashCode() {
      int var1 = this.elements.length;
      int var2 = var1 + 1;

      while(true) {
         --var1;
         if (var1 < 0) {
            return var2;
         }

         var2 += this.elements[var1].toASN1Primitive().hashCode();
      }
   }

   ASN1Primitive toDERObject() {
      ASN1Encodable[] var1;
      if (this.isSorted) {
         var1 = this.elements;
      } else {
         var1 = (ASN1Encodable[])((ASN1Encodable[])this.elements.clone());
         sort(var1);
      }

      return new DERSet(true, var1);
   }

   ASN1Primitive toDLObject() {
      return new DLSet(this.isSorted, this.elements);
   }

   boolean asn1Equals(ASN1Primitive var1) {
      if (!(var1 instanceof ASN1Set)) {
         return false;
      } else {
         ASN1Set var2 = (ASN1Set)var1;
         int var3 = this.size();
         if (var2.size() != var3) {
            return false;
         } else {
            DERSet var4 = (DERSet)this.toDERObject();
            DERSet var5 = (DERSet)var2.toDERObject();

            for(int var6 = 0; var6 < var3; ++var6) {
               ASN1Primitive var7 = var4.elements[var6].toASN1Primitive();
               ASN1Primitive var8 = var5.elements[var6].toASN1Primitive();
               if (var7 != var8 && !var7.asn1Equals(var8)) {
                  return false;
               }
            }

            return true;
         }
      }
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
      return new Arrays.Iterator(this.toArray());
   }

   private static byte[] getDEREncoded(ASN1Encodable var0) {
      try {
         return var0.toASN1Primitive().getEncoded("DER");
      } catch (IOException var2) {
         throw new IllegalArgumentException("cannot encode object added to SET");
      }
   }

   private static boolean lessThanOrEqual(byte[] var0, byte[] var1) {
      int var2 = var0[0] & -33;
      int var3 = var1[0] & -33;
      if (var2 != var3) {
         return var2 < var3;
      } else {
         int var4 = Math.min(var0.length, var1.length) - 1;

         for(int var5 = 1; var5 < var4; ++var5) {
            if (var0[var5] != var1[var5]) {
               return (var0[var5] & 255) < (var1[var5] & 255);
            }
         }

         return (var0[var4] & 255) <= (var1[var4] & 255);
      }
   }

   private static void sort(ASN1Encodable[] var0) {
      int var1 = var0.length;
      if (var1 >= 2) {
         ASN1Encodable var2 = var0[0];
         ASN1Encodable var3 = var0[1];
         byte[] var4 = getDEREncoded(var2);
         byte[] var5 = getDEREncoded(var3);
         if (lessThanOrEqual(var5, var4)) {
            ASN1Encodable var6 = var3;
            var3 = var2;
            var2 = var6;
            byte[] var7 = var5;
            var5 = var4;
            var4 = var7;
         }

         for(int var12 = 2; var12 < var1; ++var12) {
            ASN1Encodable var13 = var0[var12];
            byte[] var8 = getDEREncoded(var13);
            if (lessThanOrEqual(var5, var8)) {
               var0[var12 - 2] = var2;
               var2 = var3;
               var4 = var5;
               var3 = var13;
               var5 = var8;
            } else if (lessThanOrEqual(var4, var8)) {
               var0[var12 - 2] = var2;
               var2 = var13;
               var4 = var8;
            } else {
               int var9 = var12 - 1;

               while(true) {
                  --var9;
                  if (var9 <= 0) {
                     break;
                  }

                  ASN1Encodable var10 = var0[var9 - 1];
                  byte[] var11 = getDEREncoded(var10);
                  if (lessThanOrEqual(var11, var8)) {
                     break;
                  }

                  var0[var9] = var10;
               }

               var0[var9] = var13;
            }
         }

         var0[var1 - 2] = var2;
         var0[var1 - 1] = var3;
      }
   }
}
