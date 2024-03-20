package net.jsign.bouncycastle.pqc.crypto.xmss;

import java.io.IOException;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.Encodable;

public final class XMSSMTPrivateKeyParameters extends XMSSMTKeyParameters implements Encodable {
   private final XMSSMTParameters params;
   private final byte[] secretKeySeed;
   private final byte[] secretKeyPRF;
   private final byte[] publicSeed;
   private final byte[] root;
   private volatile long index;
   private volatile BDSStateMap bdsState;

   private XMSSMTPrivateKeyParameters(Builder var1) {
      super(true, var1.params.getTreeDigest());
      this.params = var1.params;
      if (this.params == null) {
         throw new NullPointerException("params == null");
      } else {
         int var2 = this.params.getTreeDigestSize();
         byte[] var3 = var1.privateKey;
         if (var3 != null) {
            if (var1.xmss == null) {
               throw new NullPointerException("xmss == null");
            }

            int var4 = this.params.getHeight();
            int var5 = (var4 + 7) / 8;
            int var10 = 0;
            this.index = XMSSUtil.bytesToXBigEndian(var3, var10, var5);
            if (!XMSSUtil.isIndexValid(var4, this.index)) {
               throw new IllegalArgumentException("index out of bounds");
            }

            var10 += var5;
            this.secretKeySeed = XMSSUtil.extractBytesAtOffset(var3, var10, var2);
            var10 += var2;
            this.secretKeyPRF = XMSSUtil.extractBytesAtOffset(var3, var10, var2);
            var10 += var2;
            this.publicSeed = XMSSUtil.extractBytesAtOffset(var3, var10, var2);
            var10 += var2;
            this.root = XMSSUtil.extractBytesAtOffset(var3, var10, var2);
            var10 += var2;
            byte[] var11 = XMSSUtil.extractBytesAtOffset(var3, var10, var3.length - var10);

            try {
               BDSStateMap var12 = (BDSStateMap)XMSSUtil.deserialize(var11, BDSStateMap.class);
               this.bdsState = var12.withWOTSDigest(var1.xmss.getTreeDigestOID());
            } catch (IOException var13) {
               throw new IllegalArgumentException(var13.getMessage(), var13);
            } catch (ClassNotFoundException var14) {
               throw new IllegalArgumentException(var14.getMessage(), var14);
            }
         } else {
            this.index = var1.index;
            byte[] var15 = var1.secretKeySeed;
            if (var15 != null) {
               if (var15.length != var2) {
                  throw new IllegalArgumentException("size of secretKeySeed needs to be equal size of digest");
               }

               this.secretKeySeed = var15;
            } else {
               this.secretKeySeed = new byte[var2];
            }

            byte[] var16 = var1.secretKeyPRF;
            if (var16 != null) {
               if (var16.length != var2) {
                  throw new IllegalArgumentException("size of secretKeyPRF needs to be equal size of digest");
               }

               this.secretKeyPRF = var16;
            } else {
               this.secretKeyPRF = new byte[var2];
            }

            byte[] var6 = var1.publicSeed;
            if (var6 != null) {
               if (var6.length != var2) {
                  throw new IllegalArgumentException("size of publicSeed needs to be equal size of digest");
               }

               this.publicSeed = var6;
            } else {
               this.publicSeed = new byte[var2];
            }

            byte[] var7 = var1.root;
            if (var7 != null) {
               if (var7.length != var2) {
                  throw new IllegalArgumentException("size of root needs to be equal size of digest");
               }

               this.root = var7;
            } else {
               this.root = new byte[var2];
            }

            BDSStateMap var8 = var1.bdsState;
            if (var8 != null) {
               this.bdsState = var8;
            } else {
               long var9 = var1.index;
               int var17 = this.params.getHeight();
               if (XMSSUtil.isIndexValid(var17, var9) && var6 != null && var15 != null) {
                  this.bdsState = new BDSStateMap(this.params, var1.index, var6, var15);
               } else {
                  this.bdsState = new BDSStateMap(var1.maxIndex + 1L);
               }
            }

            if (var1.maxIndex >= 0L && var1.maxIndex != this.bdsState.getMaxIndex()) {
               throw new IllegalArgumentException("maxIndex set but not reflected in state");
            }
         }

      }
   }

   public byte[] getEncoded() throws IOException {
      synchronized(this) {
         return this.toByteArray();
      }
   }

   /** @deprecated */
   public byte[] toByteArray() {
      synchronized(this) {
         int var2 = this.params.getTreeDigestSize();
         int var3 = (this.params.getHeight() + 7) / 8;
         int var8 = var3 + var2 + var2 + var2 + var2;
         byte[] var9 = new byte[var8];
         int var10 = 0;
         byte[] var11 = XMSSUtil.toBytesBigEndian(this.index, var3);
         XMSSUtil.copyBytesAtOffset(var9, var11, var10);
         var10 += var3;
         XMSSUtil.copyBytesAtOffset(var9, this.secretKeySeed, var10);
         var10 += var2;
         XMSSUtil.copyBytesAtOffset(var9, this.secretKeyPRF, var10);
         var10 += var2;
         XMSSUtil.copyBytesAtOffset(var9, this.publicSeed, var10);
         var10 += var2;
         XMSSUtil.copyBytesAtOffset(var9, this.root, var10);

         byte[] var10000;
         try {
            var10000 = Arrays.concatenate(var9, XMSSUtil.serialize(this.bdsState));
         } catch (IOException var14) {
            throw new IllegalStateException("error serializing bds state: " + var14.getMessage(), var14);
         }

         return var10000;
      }
   }

   public XMSSMTParameters getParameters() {
      return this.params;
   }

   // $FF: synthetic method
   XMSSMTPrivateKeyParameters(Builder var1, Object var2) {
      this(var1);
   }

   public static class Builder {
      private final XMSSMTParameters params;
      private long index = 0L;
      private long maxIndex = -1L;
      private byte[] secretKeySeed = null;
      private byte[] secretKeyPRF = null;
      private byte[] publicSeed = null;
      private byte[] root = null;
      private BDSStateMap bdsState = null;
      private byte[] privateKey = null;
      private XMSSParameters xmss = null;

      public Builder(XMSSMTParameters var1) {
         this.params = var1;
      }

      public Builder withIndex(long var1) {
         this.index = var1;
         return this;
      }

      public Builder withMaxIndex(long var1) {
         this.maxIndex = var1;
         return this;
      }

      public Builder withSecretKeySeed(byte[] var1) {
         this.secretKeySeed = XMSSUtil.cloneArray(var1);
         return this;
      }

      public Builder withSecretKeyPRF(byte[] var1) {
         this.secretKeyPRF = XMSSUtil.cloneArray(var1);
         return this;
      }

      public Builder withPublicSeed(byte[] var1) {
         this.publicSeed = XMSSUtil.cloneArray(var1);
         return this;
      }

      public Builder withRoot(byte[] var1) {
         this.root = XMSSUtil.cloneArray(var1);
         return this;
      }

      public Builder withBDSState(BDSStateMap var1) {
         if (var1.getMaxIndex() == 0L) {
            this.bdsState = new BDSStateMap(var1, (1L << this.params.getHeight()) - 1L);
         } else {
            this.bdsState = var1;
         }

         return this;
      }

      public XMSSMTPrivateKeyParameters build() {
         return new XMSSMTPrivateKeyParameters(this);
      }
   }
}
