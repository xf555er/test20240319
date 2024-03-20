package net.jsign.bouncycastle.cert;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.x500.X500Name;
import net.jsign.bouncycastle.asn1.x509.Certificate;
import net.jsign.bouncycastle.asn1.x509.Extension;
import net.jsign.bouncycastle.asn1.x509.Extensions;
import net.jsign.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import net.jsign.bouncycastle.util.Encodable;

public class X509CertificateHolder implements Serializable, Encodable {
   private transient Certificate x509Certificate;
   private transient Extensions extensions;

   private static Certificate parseBytes(byte[] var0) throws IOException {
      try {
         return Certificate.getInstance(CertUtils.parseNonEmptyASN1(var0));
      } catch (ClassCastException var2) {
         throw new CertIOException("malformed data: " + var2.getMessage(), var2);
      } catch (IllegalArgumentException var3) {
         throw new CertIOException("malformed data: " + var3.getMessage(), var3);
      }
   }

   public X509CertificateHolder(byte[] var1) throws IOException {
      this(parseBytes(var1));
   }

   public X509CertificateHolder(Certificate var1) {
      this.init(var1);
   }

   private void init(Certificate var1) {
      this.x509Certificate = var1;
      this.extensions = var1.getTBSCertificate().getExtensions();
   }

   public Extension getExtension(ASN1ObjectIdentifier var1) {
      return this.extensions != null ? this.extensions.getExtension(var1) : null;
   }

   public X500Name getSubject() {
      return X500Name.getInstance(this.x509Certificate.getSubject());
   }

   public SubjectPublicKeyInfo getSubjectPublicKeyInfo() {
      return this.x509Certificate.getSubjectPublicKeyInfo();
   }

   public Certificate toASN1Structure() {
      return this.x509Certificate;
   }

   public boolean isValidOn(Date var1) {
      return !var1.before(this.x509Certificate.getStartDate().getDate()) && !var1.after(this.x509Certificate.getEndDate().getDate());
   }

   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (!(var1 instanceof X509CertificateHolder)) {
         return false;
      } else {
         X509CertificateHolder var2 = (X509CertificateHolder)var1;
         return this.x509Certificate.equals(var2.x509Certificate);
      }
   }

   public int hashCode() {
      return this.x509Certificate.hashCode();
   }

   public byte[] getEncoded() throws IOException {
      return this.x509Certificate.getEncoded();
   }
}
