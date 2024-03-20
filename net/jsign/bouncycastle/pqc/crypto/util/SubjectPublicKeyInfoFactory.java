package net.jsign.bouncycastle.pqc.crypto.util;

import java.io.IOException;
import net.jsign.bouncycastle.asn1.DEROctetString;
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
import net.jsign.bouncycastle.pqc.asn1.XMSSMTPublicKey;
import net.jsign.bouncycastle.pqc.asn1.XMSSPublicKey;
import net.jsign.bouncycastle.pqc.crypto.lms.Composer;
import net.jsign.bouncycastle.pqc.crypto.lms.HSSPublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.lms.LMSPublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.mceliece.McElieceCCA2PublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.newhope.NHPublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.qtesla.QTESLAPublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.sphincs.SPHINCSPublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSMTPublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSPublicKeyParameters;
import net.jsign.bouncycastle.util.Encodable;

public class SubjectPublicKeyInfoFactory {
   public static SubjectPublicKeyInfo createSubjectPublicKeyInfo(AsymmetricKeyParameter var0) throws IOException {
      AlgorithmIdentifier var15;
      if (var0 instanceof QTESLAPublicKeyParameters) {
         QTESLAPublicKeyParameters var13 = (QTESLAPublicKeyParameters)var0;
         var15 = Utils.qTeslaLookupAlgID(var13.getSecurityCategory());
         return new SubjectPublicKeyInfo(var15, var13.getPublicData());
      } else if (var0 instanceof SPHINCSPublicKeyParameters) {
         SPHINCSPublicKeyParameters var12 = (SPHINCSPublicKeyParameters)var0;
         var15 = new AlgorithmIdentifier(PQCObjectIdentifiers.sphincs256, new SPHINCS256KeyParams(Utils.sphincs256LookupTreeAlgID(var12.getTreeDigest())));
         return new SubjectPublicKeyInfo(var15, var12.getKeyData());
      } else if (var0 instanceof NHPublicKeyParameters) {
         NHPublicKeyParameters var11 = (NHPublicKeyParameters)var0;
         var15 = new AlgorithmIdentifier(PQCObjectIdentifiers.newHope);
         return new SubjectPublicKeyInfo(var15, var11.getPubData());
      } else {
         AlgorithmIdentifier var3;
         byte[] var10;
         if (var0 instanceof LMSPublicKeyParameters) {
            LMSPublicKeyParameters var9 = (LMSPublicKeyParameters)var0;
            var10 = Composer.compose().u32str(1).bytes((Encodable)var9).build();
            var3 = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_alg_hss_lms_hashsig);
            return new SubjectPublicKeyInfo(var3, new DEROctetString(var10));
         } else if (var0 instanceof HSSPublicKeyParameters) {
            HSSPublicKeyParameters var8 = (HSSPublicKeyParameters)var0;
            var10 = Composer.compose().u32str(var8.getL()).bytes((Encodable)var8.getLMSPublicKey()).build();
            var3 = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_alg_hss_lms_hashsig);
            return new SubjectPublicKeyInfo(var3, new DEROctetString(var10));
         } else {
            byte[] var4;
            AlgorithmIdentifier var5;
            byte[] var14;
            if (var0 instanceof XMSSPublicKeyParameters) {
               XMSSPublicKeyParameters var7 = (XMSSPublicKeyParameters)var0;
               var10 = var7.getPublicSeed();
               var14 = var7.getRoot();
               var4 = var7.getEncoded();
               if (var4.length > var10.length + var14.length) {
                  var5 = new AlgorithmIdentifier(IsaraObjectIdentifiers.id_alg_xmss);
                  return new SubjectPublicKeyInfo(var5, new DEROctetString(var4));
               } else {
                  var5 = new AlgorithmIdentifier(PQCObjectIdentifiers.xmss, new XMSSKeyParams(var7.getParameters().getHeight(), Utils.xmssLookupTreeAlgID(var7.getTreeDigest())));
                  return new SubjectPublicKeyInfo(var5, new XMSSPublicKey(var10, var14));
               }
            } else if (var0 instanceof XMSSMTPublicKeyParameters) {
               XMSSMTPublicKeyParameters var6 = (XMSSMTPublicKeyParameters)var0;
               var10 = var6.getPublicSeed();
               var14 = var6.getRoot();
               var4 = var6.getEncoded();
               if (var4.length > var10.length + var14.length) {
                  var5 = new AlgorithmIdentifier(IsaraObjectIdentifiers.id_alg_xmssmt);
                  return new SubjectPublicKeyInfo(var5, new DEROctetString(var4));
               } else {
                  var5 = new AlgorithmIdentifier(PQCObjectIdentifiers.xmss_mt, new XMSSMTKeyParams(var6.getParameters().getHeight(), var6.getParameters().getLayers(), Utils.xmssLookupTreeAlgID(var6.getTreeDigest())));
                  return new SubjectPublicKeyInfo(var5, new XMSSMTPublicKey(var6.getPublicSeed(), var6.getRoot()));
               }
            } else if (var0 instanceof McElieceCCA2PublicKeyParameters) {
               McElieceCCA2PublicKeyParameters var1 = (McElieceCCA2PublicKeyParameters)var0;
               McElieceCCA2PublicKey var2 = new McElieceCCA2PublicKey(var1.getN(), var1.getT(), var1.getG(), Utils.getAlgorithmIdentifier(var1.getDigest()));
               var3 = new AlgorithmIdentifier(PQCObjectIdentifiers.mcElieceCca2);
               return new SubjectPublicKeyInfo(var3, var2);
            } else {
               throw new IOException("key parameters not recognized");
            }
         }
      }
   }
}
