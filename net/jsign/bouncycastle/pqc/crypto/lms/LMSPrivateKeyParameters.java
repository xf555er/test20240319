package net.jsign.bouncycastle.pqc.crypto.lms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;
import net.jsign.bouncycastle.crypto.Digest;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.io.Streams;

public class LMSPrivateKeyParameters extends LMSKeyParameters {
   private static CacheKey T1 = new CacheKey(1);
   private static CacheKey[] internedKeys = new CacheKey[129];
   private final byte[] I;
   private final LMSigParameters parameters;
   private final LMOtsParameters otsParameters;
   private final int maxQ;
   private final byte[] masterSecret;
   private final Map tCache;
   private final int maxCacheR;
   private final Digest tDigest;
   private int q;
   private LMSPublicKeyParameters publicKey;

   public LMSPrivateKeyParameters(LMSigParameters var1, LMOtsParameters var2, int var3, byte[] var4, int var5, byte[] var6) {
      super(true);
      this.parameters = var1;
      this.otsParameters = var2;
      this.q = var3;
      this.I = Arrays.clone(var4);
      this.maxQ = var5;
      this.masterSecret = Arrays.clone(var6);
      this.maxCacheR = 1 << this.parameters.getH() + 1;
      this.tCache = new WeakHashMap();
      this.tDigest = DigestUtil.getDigest(var1.getDigestOID());
   }

   public static LMSPrivateKeyParameters getInstance(byte[] var0, byte[] var1) throws IOException {
      LMSPrivateKeyParameters var2 = getInstance(var0);
      var2.publicKey = LMSPublicKeyParameters.getInstance(var1);
      return var2;
   }

   public static LMSPrivateKeyParameters getInstance(Object var0) throws IOException {
      if (var0 instanceof LMSPrivateKeyParameters) {
         return (LMSPrivateKeyParameters)var0;
      } else {
         DataInputStream var1;
         if (var0 instanceof DataInputStream) {
            var1 = (DataInputStream)var0;
            if (var1.readInt() != 0) {
               throw new IllegalStateException("expected version 0 lms private key");
            } else {
               LMSigParameters var12 = LMSigParameters.getParametersForType(var1.readInt());
               LMOtsParameters var3 = LMOtsParameters.getParametersForType(var1.readInt());
               byte[] var4 = new byte[16];
               var1.readFully(var4);
               int var5 = var1.readInt();
               int var6 = var1.readInt();
               int var7 = var1.readInt();
               if (var7 < 0) {
                  throw new IllegalStateException("secret length less than zero");
               } else if (var7 > var1.available()) {
                  throw new IOException("secret length exceeded " + var1.available());
               } else {
                  byte[] var8 = new byte[var7];
                  var1.readFully(var8);
                  return new LMSPrivateKeyParameters(var12, var3, var5, var4, var6, var8);
               }
            }
         } else if (var0 instanceof byte[]) {
            var1 = null;

            LMSPrivateKeyParameters var2;
            try {
               var1 = new DataInputStream(new ByteArrayInputStream((byte[])((byte[])var0)));
               var2 = getInstance(var1);
            } finally {
               if (var1 != null) {
                  var1.close();
               }

            }

            return var2;
         } else if (var0 instanceof InputStream) {
            return getInstance(Streams.readAll((InputStream)var0));
         } else {
            throw new IllegalArgumentException("cannot parse " + var0);
         }
      }
   }

   public LMSigParameters getSigParameters() {
      return this.parameters;
   }

   public LMOtsParameters getOtsParameters() {
      return this.otsParameters;
   }

   public byte[] getI() {
      return Arrays.clone(this.I);
   }

   public byte[] getMasterSecret() {
      return Arrays.clone(this.masterSecret);
   }

   public LMSPublicKeyParameters getPublicKey() {
      synchronized(this) {
         if (this.publicKey == null) {
            this.publicKey = new LMSPublicKeyParameters(this.parameters, this.otsParameters, this.findT(T1), this.I);
         }

         return this.publicKey;
      }
   }

   byte[] findT(int var1) {
      return var1 < this.maxCacheR ? this.findT(var1 < internedKeys.length ? internedKeys[var1] : new CacheKey(var1)) : this.calcT(var1);
   }

