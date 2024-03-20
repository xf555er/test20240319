package net.jsign.bouncycastle.operator.jcajce;

import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.List;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.DERBitString;
import net.jsign.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.cert.X509CertificateHolder;
import net.jsign.bouncycastle.jcajce.CompositePublicKey;
import net.jsign.bouncycastle.jcajce.io.OutputStreamFactory;
import net.jsign.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import net.jsign.bouncycastle.operator.ContentVerifier;
import net.jsign.bouncycastle.operator.ContentVerifierProvider;
import net.jsign.bouncycastle.operator.OperatorCreationException;
import net.jsign.bouncycastle.operator.RawContentVerifier;
import net.jsign.bouncycastle.operator.RuntimeOperatorException;
import net.jsign.bouncycastle.util.io.TeeOutputStream;

public class JcaContentVerifierProviderBuilder {
   private OperatorHelper helper = new OperatorHelper(new DefaultJcaJceHelper());

   public ContentVerifierProvider build(final PublicKey var1) throws OperatorCreationException {
      return new ContentVerifierProvider() {
         public boolean hasAssociatedCertificate() {
            return false;
         }

         public X509CertificateHolder getAssociatedCertificate() {
            return null;
         }

         public ContentVerifier get(AlgorithmIdentifier var1x) throws OperatorCreationException {
            if (var1x.getAlgorithm().equals(MiscObjectIdentifiers.id_alg_composite)) {
               return JcaContentVerifierProviderBuilder.this.createCompositeVerifier(var1x, var1);
            } else if (!(var1 instanceof CompositePublicKey)) {
               Signature var7 = JcaContentVerifierProviderBuilder.this.createSignature(var1x, var1);
               Signature var8 = JcaContentVerifierProviderBuilder.this.createRawSig(var1x, var1);
               return (ContentVerifier)(var8 != null ? JcaContentVerifierProviderBuilder.this.new RawSigVerifier(var1x, var7, var8) : JcaContentVerifierProviderBuilder.this.new SigVerifier(var1x, var7));
            } else {
               List var2 = ((CompositePublicKey)var1).getPublicKeys();
               int var3 = 0;

               while(var3 != var2.size()) {
                  try {
                     Signature var4 = JcaContentVerifierProviderBuilder.this.createSignature(var1x, (PublicKey)var2.get(var3));
                     Signature var5 = JcaContentVerifierProviderBuilder.this.createRawSig(var1x, (PublicKey)var2.get(var3));
                     if (var5 != null) {
                        return JcaContentVerifierProviderBuilder.this.new RawSigVerifier(var1x, var4, var5);
                     }

                     return JcaContentVerifierProviderBuilder.this.new SigVerifier(var1x, var4);
                  } catch (OperatorCreationException var6) {
                     ++var3;
                  }
               }

               throw new OperatorCreationException("no matching algorithm found for key");
            }
         }
      };
   }

   private ContentVerifier createCompositeVerifier(AlgorithmIdentifier var1, PublicKey var2) throws OperatorCreationException {
      if (var2 instanceof CompositePublicKey) {
         List var9 = ((CompositePublicKey)var2).getPublicKeys();
         ASN1Sequence var10 = ASN1Sequence.getInstance(var1.getParameters());
         Signature[] var11 = new Signature[var10.size()];

         for(int var12 = 0; var12 != var10.size(); ++var12) {
            AlgorithmIdentifier var7 = AlgorithmIdentifier.getInstance(var10.getObjectAt(var12));
            if (var9.get(var12) != null) {
               var11[var12] = this.createSignature(var7, (PublicKey)var9.get(var12));
            } else {
               var11[var12] = null;
            }
         }

         return new CompositeVerifier(var11);
      } else {
         ASN1Sequence var3 = ASN1Sequence.getInstance(var1.getParameters());
         Signature[] var4 = new Signature[var3.size()];

         for(int var5 = 0; var5 != var3.size(); ++var5) {
            AlgorithmIdentifier var6 = AlgorithmIdentifier.getInstance(var3.getObjectAt(var5));

            try {
               var4[var5] = this.createSignature(var6, var2);
            } catch (Exception var8) {
               var4[var5] = null;
            }
         }

         return new CompositeVerifier(var4);
      }
   }

