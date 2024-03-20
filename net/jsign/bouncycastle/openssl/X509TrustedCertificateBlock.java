package net.jsign.bouncycastle.openssl;

import java.io.IOException;
import net.jsign.bouncycastle.asn1.ASN1InputStream;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.cert.X509CertificateHolder;

public class X509TrustedCertificateBlock {
   private final X509CertificateHolder certificateHolder;
   private final CertificateTrustBlock trustBlock;

   public X509TrustedCertificateBlock(byte[] var1) throws IOException {
      ASN1InputStream var2 = new ASN1InputStream(var1);
      this.certificateHolder = new X509CertificateHolder(var2.readObject().getEncoded());
      ASN1Primitive var3 = var2.readObject();
      if (var3 != null) {
         this.trustBlock = new CertificateTrustBlock(var3.getEncoded());
      } else {
         this.trustBlock = null;
      }

   }
}
