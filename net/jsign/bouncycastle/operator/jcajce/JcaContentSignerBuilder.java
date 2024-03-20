package net.jsign.bouncycastle.operator.jcajce;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.List;
import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.DERBitString;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.jcajce.CompositePrivateKey;
import net.jsign.bouncycastle.jcajce.io.OutputStreamFactory;
import net.jsign.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import net.jsign.bouncycastle.jcajce.util.ProviderJcaJceHelper;
import net.jsign.bouncycastle.operator.ContentSigner;
import net.jsign.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import net.jsign.bouncycastle.operator.OperatorCreationException;
import net.jsign.bouncycastle.operator.RuntimeOperatorException;
import net.jsign.bouncycastle.util.io.TeeOutputStream;

public class JcaContentSignerBuilder {
   private OperatorHelper helper = new OperatorHelper(new DefaultJcaJceHelper());
   private SecureRandom random;
   private String signatureAlgorithm;
   private AlgorithmIdentifier sigAlgId;
   private AlgorithmParameterSpec sigAlgSpec;

   public JcaContentSignerBuilder(String var1) {
      this.signatureAlgorithm = var1;
      this.sigAlgId = (new DefaultSignatureAlgorithmIdentifierFinder()).find(var1);
      this.sigAlgSpec = null;
   }

   public JcaContentSignerBuilder setProvider(Provider var1) {
      this.helper = new OperatorHelper(new ProviderJcaJceHelper(var1));
      return this;
   }

   public ContentSigner build(PrivateKey var1) throws OperatorCreationException {
      if (var1 instanceof CompositePrivateKey) {
         return this.buildComposite((CompositePrivateKey)var1);
      } else {
         try {
            final Signature var2 = this.helper.createSignature(this.sigAlgId);
            final AlgorithmIdentifier var3 = this.sigAlgId;
            if (this.random != null) {
               var2.initSign(var1, this.random);
            } else {
               var2.initSign(var1);
            }

            return new ContentSigner() {
               private OutputStream stream = OutputStreamFactory.createStream(var2);

               public AlgorithmIdentifier getAlgorithmIdentifier() {
                  return var3;
               }

               public OutputStream getOutputStream() {
                  return this.stream;
               }

               public byte[] getSignature() {
                  try {
                     return var2.sign();
                  } catch (SignatureException var2x) {
                     throw new RuntimeOperatorException("exception obtaining signature: " + var2x.getMessage(), var2x);
                  }
               }
            };
         } catch (GeneralSecurityException var4) {
            throw new OperatorCreationException("cannot create signer: " + var4.getMessage(), var4);
         }
      }
   }

   private ContentSigner buildComposite(CompositePrivateKey var1) throws OperatorCreationException {
      try {
         List var2 = var1.getPrivateKeys();
         ASN1Sequence var3 = ASN1Sequence.getInstance(this.sigAlgId.getParameters());
         final Signature[] var4 = new Signature[var3.size()];

         for(int var5 = 0; var5 != var3.size(); ++var5) {
            var4[var5] = this.helper.createSignature(AlgorithmIdentifier.getInstance(var3.getObjectAt(var5)));
            if (this.random != null) {
               var4[var5].initSign((PrivateKey)var2.get(var5), this.random);
            } else {
               var4[var5].initSign((PrivateKey)var2.get(var5));
            }
         }

         final Object var8 = OutputStreamFactory.createStream(var4[0]);

         for(int var6 = 1; var6 != var4.length; ++var6) {
            var8 = new TeeOutputStream((OutputStream)var8, OutputStreamFactory.createStream(var4[var6]));
         }

         return new ContentSigner() {
            OutputStream stream = var8;

            public AlgorithmIdentifier getAlgorithmIdentifier() {
               return JcaContentSignerBuilder.this.sigAlgId;
            }

            public OutputStream getOutputStream() {
               return this.stream;
            }

            public byte[] getSignature() {
               try {
                  ASN1EncodableVector var1 = new ASN1EncodableVector();

                  for(int var2 = 0; var2 != var4.length; ++var2) {
                     var1.add(new DERBitString(var4[var2].sign()));
                  }

                  return (new DERSequence(var1)).getEncoded("DER");
               } catch (IOException var3) {
                  throw new RuntimeOperatorException("exception encoding signature: " + var3.getMessage(), var3);
               } catch (SignatureException var4x) {
                  throw new RuntimeOperatorException("exception obtaining signature: " + var4x.getMessage(), var4x);
               }
            }
         };
      } catch (GeneralSecurityException var7) {
         throw new OperatorCreationException("cannot create signer: " + var7.getMessage(), var7);
      }
   }
}
