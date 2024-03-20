package net.jsign;

import java.io.Closeable;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.jsign.asn1.authenticode.AuthenticodeDigestCalculatorProvider;
import net.jsign.asn1.authenticode.AuthenticodeObjectIdentifiers;
import net.jsign.asn1.authenticode.AuthenticodeSignedDataGenerator;
import net.jsign.asn1.authenticode.FilteredAttributeTableGenerator;
import net.jsign.asn1.authenticode.SpcSpOpusInfo;
import net.jsign.asn1.authenticode.SpcStatementType;
import net.jsign.bouncycastle.asn1.ASN1Encodable;
import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.DERNull;
import net.jsign.bouncycastle.asn1.DERSet;
import net.jsign.bouncycastle.asn1.cms.Attribute;
import net.jsign.bouncycastle.asn1.cms.AttributeTable;
import net.jsign.bouncycastle.asn1.cms.CMSAttributes;
import net.jsign.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.cert.X509CertificateHolder;
import net.jsign.bouncycastle.cert.jcajce.JcaCertStore;
import net.jsign.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import net.jsign.bouncycastle.cms.CMSAttributeTableGenerator;
import net.jsign.bouncycastle.cms.CMSException;
import net.jsign.bouncycastle.cms.CMSSignedData;
import net.jsign.bouncycastle.cms.DefaultCMSSignatureEncryptionAlgorithmFinder;
import net.jsign.bouncycastle.cms.DefaultSignedAttributeTableGenerator;
import net.jsign.bouncycastle.cms.SignerInfoGenerator;
import net.jsign.bouncycastle.cms.SignerInfoGeneratorBuilder;
import net.jsign.bouncycastle.cms.SignerInformation;
import net.jsign.bouncycastle.cms.SignerInformationStore;
import net.jsign.bouncycastle.cms.SignerInformationVerifier;
import net.jsign.bouncycastle.cms.jcajce.JcaSignerInfoVerifierBuilder;
import net.jsign.bouncycastle.operator.ContentSigner;
import net.jsign.bouncycastle.operator.DigestCalculatorProvider;
import net.jsign.bouncycastle.operator.OperatorCreationException;
import net.jsign.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import net.jsign.msi.MSIFile;
import net.jsign.pe.DataDirectory;
import net.jsign.pe.DataDirectoryType;
import net.jsign.pe.PEFile;
import net.jsign.timestamp.Timestamper;
import net.jsign.timestamp.TimestampingMode;

public class AuthenticodeSigner {
   protected Certificate[] chain;
   protected PrivateKey privateKey;
   protected DigestAlgorithm digestAlgorithm = DigestAlgorithm.getDefault();
   protected String signatureAlgorithm;
   protected Provider signatureProvider;
   protected String programName;
   protected String programURL;
   protected boolean replace;
   protected boolean timestamping = true;
   protected TimestampingMode tsmode;
   protected String[] tsaurlOverride;
   protected Timestamper timestamper;
   protected int timestampingRetries;
   protected int timestampingRetryWait;

   public AuthenticodeSigner(Certificate[] chain, PrivateKey privateKey) {
      this.tsmode = TimestampingMode.AUTHENTICODE;
      this.timestampingRetries = -1;
      this.timestampingRetryWait = -1;
      this.chain = chain;
      this.privateKey = privateKey;
      if (chain == null || chain.length == 0) {
         throw new IllegalArgumentException("The certificate chain is empty");
      }
   }

