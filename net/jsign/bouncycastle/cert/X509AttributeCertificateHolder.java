package net.jsign.bouncycastle.cert;

import java.io.IOException;
import java.io.Serializable;
import net.jsign.bouncycastle.asn1.x509.Attribute;
import net.jsign.bouncycastle.asn1.x509.AttributeCertificate;
import net.jsign.bouncycastle.asn1.x509.Extensions;
import net.jsign.bouncycastle.util.Encodable;

public class X509AttributeCertificateHolder implements Serializable, Encodable {
   private static Attribute[] EMPTY_ARRAY = new Attribute[0];
   private transient AttributeCertificate attrCert;
   private transient Extensions extensions;

   private static AttributeCertificate parseBytes(byte[] var0) throws IOException {
      try {
         return AttributeCertificate.getInstance(CertUtils.parseNonEmptyASN1(var0));
      } catch (ClassCastException var2) {
         throw new CertIOException("malformed data: " + var2.getMessage(), var2);
      } catch (IllegalArgumentException var3) {
         throw new CertIOException("malformed data: " + var3.getMessage(), var3);
      }
   }

   public X509AttributeCertificateHolder(byte[] var1) throws IOException {
      this(parseBytes(var1));
   }

   public X509AttributeCertificateHolder(AttributeCertificate var1) {
      this.init(var1);
   }

   private void init(AttributeCertificate var1) {
      this.attrCert = var1;
      this.extensions = var1.getAcinfo().getExtensions();
   }

   public byte[] getEncoded() throws IOException {
      return this.attrCert.getEncoded();
   }

   public AttributeCertificate toASN1Structure() {
      return this.attrCert;
   }

   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (!(var1 instanceof X509AttributeCertificateHolder)) {
         return false;
      } else {
         X509AttributeCertificateHolder var2 = (X509AttributeCertificateHolder)var1;
         return this.attrCert.equals(var2.attrCert);
      }
   }

   public int hashCode() {
      return this.attrCert.hashCode();
   }
}