   private byte[] findT(CacheKey var1) {
      synchronized(this.tCache) {
         byte[] var3 = (byte[])this.tCache.get(var1);
         if (var3 != null) {
            return var3;
         } else {
            var3 = this.calcT(var1.index);
            this.tCache.put(var1, var3);
            return var3;
         }
      }
   }

   private byte[] calcT(int var1) {
      int var2 = this.getSigParameters().getH();
      int var3 = 1 << var2;
      byte[] var4;
      byte[] var5;
      if (var1 >= var3) {
         LmsUtils.byteArray(this.getI(), this.tDigest);
         LmsUtils.u32str(var1, this.tDigest);
         LmsUtils.u16str((short)-32126, this.tDigest);
         var5 = LM_OTS.lms_ots_generatePublicKey(this.getOtsParameters(), this.getI(), var1 - var3, this.getMasterSecret());
         LmsUtils.byteArray(var5, this.tDigest);
         var4 = new byte[this.tDigest.getDigestSize()];
         this.tDigest.doFinal(var4, 0);
         return var4;
      } else {
         var5 = this.findT(2 * var1);
         byte[] var6 = this.findT(2 * var1 + 1);
         LmsUtils.byteArray(this.getI(), this.tDigest);
         LmsUtils.u32str(var1, this.tDigest);
         LmsUtils.u16str((short)-31869, this.tDigest);
         LmsUtils.byteArray(var5, this.tDigest);
         LmsUtils.byteArray(var6, this.tDigest);
         var4 = new byte[this.tDigest.getDigestSize()];
         this.tDigest.doFinal(var4, 0);
         return var4;
      }
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (var1 != null && this.getClass() == var1.getClass()) {
         LMSPrivateKeyParameters var2 = (LMSPrivateKeyParameters)var1;
         if (this.q != var2.q) {
            return false;
         } else if (this.maxQ != var2.maxQ) {
            return false;
         } else if (!Arrays.areEqual(this.I, var2.I)) {
            return false;
         } else {
            if (this.parameters != null) {
               if (!this.parameters.equals(var2.parameters)) {
                  return false;
               }
            } else if (var2.parameters != null) {
               return false;
            }

            label44: {
               if (this.otsParameters != null) {
                  if (this.otsParameters.equals(var2.otsParameters)) {
                     break label44;
                  }
               } else if (var2.otsParameters == null) {
                  break label44;
               }

               return false;
            }

            if (!Arrays.areEqual(this.masterSecret, var2.masterSecret)) {
               return false;
            } else if (this.publicKey != null && var2.publicKey != null) {
               return this.publicKey.equals(var2.publicKey);
            } else {
               return true;
            }
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int var1 = this.q;
      var1 = 31 * var1 + Arrays.hashCode(this.I);
      var1 = 31 * var1 + (this.parameters != null ? this.parameters.hashCode() : 0);
      var1 = 31 * var1 + (this.otsParameters != null ? this.otsParameters.hashCode() : 0);
      var1 = 31 * var1 + this.maxQ;
      var1 = 31 * var1 + Arrays.hashCode(this.masterSecret);
      var1 = 31 * var1 + (this.publicKey != null ? this.publicKey.hashCode() : 0);
      return var1;
   }

   public byte[] getEncoded() throws IOException {
      return Composer.compose().u32str(0).u32str(this.parameters.getType()).u32str(this.otsParameters.getType()).bytes(this.I).u32str(this.q).u32str(this.maxQ).u32str(this.masterSecret.length).bytes(this.masterSecret).build();
   }

   static {
      internedKeys[1] = T1;

      for(int var0 = 2; var0 < internedKeys.length; ++var0) {
         internedKeys[var0] = new CacheKey(var0);
      }

   }

   private static class CacheKey {
      private final int index;

      CacheKey(int var1) {
         this.index = var1;
      }

      public int hashCode() {
         return this.index;
      }

      public boolean equals(Object var1) {
         if (var1 instanceof CacheKey) {
            return ((CacheKey)var1).index == this.index;
         } else {
            return false;
         }
      }
   }
}
