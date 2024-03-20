package net.jsign.bouncycastle.pkcs;

import java.io.ByteArrayInputStream;
import net.jsign.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import net.jsign.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import net.jsign.bouncycastle.operator.InputDecryptor;
import net.jsign.bouncycastle.operator.InputDecryptorProvider;
import net.jsign.bouncycastle.util.io.Streams;

public class PKCS8EncryptedPrivateKeyInfo {
   private EncryptedPrivateKeyInfo encryptedPrivateKeyInfo;

   public PKCS8EncryptedPrivateKeyInfo(EncryptedPrivateKeyInfo var1) {
      this.encryptedPrivateKeyInfo = var1;
   }

   public PrivateKeyInfo decryptPrivateKeyInfo(InputDecryptorProvider var1) throws PKCSException {
      try {
         InputDecryptor var2 = var1.get(this.encryptedPrivateKeyInfo.getEncryptionAlgorithm());
         ByteArrayInputStream var3 = new ByteArrayInputStream(this.encryptedPrivateKeyInfo.getEncryptedData());
         return PrivateKeyInfo.getInstance(Streams.readAll(var2.getInputStream(var3)));
      } catch (Exception var4) {
         throw new PKCSException("unable to read encrypted data: " + var4.getMessage(), var4);
      }
   }
}
