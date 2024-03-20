package net.jsign.bouncycastle.jcajce.io;

import java.io.IOException;
import java.io.OutputStream;
import java.security.Signature;
import java.security.SignatureException;

class SignatureUpdatingOutputStream extends OutputStream {
   private Signature sig;

   SignatureUpdatingOutputStream(Signature var1) {
      this.sig = var1;
   }

   public void write(byte[] var1, int var2, int var3) throws IOException {
      try {
         this.sig.update(var1, var2, var3);
      } catch (SignatureException var5) {
         throw new IOException(var5.getMessage());
      }
   }

   public void write(byte[] var1) throws IOException {
      try {
         this.sig.update(var1);
      } catch (SignatureException var3) {
         throw new IOException(var3.getMessage());
      }
   }

   public void write(int var1) throws IOException {
      try {
         this.sig.update((byte)var1);
      } catch (SignatureException var3) {
         throw new IOException(var3.getMessage());
      }
   }
}
