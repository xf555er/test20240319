package net.jsign.bouncycastle.crypto.digests;

import net.jsign.bouncycastle.crypto.ExtendedDigest;

public abstract class GeneralDigest implements ExtendedDigest {
   private final byte[] xBuf = new byte[4];
   private int xBufOff = 0;
   private long byteCount;

   protected GeneralDigest() {
   }

   public void update(byte var1) {
      this.xBuf[this.xBufOff++] = var1;
      if (this.xBufOff == this.xBuf.length) {
         this.processWord(this.xBuf, 0);
         this.xBufOff = 0;
      }

      ++this.byteCount;
   }

   public void update(byte[] var1, int var2, int var3) {
      var3 = Math.max(0, var3);
      int var4 = 0;
      if (this.xBufOff != 0) {
         while(var4 < var3) {
            this.xBuf[this.xBufOff++] = var1[var2 + var4++];
            if (this.xBufOff == 4) {
               this.processWord(this.xBuf, 0);
               this.xBufOff = 0;
               break;
            }
         }
      }

      for(int var5 = (var3 - var4 & -4) + var4; var4 < var5; var4 += 4) {
         this.processWord(var1, var2 + var4);
      }

      while(var4 < var3) {
         this.xBuf[this.xBufOff++] = var1[var2 + var4++];
      }

      this.byteCount += (long)var3;
   }

   public void finish() {
      long var1 = this.byteCount << 3;
      this.update((byte)-128);

      while(this.xBufOff != 0) {
         this.update((byte)0);
      }

      this.processLength(var1);
      this.processBlock();
   }

   public void reset() {
      this.byteCount = 0L;
      this.xBufOff = 0;

      for(int var1 = 0; var1 < this.xBuf.length; ++var1) {
         this.xBuf[var1] = 0;
      }

   }

   protected abstract void processWord(byte[] var1, int var2);

   protected abstract void processLength(long var1);

   protected abstract void processBlock();
}