   private Signature createSignature(AlgorithmIdentifier var1, PublicKey var2) throws OperatorCreationException {
      try {
         Signature var3 = this.helper.createSignature(var1);
         var3.initVerify(var2);
         return var3;
      } catch (GeneralSecurityException var4) {
         throw new OperatorCreationException("exception on setup: " + var4, var4);
      }
   }

   private Signature createRawSig(AlgorithmIdentifier var1, PublicKey var2) {
      Signature var3;
      try {
         var3 = this.helper.createRawSignature(var1);
         if (var3 != null) {
            var3.initVerify(var2);
         }
      } catch (Exception var5) {
         var3 = null;
      }

      return var3;
   }

   private class CompositeVerifier implements ContentVerifier {
      private Signature[] sigs;
      private OutputStream stream;

      public CompositeVerifier(Signature[] var2) throws OperatorCreationException {
         this.sigs = var2;

         int var3;
         for(var3 = 0; var3 < var2.length && var2[var3] == null; ++var3) {
         }

         if (var3 == var2.length) {
            throw new OperatorCreationException("no matching signature found in composite");
         } else {
            this.stream = OutputStreamFactory.createStream(var2[var3]);

            for(int var4 = var3 + 1; var4 != var2.length; ++var4) {
               if (var2[var4] != null) {
                  this.stream = new TeeOutputStream(this.stream, OutputStreamFactory.createStream(var2[var4]));
               }
            }

         }
      }

      public OutputStream getOutputStream() {
         return this.stream;
      }

      public boolean verify(byte[] var1) {
         try {
            ASN1Sequence var2 = ASN1Sequence.getInstance(var1);
            boolean var3 = false;

            for(int var4 = 0; var4 != var2.size(); ++var4) {
               if (this.sigs[var4] != null && !this.sigs[var4].verify(DERBitString.getInstance(var2.getObjectAt(var4)).getBytes())) {
                  var3 = true;
               }
            }

            return !var3;
         } catch (SignatureException var5) {
            throw new RuntimeOperatorException("exception obtaining signature: " + var5.getMessage(), var5);
         }
      }
   }

   private class RawSigVerifier extends SigVerifier implements RawContentVerifier {
      private Signature rawSignature;

      RawSigVerifier(AlgorithmIdentifier var2, Signature var3, Signature var4) {
         super(var2, var3);
         this.rawSignature = var4;
      }

      public boolean verify(byte[] var1) {
         boolean var2;
         try {
            var2 = super.verify(var1);
         } finally {
            try {
               this.rawSignature.verify(var1);
            } catch (Exception var9) {
            }

         }

         return var2;
      }

      public boolean verify(byte[] var1, byte[] var2) {
         boolean var3;
         try {
            this.rawSignature.update(var1);
            var3 = this.rawSignature.verify(var2);
         } catch (SignatureException var12) {
            throw new RuntimeOperatorException("exception obtaining raw signature: " + var12.getMessage(), var12);
         } finally {
            try {
               this.rawSignature.verify(var2);
            } catch (Exception var11) {
            }

         }

         return var3;
      }
   }

   private class SigVerifier implements ContentVerifier {
      private final AlgorithmIdentifier algorithm;
      private final Signature signature;
      protected final OutputStream stream;

      SigVerifier(AlgorithmIdentifier var2, Signature var3) {
         this.algorithm = var2;
         this.signature = var3;
         this.stream = OutputStreamFactory.createStream(var3);
      }

      public OutputStream getOutputStream() {
         if (this.stream == null) {
            throw new IllegalStateException("verifier not initialised");
         } else {
            return this.stream;
         }
      }

      public boolean verify(byte[] var1) {
         try {
            return this.signature.verify(var1);
         } catch (SignatureException var3) {
            throw new RuntimeOperatorException("exception obtaining signature: " + var3.getMessage(), var3);
         }
      }
   }
}
