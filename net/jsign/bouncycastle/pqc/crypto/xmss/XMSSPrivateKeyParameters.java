package net.jsign.bouncycastle.pqc.crypto.xmss;

import java.io.IOException;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.Encodable;
import net.jsign.bouncycastle.util.Pack;

public final class XMSSPrivateKeyParameters extends XMSSKeyParameters implements Encodable {
   private final XMSSParameters params;
   private final byte[] secretKeySeed;
   private final byte[] secretKeyPRF;
   private final byte[] publicSeed;
   private final byte[] root;
   private volatile BDS bdsState;

   private XMSSPrivateKeyParameters(Builder var1) {
      super(true, var1.params.getTreeDigest());
      this.params = var1.params;
      if (this.params == null) {
         throw new NullPointerException("params == null");
      } else {
         int var2 = this.params.getTreeDigestSize();
         byte[] var3 = var1.privateKey;
         if (var3 != null) {
            int var4 = this.params.getHeight();
            byte var5 = 4;
            int var10 = 0;
            int var11 = Pack.bigEndianToInt(var3, var10);
            if (!XMSSUtil.isIndexValid(var4, (long)var11)) {
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
            byte[] var12 = XMSSUtil.extractBytesAtOffset(var3, var10, var3.length - var10);

            try {
               BDS var13 = (BDS)XMSSUtil.deserialize(var12, BDS.class);
               if (var13.getIndex() != var11) {
                  throw new IllegalStateException("serialized BDS has wrong index");
               }

               this.bdsState = var13.withWOTSDigest(var1.params.getTreeDigestOID());
            } catch (IOException var14) {
               throw new IllegalArgumentException(var14.getMessage(), var14);
            } catch (ClassNotFoundException var15) {
               throw new IllegalArgumentException(var15.getMessage(), var15);
            }
         } else {
            byte[] var16 = var1.secretKeySeed;
            if (var16 != null) {
               if (var16.length != var2) {
                  throw new IllegalArgumentException("size of secretKeySeed needs to be equal size of digest");
               }

               this.secretKeySeed = var16;
            } else {
               this.secretKeySeed = new byte[var2];
            }

            byte[] var17 = var1.secretKeyPRF;
            if (var17 != null) {
               if (var17.length != var2) {
                  throw new IllegalArgumentException("size of secretKeyPRF needs to be equal size of digest");
               }

               this.secretKeyPRF = var17;
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

            BDS var8 = var1.bdsState;
            if (var8 != null) {
               this.bdsState = var8;
            } else if (var1.index < (1 << this.params.getHeight()) - 2 && var6 != null && var16 != null) {
               this.bdsState = new BDS(this.params, var6, var16, (OTSHashAddress)(new OTSHashAddress.Builder()).build(), var1.index);
            } else {
               this.bdsState = new BDS(this.params, (1 << this.params.getHeight()) - 1, var1.index);
            }

            if (var1.maxIndex >= 0 && var1.maxIndex != this.bdsState.getMaxIndex()) {
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
         byte var3 = 4;
         int var8 = var3 + var2 + var2 + var2 + var2;
         byte[] var9 = new byte[var8];
         int var10 = 0;
         Pack.intToBigEndian(this.bdsState.getIndex(), var9, var10);
         var10 += var3;
         XMSSUtil.copyBytesAtOffset(var9, this.secretKeySeed, var10);
         var10 += var2;
         XMSSUtil.copyBytesAtOffset(var9, this.secretKeyPRF, var10);
         var10 += var2;
         XMSSUtil.copyBytesAtOffset(var9, this.publicSeed, var10);
         var10 += var2;
         XMSSUtil.copyBytesAtOffset(var9, this.root, var10);
         Object var11 = null;

         byte[] var16;
         try {
            var16 = XMSSUtil.serialize(this.bdsState);
         } catch (IOException var14) {
            throw new RuntimeException("error serializing bds state: " + var14.getMessage());
         }

         return Arrays.concatenate(var9, var16);
      }
   }

   public XMSSParameters getParameters() {
      return this.params;
   }

   // $FF: synthetic method
   XMSSPrivateKeyParameters(Builder var1, Object var2) {
      this(var1);
   }

   public static class Builder {
      private final XMSSParameters params;
      private int index = 0;
      private int maxIndex = -1;
      private byte[] secretKeySeed = null;
      private byte[] secretKeyPRF = null;
      private byte[] publicSeed = null;
      private byte[] root = null;
      private BDS bdsState = null;
      private byte[] privateKey = null;

      public Builder(XMSSParameters var1) {
         this.params = var1;
      }

      public Builder withIndex(int var1) {
         this.index = var1;
         return this;
      }

      public Builder withMaxIndex(int var1) {
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

      public Builder withBDSState(BDS var1) {
         this.bdsState = var1;
         return this;
      }

      public XMSSPrivateKeyParameters build() {
         return new XMSSPrivateKeyParameters(this);
      }
   }
}
