package net.jsign.bouncycastle.pqc.crypto.util;

import java.io.IOException;
import net.jsign.bouncycastle.asn1.ASN1BitString;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1OctetString;
import net.jsign.bouncycastle.asn1.bc.BCObjectIdentifiers;
import net.jsign.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import net.jsign.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.crypto.params.AsymmetricKeyParameter;
import net.jsign.bouncycastle.pqc.asn1.McElieceCCA2PrivateKey;
import net.jsign.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import net.jsign.bouncycastle.pqc.asn1.SPHINCS256KeyParams;
import net.jsign.bouncycastle.pqc.asn1.XMSSKeyParams;
import net.jsign.bouncycastle.pqc.asn1.XMSSMTKeyParams;
import net.jsign.bouncycastle.pqc.asn1.XMSSMTPrivateKey;
import net.jsign.bouncycastle.pqc.asn1.XMSSPrivateKey;
import net.jsign.bouncycastle.pqc.crypto.lms.HSSPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.lms.LMSPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.mceliece.McElieceCCA2PrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.newhope.NHPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.qtesla.QTESLAPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.sphincs.SPHINCSPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.xmss.BDS;
import net.jsign.bouncycastle.pqc.crypto.xmss.BDSStateMap;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSMTParameters;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSMTPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSParameters;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSUtil;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.Pack;

public class PrivateKeyFactory {
   public static AsymmetricKeyParameter createKey(PrivateKeyInfo var0) throws IOException {
      AlgorithmIdentifier var1 = var0.getPrivateKeyAlgorithm();
      ASN1ObjectIdentifier var2 = var1.getAlgorithm();
      if (var2.on(BCObjectIdentifiers.qTESLA)) {
         ASN1OctetString var13 = ASN1OctetString.getInstance(var0.parsePrivateKey());
         return new QTESLAPrivateKeyParameters(Utils.qTeslaLookupSecurityCategory(var0.getPrivateKeyAlgorithm()), var13.getOctets());
      } else if (var2.equals(BCObjectIdentifiers.sphincs256)) {
         return new SPHINCSPrivateKeyParameters(ASN1OctetString.getInstance(var0.parsePrivateKey()).getOctets(), Utils.sphincs256LookupTreeAlgName(SPHINCS256KeyParams.getInstance(var0.getPrivateKeyAlgorithm().getParameters())));
      } else if (var2.equals(BCObjectIdentifiers.newHope)) {
         return new NHPrivateKeyParameters(convert(ASN1OctetString.getInstance(var0.parsePrivateKey()).getOctets()));
      } else if (var2.equals(PKCSObjectIdentifiers.id_alg_hss_lms_hashsig)) {
         byte[] var12 = ASN1OctetString.getInstance(var0.parsePrivateKey()).getOctets();
         ASN1BitString var14 = var0.getPublicKeyData();
         byte[] var16;
         if (Pack.bigEndianToInt(var12, 0) == 1) {
            if (var14 != null) {
               var16 = var14.getOctets();
               return LMSPrivateKeyParameters.getInstance(Arrays.copyOfRange(var12, 4, var12.length), Arrays.copyOfRange(var16, 4, var16.length));
            } else {
               return LMSPrivateKeyParameters.getInstance(Arrays.copyOfRange(var12, 4, var12.length));
            }
         } else if (var14 != null) {
            var16 = var14.getOctets();
            return HSSPrivateKeyParameters.getInstance(Arrays.copyOfRange(var12, 4, var12.length), var16);
         } else {
            return HSSPrivateKeyParameters.getInstance(Arrays.copyOfRange(var12, 4, var12.length));
         }
      } else {
         ASN1ObjectIdentifier var4;
         if (var2.equals(BCObjectIdentifiers.xmss)) {
            XMSSKeyParams var11 = XMSSKeyParams.getInstance(var0.getPrivateKeyAlgorithm().getParameters());
            var4 = var11.getTreeDigest().getAlgorithm();
            XMSSPrivateKey var15 = XMSSPrivateKey.getInstance(var0.parsePrivateKey());

            try {
               XMSSPrivateKeyParameters.Builder var17 = (new XMSSPrivateKeyParameters.Builder(new XMSSParameters(var11.getHeight(), Utils.getDigest(var4)))).withIndex(var15.getIndex()).withSecretKeySeed(var15.getSecretKeySeed()).withSecretKeyPRF(var15.getSecretKeyPRF()).withPublicSeed(var15.getPublicSeed()).withRoot(var15.getRoot());
               if (var15.getVersion() != 0) {
                  var17.withMaxIndex(var15.getMaxIndex());
               }

               if (var15.getBdsState() != null) {
                  BDS var18 = (BDS)XMSSUtil.deserialize(var15.getBdsState(), BDS.class);
                  var17.withBDSState(var18.withWOTSDigest(var4));
               }

               return var17.build();
            } catch (ClassNotFoundException var8) {
               throw new IOException("ClassNotFoundException processing BDS state: " + var8.getMessage());
            }
         } else if (var2.equals(PQCObjectIdentifiers.xmss_mt)) {
            XMSSMTKeyParams var10 = XMSSMTKeyParams.getInstance(var0.getPrivateKeyAlgorithm().getParameters());
            var4 = var10.getTreeDigest().getAlgorithm();

            try {
               XMSSMTPrivateKey var5 = XMSSMTPrivateKey.getInstance(var0.parsePrivateKey());
               XMSSMTPrivateKeyParameters.Builder var6 = (new XMSSMTPrivateKeyParameters.Builder(new XMSSMTParameters(var10.getHeight(), var10.getLayers(), Utils.getDigest(var4)))).withIndex(var5.getIndex()).withSecretKeySeed(var5.getSecretKeySeed()).withSecretKeyPRF(var5.getSecretKeyPRF()).withPublicSeed(var5.getPublicSeed()).withRoot(var5.getRoot());
               if (var5.getVersion() != 0) {
                  var6.withMaxIndex(var5.getMaxIndex());
               }

               if (var5.getBdsState() != null) {
                  BDSStateMap var7 = (BDSStateMap)XMSSUtil.deserialize(var5.getBdsState(), BDSStateMap.class);
                  var6.withBDSState(var7.withWOTSDigest(var4));
               }

               return var6.build();
            } catch (ClassNotFoundException var9) {
               throw new IOException("ClassNotFoundException processing BDS state: " + var9.getMessage());
            }
         } else if (var2.equals(PQCObjectIdentifiers.mcElieceCca2)) {
            McElieceCCA2PrivateKey var3 = McElieceCCA2PrivateKey.getInstance(var0.parsePrivateKey());
            return new McElieceCCA2PrivateKeyParameters(var3.getN(), var3.getK(), var3.getField(), var3.getGoppaPoly(), var3.getP(), Utils.getDigestName(var3.getDigest().getAlgorithm()));
         } else {
            throw new RuntimeException("algorithm identifier in private key not recognised");
         }
      }
   }

   private static short[] convert(byte[] var0) {
      short[] var1 = new short[var0.length / 2];

      for(int var2 = 0; var2 != var1.length; ++var2) {
         var1[var2] = Pack.littleEndianToShort(var0, var2 * 2);
      }

      return var1;
   }
}
