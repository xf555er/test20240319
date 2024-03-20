package net.jsign.bouncycastle.cms.jcajce;

import java.security.PublicKey;
import net.jsign.bouncycastle.cms.CMSSignatureAlgorithmNameGenerator;
import net.jsign.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import net.jsign.bouncycastle.cms.SignerInformationVerifier;
import net.jsign.bouncycastle.operator.ContentVerifierProvider;
import net.jsign.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import net.jsign.bouncycastle.operator.DigestCalculatorProvider;
import net.jsign.bouncycastle.operator.OperatorCreationException;
import net.jsign.bouncycastle.operator.SignatureAlgorithmIdentifierFinder;
import net.jsign.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;

public class JcaSignerInfoVerifierBuilder {
   private Helper helper = new Helper();
   private DigestCalculatorProvider digestProvider;
   private CMSSignatureAlgorithmNameGenerator sigAlgNameGen = new DefaultCMSSignatureAlgorithmNameGenerator();
   private SignatureAlgorithmIdentifierFinder sigAlgIDFinder = new DefaultSignatureAlgorithmIdentifierFinder();

   public JcaSignerInfoVerifierBuilder(DigestCalculatorProvider var1) {
      this.digestProvider = var1;
   }

   public SignerInformationVerifier build(PublicKey var1) throws OperatorCreationException {
      return new SignerInformationVerifier(this.sigAlgNameGen, this.sigAlgIDFinder, this.helper.createContentVerifierProvider(var1), this.digestProvider);
   }

   private class Helper {
      private Helper() {
      }

      ContentVerifierProvider createContentVerifierProvider(PublicKey var1) throws OperatorCreationException {
         return (new JcaContentVerifierProviderBuilder()).build(var1);
      }

      // $FF: synthetic method
      Helper(Object var2) {
         this();
      }
   }
}
