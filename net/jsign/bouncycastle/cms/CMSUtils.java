package net.jsign.bouncycastle.cms;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.jsign.bouncycastle.asn1.ASN1Encodable;
import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1InputStream;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1Set;
import net.jsign.bouncycastle.asn1.ASN1TaggedObject;
import net.jsign.bouncycastle.asn1.BERSet;
import net.jsign.bouncycastle.asn1.DERNull;
import net.jsign.bouncycastle.asn1.DERTaggedObject;
import net.jsign.bouncycastle.asn1.DLSet;
import net.jsign.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import net.jsign.bouncycastle.asn1.cms.ContentInfo;
import net.jsign.bouncycastle.asn1.cms.OtherRevocationInfoFormat;
import net.jsign.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import net.jsign.bouncycastle.asn1.ocsp.OCSPResponse;
import net.jsign.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import net.jsign.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import net.jsign.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import net.jsign.bouncycastle.asn1.sec.SECObjectIdentifiers;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import net.jsign.bouncycastle.cert.X509AttributeCertificateHolder;
import net.jsign.bouncycastle.cert.X509CRLHolder;
import net.jsign.bouncycastle.cert.X509CertificateHolder;
import net.jsign.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import net.jsign.bouncycastle.util.Selector;
import net.jsign.bouncycastle.util.Store;
import net.jsign.bouncycastle.util.io.TeeOutputStream;

class CMSUtils {
   private static final Set des = new HashSet();
   private static final Set mqvAlgs = new HashSet();
   private static final Set ecAlgs = new HashSet();
   private static final Set gostAlgs = new HashSet();

   static boolean isEquivalent(AlgorithmIdentifier var0, AlgorithmIdentifier var1) {
      if (var0 != null && var1 != null) {
         if (!var0.getAlgorithm().equals(var1.getAlgorithm())) {
            return false;
         } else {
            ASN1Encodable var2 = var0.getParameters();
            ASN1Encodable var3 = var1.getParameters();
            if (var2 != null) {
               return var2.equals(var3) || var2.equals(DERNull.INSTANCE) && var3 == null;
            } else {
               return var3 == null || var3.equals(DERNull.INSTANCE);
            }
         }
      } else {
         return false;
      }
   }

   static ContentInfo readContentInfo(byte[] var0) throws CMSException {
      return readContentInfo(new ASN1InputStream(var0));
   }

   static ASN1Set convertToBERSet(Set var0) {
      return new DLSet((AlgorithmIdentifier[])((AlgorithmIdentifier[])var0.toArray(new AlgorithmIdentifier[var0.size()])));
   }

   static void addDigestAlgs(Set var0, SignerInformation var1, DigestAlgorithmIdentifierFinder var2) {
      var0.add(CMSSignedHelper.INSTANCE.fixDigestAlgID(var1.getDigestAlgorithmID(), var2));
      SignerInformationStore var3 = var1.getCounterSignatures();
      Iterator var4 = var3.iterator();

      while(var4.hasNext()) {
         SignerInformation var5 = (SignerInformation)var4.next();
         var0.add(CMSSignedHelper.INSTANCE.fixDigestAlgID(var5.getDigestAlgorithmID(), var2));
      }

   }

   static List getCertificatesFromStore(Store var0) throws CMSException {
      ArrayList var1 = new ArrayList();

      try {
         Iterator var2 = var0.getMatches((Selector)null).iterator();

         while(var2.hasNext()) {
            X509CertificateHolder var3 = (X509CertificateHolder)var2.next();
            var1.add(var3.toASN1Structure());
         }

         return var1;
      } catch (ClassCastException var4) {
         throw new CMSException("error processing certs", var4);
      }
   }

   static List getAttributeCertificatesFromStore(Store var0) throws CMSException {
      ArrayList var1 = new ArrayList();

      try {
         Iterator var2 = var0.getMatches((Selector)null).iterator();

         while(var2.hasNext()) {
            X509AttributeCertificateHolder var3 = (X509AttributeCertificateHolder)var2.next();
            var1.add(new DERTaggedObject(false, 2, var3.toASN1Structure()));
         }

         return var1;
      } catch (ClassCastException var4) {
         throw new CMSException("error processing certs", var4);
      }
   }

   static List getCRLsFromStore(Store var0) throws CMSException {
      ArrayList var1 = new ArrayList();

      try {
         Iterator var2 = var0.getMatches((Selector)null).iterator();

         while(var2.hasNext()) {
            Object var3 = var2.next();
            if (var3 instanceof X509CRLHolder) {
               X509CRLHolder var4 = (X509CRLHolder)var3;
               var1.add(var4.toASN1Structure());
            } else if (var3 instanceof OtherRevocationInfoFormat) {
               OtherRevocationInfoFormat var6 = OtherRevocationInfoFormat.getInstance(var3);
               validateInfoFormat(var6);
               var1.add(new DERTaggedObject(false, 1, var6));
            } else if (var3 instanceof ASN1TaggedObject) {
               var1.add(var3);
            }
         }

         return var1;
      } catch (ClassCastException var5) {
         throw new CMSException("error processing certs", var5);
      }
   }

