package net.jsign.bouncycastle.pqc.crypto.util;

import java.io.IOException;
import net.jsign.bouncycastle.asn1.ASN1Set;
import net.jsign.bouncycastle.asn1.DEROctetString;
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
import net.jsign.bouncycastle.pqc.crypto.lms.Composer;
import net.jsign.bouncycastle.pqc.crypto.lms.HSSPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.lms.LMSPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.mceliece.McElieceCCA2PrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.newhope.NHPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.qtesla.QTESLAPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.sphincs.SPHINCSPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.xmss.BDS;
import net.jsign.bouncycastle.pqc.crypto.xmss.BDSStateMap;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSMTPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.xmss.XMSSUtil;
import net.jsign.bouncycastle.util.Encodable;
import net.jsign.bouncycastle.util.Pack;

public class PrivateKeyInfoFactory {
   public static PrivateKeyInfo createPrivateKeyInfo(AsymmetricKeyParameter var0, ASN1Set var1) throws IOException {
      AlgorithmIdentifier var3;
      if (var0 instanceof QTESLAPrivateKeyParameters) {
         QTESLAPrivateKeyParameters var14 = (QTESLAPrivateKeyParameters)var0;
         var3 = Utils.qTeslaLookupAlgID(var14.getSecurityCategory());
         return new PrivateKeyInfo(var3, new DEROctetString(var14.getSecret()), var1);
      } else if (var0 instanceof SPHINCSPrivateKeyParameters) {
         SPHINCSPrivateKeyParameters var13 = (SPHINCSPrivateKeyParameters)var0;
         var3 = new AlgorithmIdentifier(PQCObjectIdentifiers.sphincs256, new SPHINCS256KeyParams(Utils.sphincs256LookupTreeAlgID(var13.getTreeDigest())));
         return new PrivateKeyInfo(var3, new DEROctetString(var13.getKeyData()));
      } else if (!(var0 instanceof NHPrivateKeyParameters)) {
         byte[] var15;
         byte[] var17;
         AlgorithmIdentifier var18;
         if (var0 instanceof LMSPrivateKeyParameters) {
            LMSPrivateKeyParameters var12 = (LMSPrivateKeyParameters)var0;
            var15 = Composer.compose().u32str(1).bytes((Encodable)var12).build();
            var17 = Composer.compose().u32str(1).bytes((Encodable)var12.getPublicKey()).build();
            var18 = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_alg_hss_lms_hashsig);
            return new PrivateKeyInfo(var18, new DEROctetString(var15), var1, var17);
         } else if (var0 instanceof HSSPrivateKeyParameters) {
            HSSPrivateKeyParameters var10 = (HSSPrivateKeyParameters)var0;
            var15 = Composer.compose().u32str(var10.getL()).bytes((Encodable)var10).build();
            var17 = Composer.compose().u32str(var10.getL()).bytes((Encodable)var10.getPublicKey().getLMSPublicKey()).build();
            var18 = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_alg_hss_lms_hashsig);
            return new PrivateKeyInfo(var18, new DEROctetString(var15), var1, var17);
         } else if (var0 instanceof XMSSPrivateKeyParameters) {
            XMSSPrivateKeyParameters var9 = (XMSSPrivateKeyParameters)var0;
            var3 = new AlgorithmIdentifier(PQCObjectIdentifiers.xmss, new XMSSKeyParams(var9.getParameters().getHeight(), Utils.xmssLookupTreeAlgID(var9.getTreeDigest())));
            return new PrivateKeyInfo(var3, xmssCreateKeyStructure(var9), var1);
         } else if (var0 instanceof XMSSMTPrivateKeyParameters) {
            XMSSMTPrivateKeyParameters var8 = (XMSSMTPrivateKeyParameters)var0;
            var3 = new AlgorithmIdentifier(PQCObjectIdentifiers.xmss_mt, new XMSSMTKeyParams(var8.getParameters().getHeight(), var8.getParameters().getLayers(), Utils.xmssLookupTreeAlgID(var8.getTreeDigest())));
            return new PrivateKeyInfo(var3, xmssmtCreateKeyStructure(var8), var1);
         } else if (var0 instanceof McElieceCCA2PrivateKeyParameters) {
            McElieceCCA2PrivateKeyParameters var7 = (McElieceCCA2PrivateKeyParameters)var0;
            McElieceCCA2PrivateKey var11 = new McElieceCCA2PrivateKey(var7.getN(), var7.getK(), var7.getField(), var7.getGoppaPoly(), var7.getP(), Utils.getAlgorithmIdentifier(var7.getDigest()));
            AlgorithmIdentifier var16 = new AlgorithmIdentifier(PQCObjectIdentifiers.mcElieceCca2);
            return new PrivateKeyInfo(var16, var11);
         } else {
            throw new IOException("key parameters not recognized");
         }
      } else {
         NHPrivateKeyParameters var2 = (NHPrivateKeyParameters)var0;
         var3 = new AlgorithmIdentifier(PQCObjectIdentifiers.newHope);
         short[] var4 = var2.getSecData();
         byte[] var5 = new byte[var4.length * 2];

         for(int var6 = 0; var6 != var4.length; ++var6) {
            Pack.shortToLittleEndian(var4[var6], var5, var6 * 2);
         }

         return new PrivateKeyInfo(var3, new DEROctetString(var5));
      }
   }

   private static XMSSPrivateKey xmssCreateKeyStructure(XMSSPrivateKeyParameters var0) throws IOException {
      byte[] var1 = var0.getEncoded();
      int var2 = var0.getParameters().getTreeDigestSize();
      int var3 = var0.getParameters().getHeight();
      byte var4 = 4;
      int var9 = 0;
      int var10 = (int)XMSSUtil.bytesToXBigEndian(var1, var9, var4);
      if (!XMSSUtil.isIndexValid(var3, (long)var10)) {
         throw new IllegalArgumentException("index out of bounds");
      } else {
         var9 += var4;
         byte[] var11 = XMSSUtil.extractBytesAtOffset(var1, var9, var2);
         var9 += var2;
         byte[] var12 = XMSSUtil.extractBytesAtOffset(var1, var9, var2);
         var9 += var2;
         byte[] var13 = XMSSUtil.extractBytesAtOffset(var1, var9, var2);
         var9 += var2;
         byte[] var14 = XMSSUtil.extractBytesAtOffset(var1, var9, var2);
         var9 += var2;
         byte[] var15 = XMSSUtil.extractBytesAtOffset(var1, var9, var1.length - var9);
         BDS var16 = null;

         try {
            var16 = (BDS)XMSSUtil.deserialize(var15, BDS.class);
         } catch (ClassNotFoundException var18) {
            throw new IOException("cannot parse BDS: " + var18.getMessage());
         }

         return var16.getMaxIndex() != (1 << var3) - 1 ? new XMSSPrivateKey(var10, var11, var12, var13, var14, var15, var16.getMaxIndex()) : new XMSSPrivateKey(var10, var11, var12, var13, var14, var15);
      }
   }

   private static XMSSMTPrivateKey xmssmtCreateKeyStructure(XMSSMTPrivateKeyParameters var0) throws IOException {
      byte[] var1 = var0.getEncoded();
      int var2 = var0.getParameters().getTreeDigestSize();
      int var3 = var0.getParameters().getHeight();
      int var4 = (var3 + 7) / 8;
      int var9 = 0;
      int var10 = (int)XMSSUtil.bytesToXBigEndian(var1, var9, var4);
      if (!XMSSUtil.isIndexValid(var3, (long)var10)) {
         throw new IllegalArgumentException("index out of bounds");
      } else {
         var9 += var4;
         byte[] var11 = XMSSUtil.extractBytesAtOffset(var1, var9, var2);
         var9 += var2;
         byte[] var12 = XMSSUtil.extractBytesAtOffset(var1, var9, var2);
         var9 += var2;
         byte[] var13 = XMSSUtil.extractBytesAtOffset(var1, var9, var2);
         var9 += var2;
         byte[] var14 = XMSSUtil.extractBytesAtOffset(var1, var9, var2);
         var9 += var2;
         byte[] var15 = XMSSUtil.extractBytesAtOffset(var1, var9, var1.length - var9);
         BDSStateMap var16 = null;

         try {
            var16 = (BDSStateMap)XMSSUtil.deserialize(var15, BDSStateMap.class);
         } catch (ClassNotFoundException var18) {
            throw new IOException("cannot parse BDSStateMap: " + var18.getMessage());
         }

         return var16.getMaxIndex() != (1L << var3) - 1L ? new XMSSMTPrivateKey((long)var10, var11, var12, var13, var14, var15, var16.getMaxIndex()) : new XMSSMTPrivateKey((long)var10, var11, var12, var13, var14, var15);
      }
   }
}
