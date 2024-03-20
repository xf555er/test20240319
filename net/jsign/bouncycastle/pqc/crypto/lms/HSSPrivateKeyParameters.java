package net.jsign.bouncycastle.pqc.crypto.lms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.jsign.bouncycastle.util.Encodable;
import net.jsign.bouncycastle.util.io.Streams;

public class HSSPrivateKeyParameters extends LMSKeyParameters {
   private final int l;
   private final boolean isShard;
   private List keys;
   private List sig;
   private final long indexLimit;
   private long index = 0L;
   private HSSPublicKeyParameters publicKey;

   private HSSPrivateKeyParameters(int var1, List var2, List var3, long var4, long var6, boolean var8) {
      super(true);
      this.l = var1;
      this.keys = Collections.unmodifiableList(var2);
      this.sig = Collections.unmodifiableList(var3);
      this.index = var4;
      this.indexLimit = var6;
      this.isShard = var8;
   }

   public static HSSPrivateKeyParameters getInstance(byte[] var0, byte[] var1) throws IOException {
      HSSPrivateKeyParameters var2 = getInstance(var0);
      var2.publicKey = HSSPublicKeyParameters.getInstance(var1);
      return var2;
   }

   public static HSSPrivateKeyParameters getInstance(Object var0) throws IOException {
      if (var0 instanceof HSSPrivateKeyParameters) {
         return (HSSPrivateKeyParameters)var0;
      } else if (!(var0 instanceof DataInputStream)) {
         if (var0 instanceof byte[]) {
            DataInputStream var13 = null;

            HSSPrivateKeyParameters var14;
            try {
               var13 = new DataInputStream(new ByteArrayInputStream((byte[])((byte[])var0)));
               var14 = getInstance(var13);
            } finally {
               if (var13 != null) {
                  var13.close();
               }

            }

            return var14;
         } else if (var0 instanceof InputStream) {
            return getInstance(Streams.readAll((InputStream)var0));
         } else {
            throw new IllegalArgumentException("cannot parse " + var0);
         }
      } else if (((DataInputStream)var0).readInt() != 0) {
         throw new IllegalStateException("unknown version for hss private key");
      } else {
         int var1 = ((DataInputStream)var0).readInt();
         long var2 = ((DataInputStream)var0).readLong();
         long var4 = ((DataInputStream)var0).readLong();
         boolean var6 = ((DataInputStream)var0).readBoolean();
         ArrayList var7 = new ArrayList();
         ArrayList var8 = new ArrayList();

         int var9;
         for(var9 = 0; var9 < var1; ++var9) {
            var7.add(LMSPrivateKeyParameters.getInstance(var0));
         }

         for(var9 = 0; var9 < var1 - 1; ++var9) {
            var8.add(LMSSignature.getInstance(var0));
         }

         return new HSSPrivateKeyParameters(var1, var7, var8, var2, var4, var6);
      }
   }

   public int getL() {
      return this.l;
   }

   private static HSSPrivateKeyParameters makeCopy(HSSPrivateKeyParameters var0) {
      try {
         return getInstance(var0.getEncoded());
      } catch (Exception var2) {
         throw new RuntimeException(var2.getMessage(), var2);
      }
   }

   LMSPrivateKeyParameters getRootKey() {
      return (LMSPrivateKeyParameters)this.keys.get(0);
   }

   public synchronized HSSPublicKeyParameters getPublicKey() {
      return new HSSPublicKeyParameters(this.l, this.getRootKey().getPublicKey());
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (var1 != null && this.getClass() == var1.getClass()) {
         HSSPrivateKeyParameters var2 = (HSSPrivateKeyParameters)var1;
         if (this.l != var2.l) {
            return false;
         } else if (this.isShard != var2.isShard) {
            return false;
         } else if (this.indexLimit != var2.indexLimit) {
            return false;
         } else if (this.index != var2.index) {
            return false;
         } else {
            return !this.keys.equals(var2.keys) ? false : this.sig.equals(var2.sig);
         }
      } else {
         return false;
      }
   }

   public synchronized byte[] getEncoded() throws IOException {
      Composer var1 = Composer.compose().u32str(0).u32str(this.l).u64str(this.index).u64str(this.indexLimit).bool(this.isShard);
      Iterator var2 = this.keys.iterator();

      while(var2.hasNext()) {
         LMSPrivateKeyParameters var3 = (LMSPrivateKeyParameters)var2.next();
         var1.bytes((Encodable)var3);
      }

      var2 = this.sig.iterator();

      while(var2.hasNext()) {
         LMSSignature var4 = (LMSSignature)var2.next();
         var1.bytes((Encodable)var4);
      }

      return var1.build();
   }

   public int hashCode() {
      int var1 = this.l;
      var1 = 31 * var1 + (this.isShard ? 1 : 0);
      var1 = 31 * var1 + this.keys.hashCode();
      var1 = 31 * var1 + this.sig.hashCode();
      var1 = 31 * var1 + (int)(this.indexLimit ^ this.indexLimit >>> 32);
      var1 = 31 * var1 + (int)(this.index ^ this.index >>> 32);
      return var1;
   }

   protected Object clone() throws CloneNotSupportedException {
      return makeCopy(this);
   }
}
