package net.jsign.bouncycastle.pqc.jcajce.provider.mceliece;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import net.jsign.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import net.jsign.bouncycastle.jcajce.provider.util.AsymmetricKeyInfoConverter;
import net.jsign.bouncycastle.pqc.asn1.McElieceCCA2PrivateKey;
import net.jsign.bouncycastle.pqc.asn1.McElieceCCA2PublicKey;
import net.jsign.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import net.jsign.bouncycastle.pqc.crypto.mceliece.McElieceCCA2PrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.mceliece.McElieceCCA2PublicKeyParameters;

public class McElieceCCA2KeyFactorySpi extends KeyFactorySpi implements AsymmetricKeyInfoConverter {
   protected PublicKey engineGeneratePublic(KeySpec var1) throws InvalidKeySpecException {
      if (var1 instanceof X509EncodedKeySpec) {
         byte[] var2 = ((X509EncodedKeySpec)var1).getEncoded();

         SubjectPublicKeyInfo var3;
         try {
            var3 = SubjectPublicKeyInfo.getInstance(ASN1Primitive.fromByteArray(var2));
         } catch (IOException var6) {
            throw new InvalidKeySpecException(var6.toString());
         }

         try {
            if (PQCObjectIdentifiers.mcElieceCca2.equals(var3.getAlgorithm().getAlgorithm())) {
               McElieceCCA2PublicKey var4 = McElieceCCA2PublicKey.getInstance(var3.parsePublicKey());
               return new BCMcElieceCCA2PublicKey(new McElieceCCA2PublicKeyParameters(var4.getN(), var4.getT(), var4.getG(), Utils.getDigest(var4.getDigest()).getAlgorithmName()));
            } else {
               throw new InvalidKeySpecException("Unable to recognise OID in McEliece private key");
            }
         } catch (IOException var5) {
            throw new InvalidKeySpecException("Unable to decode X509EncodedKeySpec: " + var5.getMessage());
         }
      } else {
         throw new InvalidKeySpecException("Unsupported key specification: " + var1.getClass() + ".");
      }
   }

   protected PrivateKey engineGeneratePrivate(KeySpec var1) throws InvalidKeySpecException {
      if (var1 instanceof PKCS8EncodedKeySpec) {
         byte[] var2 = ((PKCS8EncodedKeySpec)var1).getEncoded();

         PrivateKeyInfo var3;
         try {
            var3 = PrivateKeyInfo.getInstance(ASN1Primitive.fromByteArray(var2));
         } catch (IOException var6) {
            throw new InvalidKeySpecException("Unable to decode PKCS8EncodedKeySpec: " + var6);
         }

         try {
            if (PQCObjectIdentifiers.mcElieceCca2.equals(var3.getPrivateKeyAlgorithm().getAlgorithm())) {
               McElieceCCA2PrivateKey var4 = McElieceCCA2PrivateKey.getInstance(var3.parsePrivateKey());
               return new BCMcElieceCCA2PrivateKey(new McElieceCCA2PrivateKeyParameters(var4.getN(), var4.getK(), var4.getField(), var4.getGoppaPoly(), var4.getP(), Utils.getDigest(var4.getDigest()).getAlgorithmName()));
            } else {
               throw new InvalidKeySpecException("Unable to recognise OID in McEliece public key");
            }
         } catch (IOException var5) {
            throw new InvalidKeySpecException("Unable to decode PKCS8EncodedKeySpec.");
         }
      } else {
         throw new InvalidKeySpecException("Unsupported key specification: " + var1.getClass() + ".");
      }
   }

   protected KeySpec engineGetKeySpec(Key var1, Class var2) throws InvalidKeySpecException {
      return null;
   }

   protected Key engineTranslateKey(Key var1) throws InvalidKeyException {
      return null;
   }
}
