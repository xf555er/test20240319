package net.jsign.jca;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

class SigningServiceKeyStore extends KeyStoreSpi {
   private final SigningService service;

   public SigningServiceKeyStore(SigningService service) {
      this.service = service;
   }

   public Key engineGetKey(String alias, char[] password) throws UnrecoverableKeyException {
      return this.service.getPrivateKey(alias);
   }

   public Certificate[] engineGetCertificateChain(String alias) {
      try {
         return this.service.getCertificateChain(alias);
      } catch (KeyStoreException var3) {
         return null;
      }
   }

   public Certificate engineGetCertificate(String alias) {
      throw new UnsupportedOperationException();
   }

   public Date engineGetCreationDate(String alias) {
      throw new UnsupportedOperationException();
   }

   public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) {
      throw new UnsupportedOperationException();
   }

   public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) {
      throw new UnsupportedOperationException();
   }

   public void engineSetCertificateEntry(String alias, Certificate cert) {
      throw new UnsupportedOperationException();
   }

   public void engineDeleteEntry(String alias) {
      throw new UnsupportedOperationException();
   }

   public Enumeration engineAliases() {
      try {
         return (new Vector(this.service.aliases())).elements();
      } catch (KeyStoreException var2) {
         throw new RuntimeException(var2);
      }
   }

   public boolean engineContainsAlias(String alias) {
      Enumeration aliases = this.engineAliases();

      do {
         if (!aliases.hasMoreElements()) {
            return false;
         }
      } while(!((String)aliases.nextElement()).equals(alias));

      return true;
   }

   public int engineSize() {
      return Collections.list(this.engineAliases()).size();
   }

   public boolean engineIsKeyEntry(String alias) {
      throw new UnsupportedOperationException();
   }

   public boolean engineIsCertificateEntry(String alias) {
      throw new UnsupportedOperationException();
   }

   public String engineGetCertificateAlias(Certificate cert) {
      throw new UnsupportedOperationException();
   }

   public void engineStore(OutputStream stream, char[] password) {
      throw new UnsupportedOperationException();
   }

   public void engineLoad(InputStream stream, char[] password) {
   }
}
