package net.jsign.bouncycastle.pqc.crypto.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1OctetString;
import net.jsign.bouncycastle.asn1.isara.IsaraObjectIdentifiers;
import net.jsign.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import net.jsign.bouncycastle.crypto.params.AsymmetricKeyParameter;
import net.jsign.bouncycastle.pqc.asn1.McElieceCCA2PublicKey;
import net.jsign.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import net.jsign.bouncycastle.pqc.asn1.SPHINCS256KeyParams;
import net.jsign.bouncycastle.pqc.asn1.XMSSKeyParams;
import net.jsign.bouncycastle.pqc.asn1.XMSSMTKeyParams;
import net.jsign.bouncycastle.pqc.asn1.XMSSPublicKey;
import net.jsign.bouncycastle.pqc.crypto.lms.HSSPublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.lms.LMSPublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.mceliece.McElieceCCA2PublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.newhope.NHPublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.qtesla.QTESLAPublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.sphincs.SPHINCSPublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSMTParameters;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSMTPublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSParameters;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSPublicKeyParameters;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.Pack;

public class PublicKeyFactory {
   private static Map converters = new HashMap();

   public static AsymmetricKeyParameter createKey(SubjectPublicKeyInfo var0) throws IOException {
      return createKey(var0, (Object)null);
   }

   public static AsymmetricKeyParameter createKey(SubjectPublicKeyInfo var0, Object var1) throws IOException {
      AlgorithmIdentifier var2 = var0.getAlgorithm();
      SubjectPublicKeyInfoConverter var3 = (SubjectPublicKeyInfoConverter)converters.get(var2.getAlgorithm());
      if (var3 != null) {
         return var3.getPublicKeyParameters(var0, var1);
      } else {
         throw new IOException("algorithm identifier in public key not recognised: " + var2.getAlgorithm());
      }
   }

   static {
      converters.put(PQCObjectIdentifiers.qTESLA_p_I, new QTeslaConverter());
      converters.put(PQCObjectIdentifiers.qTESLA_p_III, new QTeslaConverter());
      converters.put(PQCObjectIdentifiers.sphincs256, new SPHINCSConverter());
      converters.put(PQCObjectIdentifiers.newHope, new NHConverter());
      converters.put(PQCObjectIdentifiers.xmss, new XMSSConverter());
      converters.put(PQCObjectIdentifiers.xmss_mt, new XMSSMTConverter());
      converters.put(IsaraObjectIdentifiers.id_alg_xmss, new XMSSConverter());
      converters.put(IsaraObjectIdentifiers.id_alg_xmssmt, new XMSSMTConverter());
      converters.put(PKCSObjectIdentifiers.id_alg_hss_lms_hashsig, new LMSConverter());
      converters.put(PQCObjectIdentifiers.mcElieceCca2, new McElieceCCA2Converter());
   }

   private static class LMSConverter extends SubjectPublicKeyInfoConverter {
      private LMSConverter() {
         super(null);
      }

      AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo var1, Object var2) throws IOException {
         byte[] var3 = ASN1OctetString.getInstance(var1.parsePublicKey()).getOctets();
         if (Pack.bigEndianToInt(var3, 0) == 1) {
            return LMSPublicKeyParameters.getInstance(Arrays.copyOfRange(var3, 4, var3.length));
         } else {
            if (var3.length == 64) {
               var3 = Arrays.copyOfRange(var3, 4, var3.length);
            }

            return HSSPublicKeyParameters.getInstance(var3);
         }
      }