   public AuthenticodeSigner(KeyStore keystore, String alias, String password) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
      this.tsmode = TimestampingMode.AUTHENTICODE;
      this.timestampingRetries = -1;
      this.timestampingRetryWait = -1;
      Certificate[] chain = keystore.getCertificateChain(alias);
      if (chain == null) {
         throw new IllegalArgumentException("No certificate found in the keystore with the alias '" + alias + "'");
      } else {
         this.chain = chain;
         this.privateKey = (PrivateKey)keystore.getKey(alias, password != null ? password.toCharArray() : null);
      }
   }

   public AuthenticodeSigner withProgramName(String programName) {
      this.programName = programName;
      return this;
   }

   public AuthenticodeSigner withProgramURL(String programURL) {
      this.programURL = programURL;
      return this;
   }

   public AuthenticodeSigner withSignaturesReplaced(boolean replace) {
      this.replace = replace;
      return this;
   }

   public AuthenticodeSigner withTimestamping(boolean timestamping) {
      this.timestamping = timestamping;
      return this;
   }

   public AuthenticodeSigner withTimestampingMode(TimestampingMode tsmode) {
      this.tsmode = tsmode;
      return this;
   }

   public AuthenticodeSigner withTimestampingAuthority(String url) {
      return this.withTimestampingAuthority(url);
   }

   public AuthenticodeSigner withTimestampingAuthority(String... urls) {
      this.tsaurlOverride = urls;
      return this;
   }

   public AuthenticodeSigner withTimestamper(Timestamper timestamper) {
      this.timestamper = timestamper;
      return this;
   }

   public AuthenticodeSigner withTimestampingRetries(int timestampingRetries) {
      this.timestampingRetries = timestampingRetries;
      return this;
   }

   public AuthenticodeSigner withTimestampingRetryWait(int timestampingRetryWait) {
      this.timestampingRetryWait = timestampingRetryWait;
      return this;
   }

   public AuthenticodeSigner withDigestAlgorithm(DigestAlgorithm algorithm) {
      if (algorithm != null) {
         this.digestAlgorithm = algorithm;
      }

      return this;
   }

   public AuthenticodeSigner withSignatureAlgorithm(String signatureAlgorithm) {
      this.signatureAlgorithm = signatureAlgorithm;
      return this;
   }

   public AuthenticodeSigner withSignatureAlgorithm(String signatureAlgorithm, String signatureProvider) {
      return this.withSignatureAlgorithm(signatureAlgorithm, Security.getProvider(signatureProvider));
   }

   public AuthenticodeSigner withSignatureAlgorithm(String signatureAlgorithm, Provider signatureProvider) {
      this.signatureAlgorithm = signatureAlgorithm;
      this.signatureProvider = signatureProvider;
      return this;
   }

   public AuthenticodeSigner withSignatureProvider(Provider signatureProvider) {
      this.signatureProvider = signatureProvider;
      return this;
   }

   public void sign(Signable file) throws Exception {
      if (file instanceof PEFile) {
         PEFile pefile = (PEFile)file;
         pefile.pad(8);
         if (this.replace) {
            DataDirectory certificateTable = pefile.getDataDirectory(DataDirectoryType.CERTIFICATE_TABLE);
            if (certificateTable != null && !certificateTable.isTrailing()) {
               certificateTable.erase();
               certificateTable.write(0L, 0);
            }
         }
      } else if (file instanceof MSIFile) {
         MSIFile msi = (MSIFile)file;
         if (!this.replace && msi.hasExtendedSignature()) {
            throw new UnsupportedOperationException("The file has an extended signature which isn't supported by Jsign, it can't be signed without replacing the existing signature");
         }
      }

      CMSSignedData sigData = this.createSignedData(file);
      if (!this.replace) {
         List signatures = file.getSignatures();
         if (!signatures.isEmpty()) {
            sigData = this.addNestedSignature((CMSSignedData)signatures.get(0), sigData);
         }
      }

      file.setSignature(sigData);
      file.save();
      if (file instanceof Closeable) {
         ((Closeable)file).close();
      }

   }

   protected CMSSignedData createSignedData(Signable file) throws Exception {
      AuthenticodeSignedDataGenerator generator = this.createSignedDataGenerator();
      CMSSignedData sigData = generator.generate(AuthenticodeObjectIdentifiers.SPC_INDIRECT_DATA_OBJID, file.createIndirectData(this.digestAlgorithm));
      DigestCalculatorProvider digestCalculatorProvider = new AuthenticodeDigestCalculatorProvider();
      SignerInformationVerifier verifier = (new JcaSignerInfoVerifierBuilder(digestCalculatorProvider)).build(this.chain[0].getPublicKey());
      ((SignerInformation)sigData.getSignerInfos().iterator().next()).verify(verifier);
      if (this.timestamping) {
         Timestamper ts = this.timestamper;
         if (ts == null) {
            ts = Timestamper.create(this.tsmode);
         }

         if (this.tsaurlOverride != null) {
            ts.setURLs(this.tsaurlOverride);
         }

         if (this.timestampingRetries != -1) {
            ts.setRetries(this.timestampingRetries);
         }

         if (this.timestampingRetryWait != -1) {
            ts.setRetryWait(this.timestampingRetryWait);
         }

         sigData = ts.timestamp(this.digestAlgorithm, sigData);
      }

      return sigData;
   }

   private AuthenticodeSignedDataGenerator createSignedDataGenerator() throws CMSException, OperatorCreationException, CertificateEncodingException {
      String sigAlg;
      if (this.signatureAlgorithm == null) {
         sigAlg = this.digestAlgorithm + "with" + this.privateKey.getAlgorithm();
      } else {
         sigAlg = this.signatureAlgorithm;
      }

      JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder(sigAlg);
      if (this.signatureProvider != null) {
         contentSignerBuilder.setProvider(this.signatureProvider);
      }

      ContentSigner shaSigner = contentSignerBuilder.build(this.privateKey);
      DigestCalculatorProvider digestCalculatorProvider = new AuthenticodeDigestCalculatorProvider();
      CMSAttributeTableGenerator attributeTableGenerator = new DefaultSignedAttributeTableGenerator(this.createAuthenticatedAttributes());
      CMSAttributeTableGenerator attributeTableGenerator = new FilteredAttributeTableGenerator(attributeTableGenerator, new ASN1ObjectIdentifier[]{CMSAttributes.signingTime, CMSAttributes.cmsAlgorithmProtect});
      X509CertificateHolder certificate = new JcaX509CertificateHolder((X509Certificate)this.chain[0]);
      SignerInfoGeneratorBuilder signerInfoGeneratorBuilder = new SignerInfoGeneratorBuilder(digestCalculatorProvider, new DefaultCMSSignatureEncryptionAlgorithmFinder() {
         public AlgorithmIdentifier findEncryptionAlgorithm(AlgorithmIdentifier signatureAlgorithm) {
            return !signatureAlgorithm.getAlgorithm().equals(PKCSObjectIdentifiers.sha256WithRSAEncryption) && !signatureAlgorithm.getAlgorithm().equals(PKCSObjectIdentifiers.sha384WithRSAEncryption) && !signatureAlgorithm.getAlgorithm().equals(PKCSObjectIdentifiers.sha512WithRSAEncryption) ? super.findEncryptionAlgorithm(signatureAlgorithm) : new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE);
         }
      });
      signerInfoGeneratorBuilder.setSignedAttributeGenerator(attributeTableGenerator);
      SignerInfoGenerator signerInfoGenerator = signerInfoGeneratorBuilder.build(shaSigner, certificate);
      AuthenticodeSignedDataGenerator generator = new AuthenticodeSignedDataGenerator();
      generator.addCertificates(new JcaCertStore(this.removeRoot(this.chain)));
      generator.addSignerInfoGenerator(signerInfoGenerator);
      return generator;
   }

   private List removeRoot(Certificate[] certificates) {
      List list = new ArrayList();
      if (certificates.length == 1) {
         list.add(certificates[0]);
      } else {
         Certificate[] var3 = certificates;
         int var4 = certificates.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Certificate certificate = var3[var5];
            if (!this.isSelfSigned((X509Certificate)certificate)) {
               list.add(certificate);
            }
         }
      }

      return list;
   }

   private boolean isSelfSigned(X509Certificate certificate) {
      return certificate.getSubjectDN().equals(certificate.getIssuerDN());
   }

   private AttributeTable createAuthenticatedAttributes() {
      List attributes = new ArrayList();
      SpcStatementType spcStatementType = new SpcStatementType(AuthenticodeObjectIdentifiers.SPC_INDIVIDUAL_SP_KEY_PURPOSE_OBJID);
      attributes.add(new Attribute(AuthenticodeObjectIdentifiers.SPC_STATEMENT_TYPE_OBJID, new DERSet(spcStatementType)));
      SpcSpOpusInfo spcSpOpusInfo = new SpcSpOpusInfo(this.programName, this.programURL);
      attributes.add(new Attribute(AuthenticodeObjectIdentifiers.SPC_SP_OPUS_INFO_OBJID, new DERSet(spcSpOpusInfo)));
      return new AttributeTable(new DERSet((ASN1Encodable[])attributes.toArray(new ASN1Encodable[0])));
   }

   protected CMSSignedData addNestedSignature(CMSSignedData primary, CMSSignedData secondary) {
      SignerInformation signerInformation = (SignerInformation)primary.getSignerInfos().getSigners().iterator().next();
      AttributeTable unsignedAttributes = signerInformation.getUnsignedAttributes();
      if (unsignedAttributes == null) {
         unsignedAttributes = new AttributeTable(new DERSet());
      }

      Attribute nestedSignaturesAttribute = unsignedAttributes.get(AuthenticodeObjectIdentifiers.SPC_NESTED_SIGNATURE_OBJID);
      if (nestedSignaturesAttribute == null) {
         unsignedAttributes = unsignedAttributes.add(AuthenticodeObjectIdentifiers.SPC_NESTED_SIGNATURE_OBJID, secondary.toASN1Structure());
      } else {
         ASN1EncodableVector nestedSignatures = new ASN1EncodableVector();
         Iterator var7 = nestedSignaturesAttribute.getAttrValues().iterator();

         while(var7.hasNext()) {
            ASN1Encodable nestedSignature = (ASN1Encodable)var7.next();
            nestedSignatures.add(nestedSignature);
         }

         nestedSignatures.add(secondary.toASN1Structure());
         ASN1EncodableVector attributes = unsignedAttributes.remove(AuthenticodeObjectIdentifiers.SPC_NESTED_SIGNATURE_OBJID).toASN1EncodableVector();
         attributes.add(new Attribute(AuthenticodeObjectIdentifiers.SPC_NESTED_SIGNATURE_OBJID, new DERSet(nestedSignatures)));
         unsignedAttributes = new AttributeTable(attributes);
      }

      signerInformation = SignerInformation.replaceUnsignedAttributes(signerInformation, unsignedAttributes);
      return CMSSignedData.replaceSigners(primary, new SignerInformationStore(signerInformation));
   }
}
