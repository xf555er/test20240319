package net.jsign.bouncycastle.pqc.crypto.xmss;

import net.jsign.bouncycastle.util.Pack;

final class HashTreeAddress extends XMSSAddress {
   private final int padding;
   private final int treeHeight;
   private final int treeIndex;

   private HashTreeAddress(Builder var1) {
      super(var1);
      this.padding = 0;
      this.treeHeight = var1.treeHeight;
      this.treeIndex = var1.treeIndex;
   }

   protected byte[] toByteArray() {
      byte[] var1 = super.toByteArray();
      Pack.intToBigEndian(this.padding, var1, 16);
      Pack.intToBigEndian(this.treeHeight, var1, 20);
      Pack.intToBigEndian(this.treeIndex, var1, 24);
      return var1;
   }

   protected int getTreeHeight() {
      return this.treeHeight;
   }

   protected int getTreeIndex() {
      return this.treeIndex;
   }

   // $FF: synthetic method
   HashTreeAddress(Builder var1, Object var2) {
      this(var1);
   }

   protected static class Builder extends XMSSAddress.Builder {
      private int treeHeight = 0;
      private int treeIndex = 0;

      protected Builder() {
         super(2);
      }

      protected Builder withTreeHeight(int var1) {
         this.treeHeight = var1;
         return this;
      }

      protected Builder withTreeIndex(int var1) {
         this.treeIndex = var1;
         return this;
      }

      protected XMSSAddress build() {
         return new HashTreeAddress(this);
      }

      protected Builder getThis() {
         return this;
      }
   }
}
