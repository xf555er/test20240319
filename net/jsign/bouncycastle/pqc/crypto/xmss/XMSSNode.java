package net.jsign.bouncycastle.pqc.crypto.xmss;

import java.io.Serializable;

public final class XMSSNode implements Serializable {
   private final int height;
   private final byte[] value;

   protected XMSSNode(int var1, byte[] var2) {
      this.height = var1;
      this.value = var2;
   }

   public int getHeight() {
      return this.height;
   }

   public byte[] getValue() {
      return XMSSUtil.cloneArray(this.value);
   }
}