   private static void validateInfoFormat(OtherRevocationInfoFormat var0) {
      if (CMSObjectIdentifiers.id_ri_ocsp_response.equals(var0.getInfoFormat())) {
         OCSPResponse var1 = OCSPResponse.getInstance(var0.getInfo());
         if (0 != var1.getResponseStatus().getIntValue()) {
            throw new IllegalArgumentException("cannot add unsuccessful OCSP response to CMS SignedData");
         }
      }

   }

   static Collection getOthersFromStore(ASN1ObjectIdentifier var0, Store var1) {
      ArrayList var2 = new ArrayList();
      Iterator var3 = var1.getMatches((Selector)null).iterator();

      while(var3.hasNext()) {
         ASN1Encodable var4 = (ASN1Encodable)var3.next();
         OtherRevocationInfoFormat var5 = new OtherRevocationInfoFormat(var0, var4);
         validateInfoFormat(var5);
         var2.add(new DERTaggedObject(false, 1, var5));
      }

      return var2;
   }

   static ASN1Set createBerSetFromList(List var0) {
      ASN1EncodableVector var1 = new ASN1EncodableVector();
      Iterator var2 = var0.iterator();

      while(var2.hasNext()) {
         var1.add((ASN1Encodable)var2.next());
      }

      return new BERSet(var1);
   }

   private static ContentInfo readContentInfo(ASN1InputStream var0) throws CMSException {
      try {
         ContentInfo var1 = ContentInfo.getInstance(var0.readObject());
         if (var1 == null) {
            throw new CMSException("No content found.");
         } else {
            return var1;
         }
      } catch (IOException var2) {
         throw new CMSException("IOException reading content.", var2);
      } catch (ClassCastException var3) {
         throw new CMSException("Malformed content.", var3);
      } catch (IllegalArgumentException var4) {
         throw new CMSException("Malformed content.", var4);
      }
   }

   static OutputStream attachSignersToOutputStream(Collection var0, OutputStream var1) {
      OutputStream var2 = var1;

      SignerInfoGenerator var4;
      for(Iterator var3 = var0.iterator(); var3.hasNext(); var2 = getSafeTeeOutputStream(var2, var4.getCalculatingOutputStream())) {
         var4 = (SignerInfoGenerator)var3.next();
      }

      return var2;
   }

   static OutputStream getSafeOutputStream(OutputStream var0) {
      return (OutputStream)(var0 == null ? new NullOutputStream() : var0);
   }

   static OutputStream getSafeTeeOutputStream(OutputStream var0, OutputStream var1) {
      return (OutputStream)(var0 == null ? getSafeOutputStream(var1) : (var1 == null ? getSafeOutputStream(var0) : new TeeOutputStream(var0, var1)));
   }

   static {
      des.add("DES");
      des.add("DESEDE");
      des.add(OIWObjectIdentifiers.desCBC.getId());
      des.add(PKCSObjectIdentifiers.des_EDE3_CBC.getId());
      des.add(PKCSObjectIdentifiers.id_alg_CMS3DESwrap.getId());
      mqvAlgs.add(X9ObjectIdentifiers.mqvSinglePass_sha1kdf_scheme);
      mqvAlgs.add(SECObjectIdentifiers.mqvSinglePass_sha224kdf_scheme);
      mqvAlgs.add(SECObjectIdentifiers.mqvSinglePass_sha256kdf_scheme);
      mqvAlgs.add(SECObjectIdentifiers.mqvSinglePass_sha384kdf_scheme);
      mqvAlgs.add(SECObjectIdentifiers.mqvSinglePass_sha512kdf_scheme);
      ecAlgs.add(X9ObjectIdentifiers.dhSinglePass_cofactorDH_sha1kdf_scheme);
      ecAlgs.add(X9ObjectIdentifiers.dhSinglePass_stdDH_sha1kdf_scheme);
      ecAlgs.add(SECObjectIdentifiers.dhSinglePass_cofactorDH_sha224kdf_scheme);
      ecAlgs.add(SECObjectIdentifiers.dhSinglePass_stdDH_sha224kdf_scheme);
      ecAlgs.add(SECObjectIdentifiers.dhSinglePass_cofactorDH_sha256kdf_scheme);
      ecAlgs.add(SECObjectIdentifiers.dhSinglePass_stdDH_sha256kdf_scheme);
      ecAlgs.add(SECObjectIdentifiers.dhSinglePass_cofactorDH_sha384kdf_scheme);
      ecAlgs.add(SECObjectIdentifiers.dhSinglePass_stdDH_sha384kdf_scheme);
      ecAlgs.add(SECObjectIdentifiers.dhSinglePass_cofactorDH_sha512kdf_scheme);
      ecAlgs.add(SECObjectIdentifiers.dhSinglePass_stdDH_sha512kdf_scheme);
      gostAlgs.add(CryptoProObjectIdentifiers.gostR3410_2001_CryptoPro_ESDH);
      gostAlgs.add(RosstandartObjectIdentifiers.id_tc26_agreement_gost_3410_12_256);
      gostAlgs.add(RosstandartObjectIdentifiers.id_tc26_agreement_gost_3410_12_512);
   }
}
