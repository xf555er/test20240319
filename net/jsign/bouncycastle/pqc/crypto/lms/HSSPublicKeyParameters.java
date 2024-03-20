package net.jsign.bouncycastle.pqc.crypto.lms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.jsign.bouncycastle.util.io.Streams;

public class HSSPublicKeyParameters extends LMSKeyParameters {
   private final int l;
   private final LMSPublicKeyParameters lmsPublicKey;

   public HSSPublicKeyParameters(int var1, LMSPublicKeyParameters var2) {
      super(false);
      this.l = var1;
      this.lmsPublicKey = var2;
   }

   public static HSSPublicKeyParameters getInstance(Object var0) throws IOException {
      if (var0 instanceof HSSPublicKeyParameters) {
         return (HSSPublicKeyParameters)var0;
      } else if (var0 instanceof DataInputStream) {
         int var6 = ((DataInputStream)var0).readInt();
         LMSPublicKeyParameters var7 = LMSPublicKeyParameters.getInstance(var0);
         return new HSSPublicKeyParameters(var6, var7);
      } else if (var0 instanceof byte[]) {
         DataInputStream var1 = null;

         HSSPublicKeyParameters var2;
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

   public int getL() {
      return this.l;
   }

   public LMSPublicKeyParameters getLMSPublicKey() {
      return this.lmsPublicKey;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (var1 != null && this.getClass() == var1.getClass()) {
         HSSPublicKeyParameters var2 = (HSSPublicKeyParameters)var1;
         return this.l != var2.l ? false : this.lmsPublicKey.equals(var2.lmsPublicKey);
      } else {
         return false;
      }
   }

   public int hashCode() {
      int var1 = this.l;
      var1 = 31 * var1 + this.lmsPublicKey.hashCode();
      return var1;
   }

   public byte[] getEncoded() throws IOException {
      return Composer.compose().u32str(this.l).bytes(this.lmsPublicKey.getEncoded()).build();
   }
}
