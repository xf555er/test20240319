package net.jsign.bouncycastle.asn1;

public class ASN1EncodableVector {
   static final ASN1Encodable[] EMPTY_ELEMENTS = new ASN1Encodable[0];
   private ASN1Encodable[] elements;
   private int elementCount;
   private boolean copyOnWrite;

   public ASN1EncodableVector() {
      this(10);
   }

   public ASN1EncodableVector(int var1) {
      if (var1 < 0) {
         throw new IllegalArgumentException("'initialCapacity' must not be negative");
      } else {
         this.elements = var1 == 0 ? EMPTY_ELEMENTS : new ASN1Encodable[var1];
         this.elementCount = 0;
         this.copyOnWrite = false;
      }
   }

   public void add(ASN1Encodable var1) {
      if (null == var1) {
         throw new NullPointerException("'element' cannot be null");
      } else {
         int var2 = this.elements.length;
         int var3 = this.elementCount + 1;
         if (var3 > var2 | this.copyOnWrite) {
            this.reallocate(var3);
         }

         this.elements[this.elementCount] = var1;
         this.elementCount = var3;
      }
   }

   public ASN1Encodable get(int var1) {
      if (var1 >= this.elementCount) {
         throw new ArrayIndexOutOfBoundsException(var1 + " >= " + this.elementCount);
      } else {
         return this.elements[var1];
      }
   }

   public int size() {
      return this.elementCount;
   }

   ASN1Encodable[] copyElements() {
      if (0 == this.elementCount) {
         return EMPTY_ELEMENTS;
      } else {
         ASN1Encodable[] var1 = new ASN1Encodable[this.elementCount];
         System.arraycopy(this.elements, 0, var1, 0, this.elementCount);
         return var1;
      }
   }

   ASN1Encodable[] takeElements() {
      if (0 == this.elementCount) {
         return EMPTY_ELEMENTS;
      } else if (this.elements.length == this.elementCount) {
         this.copyOnWrite = true;
         return this.elements;
      } else {
         ASN1Encodable[] var1 = new ASN1Encodable[this.elementCount];
         System.arraycopy(this.elements, 0, var1, 0, this.elementCount);
         return var1;
      }
   }

   private void reallocate(int var1) {
      int var2 = this.elements.length;
      int var3 = Math.max(var2, var1 + (var1 >> 1));
      ASN1Encodable[] var4 = new ASN1Encodable[var3];
      System.arraycopy(this.elements, 0, var4, 0, this.elementCount);
      this.elements = var4;
      this.copyOnWrite = false;
   }

   static ASN1Encodable[] cloneElements(ASN1Encodable[] var0) {
      return var0.length < 1 ? EMPTY_ELEMENTS : (ASN1Encodable[])((ASN1Encodable[])var0.clone());
   }
}