      // $FF: synthetic method
      LMSConverter(Object var1) {
         this();
      }
   }

   private static class McElieceCCA2Converter extends SubjectPublicKeyInfoConverter {
      private McElieceCCA2Converter() {
         super(null);
      }

      AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo var1, Object var2) throws IOException {
         McElieceCCA2PublicKey var3 = McElieceCCA2PublicKey.getInstance(var1.parsePublicKey());
         return new McElieceCCA2PublicKeyParameters(var3.getN(), var3.getT(), var3.getG(), Utils.getDigestName(var3.getDigest().getAlgorithm()));
      }

      // $FF: synthetic method
      McElieceCCA2Converter(Object var1) {
         this();
      }
   }

   private static class NHConverter extends SubjectPublicKeyInfoConverter {
      private NHConverter() {
         super(null);
      }

      AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo var1, Object var2) throws IOException {
         return new NHPublicKeyParameters(var1.getPublicKeyData().getBytes());
      }

      // $FF: synthetic method
      NHConverter(Object var1) {
         this();
      }
   }

   private static class QTeslaConverter extends SubjectPublicKeyInfoConverter {
      private QTeslaConverter() {
         super(null);
      }

      AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo var1, Object var2) throws IOException {
         return new QTESLAPublicKeyParameters(Utils.qTeslaLookupSecurityCategory(var1.getAlgorithm()), var1.getPublicKeyData().getOctets());
      }

      // $FF: synthetic method
      QTeslaConverter(Object var1) {
         this();
      }
   }

   private static class SPHINCSConverter extends SubjectPublicKeyInfoConverter {
      private SPHINCSConverter() {
         super(null);
      }

      AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo var1, Object var2) throws IOException {
         return new SPHINCSPublicKeyParameters(var1.getPublicKeyData().getBytes(), Utils.sphincs256LookupTreeAlgName(SPHINCS256KeyParams.getInstance(var1.getAlgorithm().getParameters())));
      }

      // $FF: synthetic method
      SPHINCSConverter(Object var1) {
         this();
      }
   }

   private abstract static class SubjectPublicKeyInfoConverter {
      private SubjectPublicKeyInfoConverter() {
      }

      abstract AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo var1, Object var2) throws IOException;

      // $FF: synthetic method
      SubjectPublicKeyInfoConverter(Object var1) {
         this();
      }
   }

   private static class XMSSConverter extends SubjectPublicKeyInfoConverter {
      private XMSSConverter() {
         super(null);
      }

      AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo var1, Object var2) throws IOException {
         XMSSKeyParams var3 = XMSSKeyParams.getInstance(var1.getAlgorithm().getParameters());
         if (var3 != null) {
            ASN1ObjectIdentifier var6 = var3.getTreeDigest().getAlgorithm();
            XMSSPublicKey var5 = XMSSPublicKey.getInstance(var1.parsePublicKey());
            return (new XMSSPublicKeyParameters.Builder(new XMSSParameters(var3.getHeight(), Utils.getDigest(var6)))).withPublicSeed(var5.getPublicSeed()).withRoot(var5.getRoot()).build();
         } else {
            byte[] var4 = ASN1OctetString.getInstance(var1.parsePublicKey()).getOctets();
            return (new XMSSPublicKeyParameters.Builder(XMSSParameters.lookupByOID(Pack.bigEndianToInt(var4, 0)))).withPublicKey(var4).build();
         }
      }

      // $FF: synthetic method
      XMSSConverter(Object var1) {
         this();
      }
   }

   private static class XMSSMTConverter extends SubjectPublicKeyInfoConverter {
      private XMSSMTConverter() {
         super(null);
      }

      AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo var1, Object var2) throws IOException {
         XMSSMTKeyParams var3 = XMSSMTKeyParams.getInstance(var1.getAlgorithm().getParameters());
         if (var3 != null) {
            ASN1ObjectIdentifier var6 = var3.getTreeDigest().getAlgorithm();
            XMSSPublicKey var5 = XMSSPublicKey.getInstance(var1.parsePublicKey());
            return (new XMSSMTPublicKeyParameters.Builder(new XMSSMTParameters(var3.getHeight(), var3.getLayers(), Utils.getDigest(var6)))).withPublicSeed(var5.getPublicSeed()).withRoot(var5.getRoot()).build();
         } else {
            byte[] var4 = ASN1OctetString.getInstance(var1.parsePublicKey()).getOctets();
            return (new XMSSMTPublicKeyParameters.Builder(XMSSMTParameters.lookupByOID(Pack.bigEndianToInt(var4, 0)))).withPublicKey(var4).build();
         }
      }

      // $FF: synthetic method
      XMSSMTConverter(Object var1) {
         this();
      }
   }
}
