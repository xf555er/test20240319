package net.jsign.bouncycastle.pqc.jcajce.provider.sphincs;

import java.io.IOException;
import java.security.PrivateKey;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1Set;
import net.jsign.bouncycastle.asn1.DEROctetString;
import net.jsign.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import net.jsign.bouncycastle.pqc.asn1.SPHINCS256KeyParams;
import net.jsign.bouncycastle.pqc.crypto.sphincs.SPHINCSPrivateKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.util.PrivateKeyFactory;
import net.jsign.bouncycastle.pqc.crypto.util.PrivateKeyInfoFactory;
import net.jsign.bouncycastle.pqc.jcajce.interfaces.SPHINCSKey;
import net.jsign.bouncycastle.util.Arrays;

public class BCSphincs256PrivateKey implements PrivateKey, SPHINCSKey {
   private transient ASN1ObjectIdentifier treeDigest;
   private transient SPHINCSPrivateKeyParameters params;
   private transient ASN1Set attributes;

   public BCSphincs256PrivateKey(PrivateKeyInfo var1) throws IOException {
      this.init(var1);
   }

   private void init(PrivateKeyInfo var1) throws IOException {
      this.attributes = var1.getAttributes();
      this.treeDigest = SPHINCS256KeyParams.getInstance(var1.getPrivateKeyAlgorithm().getParameters()).getTreeDigest().getAlgorithm();
      this.params = (SPHINCSPrivateKeyParameters)PrivateKeyFactory.createKey(var1);
   }

   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (!(var1 instanceof BCSphincs256PrivateKey)) {
         return false;
      } else {
         BCSphincs256PrivateKey var2 = (BCSphincs256PrivateKey)var1;
         return this.treeDigest.equals(var2.treeDigest) && Arrays.areEqual(this.params.getKeyData(), var2.params.getKeyData());
      }
   }

   public int hashCode() {
      return this.treeDigest.hashCode() + 37 * Arrays.hashCode(this.params.getKeyData());
   }

   public final String getAlgorithm() {
      return "SPHINCS-256";
   }

   public byte[] getEncoded() {
      try {
         PrivateKeyInfo var1;
         if (this.params.getTreeDigest() != null) {
            var1 = PrivateKeyInfoFactory.createPrivateKeyInfo(this.params, this.attributes);
         } else {
            AlgorithmIdentifier var2 = new AlgorithmIdentifier(PQCObjectIdentifiers.sphincs256, new SPHINCS256KeyParams(new AlgorithmIdentifier(this.treeDigest)));
            var1 = new PrivateKeyInfo(var2, new DEROctetString(this.params.getKeyData()), this.attributes);
         }

         return var1.getEncoded();
      } catch (IOException var3) {
         return null;
      }
   }

   public String getFormat() {
      return "PKCS#8";
   }
}
