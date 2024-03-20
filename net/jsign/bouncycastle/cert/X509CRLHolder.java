package net.jsign.bouncycastle.cert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import net.jsign.bouncycastle.asn1.ASN1InputStream;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.x509.CertificateList;
import net.jsign.bouncycastle.asn1.x509.Extension;
import net.jsign.bouncycastle.asn1.x509.Extensions;
import net.jsign.bouncycastle.asn1.x509.GeneralName;
import net.jsign.bouncycastle.asn1.x509.GeneralNames;
import net.jsign.bouncycastle.asn1.x509.IssuingDistributionPoint;
import net.jsign.bouncycastle.util.Encodable;

public class X509CRLHolder implements Serializable, Encodable {
   private transient CertificateList x509CRL;
   private transient boolean isIndirect;
   private transient Extensions extensions;
   private transient GeneralNames issuerName;

   private static CertificateList parseStream(InputStream var0) throws IOException {
      try {
         ASN1Primitive var1 = (new ASN1InputStream(var0, true)).readObject();
         if (var1 == null) {
            throw new IOException("no content found");
         } else {
            return CertificateList.getInstance(var1);
         }
      } catch (ClassCastException var2) {
         throw new CertIOException("malformed data: " + var2.getMessage(), var2);
      } catch (IllegalArgumentException var3) {
         throw new CertIOException("malformed data: " + var3.getMessage(), var3);
      }
   }

   private static boolean isIndirectCRL(Extensions var0) {
      if (var0 == null) {
         return false;
      } else {
         Extension var1 = var0.getExtension(Extension.issuingDistributionPoint);
         return var1 != null && IssuingDistributionPoint.getInstance(var1.getParsedValue()).isIndirectCRL();
      }
   }

   public X509CRLHolder(byte[] var1) throws IOException {
      this(parseStream(new ByteArrayInputStream(var1)));
   }

   public X509CRLHolder(CertificateList var1) {
      this.init(var1);
   }

   private void init(CertificateList var1) {
      this.x509CRL = var1;
      this.extensions = var1.getTBSCertList().getExtensions();
      this.isIndirect = isIndirectCRL(this.extensions);
      this.issuerName = new GeneralNames(new GeneralName(var1.getIssuer()));
   }

   public byte[] getEncoded() throws IOException {
      return this.x509CRL.getEncoded();
   }

   public CertificateList toASN1Structure() {
      return this.x509CRL;
   }

   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (!(var1 instanceof X509CRLHolder)) {
         return false;
      } else {
         X509CRLHolder var2 = (X509CRLHolder)var1;
         return this.x509CRL.equals(var2.x509CRL);
      }
   }

   public int hashCode() {
      return this.x509CRL.hashCode();
   }
}
