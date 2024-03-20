package net.jsign.bouncycastle.pqc.crypto.xmss;

import java.io.IOException;
import net.jsign.bouncycastle.util.Encodable;
import net.jsign.bouncycastle.util.Pack;

public final class XMSSPublicKeyParameters extends XMSSKeyParameters implements Encodable {
   private final XMSSParameters params;
   private final int oid;
   private final byte[] root;
   private final byte[] publicSeed;

   private XMSSPublicKeyParameters(Builder var1) {
      super(false, var1.params.getTreeDigest());
      this.params = var1.params;
      if (this.params == null) {
         throw new NullPointerException("params == null");
      } else {
         int var2 = this.params.getTreeDigestSize();
         byte[] var3 = var1.publicKey;
         if (var3 != null) {
            byte var4 = 4;
            int var7 = 0;
            if (var3.length == var2 + var2) {
               this.oid = 0;
               this.root = XMSSUtil.extractBytesAtOffset(var3, var7, var2);
               var7 += var2;
               this.publicSeed = XMSSUtil.extractBytesAtOffset(var3, var7, var2);
            } else {
               if (var3.length != var4 + var2 + var2) {
                  throw new IllegalArgumentException("public key has wrong size");
               }

               this.oid = Pack.bigEndianToInt(var3, 0);
               var7 += var4;
               this.root = XMSSUtil.extractBytesAtOffset(var3, var7, var2);
               var7 += var2;
               this.publicSeed = XMSSUtil.extractBytesAtOffset(var3, var7, var2);
            }
         } else {
            if (this.params.getOid() != null) {
               this.oid = this.params.getOid().getOid();
            } else {
               this.oid = 0;
            }

            byte[] var8 = var1.root;
            if (var8 != null) {
               if (var8.length != var2) {
                  throw new IllegalArgumentException("length of root must be equal to length of digest");
               }

               this.root = var8;
            } else {
               this.root = new byte[var2];
            }

            byte[] var5 = var1.publicSeed;
            if (var5 != null) {
               if (var5.length != var2) {
                  throw new IllegalArgumentException("length of publicSeed must be equal to length of digest");
               }

               this.publicSeed = var5;
            } else {
               this.publicSeed = new byte[var2];
            }
         }

      }
   }

   public byte[] getEncoded() throws IOException {
      return this.toByteArray();
   }

   /** @deprecated */
   public byte[] toByteArray() {
      int var1 = this.params.getTreeDigestSize();
      byte var2 = 4;
      int var6 = 0;
      byte[] var5;
      if (this.oid != 0) {
         var5 = new byte[var2 + var1 + var1];
         Pack.intToBigEndian(this.oid, var5, var6);
         var6 += var2;
      } else {
         var5 = new byte[var1 + var1];
      }

      XMSSUtil.copyBytesAtOffset(var5, this.root, var6);
      var6 += var1;
      XMSSUtil.copyBytesAtOffset(var5, this.publicSeed, var6);
      return var5;
   }

   public byte[] getRoot() {
      return XMSSUtil.cloneArray(this.root);
   }

   public byte[] getPublicSeed() {
      return XMSSUtil.cloneArray(this.publicSeed);
   }

   public XMSSParameters getParameters() {
      return this.params;
   }

   // $FF: synthetic method
   XMSSPublicKeyParameters(Builder var1, Object var2) {
      this(var1);
   }

   public static class Builder {
      private final XMSSParameters params;
      private byte[] root = null;
      private byte[] publicSeed = null;
      private byte[] publicKey = null;

      public Builder(XMSSParameters var1) {
         this.params = var1;
      }

      public Builder withRoot(byte[] var1) {
         this.root = XMSSUtil.cloneArray(var1);
         return this;
      }

      public Builder withPublicSeed(byte[] var1) {
         this.publicSeed = XMSSUtil.cloneArray(var1);
         return this;
      }

      public Builder withPublicKey(byte[] var1) {
         this.publicKey = XMSSUtil.cloneArray(var1);
         return this;
      }

      public XMSSPublicKeyParameters build() {
         return new XMSSPublicKeyParameters(this);
      }
   }
}
