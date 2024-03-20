package net.jsign.bouncycastle.pqc.jcajce.provider.sphincs;

import java.io.IOException;
import java.security.PublicKey;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import net.jsign.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import net.jsign.bouncycastle.pqc.asn1.SPHINCS256KeyParams;
import net.jsign.bouncycastle.pqc.crypto.sphincs.SPHINCSPublicKeyParameters;
import net.jsign.bouncycastle.pqc.crypto.util.PublicKeyFactory;
import net.jsign.bouncycastle.pqc.crypto.util.SubjectPublicKeyInfoFactory;
import net.jsign.bouncycastle.pqc.jcajce.interfaces.SPHINCSKey;
import net.jsign.bouncycastle.util.Arrays;

public class BCSphincs256PublicKey implements PublicKey, SPHINCSKey {
   private transient ASN1ObjectIdentifier treeDigest;
   private transient SPHINCSPublicKeyParameters params;

   public BCSphincs256PublicKey(SubjectPublicKeyInfo var1) throws IOException {
      this.init(var1);
   }

   private void init(SubjectPublicKeyInfo var1) throws IOException {
      this.treeDigest = SPHINCS256KeyParams.getInstance(var1.getAlgorithm().getParameters()).getTreeDigest().getAlgorithm();
      this.params = (SPHINCSPublicKeyParameters)PublicKeyFactory.createKey(var1);
   }

   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (!(var1 instanceof BCSphincs256PublicKey)) {
         return false;
      } else {
         BCSphincs256PublicKey var2 = (BCSphincs256PublicKey)var1;
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
         SubjectPublicKeyInfo var1;
         if (this.params.getTreeDigest() != null) {
            var1 = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(this.params);
         } else {
            AlgorithmIdentifier var2 = new AlgorithmIdentifier(PQCObjectIdentifiers.sphincs256, new SPHINCS256KeyParams(new AlgorithmIdentifier(this.treeDigest)));
            var1 = new SubjectPublicKeyInfo(var2, this.params.getKeyData());
         }

         return var1.getEncoded();
      } catch (IOException var3) {
         return null;
      }
   }

   public String getFormat() {
      return "X.509";
   }
}
